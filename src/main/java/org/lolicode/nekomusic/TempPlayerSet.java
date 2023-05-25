package org.lolicode.nekomusic;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TempPlayerSet {
    private Set<ServerPlayerEntity> players;
    private Map<String, Long> loginTime;
    private Lock lock = new ReentrantLock();

    public TempPlayerSet() {
        this.players = new HashSet<>();
        this.loginTime = new HashMap<>();
    }

    public void add(ServerPlayerEntity player) {
        var existing = this.players.stream().filter(p -> p.getUuid().equals(player.getUuid())).findFirst();
        try {
            lock.lock();
            if (existing.isPresent()) {
                this.players.remove(existing.get());
                this.loginTime.remove(existing.get().getUuid().toString());
            }
            this.players.add(player);
            this.loginTime.put(player.getUuid().toString(), System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }

    public void remove(ServerPlayerEntity player) {
        try {
            lock.lock();
            this.players.remove(player);
            this.loginTime.remove(player.getUuid().toString());
        } finally {
            lock.unlock();
        }
    }

    public Set<ServerPlayerEntity> getPlayers() {
        try {
            lock.lock();
            for (ServerPlayerEntity player : this.players) {
                if (System.currentTimeMillis() - this.loginTime.get(player.getUuid().toString()) > 3000) {
                    this.players.remove(player);
                    this.loginTime.remove(player.getUuid().toString());
                }
            }
        } finally {
            lock.unlock();
        }
        return new HashSet<>(this.players);
    }
}
