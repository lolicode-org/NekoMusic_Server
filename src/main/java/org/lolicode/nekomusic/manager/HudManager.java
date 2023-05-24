package org.lolicode.nekomusic.manager;

import lol.bai.badpackets.api.PacketSender;
import net.minecraft.network.PacketByteBuf;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.helper.PacketHelper;
import org.lolicode.nekomusic.libs.lrcparser.Lyric;
import org.lolicode.nekomusic.libs.lrcparser.parser.LyricParser;
import org.lolicode.nekomusic.libs.lrcparser.parser.Sentence;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

public class HudManager {
    /*
    * Always call this method in a new thread
    * Call me **AFTER** updating currentMusic
     */
    public static void sendNext() {
        sendClear();

        sendInfo();
        sendList();
        sendCover();
        sendLyric();
    }

    static void sendClear() {
        PacketByteBuf clearBuf = PacketHelper.getClearPacket();

        for (var player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NekoMusic.ID, clearBuf);
//                ServerPlayNetworking.send(player, NekoMusic.ID, clearBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send hud clear failed", e);
            }
        }
    }

    public static void sendInfo() {
        PacketByteBuf infoBuf = PacketHelper.getInfoPacket(NekoMusic.currentMusic);

        for (var player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NekoMusic.ID, infoBuf);
//                ServerPlayNetworking.send(player, NekoMusic.ID, infoBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send music info failed", e);
            }
        }
    }

    public static void sendList() {
        PacketByteBuf listBuf = PacketHelper.getListPacket();

        for (var player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NekoMusic.ID, listBuf);
//                ServerPlayNetworking.send(player, NekoMusic.ID, listBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send music list failed", e);
            }
        }
    }

    static void sendCover() {
        PacketByteBuf coverBuf = PacketHelper.getCoverPacket(NekoMusic.currentMusic);

        if (coverBuf == null)
            return;

        for (var player : NekoMusic.nekoPlayerSet) {
            try {
                PacketSender.s2c(player).send(NekoMusic.ID, coverBuf);
//                ServerPlayNetworking.send(player, NekoMusic.ID, coverBuf);
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Send music cover failed", e);
            }
        }
    }

    static void sendLyric() {
        if (NekoMusic.LYRIC_FUTURE != null && !NekoMusic.LYRIC_FUTURE.isDone())
            NekoMusic.LYRIC_FUTURE.cancel(true);
        NekoMusic.LYRIC_FUTURE = null;

        if (NekoMusic.currentMusic.lyric == null || NekoMusic.currentMusic.lyric.getLyric() == null) {
//            PacketByteBuf lyricBuf = PacketHelper.getLyricPacket(new ArrayList<>() {{
//                new Sentence("No lyric", 0, 999999);
//            }}, null);
            PacketByteBuf lyricBuf = PacketHelper.getLyricPacket(new ArrayList<>() {{
                new Sentence("No lyric", 0, 999999);
            }});
            for (var player : NekoMusic.nekoPlayerSet) {
                try {
                    PacketSender.s2c(player).send(NekoMusic.ID, lyricBuf);
                } catch (Exception e) {
                    NekoMusic.LOGGER.error("Send lyric failed", e);
                }
            }
            return;
        }
        Lyric lyric;
        Lyric translation;
        try {
            LyricParser lyricParser = LyricParser.create(new BufferedReader(new StringReader(NekoMusic.currentMusic.lyric.getLyric())));
            LyricParser translationParser = NekoMusic.currentMusic.lyric.getTranslation() == null ? null : LyricParser.create(new BufferedReader(new StringReader(NekoMusic.currentMusic.lyric.getTranslation())));
//            lyric = new Lyric(lyricParser.getTags(), lyricParser.getSentences());
            translation = translationParser == null ? null : new Lyric(translationParser.getTags(), translationParser.getSentences());

            lyric = new Lyric(lyricParser.getTags(), lyricParser.getSentences());
            if (translation != null) {
                for (var sentence : lyric.getSentences()) {
                    var translationSentence = translation.findSentence(sentence.getFromTime());
                    if (translationSentence != null) {
                        sentence.setContent(sentence.getContent() + " / " + translationSentence.getContent());
                    }
                }
                lyric.updateDuration();
            }
        } catch (Exception e) {
            NekoMusic.LOGGER.error("Parse lyric failed", e);
            return;
        }

        NekoMusic.LYRIC_FUTURE = NekoMusic.LYRIC_EXECUTOR.submit(() -> {
            try {
                long startTime = System.currentTimeMillis();
                while (true) {
                    long currentTime = System.currentTimeMillis() - startTime;
                    if (currentTime > lyric.getDuration() || currentTime < 0) {
                        break;
                    }
                    ArrayList<Sentence> sentence = lyric.findAllSentences(currentTime, currentTime + 500);
//                    ArrayList<Sentence> translationSentence = translation == null ? null : translation.findAllSentences(currentTime, currentTime + 500);
//                    PacketByteBuf lyricBuf = PacketHelper.getLyricPacket(sentence, translationSentence);
                    PacketByteBuf lyricBuf = PacketHelper.getLyricPacket(sentence);
                    for (var player : NekoMusic.nekoPlayerSet) {
                        try {
//                            PacketSender.s2c(player).send(NekoMusic.ID, lyricBuf);
//                            ServerPlayNetworking.send(player, NekoMusic.ID, lyricBuf);
                        } catch (Exception e) {
                            NekoMusic.LOGGER.error("Send lyric to player {} failed", player.getName().getString(), e);
                        }
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException ignored) {
            }
        });
    }
}
