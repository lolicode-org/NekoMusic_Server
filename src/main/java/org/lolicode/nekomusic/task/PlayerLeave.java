package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.PlayerManager;

public class PlayerLeave {
    public static void OnPlayerLeave(ServerPlayerEntity player, MinecraftServer server) {
        NekoMusic.nekoPlayerSet.remove(player);
        if (PlayerManager.getOnlineRealPlayerList(server).size() == 0) {
            if (NekoMusic.task != null) {
                NekoMusic.task.cancel();
                NekoMusic.task = null;
            }
        }
    }
}
