package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.CarpetHelper;
import org.lolicode.nekomusic.helper.OnlineRealPlayerHelper;
import org.lolicode.nekomusic.manager.MusicManager;

public class PlayerJoin {
    public static void OnPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        if (CarpetHelper.isPlayerFake(player))
            return;
        if (OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() == 1) {
            try {
                MusicManager.playNext(server);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Play music failed", e);
            }
        }
    }
}
