package org.lolicode.nekomusic.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.CarpetHelper;

import java.util.List;
import java.util.Set;

public class PlayerManager {

    public static List<ServerPlayer> getOnlineRealPlayerList(MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> !CarpetHelper.isPlayerFake(player)).toList();
    }

    public static Set<ServerPlayer> getNekoPlayerSet() {
        return NekoMusic.nekoPlayerSet;
    }
}
