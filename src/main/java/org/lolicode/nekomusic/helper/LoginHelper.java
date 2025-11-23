package org.lolicode.nekomusic.helper;

import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.LanguageManager;
import org.lolicode.nekomusic.music.Api;

import java.net.URI;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LoginHelper {
    public static void genQr(CommandSourceStack source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                if (Api.genLoginKey()) {
                    if (source.isPlayer()) {
                        source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_prompt")), false);
                        MutableComponent link = Component.literal("https://qrcode.lolicode.org/?text=https://music.163.com/login?codekey=" + Api.getLoginKey());
                        link.setStyle(link.getStyle().withClickEvent(new ClickEvent.OpenUrl(new URI(link.getString()))).withColor(ChatFormatting.AQUA));
                        source.sendSuccess(() -> link, false);
                        source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_check")), false);
                        return;
                    }
                    String qrCode = Api.genLoginQrcode();
                    if (qrCode != null) {
                        source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_scan")), false);
                        source.sendSuccess(() -> Component.nullToEmpty(qrCode), false);
                        source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_check")), false);
                    } else {
                        source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_failed")), false);
                    }
                } else {
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.key_failed")), false);
                }
            } catch (Exception e) {
                source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_failed")), false);
                NekoMusic.LOGGER.error("Failed to generate QR code", e);
            }
        });
    }

    public static void check(CommandSourceStack source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                switch (Api.checkLoginStatus()) {
                    case Api.LOGIN_STATUS.NO_KEY ->
                            source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.no_key")), false);
                    case Api.LOGIN_STATUS.FAILED ->
                            source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.status_failed")), false);
                    case Api.LOGIN_STATUS.EXPIRED ->
                            source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.key_expired")), false);
                    case Api.LOGIN_STATUS.WAITING ->
                            source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_waiting_scan")), false);
                    case Api.LOGIN_STATUS.SCANNED ->
                            source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.qr_waiting_confirm")), false);
                    case Api.LOGIN_STATUS.SUCCESS -> source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.status_logged_in")), false);
                }
            } catch (Exception e) {
                source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.status_internal_error")), false);
                NekoMusic.LOGGER.error("Failed to check login status", e);
            }
        });
    }

    public static void status(CommandSourceStack source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                Api.UserInfo.Data.Profile profile = Api.getUserInfo();
                if (profile != null) {
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.status_logged_in")), false);
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.user_id") + profile.userId), false);
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.nickname") + profile.nickname), false);
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.vip_type") + profile.vipType), false);
                } else {
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.user_info_failed")), false);
                }
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Not logged in")) {
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.status_not_logged_in")), false);
                } else {
                    source.sendSuccess(() -> Component.nullToEmpty(LanguageManager.getMessage("login.user_info_failed")), false);
                    NekoMusic.LOGGER.error("Failed to get user info", e);
                }
            }
        });
    }
}