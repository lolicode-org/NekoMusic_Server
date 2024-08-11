package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.CarpetHelper;
import org.lolicode.nekomusic.manager.PlayerManager;
import org.lolicode.nekomusic.manager.MusicManager;

public class PlayerJoin {
    public static void OnPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        OnPlayerJoin(player, server, true);
    }

    public static void OnPlayerJoin(ServerPlayerEntity player, MinecraftServer server, boolean addToTempSet) {
        if (CarpetHelper.isPlayerFake(player))
            return;
        if (addToTempSet)
            NekoMusic.tempNekoPlayerSet.add(player);
        if (PlayerManager.getNekoPlayerSet().size() == 1) {
            try {
                MusicManager.playNext(server);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Play music failed", e);
            }
        }
    }
}
