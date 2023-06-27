package org.lolicode.nekomusic.helper;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.music.Api;


public class LoginHelper {
    public static void genQr(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                if (Api.genLoginKey()) {
                    if (source.isExecutedByPlayer()) {
                        source.sendFeedback(Text.of("§ePlease open the link below and scan the QR code displayed in your browser:"), false);
                        MutableText link = Text.literal("https://qrcode.lolicode.org/?text=https://music.163.com/login?codekey=" + Api.getLoginKey());
                        link.setStyle(link.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.getString())).withColor(Formatting.AQUA));
                        source.sendFeedback(link, false);
                        source.sendFeedback(Text.of("§eAfter scanning, please run §b/music login check§e to continue"), false);
                        return;
                    }
                    String qrCode = Api.genLoginQrcode();
                    if (qrCode != null) {
                        source.sendFeedback(Text.of("§ePlease scan the QR code below to login"), false);
                        source.sendFeedback(Text.of(qrCode), false);
                        source.sendFeedback(Text.of("§eAfter scanning, please run §b/music login check§e to continue"), false);
                    } else {
                        source.sendFeedback(Text.of("§cFailed to generate QR code"), false);
                    }
                } else {
                    source.sendFeedback(Text.of("§cFailed to get login key"), false);
                }
            } catch (Exception e) {
                source.sendFeedback(Text.of("§cFailed to generate QR code"), false);
                NekoMusic.LOGGER.error("Failed to generate QR code", e);
            }
        });
    }

    public static void check(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
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
                NekoMusic.LOGGER.error("Failed to check login status", e);
            }
        });
    }

    public static void status(ServerCommandSource source) {
        NekoMusic.EXECUTOR.execute(() -> {
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
                    NekoMusic.LOGGER.error("Failed to get user info", e);
                }
            }
        });
    }
}
