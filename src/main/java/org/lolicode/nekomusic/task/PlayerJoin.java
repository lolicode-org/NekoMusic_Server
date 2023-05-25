package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.CarpetHelper;
import org.lolicode.nekomusic.manager.PlayerManager;
import org.lolicode.nekomusic.manager.MusicManager;

import java.util.TimerTask;

public class PlayerJoin {
    public static void OnPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        if (CarpetHelper.isPlayerFake(player))
            return;
        if (PlayerManager.getOnlineRealPlayerList(server).size() == 1) {
            try {
                if (NekoMusic.task != null) {
                    NekoMusic.task.cancel();
                    NekoMusic.task = null;
                }
                NekoMusic.task = new TimerTask() {
                    @Override
                    public void run() {
                        MusicManager.playNext(server);
                    }
                };
                NekoMusic.TIMER.schedule(NekoMusic.task, 3000); // Add a delay to wait for the client to send hello packet
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Play music failed", e);
            }
        }
    }
}
