package org.lolicode.allmusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.helper.CarpetHelper;
import org.lolicode.allmusic.helper.OnlineRealPlayerHelper;
import org.lolicode.allmusic.manager.MusicManager;

public class PlayerJoin {
    public static void OnPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        if (CarpetHelper.isPlayerFake(player))
            return;
        if (OnlineRealPlayerHelper.getOnlineRealPlayerList(server).size() == 1) {
            MusicManager.playNext(server);
        }
    }
}
