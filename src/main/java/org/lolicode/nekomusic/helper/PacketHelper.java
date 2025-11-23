package org.lolicode.nekomusic.helper;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.LanguageManager;
import org.lolicode.nekomusic.music.Api;
import org.lolicode.nekomusic.music.MusicList;
import org.lolicode.nekomusic.music.MusicObj;

import java.util.function.Supplier;

public class PacketHelper {
    public static FriendlyByteBuf getMetadataPacket(MusicObj music) {
        return getMetadataPacket(music, false);
    }

    public static FriendlyByteBuf getMetadataPacket(MusicObj musicObj, boolean seek) {
        if (musicObj == null)
            return null;
        musicObj.seekTo = seek ? (System.currentTimeMillis() - NekoMusic.currentStartTime) / 1000 : 0;
        String serialized = NekoMusic.GSON.toJson(musicObj);

        return PacketByteBufs.create().writeUtf(serialized);
    }

    public static FriendlyByteBuf getPlayListPacket() {
        MusicList musicList = new MusicList();
        musicList.musics = NekoMusic.orderList.getSongs().stream().limit(10).map(musicObj -> {
            MusicList.Music music = new MusicList.Music();
            music.name = musicObj.name;
            music.artist = musicObj.ar.stream().map(artistObj -> artistObj.name).reduce((a, b) -> a + " & " + b).orElse("");
            music.album = musicObj.album.name;
            return music;
        }).toArray(MusicList.Music[]::new);
        String serialized = NekoMusic.GSON.toJson(musicList);

        return PacketByteBufs.create().writeUtf(serialized);
    }

    public static Component getPlayMessage(@NotNull MusicObj musicObj) {
        String player = musicObj.player;
        if (player == null || player.isBlank())
            player = LanguageManager.getMessage("player.default");
        return Component.nullToEmpty(LanguageManager.getMessage("music.playing") + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + player);
    }

    public static Component getVoteMessage(int count, int total) {
        return Component.nullToEmpty(LanguageManager.getMessage("music.vote_count") + count + " §e/ §9" + total +
                " §e(§a" + (int) (count * 100.0 / total) + "%§e of §a" + NekoMusic.CONFIG.voteThreshold * 100 + "%§e)");
    }

    public static Component getOrderMessage(@NotNull MusicObj musicObj) {
        return Component.nullToEmpty(LanguageManager.getMessage("music.ordered") + musicObj.name + " §e-§9 "
                + String.join(" & ",
                musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                + " §eby §d" + musicObj.player);
    }

    public static Supplier<Component> getOrderMessage() {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("music.get_info_failed"));
    }

    public static Supplier<Component> getOrderedMessage() {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("music.ordered_already"));
    }

    public static Supplier<Component> getBannedMessage() {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("music.banned"));
    }

    public static Supplier<Component> getDelMessage(MusicObj musicObj) {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("music.deleted") + musicObj.name);
    }

    public static Supplier<Component> getDelMessage(int error) {
        Component result;
        if (error == 1) {
            result = Component.nullToEmpty(LanguageManager.getMessage("music.invalid_index"));
        } else if (error == 2) {
            result = Component.nullToEmpty(LanguageManager.getMessage("music.no_permission"));
        } else {
            result = Component.nullToEmpty(LanguageManager.getMessage("music.delete_failed"));
        }
        return () -> result;
    }

    public static Supplier<Component> getListMessage() {
        if (NekoMusic.orderList.size() == 0 && NekoMusic.currentMusic == null) {
            return () -> Component.nullToEmpty(LanguageManager.getMessage("music.no_music"));
        } else {
            MutableComponent text = Component.literal(LanguageManager.getMessage("music.playlist"));
            if (NekoMusic.currentMusic != null) {
                var current = NekoMusic.currentMusic;
                text.append(Component.literal(LanguageManager.getMessage("music.current") + current.name + " §e-§9 "
                                + String.join(" & ",
                                current.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                                + " §eby §d" + (current.player == null ? LanguageManager.getMessage("player.default") : current.player)))
                        .append(Component.literal(" [⏭]").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music next"))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.click_next"))))))
                        .append(Component.literal(" [B]").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music ban " + current.id))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.click_ban"))))))
                        .append(Component.literal("\n"));
            }
            int num = 0;
            for (MusicObj musicObj : NekoMusic.orderList.getSongs()) {
                text.append(Component.literal("§e" + (++num) + ". " + "§a" + musicObj.name + " §e-§9 "
                        + String.join(" & ",
                        musicObj.ar.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                        + " §eby §d" + (musicObj.player == null ? LanguageManager.getMessage("player.default") : musicObj.player)))
                        .append(Component.literal(" [⏩]").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/music add --replace " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.click_next"))))))
                        .append(Component.literal(" [X]").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music del id " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.click_delete"))))))
                        .append(Component.literal(" [B]").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                .withClickEvent(new ClickEvent.RunCommand("/music ban " + musicObj.id))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.click_ban"))))));
                if (num != NekoMusic.orderList.size())
                    text.append(Component.literal("\n"));
            }
            return () -> text;
        }
    }

    public static Supplier<Component> getSearchMessage(Api.SearchResult result, CommandSourceStack source) {
        if (result.result.songs == null || result.result.songs.length == 0) {
            return () -> Component.nullToEmpty(LanguageManager.getMessage("search.no_search_result"));
        } else {
            MutableComponent text = Component.literal(LanguageManager.getMessage("search.search_result_header") + "\n")
                    .setStyle(Component.empty().getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)));
            int num = 0;
            for (Api.SearchResult.Result.OneSong song : result.result.songs) {
                if (NekoMusic.CONFIG.bannedSongs.contains(song.id) &&
                        !(Permissions.check(source, "nekomusic.bypassban", 1) ||
                                Permissions.check(source, "nekomusic.unban", 1))) {
                    continue;
                }
                text.append(
                        Component.literal(
                                "§e" + (++num) + ". " + "§a" + song.name + " §e-§9 "
                                        + String.join(" & ", song.artists.stream().map(artistObj -> artistObj.name).toArray(String[]::new))
                                        + "§e - §d" + song.album.name + " §6[+]"
                        ).setStyle(Component.empty().getStyle()
                                .withClickEvent(new ClickEvent.RunCommand("/music add " + song.id))
                                .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.add"))))));
                if (Permissions.check(source, "nekomusic.add.now", 0)) {
                    text.append(Component.literal(" [▶]").setStyle(Component.empty().getStyle()
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.GOLD))
                            .withClickEvent(new ClickEvent.RunCommand("/music add --now " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.add_now"))))
                    ));
                }
                if (Permissions.check(source, "nekomusic.add.replace", 1)) {
                    text.append(Component.literal(" [⏩]").setStyle(Component.empty().getStyle()
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.GOLD))
                            .withClickEvent(new ClickEvent.RunCommand("/music add --replace " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.add_force_now"))))
                    ));
                }
                if (NekoMusic.CONFIG.bannedSongs.contains(song.id) && Permissions.check(source, "nekomusic.unban", 1)) {
                    text.append(Component.literal(" [✔]").setStyle(Component.empty().getStyle()
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))
                            .withClickEvent(new ClickEvent.RunCommand("/music unban " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.unban"))))));
                } else if (Permissions.check(source, "nekomusic.ban", 1)) {
                    text.append(Component.literal(" [X]").setStyle(Component.empty().getStyle()
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))
                            .withClickEvent(new ClickEvent.RunCommand("/music ban " + song.id))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("music.ban"))))));
                }
                text.append(Component.literal("\n"));
            }
            MutableComponent pagePrev = Component.literal("<<");
            if (result.result.page == 1) {
                pagePrev.setStyle(Component.empty().getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
            } else {
                pagePrev.setStyle(Component.empty().getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                        .withClickEvent(new ClickEvent.RunCommand("/music search page " + (result.result.page - 1) + " " + result.result.keyword))
                        .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("list.previous_page")))));
            }
            MutableComponent pageNext = Component.literal(">>");
            if (result.result.page == (result.result.songCount + 9) / 10) {
                pageNext.setStyle(Component.empty().getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
            } else {
                pageNext.setStyle(Component.empty().getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                        .withClickEvent(new ClickEvent.RunCommand("/music search page " + (result.result.page + 1) + " " + result.result.keyword))
                        .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(LanguageManager.getMessage("list.next_page")))));
            }
            text.append(pagePrev).append(Component.nullToEmpty(
                            "§r ----"+ " §a" + result.result.page + " §r/ §a" + (result.result.songCount + 9) / 10 + "§r ---- "))
                    .append(pageNext);
            return () -> text;
        }
    }

    public static Supplier<Component> getSearchMessage() {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("search.search_failed"));
    }

    public static Supplier<Component> getWorkingMessage() {
        return () -> Component.nullToEmpty(LanguageManager.getMessage("music.working"));
    }

    public static Supplier<Component> getBanMessage(int result) {
        return () -> switch (result) {
            case 1 -> Component.nullToEmpty(LanguageManager.getMessage("music.ban_already"));
            case 2 -> Component.nullToEmpty(LanguageManager.getMessage("music.invalid_id"));
            case 3 -> Component.nullToEmpty(LanguageManager.getMessage("music.ban_success"));
            default -> Component.nullToEmpty(LanguageManager.getMessage("music.unknown_error"));
        };
    }

    public static Supplier<Component> getUnbanMessage(int result) {
        return () -> switch (result) {
            case 1 -> Component.nullToEmpty(LanguageManager.getMessage("music.unban_not_banned"));
            case 2 -> Component.nullToEmpty(LanguageManager.getMessage("music.unban_success"));
            default -> Component.nullToEmpty(LanguageManager.getMessage("music.unknown_error"));
        };
    }

    public static Component getGetMusicErrorMessage(String music) {
        return Component.nullToEmpty(LanguageManager.getMessage("music.get_url_failed", music));
    }
}
