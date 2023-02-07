package org.lolicode.allmusic.helper;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.music.Api;


public class LoginHelper {
    public static void genQr(ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            source.sendFeedback(Text.of("§cPlease run this command in the console"), false);
            return;
        }
        Allmusic.EXECUTOR.execute(() -> {
            try {
                if (Api.genLoginKey()) {
                    String qrCode = Api.genLoginQrcode();
                    if (qrCode != null) {
                        source.sendFeedback(Text.of("§ePlease scan the QR code below to login"), false);
                        source.sendFeedback(Text.of(qrCode), false);
                        source.sendFeedback(Text.of("§eAfter login, please run §b/music login check§e to check login status"), false);
                    } else {
                        source.sendFeedback(Text.of("§cFailed to generate QR code"), false);
                    }
                } else {
                    source.sendFeedback(Text.of("§cFailed to get login key"), false);
                }
            } catch (Exception e) {
                source.sendFeedback(Text.of("§cFailed to generate QR code"), false);
                Allmusic.LOGGER.error("Failed to generate QR code", e);
            }
        });
    }

    public static void check(ServerCommandSource source) {
        Allmusic.EXECUTOR.execute(() -> {
            try {
                switch (Api.checkLoginStatus()) {
                    case Api.LOGIN_STATUS.NO_KEY ->
                            source.sendFeedback(Text.of("§cFailed: Please generate QR code first"), false);
                    case Api.LOGIN_STATUS.FAILED ->
                            source.sendFeedback(Text.of("§cFailed to check login status: Unknown error"), false);
                    case Api.LOGIN_STATUS.EXPIRED -> source.sendFeedback(Text.of("§cFailed: Login key expired"), false);
                    case Api.LOGIN_STATUS.WAITING ->
                            source.sendFeedback(Text.of("§cFailed: Please scan the QR code first"), false);
                    case Api.LOGIN_STATUS.SCANNED ->
                            source.sendFeedback(Text.of("§cFailed: Please confirm login on your phone"), false);
                    case Api.LOGIN_STATUS.SUCCESS -> source.sendFeedback(Text.of("§aLogin success"), false);
                }
            } catch (Exception e) {
                source.sendFeedback(Text.of("§cFailed to check login status: Internal error"), false);
                Allmusic.LOGGER.error("Failed to check login status", e);
            }
        });
    }

    public static void status(ServerCommandSource source) {
        Allmusic.EXECUTOR.execute(() -> {
            try {
                Api.UserInfo.Data.Profile profile = Api.getUserInfo();
                if (profile != null) {
                    source.sendFeedback(Text.of("§eLogin status: §aLogged in"), false);
                    source.sendFeedback(Text.of("§eUser ID: §b" + profile.userId), false);
                    source.sendFeedback(Text.of("§eNickname: §b" + profile.nickname), false);
                    source.sendFeedback(Text.of("§eVip type: §b" + profile.vipType), false);
                } else {
                    source.sendFeedback(Text.of("§cFailed to get user info"), false);
                }
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Not logged in")) {
                    source.sendFeedback(Text.of("§eLogin status: §cNot logged in"), false);
                } else {
                    source.sendFeedback(Text.of("§cFailed to get user info: Internal error"), false);
                    Allmusic.LOGGER.error("Failed to get user info", e);
                }
            }
        });
    }
}
