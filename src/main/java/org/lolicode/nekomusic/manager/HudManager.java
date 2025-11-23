package org.lolicode.nekomusic.manager;

import lol.bai.badpackets.api.PacketSender;
import lol.bai.badpackets.api.play.PlayPackets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.music.MusicObj;

// TODO: THIS IS NOT HUD MANAGER...
public class HudManager {

    private static final Identifier NEKO_META_ID = Identifier.fromNamespaceAndPath(NekoMusic.MOD_ID, "metadata");
    private static final Identifier NEKO_PLAY_LIST_ID = Identifier.fromNamespaceAndPath(NekoMusic.MOD_ID, "list");

    public static void registerChannel() {
        PlayPackets.registerClientChannel(NEKO_META_ID);
        PlayPackets.registerClientChannel(NEKO_PLAY_LIST_ID);
    }

    static void sendMetadata(@NotNull MusicObj musicObj) {
        FriendlyByteBuf metadataBuf = PacketHelper.getMetadataPacket(musicObj);
        if (metadataBuf == null)
            throw new RuntimeException("Generate metadata packet failed");
        for (ServerPlayer player : PlayerManager.getNekoPlayerSet()) {
            try {
                PacketSender.s2c(player).send(NEKO_META_ID, metadataBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send metadata packet failed", e);
            }
        }
    }

    static void sendMetadata(@NotNull MusicObj musicObj, @NotNull ServerPlayer player, boolean seek) {
        FriendlyByteBuf metadataBuf = PacketHelper.getMetadataPacket(musicObj, seek);
        if (metadataBuf == null)
            throw new RuntimeException("Generate metadata packet failed");
        try {
            PacketSender.s2c(player).send(NEKO_META_ID, metadataBuf);
        } catch (Exception e) {
            NekoMusic.LOGGER.error("Send metadata packet failed", e);
        }
    }

    static void sendPlayList() {
        FriendlyByteBuf playListBuf = PacketHelper.getPlayListPacket();
        if (playListBuf == null)
            return;
        for (ServerPlayer player : PlayerManager.getNekoPlayerSet()) {
            try {
                PacketSender.s2c(player).send(NEKO_PLAY_LIST_ID, playListBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send play list packet failed", e);
            }
        }
    }

    static void sendPlayList(@NotNull ServerPlayer player) {
        FriendlyByteBuf playListBuf = PacketHelper.getPlayListPacket();
        if (playListBuf == null)
            return;
        try {
            PacketSender.s2c(player).send(NEKO_PLAY_LIST_ID, playListBuf);
        } catch (Exception e) {
            NekoMusic.LOGGER.error("Send play list packet failed", e);
        }
    }
}
