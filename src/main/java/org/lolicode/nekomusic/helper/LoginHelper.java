package org.lolicode.nekomusic.helper;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.manager.LanguageManager;
import org.lolicode.nekomusic.music.Api;

import java.net.URI;

public class LoginHelper {
    public static void genQr(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                if (Api.genLoginKey()) {
                    if (source.isExecutedByPlayer()) {
                        source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_prompt")), false);
                        MutableText link = Text.literal("https://qrcode.lolicode.org/?text=https://music.163.com/login?codekey=" + Api.getLoginKey());
                        link.setStyle(link.getStyle().withClickEvent(new ClickEvent.OpenUrl(new URI(link.getString()))).withColor(Formatting.AQUA));
                        source.sendFeedback(() -> link, false);
                        source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_check")), false);
                        return;
                    }
                    String qrCode = Api.genLoginQrcode();
                    if (qrCode != null) {
                        source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_scan")), false);
                        source.sendFeedback(() -> Text.of(qrCode), false);
                        source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_check")), false);
                    } else {
                        source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_failed")), false);
                    }
                } else {
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.key_failed")), false);
                }
            } catch (Exception e) {
                source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_failed")), false);
                NekoMusic.LOGGER.error("Failed to generate QR code", e);
            }
        });
    }

    public static void check(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                switch (Api.checkLoginStatus()) {
                    case Api.LOGIN_STATUS.NO_KEY ->
                            source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.no_key")), false);
                    case Api.LOGIN_STATUS.FAILED ->
                            source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.status_failed")), false);
                    case Api.LOGIN_STATUS.EXPIRED ->
                            source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.key_expired")), false);
                    case Api.LOGIN_STATUS.WAITING ->
                            source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_waiting_scan")), false);
                    case Api.LOGIN_STATUS.SCANNED ->
                            source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.qr_waiting_confirm")), false);
                    case Api.LOGIN_STATUS.SUCCESS -> source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.status_logged_in")), false);
                }
            } catch (Exception e) {
                source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.status_internal_error")), false);
                NekoMusic.LOGGER.error("Failed to check login status", e);
            }
        });
    }

    public static void status(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                Api.UserInfo.Data.Profile profile = Api.getUserInfo();
                if (profile != null) {
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.status_logged_in")), false);
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.user_id") + profile.userId), false);
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.nickname") + profile.nickname), false);
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.vip_type") + profile.vipType), false);
                } else {
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.user_info_failed")), false);
                }
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Not logged in")) {
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.status_not_logged_in")), false);
                } else {
                    source.sendFeedback(() -> Text.of(LanguageManager.getMessage("login.user_info_failed")), false);
                    NekoMusic.LOGGER.error("Failed to get user info", e);
                }
            }
        });
    }
}