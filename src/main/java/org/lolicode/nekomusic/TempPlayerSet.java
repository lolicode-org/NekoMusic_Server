package org.lolicode.nekomusic;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TempPlayerSet {
    private final ConcurrentHashMap<ServerPlayerEntity, Long> players = new ConcurrentHashMap<>();

    public void add(ServerPlayerEntity player) {
        this.players.keySet().stream().filter(p -> p.getUuid().equals(player.getUuid())).findFirst().ifPresent(this.players::remove);
        this.players.put(player, System.currentTimeMillis());
    }

    public void remove(ServerPlayerEntity player) {
        this.players.remove(player);
    }

    public Set<ServerPlayerEntity> getPlayers() {
        this.players.keySet().removeIf(player -> System.currentTimeMillis() - this.players.get(player) > 3000);
        return new HashSet<>(this.players.keySet());
    }
}
