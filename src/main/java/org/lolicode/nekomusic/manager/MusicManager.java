package org.lolicode.nekomusic.manager;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.PlayerHelper;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.Api;
import org.lolicode.nekomusic.music.MusicObj;

import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicManager {
    private static final Pattern intPattern = Pattern.compile("\\d+");
    private static final Pattern urlPattern1 = Pattern.compile("song/(\\d+)");
    private static final Pattern urlPattern2 = Pattern.compile("[?&]id=(\\d+)");
    public static void playNext(MinecraftServer server) {
        if (NekoMusic.task != null) {
            NekoMusic.task.cancel();  // If user issues next command, cancel the current task in case it's not finished
            NekoMusic.task = null;
        }
        if (PlayerHelper.getOnlineRealPlayerList(server).size() == 0)
            return;

        NekoMusic.currentVote.clear();
        NekoMusic.EXECUTOR.execute(() -> {
            boolean success = false;

            MusicObj next = NekoMusic.orderList.next();
            if (next == null) {
                next = NekoMusic.idleList.next();
            }
            if (next == null) {
                NekoMusic.LOGGER.error("Get next music failed");
                return;  // In case runs forever if idleList is empty
            } else {
                try {
                    play(next, server);
                    NekoMusic.currentMusic = next;
                    NekoMusic.task = new TimerTask() {
                        @Override
                        public void run() {
                            playNext(server);
                        }
                    };
                    NekoMusic.TIMER.schedule(NekoMusic.task, next.dt + 3000);  // Add 3 seconds to avoid the music starts before the previous one ends
                    HudManager.sendNext();
                    success = true;
                } catch (Exception e) {
                    NekoMusic.LOGGER.error("Play music failed", e);
                }
            }
            if (!success) {
                NekoMusic.task = new TimerTask() {
                    @Override
                    public void run() {
                        playNext(server);
                    }
                };
                NekoMusic.TIMER.schedule(NekoMusic.task, 5000);
            }
        });
    }

    /*
    * Always call this method in a new thread
     */
    public static void play(@NotNull MusicObj musicObj, MinecraftServer server) {
        // for compatibility with allmusic, use the same id as it
        List<ServerPlayerEntity> playerList = PlayerHelper.getOnlineRealPlayerList(server);
        if (playerList.size() == 0)
            return;
//        List<ServerPlayerEntity> nekoPlayerList = PlayerHelper.getOnlineRealPlayerList(server);
//        List<ServerPlayerEntity> allMusicPlayerList = PlayerHelper.getAllMusicCompatUserList(server);
//        if (nekoPlayerList.size() + allMusicPlayerList.size() == 0)
//            return;

        PacketByteBuf stopBuf = PacketHelper.getStopPacket();
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, NekoMusic.ALLMUSIC_COMPAT_ID, stopBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send stop packet failed", e);
            }
        }

        PacketByteBuf playBuf = PacketHelper.getPlayPacket(musicObj);
        if (playBuf == null)
            throw new RuntimeException("Generate play packet failed");
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, NekoMusic.ALLMUSIC_COMPAT_ID, playBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send play packet failed", e);
            }
        }
        server.getPlayerManager().broadcast(PacketHelper.getPlayMessage(musicObj), false);
    }

    public static void next(MinecraftServer server) {
        playNext(server);
    }

    public static void vote(MinecraftServer server,  ServerCommandSource source) {
        if (Permissions.check(source, "nekomusic.next", 1)) {
            next(server);
            return;
        }
        if (source.isExecutedByPlayer()) {
            NekoMusic.currentVote.add(source.getName());
        } else {
            NekoMusic.currentVote.add("console");
        }
        if ( (float)(NekoMusic.currentVote.size()) / PlayerHelper.getOnlineRealPlayerList(server).size()
                >= NekoMusic.CONFIG.voteThreshold) {
            playNext(server);
        } else {
            server.getPlayerManager().broadcast(PacketHelper.getVoteMessage(
                    NekoMusic.currentVote.size(), PlayerHelper.getOnlineRealPlayerList(server).size()), false);
        }
    }

    public static void order(MinecraftServer server, ServerCommandSource source, String url) {
        long id;
        if (intPattern.matcher(url).matches()) {
            id = Long.parseLong(url);
        } else {
            Matcher matcher;
            if (url.contains("m/song/") || url.contains("#/song/")) {
                matcher = urlPattern1.matcher(url);
            } else {
                matcher = urlPattern2.matcher(url);
            }
            if (matcher.find()) {
                id = Long.parseLong(matcher.group(1));
            } else {
                id = 0;
            }
        }
        if (NekoMusic.currentMusic.id == id || NekoMusic.orderList.hasSong(id)) {
            source.sendFeedback(PacketHelper.getOrderedMessage(), false);
            return;
        }

        NekoMusic.EXECUTOR.execute(() -> {
            MusicObj musicObj = Api.getMusicInfo(id);
            if (musicObj != null) {
                if (source.isExecutedByPlayer()) {
                    musicObj.player = source.getName();
                } else {
                    musicObj.player = "console";
                }
                NekoMusic.orderList.add(musicObj);
                server.getPlayerManager().broadcast(PacketHelper.getOrderMessage(musicObj), false);
                if (!NekoMusic.orderList.isPlaying
                        && PlayerHelper.getOnlineRealPlayerList(server).size() > 0) {
                    playNext(server);
                } else {
                    HudManager.sendList();
                }
            } else {
                source.sendFeedback(PacketHelper.getOrderMessage(), false);
            }
        });
    }

    public static void del(MinecraftServer server, ServerCommandSource source, int index) {
        if (index <= 0 || index > NekoMusic.orderList.songs.size()) {
            source.sendFeedback(PacketHelper.getDelMessage(1), true);
            return;
        }
        MusicObj musicObj = NekoMusic.orderList.songs.get(index - 1);
        del(server, source, musicObj);
    }

    public static void del(MinecraftServer server, ServerCommandSource source, long id) {
        MusicObj musicObj = NekoMusic.orderList.get(id);
        if (musicObj == null) {
            source.sendFeedback(PacketHelper.getDelMessage(1), true);
            return;
        }
        del(server, source, musicObj);
    }

    static void del(MinecraftServer server, ServerCommandSource source, MusicObj musicObj) {
        if (musicObj.player.equals(source.getName())
                || Permissions.check(source, "nekomusic.del.other", 1)) {
            NekoMusic.orderList.songs.remove(musicObj);
            source.sendFeedback(PacketHelper.getDelMessage(musicObj), true);
            HudManager.sendList();
        } else {
            source.sendFeedback(PacketHelper.getDelMessage(2), false);
        }
    }

    public static void list(MinecraftServer server, ServerCommandSource source) {
        source.sendFeedback(PacketHelper.getListMessage(), false);
    }

    public static void search(MinecraftServer server, ServerCommandSource source, String keyword, int page) {
        NekoMusic.EXECUTOR.execute(() -> {
            Api.SearchResult result = Api.search(keyword, page, 10);
            if (result != null && result.result != null) {
                source.sendFeedback(PacketHelper.getSearchMessage(result), false);
            } else {
                source.sendFeedback(PacketHelper.getSearchMessage(), false);
            }
        });
    }
}
