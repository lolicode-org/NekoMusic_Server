package org.lolicode.nekomusic.receiver;

import lol.bai.badpackets.api.PacketReceiver;
import lol.bai.badpackets.api.play.PlayPackets;
import lol.bai.badpackets.api.play.ServerPlayContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.MusicManager;
import org.lolicode.nekomusic.manager.PlayerManager;

public class ClientHelloReceiver implements PacketReceiver<ServerPlayContext, PacketByteBuf> {
    @Override
    public void receive(ServerPlayContext context, PacketByteBuf buf) {
        NekoMusic.nekoPlayerSet.add(context.player());
        if (PlayerManager.getNekoPlayerSet().size() == 1) {
            try {
                MusicManager.playNext(context.server());
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Play music failed", e);
            }
        } else if (NekoMusic.currentMusic != null && (System.currentTimeMillis() - NekoMusic.currentStartTime + 3000 < NekoMusic.currentMusic.dt)) {
            MusicManager.playToPlayer(NekoMusic.currentMusic, context.player(), System.currentTimeMillis() - NekoMusic.currentStartTime > 5000);  // don't seek if the audio has been playing for less than 5 seconds
        }
    }

    public static void register() {
        final Identifier CLIENT_HELLO_PACKET_ID = Identifier.of(NekoMusic.MOD_ID, "client_hello");
        PlayPackets.registerServerChannel(CLIENT_HELLO_PACKET_ID);
        PlayPackets.registerServerReceiver(CLIENT_HELLO_PACKET_ID, new ClientHelloReceiver());
    }
}
