package org.lolicode.allmusic.music;

import com.google.gson.annotations.SerializedName;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.config.ModConfig;
import xyz.dunjiao.cloud.commons.lang.QRCodeUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Api {
    private static String key = "";

    public static class LOGIN_STATUS {
        public static final int FAILED = -1;
        public static final int NO_KEY = -2;
        public static final int EXPIRED = 800;
        public static final int WAITING = 801;
        public static final int SCANNED = 802;
        public static final int SUCCESS = 803;
    }

    private static class KeyObj {
        static class Data {
            String unikey;
            int code;
        }
        Data data;
        int code;
    }

    private static class QrCodeObj {
        static class Data {
            String qrurl;
//          String qrimg;
        }

        Data data;
        int code;
    }

    private static class CookieObj {
        int code;
        String cookie;
    }

    public static class UserInfo {
        public static class Data {
            public static class Profile {
                @SerializedName("userId")
                public long userId;
                public String nickname;
                @SerializedName("vipType")
                public int vipType;
            }

            public Profile profile;
            public int code;
        }

        public Data data;
    }

    private static class MusicObjWrapper {
        List<MusicObj> data;
        int code;
    }

    private static class MusicInfoWrapper {
        MusicObj[] songs;
        int code;
    }

    public static class SearchResult {
        public static class Result {
            public static class OneSong {
                public static class AlbumObj {
                    public long id;
                    public String name;
                }
                public long id;
                public String name;
                public List<MusicObj.ArtistObj> artists;
                public AlbumObj album;
            }
            public OneSong[] songs;
            @SerializedName("hasMore")
            public boolean hasMore;
            @SerializedName("songCount")
            public int songCount;
            public int page;
            public String keyword;
        }
        public Result result;
        public int code;
    }

    public static boolean genLoginKey() {
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(Allmusic.CONFIG.apiAddress + "/login/qr/key")
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                key = Allmusic.GSON.fromJson(response.body().string(), KeyObj.class).data.unikey;
                return true;
            } else {
                Allmusic.LOGGER.error("Failed to generate login key, response code: " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to generate login key", e);
        }
        return false;
    }

    public static String genLoginQrcode() {
        if (key == null || key.isEmpty()) return null;
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(Allmusic.CONFIG.apiAddress + "/login/qr/create?key=" + key)
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                String qrurl = Allmusic.GSON.fromJson(response.body().string(), QrCodeObj.class).data.qrurl;
                if (qrurl != null && !qrurl.isEmpty()) {
                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.CHARACTER_SET, "UTF-8",
                            EncodeHintType.MARGIN, 1);
                    BitMatrix bitMatrix = qrCodeWriter.encode(qrurl, BarcodeFormat.QR_CODE, 32, 32, hints);
                    return QRCodeUtils.toString(bitMatrix, false);
                }
            }
            Allmusic.LOGGER.error("Failed to generate login qrcode: " + response.code());
        } catch (IOException | WriterException e) {
            Allmusic.LOGGER.error("Failed to generate login qrcode", e);
        }
        return null;
    }

    public static int checkLoginStatus() {
        if (key == null || key.isEmpty()) return LOGIN_STATUS.NO_KEY;
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(Allmusic.CONFIG.apiAddress + "/login/qr/check?key=" + key)
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                CookieObj cookieObj = Allmusic.GSON.fromJson(response.body().string(), CookieObj.class);
                if (cookieObj.code == LOGIN_STATUS.SUCCESS) {
                    Allmusic.CONFIG.cookie = cookieObj.cookie;
                    ModConfig.save();
                    return LOGIN_STATUS.SUCCESS;
                } else {
                    Allmusic.LOGGER.error("Failed to check login status: " + cookieObj.code);
                    return cookieObj.code;
                }
            } else {
                Allmusic.LOGGER.error("Failed to check login status: " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to check login status", e);
        }
        return LOGIN_STATUS.FAILED;
    }

    public static UserInfo.Data.Profile getUserInfo() {
        if (Allmusic.CONFIG.cookie == null || Allmusic.CONFIG.cookie.isEmpty()) throw new RuntimeException("Not logged in");
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(Allmusic.CONFIG.apiAddress + "/login/status?cookie=" + Allmusic.CONFIG.cookie)
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                UserInfo userInfo = Allmusic.GSON.fromJson(response.body().string(), UserInfo.class);
                if (userInfo.data.code == 200) {
                    return userInfo.data.profile;
                } else {
                    Allmusic.LOGGER.error("Failed to get user info: " + userInfo.data.code);
                }
            } else {
                Allmusic.LOGGER.error("Failed to get user info: Invaild response: " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to get user info", e);
        }
        return null;
    }

    private static MusicObj getMusic(long id) {
        if (id == 0) return null;
        HttpUrl.Builder url = HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/song/url").newBuilder()
                .addQueryParameter("id", String.valueOf(id))
                .addQueryParameter("br", String.valueOf(Allmusic.CONFIG.maxQuality));
        if (Allmusic.CONFIG.cookie != null && !Allmusic.CONFIG.cookie.isEmpty()) {
            url.addQueryParameter("cookie", Allmusic.CONFIG.cookie);
        }

        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                        .url(url.build())
                        .build())
                .execute()) {
            if (response.code() == 200 && response.body() != null) {
                MusicObjWrapper music = Allmusic.GSON.fromJson(response.body().string(), MusicObjWrapper.class);
                if (music.data != null && music.data.size() > 0) {
                    return music.data.get(0);
                }
            } else {
                Allmusic.LOGGER.error("Failed to get music: Invalid response " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to get music: Network error", e);
        }
        return null;
    }

    public static String getMusicUrl(long id) {
        MusicObj music = getMusic(id);
        if (music != null && (music.freeTrialInfo == null || (music.fee != 0 && music.payed != 1))) {  // Don't play trial music
            return music.url;
        }
        return null;
    }

    public static String getMusicUrl(MusicObj musicObj) {
        MusicObj music = getMusic(musicObj.id);
        if (music != null && (music.freeTrialInfo == null || (music.fee != 0 && music.payed != 1))
                && ((musicObj.dt == 0) || music.time == musicObj.dt)) {  // Check if time matches
            return music.url;
        }
        return null;
    }

    public static MusicObj getMusicInfo(long id) {
        if (id == 0) return null;
        HttpUrl.Builder url = HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/song/detail").newBuilder()
                .addQueryParameter("ids", String.valueOf(id));
        if (Allmusic.CONFIG.cookie != null && !Allmusic.CONFIG.cookie.isEmpty()) {
            url.addQueryParameter("cookie", Allmusic.CONFIG.cookie);
        }
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                        .url(url.build())
                        .build())
                .execute()) {
            if (response.code() == 200 && response.body() != null) {
                MusicInfoWrapper music = Allmusic.GSON.fromJson(response.body().string(), MusicInfoWrapper.class);
                if (music.songs != null && music.songs.length > 0) {
                    return music.songs[0];
                }
            } else {
                Allmusic.LOGGER.error("Failed to get music info: Invalid response " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to get music info: Network error", e);
        }
        return null;
    }

    public static SongList getSongList(long id) {
        if (id == 0) return null;
        HttpUrl.Builder url = HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/playlist/track/all").newBuilder()
                .addQueryParameter("id", String.valueOf(id));
        if (Allmusic.CONFIG.cookie != null && !Allmusic.CONFIG.cookie.isEmpty()) {
            url.addQueryParameter("cookie", Allmusic.CONFIG.cookie);
        }
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                        .url(url.build())
                        .build())
                .execute()) {
            if (response.code() == 200 && response.body() != null) {
                SongList list = Allmusic.GSON.fromJson(response.body().string(), SongList.class);
                list.id = id;
                return list;
            } else {
                Allmusic.LOGGER.error("Failed to get songs: Invalid response " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to get songs: Network error", e);
        }
        return null;
    }

    public static SearchResult search(String keyword, int page, int limit) {
        if (keyword == null || keyword.isEmpty()) return null;
        HttpUrl.Builder url = HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/search").newBuilder()
                .addQueryParameter("keywords", keyword)
                .addQueryParameter("type", "1")
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf((page - 1) * limit));
        if (Allmusic.CONFIG.cookie != null && !Allmusic.CONFIG.cookie.isEmpty()) {
            url.addQueryParameter("cookie", Allmusic.CONFIG.cookie);
        }
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                        .url(url.build())
                        .build())
                .execute()) {
            if (response.code() == 200 && response.body() != null) {
                SearchResult result = Allmusic.GSON.fromJson(response.body().string(), SearchResult.class);
                result.result.page = page;
                result.result.keyword = keyword;
                return result;
            } else {
                Allmusic.LOGGER.error("Failed to search songs: Invalid response " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to search songs: Network error", e);
        }
        return null;
    }
}
