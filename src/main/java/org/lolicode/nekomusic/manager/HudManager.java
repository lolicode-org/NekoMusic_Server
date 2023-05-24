package org.lolicode.nekomusic.manager;

import lol.bai.badpackets.api.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.MusicObj;

public class HudManager {
    static void sendMetadata(@NotNull MusicObj musicObj) {
        PacketByteBuf metadataBuf = PacketHelper.getMetadataPacket(musicObj);
        if (metadataBuf == null)
            throw new RuntimeException("Generate metadata packet failed");
        Identifier NEKO_META_ID = new Identifier(NekoMusic.MOD_ID, "metadata");
        for (ServerPlayerEntity player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NEKO_META_ID, metadataBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send metadata packet failed", e);
            }
        }
    }

    static void sendPlayList() {
        PacketByteBuf playListBuf = PacketHelper.getPlayListPacket();
        if (playListBuf == null)
            return;
        Identifier NEKO_PLAY_LIST_ID = new Identifier(NekoMusic.MOD_ID, "list");
        for (ServerPlayerEntity player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NEKO_PLAY_LIST_ID, playListBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send play list packet failed", e);
            }
        }
    }
}
