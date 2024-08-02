package org.lolicode.nekomusic.receiver;

import lol.bai.badpackets.api.PacketReceiver;
import lol.bai.badpackets.api.play.PlayPackets;
import lol.bai.badpackets.api.play.ServerPlayContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.task.PlayerLeave;

public class ClientByeReceiver implements PacketReceiver<ServerPlayContext, PacketByteBuf> {
    @Override
    public void receive(ServerPlayContext context, PacketByteBuf buf) {
        PlayerLeave.OnPlayerLeave(context.player(), context.server());
    }

    public static void register() {
        final Identifier CLIENT_BYE_PACKET_ID = Identifier.of(NekoMusic.MOD_ID, "client_bye");
        PlayPackets.registerServerChannel(CLIENT_BYE_PACKET_ID);
        PlayPackets.registerServerReceiver(CLIENT_BYE_PACKET_ID, new ClientByeReceiver());
    }
}
