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
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicManager {
    private static final Pattern intPattern = Pattern.compile("\\d+");
    private static final Pattern urlPattern1 = Pattern.compile("song/(\\d+)");
    private static final Pattern urlPattern2 = Pattern.compile("[?&]id=(\\d+)");

    public static void playNext(MinecraftServer server) {
        if (Allmusic.EXECUTORS.size() > 0) {
            for (java.util.concurrent.ScheduledFuture<?> future : Allmusic.EXECUTORS) {
                if (future.isDone())
                    continue;
                future.cancel(false);
//                Allmusic.EXECUTORS.remove(future);  // FIXME: ConcurrentModificationException
            }
        }
        if (OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() == 0)
            return;
        boolean success = false;
        Allmusic.currentVote.clear();
        MusicObj next;
        if (Allmusic.playingList.songs.size() == 0) {
            next = Allmusic.idleList.next();
        } else {
            next = Allmusic.playingList.next();
        }
        if (next == null) {
            Allmusic.LOGGER.error("Get next music failed");
        } else {
            try {
                play(next, server);
                Allmusic.EXECUTORS.add(0,
                        Executors.newSingleThreadScheduledExecutor()
                                .schedule(() -> playNext(server), next.time, java.util.concurrent.TimeUnit.MILLISECONDS));  // FIXME: Server can't shutdown
                success = true;
            } catch (Exception e) {
                Allmusic.LOGGER.error("Play music failed", e);
            }
        }
        if (!success) {
            Allmusic.EXECUTORS.add(0, Executors.newSingleThreadScheduledExecutor()
                    .schedule(() -> playNext(server), 5, java.util.concurrent.TimeUnit.SECONDS));
        }
    }

    public static void play(@NotNull MusicObj musicObj, MinecraftServer server) {
        List<ServerPlayerEntity> playerList = OnlineRealPlayerHelper.getOnlineRealPlayerList(server);
        PacketByteBuf buf = PacketHelper.getPlayPacket(musicObj);
        if (buf == null)
            throw new RuntimeException("Generate play packet failed");
        for (ServerPlayerEntity player : playerList) {
            try {
                ServerPlayNetworking.send(player, Allmusic.ID, buf);
            } catch (Exception e) {
                e.printStackTrace();
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
        int id = 0;
        if (intPattern.matcher(url).matches()) {
            id = Integer.parseInt(url);
        } else {
            Matcher matcher;
            if (url.contains("m/song/") || url.contains("#/song/")) {
                matcher = urlPattern1.matcher(url);
            } else {
                matcher = urlPattern2.matcher(url);
            }
            if (matcher.find()) {
                id = Integer.parseInt(matcher.group(0));
            }
        }
        MusicObj musicObj = Api.getMusic(id);
        if (musicObj != null) {
            if (source.isExecutedByPlayer()) {
                musicObj.player = source.getName();
            } else {
                musicObj.player = "console";
            }
            Allmusic.playingList.add(musicObj);
            server.getPlayerManager().broadcast(PacketHelper.getOrderMessage(musicObj), false);
            if (Allmusic.playingList.songs.size() == 1
                    && OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() > 0) {
                playNext(server);
            }
        } else {
            source.sendFeedback(PacketHelper.getOrderMessage(), false);
        }
    }

    public static void del(MinecraftServer server, ServerCommandSource source, int index) {
        if (index < 0 || index >= Allmusic.playingList.songs.size()) {
            source.sendFeedback(PacketHelper.getDelMessage(), true);
            return;
        }
        MusicObj musicObj = Allmusic.playingList.songs.get(index);
        if (musicObj.player.equals(source.getName())
                || Permissions.check(source, "allmusic.del.other", 1)) {
            Allmusic.playingList.songs.remove(index);
            server.getPlayerManager().broadcast(PacketHelper.getDelMessage(musicObj),false);
        } else {
            source.sendFeedback(PacketHelper.getDelMessage(), true);
        }
    }

    public static void list(MinecraftServer server, ServerCommandSource source) {
        source.sendFeedback(PacketHelper.getListMessage(), false);
    }
}
