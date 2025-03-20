package org.lolicode.nekomusic.helper;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.LanguageManager;
import org.lolicode.nekomusic.music.Api;
import org.lolicode.nekomusic.music.MusicList;
import org.lolicode.nekomusic.music.MusicObj;

import java.util.function.Supplier;

public class PacketHelper {
    public static PacketByteBuf getMetadataPacket(MusicObj music) {
        return getMetadataPacket(music, false);
    }

    public static PacketByteBuf getMetadataPacket(MusicObj musicObj, boolean seek) {
        if (musicObj == null)
            return null;
        musicObj.seekTo = seek ? (System.currentTimeMillis() - NekoMusic.currentStartTime) / 1000 : 0;
        String serialized = NekoMusic.GSON.toJson(musicObj);

        return PacketByteBufs.create().writeString(serialized);
    }

    public static PacketByteBuf getPlayListPacket() {
        MusicList musicList = new MusicList();
        musicList.musics = NekoMusic.orderList.getSongs().stream().limit(10).map(musicObj -> {
            MusicList.Music music = new MusicList.Music();
            music.name = musicObj.name;
            music.artist = musicObj.ar.stream().map(artistObj -> artistObj.name).reduce((a, b) -> a + " & " + b).orElse("");
            music.album = musicObj.album.name;
            return music;
        }).toArray(MusicList.Music[]::new);
        String serialized = NekoMusic.GSON.toJson(musicList);

        return PacketByteBufs.create().writeString(serialized);
    }

    public static Text getPlayMessage(@NotNull MusicObj musicObj) {
        String player = musicObj.player;
        if (player == null || player.isBlank())
            player = LanguageManager.getMessage("player.default");
        return Text.of(LanguageManager.getMessage("music.playing") + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + player);
    }

    public static Text getVoteMessage(int count, int total) {
        return Text.of(LanguageManager.getMessage("music.vote_count") + count + " §e/ §9" + total +
                " §e(§a" + (int) (count * 100.0 / total) + "%§e of §a" + NekoMusic.CONFIG.voteThreshold * 100 + "%§e)");
    }

    public static Text getOrderMessage(@NotNull MusicObj musicObj) {
        return Text.of(LanguageManager.getMessage("music.ordered") + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + musicObj.player);
    }

    public static Supplier<Text> getOrderMessage() {
        return () -> Text.of(LanguageManager.getMessage("music.get_info_failed"));
    }

    public static Supplier<Text> getOrderedMessage() {
        return () -> Text.of(LanguageManager.getMessage("music.ordered_already"));
    }

    public static Supplier<Text> getBannedMessage() {
        return () -> Text.of(LanguageManager.getMessage("music.banned"));
    }

    public static Supplier<Text> getDelMessage(MusicObj musicObj) {
        return () -> Text.of(LanguageManager.getMessage("music.deleted") + musicObj.name);
    }

    public static Supplier<Text> getDelMessage(int error) {
        Text result;
        if (error == 1) {
            result = Text.of(LanguageManager.getMessage("music.invalid_index"));
        } else if (error == 2) {
            result = Text.of(LanguageManager.getMessage("music.no_permission"));
        } else {
            result = Text.of(LanguageManager.getMessage("music.delete_failed"));
        }
        return () -> result;
    }

    public static Supplier<Text> getListMessage() {
        if (NekoMusic.orderList.size() == 0 && NekoMusic.currentMusic == null) {
            return () -> Text.of(LanguageManager.getMessage("music.no_music"));
        } else {
            MutableText text = Text.literal(LanguageManager.getMessage("music.playlist"));
            if (NekoMusic.currentMusic != null) {
                var current = NekoMusic.currentMusic;
                text.append(Text.literal(LanguageManager.getMessage("music.current") + current.name + " §e-§9 "
                                + String.join(" & ",
                                current.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                                + " §eby §d" + (current.player == null ? LanguageManager.getMessage("player.default") : current.player)))
                        .append(Text.literal(" [⏭]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music next"))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.click_next"))))))
                        .append(Text.literal(" [B]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music ban " + current.id))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.click_ban"))))))
                        .append(Text.literal("\n"));
            }
            int num = 0;
            for (MusicObj musicObj : NekoMusic.orderList.getSongs()) {
                text.append(Text.literal("§e" + (++num) + ". " + "§a" + musicObj.name + " §e-§9 "
                        + String.join(" & ",
                        musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                        + " §eby §d" + (musicObj.player == null ? LanguageManager.getMessage("player.default") : musicObj.player)))
                        .append(Text.literal(" [⏩]").setStyle(Style.EMPTY.withColor(Formatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/music add --replace " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.click_next"))))))
                        .append(Text.literal(" [X]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music del id " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.click_delete"))))))
                        .append(Text.literal(" [B]").setStyle(Style.EMPTY.withColor(Formatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music ban " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.click_ban"))))));
                if (num != NekoMusic.orderList.size())
                    text.append(Text.literal("\n"));
            }
            return () -> text;
        }
    }

    public static Supplier<Text> getSearchMessage(Api.SearchResult result, ServerCommandSource source) {
        if (result.result.songs == null || result.result.songs.length == 0) {
            return () -> Text.of(LanguageManager.getMessage("search.no_search_result"));
        } else {
            MutableText text = Text.literal(LanguageManager.getMessage("search.search_result_header") + "\n")
                    .setStyle(Text.empty().getStyle().withColor(TextColor.fromFormatting(Formatting.YELLOW)));
            int num = 0;
            for (Api.SearchResult.Result.OneSong song : result.result.songs) {
                if (NekoMusic.CONFIG.bannedSongs.contains(song.id) &&
                        !(Permissions.check(source, "nekomusic.bypassban", 1) ||
                                Permissions.check(source, "nekomusic.unban", 1))) {
                    continue;
                }
                text.append(
                        Text.literal(
                                "§e" + (++num) + ". " + "§a" + song.name + " §e-§9 "
                                        + String.join(" & ", song.artists.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                                        + "§e - §d" + song.album.name + " §6[+]"
                        ).setStyle(Text.empty().getStyle()
                                .withClickEvent(new ClickEvent.RunCommand("/music add " + song.id))
                                .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.add"))))));
                if (Permissions.check(source, "nekomusic.add.now", 0)) {
                    text.append(Text.literal(" [▶]").setStyle(Text.empty().getStyle()
                            .withColor(TextColor.fromFormatting(Formatting.GOLD))
                            .withClickEvent(new ClickEvent.RunCommand("/music add --now " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.add_now"))))
                    ));
                }
                if (Permissions.check(source, "nekomusic.add.replace", 1)) {
                    text.append(Text.literal(" [⏩]").setStyle(Text.empty().getStyle()
                            .withColor(TextColor.fromFormatting(Formatting.GOLD))
                            .withClickEvent(new ClickEvent.RunCommand("/music add --replace " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.add_force_now"))))
                    ));
                }
                if (NekoMusic.CONFIG.bannedSongs.contains(song.id) && Permissions.check(source, "nekomusic.unban", 1)) {
                    text.append(Text.literal(" [✔]").setStyle(Text.empty().getStyle()
                            .withColor(TextColor.fromFormatting(Formatting.RED))
                            .withClickEvent(new ClickEvent.RunCommand("/music unban " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.unban"))))));
                } else if (Permissions.check(source, "nekomusic.ban", 1)) {
                    text.append(Text.literal(" [X]").setStyle(Text.empty().getStyle()
                            .withColor(TextColor.fromFormatting(Formatting.RED))
                            .withClickEvent(new ClickEvent.RunCommand("/music ban " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("music.ban"))))));
                }
                text.append(Text.literal("\n"));
            }
            MutableText pagePrev = Text.literal("<<");
            if (result.result.page == 1) {
                pagePrev.setStyle(Text.empty().getStyle().withColor(TextColor.fromFormatting(Formatting.GRAY)));
            } else {
                pagePrev.setStyle(Text.empty().getStyle().withColor(TextColor.fromFormatting(Formatting.BLUE))
                        .withClickEvent(new ClickEvent.RunCommand("/music search page " + (result.result.page - 1) + " " + result.result.keyword))
                        .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("list.previous_page")))));
            }
            MutableText pageNext = Text.literal(">>");
            if (result.result.page == (result.result.songCount + 9) / 10) {
                pageNext.setStyle(Text.empty().getStyle().withColor(TextColor.fromFormatting(Formatting.GRAY)));
            } else {
                pageNext.setStyle(Text.empty().getStyle().withColor(TextColor.fromFormatting(Formatting.BLUE))
                        .withClickEvent(new ClickEvent.RunCommand("/music search page " + (result.result.page + 1) + " " + result.result.keyword))
                        .withHoverEvent(new HoverEvent.ShowText(Text.of(LanguageManager.getMessage("list.next_page")))));
            }
            text.append(pagePrev).append(Text.of(
                            "§r ----"+ " §a" + result.result.page + " §r/ §a" + (result.result.songCount + 9) / 10 + "§r ---- "))
                    .append(pageNext);
            return () -> text;
        }
    }

    public static Supplier<Text> getSearchMessage() {
        return () -> Text.of(LanguageManager.getMessage("search.search_failed"));
    }

    public static Supplier<Text> getWorkingMessage() {
        return () -> Text.of(LanguageManager.getMessage("music.working"));
    }

    public static Supplier<Text> getBanMessage(int result) {
        return () -> switch (result) {
            case 1 -> Text.of(LanguageManager.getMessage("music.ban_already"));
            case 2 -> Text.of(LanguageManager.getMessage("music.invalid_id"));
            case 3 -> Text.of(LanguageManager.getMessage("music.ban_success"));
            default -> Text.of(LanguageManager.getMessage("music.unknown_error"));
        };
    }

    public static Supplier<Text> getUnbanMessage(int result) {
        return () -> switch (result) {
            case 1 -> Text.of(LanguageManager.getMessage("music.unban_not_banned"));
            case 2 -> Text.of(LanguageManager.getMessage("music.unban_success"));
            default -> Text.of(LanguageManager.getMessage("music.unknown_error"));
        };
    }

    public static Text getGetMusicErrorMessage(String music) {
        return Text.of(LanguageManager.getMessage("music.get_url_failed", music));
    }
}
