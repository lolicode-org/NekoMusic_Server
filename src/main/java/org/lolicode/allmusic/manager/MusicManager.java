package org.lolicode.allmusic.manager;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.helper.OnlineRealPlayerHelper;
import org.lolicode.allmusic.helper.PacketHelper;
import org.lolicode.allmusic.music.Api;
import org.lolicode.allmusic.music.MusicObj;

import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicManager {
    private static final Pattern intPattern = Pattern.compile("\\d+");
    private static final Pattern urlPattern1 = Pattern.compile("song/(\\d+)");
    private static final Pattern urlPattern2 = Pattern.compile("[?&]id=(\\d+)");
    public static void playNext(MinecraftServer server) {
        if (Allmusic.task != null) {
            Allmusic.task.cancel();  // If user issues next command, cancel the current task in case it's not finished
            Allmusic.task = null;
        }
        if (OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() == 0)
            return;

        Allmusic.currentVote.clear();
        Allmusic.EXECUTOR.execute(() -> {
            boolean success = false;

            MusicObj next = Allmusic.orderList.next();
            if (next == null) {
                next = Allmusic.idleList.next();
            }
            if (next == null) {
                Allmusic.LOGGER.error("Get next music failed");
                return;  // In case runs forever if idleList is empty
            } else {
                try {
                    play(next, server);
                    Allmusic.currentMusic = next;
                    Allmusic.task = new TimerTask() {
                        @Override
                        public void run() {
                            playNext(server);
                        }
                    };
                    Allmusic.TIMER.schedule(Allmusic.task, next.dt + 3000);  // Add 3 seconds to avoid the music starts before the previous one ends
                    success = true;
                } catch (Exception e) {
                    Allmusic.LOGGER.error("Play music failed", e);
                }
            }
            if (!success) {
                Allmusic.task = new TimerTask() {
                    @Override
                    public void run() {
                        playNext(server);
                    }
                };
                Allmusic.TIMER.schedule(Allmusic.task, 5000);
            }
        });
    }

    public static void play(@NotNull MusicObj musicObj, MinecraftServer server) {
        List<ServerPlayerEntity> playerList = OnlineRealPlayerHelper.getOnlineRealPlayerList(server);
        if (playerList.size() == 0)
            return;

        PacketByteBuf stopBuf = PacketHelper.getStopPacket();
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, Allmusic.ID, stopBuf);
            } catch (Exception e) {
                Allmusic.LOGGER.error("Send stop packet failed", e);
            }
        }

        PacketByteBuf playBuf = PacketHelper.getPlayPacket(musicObj);
        if (playBuf == null)
            throw new RuntimeException("Generate play packet failed");
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, Allmusic.ID, playBuf);
            } catch (Exception e) {
                Allmusic.LOGGER.error("Send play packet failed", e);
            }
        }
        server.getPlayerManager().broadcast(PacketHelper.getPlayMessage(musicObj), false);
    }

    public static void next(MinecraftServer server) {
        playNext(server);
    }

    public static void vote(MinecraftServer server,  ServerCommandSource source) {
        if (Permissions.check(source, "allmusic.next", 1)) {
            next(server);
            return;
        }
        if (source.isExecutedByPlayer()) {
            Allmusic.currentVote.add(source.getName());
        } else {
            Allmusic.currentVote.add("console");
        }
        if ( (float)(Allmusic.currentVote.size()) / OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size()
                >= Allmusic.CONFIG.voteThreshold) {
            playNext(server);
        } else {
            server.getPlayerManager().broadcast(PacketHelper.getVoteMessage(
                    Allmusic.currentVote.size(), OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size()), false);
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
        if (Allmusic.currentMusic.id == id || Allmusic.orderList.hasSong(id)) {
            source.sendFeedback(PacketHelper.getOrderedMessage(), false);
            return;
        }

        Allmusic.EXECUTOR.execute(() -> {
            MusicObj musicObj = Api.getMusicInfo(id);
            if (musicObj != null) {
                if (source.isExecutedByPlayer()) {
                    musicObj.player = source.getName();
                } else {
                    musicObj.player = "console";
                }
                Allmusic.orderList.add(musicObj);
                server.getPlayerManager().broadcast(PacketHelper.getOrderMessage(musicObj), false);
                if (!Allmusic.orderList.isPlaying
                        && OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() > 0) {
                    playNext(server);
                }
            } else {
                source.sendFeedback(PacketHelper.getOrderMessage(), false);
            }
        });
    }

    public static void del(MinecraftServer server, ServerCommandSource source, int index) {
        if (index <= 0 || index > Allmusic.orderList.songs.size()) {
            source.sendFeedback(PacketHelper.getDelMessage(1), true);
            return;
        }
        MusicObj musicObj = Allmusic.orderList.songs.get(index - 1);
        if (musicObj.player.equals(source.getName())
                || Permissions.check(source, "allmusic.del.other", 1)) {
            Allmusic.orderList.songs.remove(index - 1);
            source.sendFeedback(PacketHelper.getDelMessage(musicObj), true);
        } else {
            source.sendFeedback(PacketHelper.getDelMessage(2), true);
        }
    }

    public static void del(MinecraftServer server, ServerCommandSource source, long id) {
        MusicObj musicObj = Allmusic.orderList.get(id);
        if (musicObj == null) {
            source.sendFeedback(PacketHelper.getDelMessage(1), true);
            return;
        }
        if (musicObj.player.equals(source.getName())
                || Permissions.check(source, "allmusic.del.other", 1)) {
            Allmusic.orderList.songs.remove(musicObj);
            source.sendFeedback(PacketHelper.getDelMessage(musicObj), true);
        } else {
            source.sendFeedback(PacketHelper.getDelMessage(2), true);
        }
    }

    public static void list(MinecraftServer server, ServerCommandSource source) {
        source.sendFeedback(PacketHelper.getListMessage(), false);
    }

    public static void search(MinecraftServer server, ServerCommandSource source, String keyword, int page) {
        Allmusic.EXECUTOR.execute(() -> {
            Api.SearchResult result = Api.search(keyword, page, 10);
            if (result != null && result.result != null) {
                source.sendFeedback(PacketHelper.getSearchMessage(result), false);
            } else {
                source.sendFeedback(PacketHelper.getSearchMessage(), false);
            }
        });
    }
}
