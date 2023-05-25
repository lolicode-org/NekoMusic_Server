package org.lolicode.nekomusic.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.CarpetHelper;

import java.util.List;
import java.util.Set;

public class PlayerManager {

    public static List<ServerPlayerEntity> getOnlineRealPlayerList(MinecraftServer server) {
        return server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !CarpetHelper.isPlayerFake(player)).toList();
    }

    public static Set<ServerPlayerEntity> getNekoPlayerSet() {
        var players = NekoMusic.tempNekoPlayerSet.getPlayers();
        players.addAll(NekoMusic.nekoPlayerSet);  // This is a shadow copy, so it's safe to add elements to it.
        return players;
    }
}
