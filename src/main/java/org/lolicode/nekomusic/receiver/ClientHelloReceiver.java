package org.lolicode.nekomusic.receiver;

import lol.bai.badpackets.api.PacketReceiver;
import lol.bai.badpackets.api.play.PlayPackets;
import lol.bai.badpackets.api.play.ServerPlayContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lolicode.nekomusic.NekoMusic;

public class ClientHelloReceiver implements PacketReceiver<ServerPlayContext, PacketByteBuf> {
    @Override
    public void receive(ServerPlayContext context, PacketByteBuf buf) {
        NekoMusic.nekoPlayerSet.add(context.player());
    }

    public static void register() {
        final Identifier CLIENT_HELLO_PACKET_ID = Identifier.of(NekoMusic.MOD_ID, "client_hello");
        PlayPackets.registerServerChannel(CLIENT_HELLO_PACKET_ID);
        PlayPackets.registerServerReceiver(CLIENT_HELLO_PACKET_ID, new ClientHelloReceiver());
    }
}
