package org.lolicode.nekomusic.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.helper.CarpetHelper;

import java.util.List;

public class PlayerManager {

    public static List<ServerPlayerEntity> getOnlineRealPlayerList(MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !CarpetHelper.isPlayerFake(player)).toList();
    }
}
