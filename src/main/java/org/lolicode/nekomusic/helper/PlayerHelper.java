package org.lolicode.nekomusic.helper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;

import java.util.List;

public class PlayerHelper {
    public static List<ServerPlayerEntity> getOnlineRealPlayerList(MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !CarpetHelper.isPlayerFake(player)).toList();
    }

//    public static List<ServerPlayerEntity> getAllMusicCompatUserList(MinecraftServer server) {
//        return server.getPlayerManager().getPlayerList().stream()
//                .filter(player -> !CarpetHelper.isPlayerFake(player) && !NekoMusic.nekoPlayerList.contains(player)).toList();
//    }
}
