package org.lolicode.allmusic.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.music.MusicObj;

import java.nio.charset.StandardCharsets;

public class PacketHelper {
    public static PacketByteBuf getPlayPacket(@NotNull MusicObj musicObj) {
        if (musicObj.url == null || musicObj.url.equals(""))
            return null;

        // What's these?
        // IDK, just copy from coloryr's code
        String data = "[Play]" + musicObj.url;

        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer(bytes.length + 1);
        buf.writeByte(666);
        buf.writeBytes(bytes);

        return new PacketByteBuf(buf);
    }

    public static Text getPlayMessage(@NotNull MusicObj musicObj) {
        String player = musicObj.player;
        if (player == null || player.equals(""))
            player = "Default";
        return Text.of("§eNow playing: §a" + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + player);
    }

    public static Text getVoteMessage(int count, int total) {
        return Text.of("§eVote count: §a" + count + " §e/ §9" + total +
                " §e(§a" + (int) (count * 100.0 / total) + "%§e of §a" + Allmusic.CONFIG.voteThreshold + "%§e)");
    }

    public static Text getOrderMessage(@NotNull MusicObj musicObj) {
        return Text.of("§eOrdered: §a" + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + musicObj.player);
    }

    public static Text getOrderMessage() {
        return Text.of("§cGet music info failed.");
    }

    public static Text getOrderedMessage() {
        return Text.of("§cThis song has been ordered.");
    }

    public static Text getDelMessage(MusicObj musicObj) {
        return Text.of("§eDeleted: §a" + musicObj.name);
    }

    public static Text getDelMessage() {
        return Text.of("§cDelete music failed.");
    }

    public static Text getListMessage() {
        if (Allmusic.orderList.songs.size() == 0) {
            return Text.of("§cNo music in playing list.");
        } else {
            StringBuilder sb = new StringBuilder("§ePlaying list: \n");
            for (MusicObj musicObj : Allmusic.orderList.songs) {
                sb.append("§a").append(musicObj.name).append(" §e-§9 ")
                        .append(String.join(" & ",
                                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new)))
                        .append(" §eby §d").append(musicObj.player).append("\n");
            }
            return Text.of(sb.substring(0, sb.length() - 1));
        }
    }

}
