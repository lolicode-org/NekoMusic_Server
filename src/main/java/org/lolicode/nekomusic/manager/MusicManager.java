package org.lolicode.nekomusic.manager;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.config.ModConfig;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.Api;
import org.lolicode.nekomusic.music.MusicObj;
import org.lolicode.nekomusic.music.MusicUrlGetException;

import java.util.ArrayList;
import java.util.Set;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicManager {
    private static final Pattern intPattern = Pattern.compile("\\d+");
    private static final Pattern urlPattern1 = Pattern.compile("song/(\\d+)");
    private static final Pattern urlPattern2 = Pattern.compile("[?&]id=(\\d+)");
    private static final Pattern urlPatternShort = Pattern.compile("https?://163cn\\.tv/[a-zA-Z0-9]+");
    public static void playNext(MinecraftServer server) {
        if (NekoMusic.task != null) {
            NekoMusic.task.cancel();  // If user issues next command, cancel the current task in case it's not finished
            NekoMusic.task = null;
        }
        if (PlayerManager.getNekoPlayerSet().isEmpty())
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
                    server.getPlayerList().broadcastSystemMessage(PacketHelper.getGetMusicErrorMessage(e.getMusicDescription()), false);
                }
                if (urlGetSuccess) {
                    try {
                        assert next != null;
                        play(next, server);
                        NekoMusic.currentMusic = next;
                        NekoMusic.currentStartTime = System.currentTimeMillis();
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
        Set<ServerPlayer> playerList = PlayerManager.getNekoPlayerSet();
        if (playerList.isEmpty())
            return;

        HudManager.sendMetadata(musicObj);
        HudManager.sendPlayList();

        server.getPlayerList().broadcastSystemMessage(PacketHelper.getPlayMessage(musicObj), false);
    }

    public static void playToPlayer(@NotNull MusicObj musicObj, ServerPlayer player, boolean seek) {
        HudManager.sendMetadata(musicObj, player, seek);
        HudManager.sendPlayList(player);
        player.displayClientMessage(PacketHelper.getPlayMessage(musicObj), false);
    }

    public static void resumeToPlayer(ServerPlayer player) {
        if (NekoMusic.currentMusic == null) return;
        playToPlayer(NekoMusic.currentMusic, player, System.currentTimeMillis() - NekoMusic.currentStartTime > 5000);  // don't seek if the audio has been playing for less than 5 seconds
    }

    public static void next(MinecraftServer server, CommandSourceStack source) {
        source.sendSuccess(PacketHelper.getWorkingMessage(), false);
        playNext(server);
    }

    public static void vote(MinecraftServer server,  CommandSourceStack source) {
        if (source.isPlayer()) {
            NekoMusic.currentVote.add(source.getTextName());
        } else {
            NekoMusic.currentVote.add("console");
            NekoMusic.LOGGER.warn("Got vote from console, this should not happen, please check your permission manager.");
        }
        if ( (float)(NekoMusic.currentVote.size()) / PlayerManager.getOnlineRealPlayerList(server).size()
                >= NekoMusic.CONFIG.voteThreshold) {
            next(server, source);
        } else {
            server.getPlayerList().broadcastSystemMessage(PacketHelper.getVoteMessage(
                    NekoMusic.currentVote.size(), PlayerManager.getOnlineRealPlayerList(server).size()), false);
        }
    }

    public static void order(MinecraftServer server, CommandSourceStack source, String url, boolean skipIdle, boolean addToFirst) {
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

        source.sendSuccess(PacketHelper.getWorkingMessage(), false);

        NekoMusic.EXECUTOR.execute(() -> {
            long real_id = 0;
            if (id <= 0 && url.contains("163cn.tv")) {
                try {
                    var extracted = urlPatternShort.matcher(url);
                    if (extracted.find()) {
                        var real_url = Api.getRealUrl(extracted.group());
                        if (real_url != null) {
                            var matcher = urlPattern2.matcher(real_url);
                            if (matcher.find()) {
                                real_id = Long.parseLong(matcher.group(1));
                            }
                        }
                    }
                } catch (Exception e) {
                    source.sendSuccess(PacketHelper.getOrderMessage(), false);
                    return;
                }
            }
            if (real_id <= 0) {
                if (id <= 0) {
                    source.sendSuccess(PacketHelper.getOrderMessage(), false);
                    return;
                } else {
                    real_id = id;
                }
            }
            if ((NekoMusic.currentMusic != null && NekoMusic.currentMusic.id == real_id)
                    || (NekoMusic.orderList.hasSong(real_id) && !(addToFirst && skipIdle))) {
                source.sendSuccess(PacketHelper.getOrderedMessage(), false);
                return;
            }
            if (NekoMusic.CONFIG.bannedSongs != null && NekoMusic.CONFIG.bannedSongs.contains(real_id)
                    && !Permissions.check(source, "nekomusic.bypassban", PermissionLevel.MODERATORS)) {
                source.sendSuccess(PacketHelper.getBannedMessage(), false);
                return;
            }
            MusicObj musicObj = Api.getMusicInfo(real_id);
            if (musicObj != null) {
                if (source.isPlayer()) {
                    musicObj.player = source.getTextName();
                } else {
                    musicObj.player = "console";
                }
                if (addToFirst) {
                    NekoMusic.orderList.remove(real_id);
                    NekoMusic.orderList.addToFirst(musicObj);
                } else {
                    NekoMusic.orderList.add(musicObj);
                }
                server.getPlayerList().broadcastSystemMessage(PacketHelper.getOrderMessage(musicObj), false);
                if (((!NekoMusic.orderList.isPlaying && (skipIdle || NekoMusic.idleList.size() <= 0)) || addToFirst)
                        && !PlayerManager.getNekoPlayerSet().isEmpty()) {
                    playNext(server);
                } else {
//                    HudManager.sendList();
                    HudManager.sendPlayList();
                }
            } else {
                source.sendSuccess(PacketHelper.getOrderMessage(), false);
            }
        });
    }

    public static void del(MinecraftServer server, CommandSourceStack source, int index) {
        if (index <= 0 || index > NekoMusic.orderList.size()) {
            source.sendSuccess(PacketHelper.getDelMessage(1), true);
            return;
        }
        MusicObj musicObj = NekoMusic.orderList.get(index - 1);
        del(server, source, musicObj);
    }

    public static void del(MinecraftServer server, CommandSourceStack source, long id) {
        MusicObj musicObj = NekoMusic.orderList.get(id);
        if (musicObj == null) {
            source.sendSuccess(PacketHelper.getDelMessage(1), true);
            return;
        }
        del(server, source, musicObj);
    }

    static void del(MinecraftServer server, CommandSourceStack source, MusicObj musicObj) {
        if (musicObj.player.equals(source.getTextName())
                || Permissions.check(source, "nekomusic.del.other", PermissionLevel.MODERATORS)) {
            NekoMusic.orderList.remove(musicObj);
            source.sendSuccess(PacketHelper.getDelMessage(musicObj), true);
//            HudManager.sendList();
            HudManager.sendPlayList();
        } else {
            source.sendSuccess(PacketHelper.getDelMessage(2), false);
        }
    }

    public static void list(MinecraftServer server, CommandSourceStack source) {
        source.sendSuccess(PacketHelper.getListMessage(), false);
    }

    public static void search(MinecraftServer server, CommandSourceStack source, String keyword, int page) {
        source.sendSuccess(PacketHelper.getWorkingMessage(), false);
        NekoMusic.EXECUTOR.execute(() -> {
            Api.SearchResult result = Api.search(keyword, page, 10);
            if (result != null && result.result != null) {
                source.sendSuccess(PacketHelper.getSearchMessage(result, source), false);
            } else {
                source.sendSuccess(PacketHelper.getSearchMessage(), false);
            }
        });
    }

    public static void ban(MinecraftServer server, CommandSourceStack source, long id) {
        if (NekoMusic.CONFIG.bannedSongs == null) {
            NekoMusic.CONFIG.bannedSongs = new ArrayList<>();
        }
        if (NekoMusic.CONFIG.bannedSongs.contains(id)) {
            source.sendSuccess(PacketHelper.getBanMessage(1), false);
            return;
        }
        if (id <= 0) {
            source.sendSuccess(PacketHelper.getBanMessage(2), false);
            return;
        }
        NekoMusic.CONFIG.bannedSongs.add(id);
        ModConfig.save();
        if (NekoMusic.currentMusic != null && NekoMusic.currentMusic.id == id) {
            playNext(server);
        }
        NekoMusic.orderList.remove(id);
        source.sendSuccess(PacketHelper.getBanMessage(3), false);
    }

    public static void unban(MinecraftServer server, CommandSourceStack source, long id) {
        if (NekoMusic.CONFIG.bannedSongs == null || !NekoMusic.CONFIG.bannedSongs.contains(id)) {
            source.sendSuccess(PacketHelper.getUnbanMessage(1), false);
            return;
        }
        NekoMusic.CONFIG.bannedSongs.remove(id);
        ModConfig.save();
        source.sendSuccess(PacketHelper.getUnbanMessage(2), false);
    }
}
