package org.lolicode.nekomusic.manager;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.config.ModConfig;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.Api;
import org.lolicode.nekomusic.music.MusicObj;
import org.lolicode.nekomusic.music.MusicUrlGetException;

import java.util.ArrayList;
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
        if (PlayerManager.getOnlineRealPlayerList(server).size() == 0)
            return;

        NekoMusic.currentVote.clear();
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                boolean playSuccess = false;
                boolean urlGetSuccess = false;

                MusicObj next = null;
                try {
                    next = NekoMusic.orderList.next();
                    if (next == null) {  // Empty order list
                        try {
                            next = NekoMusic.idleList.next();
                            urlGetSuccess = true;
                            if (next == null) { // Empty idle list
                                NekoMusic.LOGGER.info("No music to play");
                                return;
                            }
                        } catch (MusicUrlGetException ignored) {  // Idle list fails, dont send broadcast. The error log has already been sent
                        }
                    } else {
                        urlGetSuccess = true;
                    }
                } catch (MusicUrlGetException e) {  // Music order list fails, send broadcast
                    server.getPlayerManager().broadcast(PacketHelper.getGetMusicErrorMessage(e.getMusicDescription()), false);
                }
                if (urlGetSuccess) {
                    try {
                        assert next != null;
                        play(next, server);
                        NekoMusic.currentMusic = next;
                        NekoMusic.task = new TimerTask() {
                            @Override
                            public void run() {
                                playNext(server);
                            }
                        };
                        NekoMusic.TIMER.schedule(NekoMusic.task, next.dt + 3000);  // Add 3 seconds to avoid the music starts before the previous one ends
//                        HudManager.sendNext();
                        playSuccess = true;
                    } catch (Exception e) {
                        NekoMusic.LOGGER.error("Play music failed", e);
                    }
                }
                if (!playSuccess) {
                    NekoMusic.task = new TimerTask() {
                        @Override
                        public void run() {
                            playNext(server);
                        }
                    };
                    NekoMusic.TIMER.schedule(NekoMusic.task, 5000);
                }
            } catch (InterruptedException e) {
                NekoMusic.LOGGER.info("Interrupted");
            }
        });
    }

    /*
    * Always call this method in a new thread
     */
    public static void play(@NotNull MusicObj musicObj, MinecraftServer server) {
        // for compatibility with allmusic, use the same id as it
        List<ServerPlayerEntity> playerList = PlayerManager.getOnlineRealPlayerList(server);
        if (playerList.size() == 0)
            return;

        PacketByteBuf stopBuf = PacketHelper.getStopPacket();
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, NekoMusic.ALLMUSIC_COMPAT_ID, stopBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send stop packet failed", e);
            }
        }

        HudManager.sendMetadata(musicObj);  // send metadata first, so that the client can determine whether this is a neko server
        HudManager.sendPlayList();

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

    public static void next(MinecraftServer server, ServerCommandSource source) {
        source.sendFeedback(PacketHelper.getWorkingMessage(), false);
        playNext(server);
    }

    public static void vote(MinecraftServer server,  ServerCommandSource source) {
        if (Permissions.check(source, "nekomusic.next", 1)) {
            next(server, source);
            return;
        }
        if (source.isExecutedByPlayer()) {
            NekoMusic.currentVote.add(source.getName());
        } else {
            NekoMusic.currentVote.add("console");
            NekoMusic.LOGGER.warn("Got vote from console, this should not happen, please check your permission manager.");
        }
        if ( (float)(NekoMusic.currentVote.size()) / PlayerManager.getOnlineRealPlayerList(server).size()
                >= NekoMusic.CONFIG.voteThreshold) {
            next(server, source);
        } else {
            server.getPlayerManager().broadcast(PacketHelper.getVoteMessage(
                    NekoMusic.currentVote.size(), PlayerManager.getOnlineRealPlayerList(server).size()), false);
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
        if (NekoMusic.CONFIG.bannedSongs != null && NekoMusic.CONFIG.bannedSongs.contains(id)
                && !Permissions.check(source, "nekomusic.bypassban", 1)) {
            source.sendFeedback(PacketHelper.getBannedMessage(), false);
            return;
        }

        source.sendFeedback(PacketHelper.getWorkingMessage(), false);

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
                        && PlayerManager.getOnlineRealPlayerList(server).size() > 0) {
                    playNext(server);
                } else {
//                    HudManager.sendList();
                    HudManager.sendPlayList();
                }
            } else {
                source.sendFeedback(PacketHelper.getOrderMessage(), false);
            }
        });
    }

    public static void del(MinecraftServer server, ServerCommandSource source, int index) {
        if (index <= 0 || index > NekoMusic.orderList.size()) {
            source.sendFeedback(PacketHelper.getDelMessage(1), true);
            return;
        }
        MusicObj musicObj = NekoMusic.orderList.get(index - 1);
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
            NekoMusic.orderList.remove(musicObj);
            source.sendFeedback(PacketHelper.getDelMessage(musicObj), true);
//            HudManager.sendList();
            HudManager.sendPlayList();
        } else {
            source.sendFeedback(PacketHelper.getDelMessage(2), false);
        }
    }

    public static void list(MinecraftServer server, ServerCommandSource source) {
        source.sendFeedback(PacketHelper.getListMessage(), false);
    }

    public static void search(MinecraftServer server, ServerCommandSource source, String keyword, int page) {
        source.sendFeedback(PacketHelper.getWorkingMessage(), false);
        NekoMusic.EXECUTOR.execute(() -> {
            Api.SearchResult result = Api.search(keyword, page, 10);
            if (result != null && result.result != null) {
                source.sendFeedback(PacketHelper.getSearchMessage(result), false);
            } else {
                source.sendFeedback(PacketHelper.getSearchMessage(), false);
            }
        });
    }

    public static void ban(MinecraftServer server, ServerCommandSource source, long id) {
        if (NekoMusic.CONFIG.bannedSongs == null) {
            NekoMusic.CONFIG.bannedSongs = new ArrayList<>();
        }
        if (NekoMusic.CONFIG.bannedSongs.contains(id)) {
            source.sendFeedback(PacketHelper.getBanMessage(1), false);
            return;
        }
        if (id <= 0) {
            source.sendFeedback(PacketHelper.getBanMessage(2), false);
            return;
        }
        NekoMusic.CONFIG.bannedSongs.add(id);
        ModConfig.save();
        source.sendFeedback(PacketHelper.getBanMessage(3), false);
    }

    public static void unban(MinecraftServer server, ServerCommandSource source, long id) {
        if (NekoMusic.CONFIG.bannedSongs == null || !NekoMusic.CONFIG.bannedSongs.contains(id)) {
            source.sendFeedback(PacketHelper.getUnbanMessage(1), false);
            return;
        }
        NekoMusic.CONFIG.bannedSongs.remove(id);
        ModConfig.save();
        source.sendFeedback(PacketHelper.getUnbanMessage(2), false);
    }
}
