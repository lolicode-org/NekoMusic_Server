package org.lolicode.nekomusic.manager;

import lol.bai.badpackets.api.PacketSender;
import lol.bai.badpackets.api.play.PlayPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.MusicObj;

public class HudManager {

    private static final Identifier NEKO_META_ID = Identifier.of(NekoMusic.MOD_ID, "metadata");
    private static final Identifier NEKO_PLAY_LIST_ID = Identifier.of(NekoMusic.MOD_ID, "list");

    public static void registerChannel() {
        PlayPackets.registerClientChannel(NEKO_META_ID);
        PlayPackets.registerClientChannel(NEKO_PLAY_LIST_ID);
    }

    static void sendMetadata(@NotNull MusicObj musicObj) {
        PacketByteBuf metadataBuf = PacketHelper.getMetadataPacket(musicObj);
        if (metadataBuf == null)
            throw new RuntimeException("Generate metadata packet failed");
        for (ServerPlayerEntity player : PlayerManager.getNekoPlayerSet()) {
            try {
                PacketSender.s2c(player).send(NEKO_META_ID, metadataBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send metadata packet failed", e);
            }
        }
    }

    static void sendMetadata(@NotNull MusicObj musicObj, @NotNull ServerPlayerEntity player, boolean seek) {
        PacketByteBuf metadataBuf = PacketHelper.getMetadataPacket(musicObj, seek);
        if (metadataBuf == null)
            throw new RuntimeException("Generate metadata packet failed");
        try {
            PacketSender.s2c(player).send(NEKO_META_ID, metadataBuf);
        } catch (Exception e) {
            NekoMusic.LOGGER.error("Send metadata packet failed", e);
        }
    }

    static void sendPlayList() {
        PacketByteBuf playListBuf = PacketHelper.getPlayListPacket();
        if (playListBuf == null)
            return;
        for (ServerPlayerEntity player : PlayerManager.getNekoPlayerSet()) {
            try {
                PacketSender.s2c(player).send(NEKO_PLAY_LIST_ID, playListBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send play list packet failed", e);
            }
        }
    }

    static void sendPlayList(@NotNull ServerPlayerEntity player) {
        PacketByteBuf playListBuf = PacketHelper.getPlayListPacket();
        if (playListBuf == null)
            return;
        try {
            PacketSender.s2c(player).send(NEKO_PLAY_LIST_ID, playListBuf);
        } catch (Exception e) {
            NekoMusic.LOGGER.error("Send play list packet failed", e);
        }
    }
}
