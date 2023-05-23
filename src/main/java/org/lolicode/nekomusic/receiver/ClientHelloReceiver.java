package org.lolicode.nekomusic.receiver;

import lol.bai.badpackets.api.C2SPacketReceiver;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.lolicode.nekomusic.NekoMusic;

public class ClientHelloReceiver implements C2SPacketReceiver {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        NekoMusic.nekoPlayerSet.add(player);
    }

    public static void register() {
        C2SPacketReceiver.register(Identifier.of(NekoMusic.MOD_ID, "client_hello"), new ClientHelloReceiver());
    }
}
