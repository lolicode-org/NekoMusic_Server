package org.lolicode.nekomusic.receiver;

import lol.bai.badpackets.api.C2SPacketReceiver;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.task.PlayerLeave;

public class ClientByeReceiver implements C2SPacketReceiver {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        PlayerLeave.OnPlayerLeave(player, server);
    }

    public static void register() {
        C2SPacketReceiver.register(Identifier.of(NekoMusic.MOD_ID, "client_bye"), new ClientByeReceiver());
    }
}
