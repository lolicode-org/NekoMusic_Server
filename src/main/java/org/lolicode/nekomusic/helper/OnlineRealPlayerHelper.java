package org.lolicode.nekomusic.helper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class OnlineRealPlayerHelper {
    public static List<ServerPlayerEntity> getOnlineRealPlayerList(MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !CarpetHelper.isPlayerFake(player)).toList();
    }
}
