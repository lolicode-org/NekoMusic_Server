package org.lolicode.nekomusic.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

// Copied from https://github.com/ByMartrixx/player-events/blob/master/api/src/main/java/me/bymartrixx/playerevents/api/event/PlayerLeaveCallback.java

public interface PlayerLeaveCallback {
    Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class, (listeners) -> (player, server) -> {
        for (PlayerLeaveCallback listener : listeners) {
            listener.leaveServer(player, server);
        }
    });

    void leaveServer(ServerPlayerEntity player, MinecraftServer server);
}
