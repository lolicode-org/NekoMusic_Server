package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lolicode.nekomusic.NekoMusic;

public class PlayerLeave {
    public static void OnPlayerLeave(ServerPlayerEntity player, MinecraftServer server) {
        NekoMusic.nekoPlayerSet.remove(player);
        NekoMusic.tempNekoPlayerSet.remove(player);
    }
}
