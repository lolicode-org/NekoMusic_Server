package org.lolicode.nekomusic.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.lolicode.nekomusic.NekoMusic;

public class PlayerLeave {
    public static void OnPlayerLeave(ServerPlayer player, MinecraftServer server) {
        NekoMusic.nekoPlayerSet.remove(player);
    }
}
