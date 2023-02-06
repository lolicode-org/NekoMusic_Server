package org.lolicode.allmusic.music;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.config.ModConfig;

import java.io.IOException;
import java.util.List;

public class Api {
    private static class CookieObj {
        protected int code;
        protected String cookie;
    }

    private static class MusicObjWrapper {
        protected List<MusicObj> data;
        protected int code;
    }

    private static class MusicInfoWrapper {
        protected MusicObj[] songs;
        protected int code;
    }

    public static boolean loginAnonimous() {
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(Allmusic.CONFIG.apiAddress + "/register/anonimous")
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                Allmusic.CONFIG.cookie = Allmusic.GSON.fromJson(response.body().string(), CookieObj.class).cookie;
                ModConfig.save();
                return true;
            } else {
                Allmusic.LOGGER.error("Failed to register anonimous user: " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to register anonimous user", e);
        }
        return false;
    }

    public static boolean refreshCookie() {
        if (Allmusic.CONFIG.cookie == null || Allmusic.CONFIG.cookie.isEmpty()) {
            return loginAnonimous();
        }
        try (Response response = Allmusic.HTTP_CLIENT.newCall(new Request.Builder()
                .url(HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/login/refresh").newBuilder()
                        .addQueryParameter("cookie", Allmusic.CONFIG.cookie)
                        .build())
                .build()).execute()) {
            if (response.code() == 200 && response.body() != null) {
                String newCookie = Allmusic.GSON.fromJson(response.body().string(), CookieObj.class).cookie;
                if (newCookie == null || newCookie.isEmpty()) {
                    Allmusic.LOGGER.error("Failed to refresh cookie: Invalid response");
                } else {
                    Allmusic.CONFIG.cookie = newCookie;
                    ModConfig.save();
                    return true;
                }
            } else {
                Allmusic.LOGGER.error("Failed to refresh cookie: " + response.code());
            }
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to refresh cookie", e);
        }
        return false;
    }

    private static MusicObj getMusic(int id) {
        if (id == 0) return null;
        HttpUrl.Builder url = HttpUrl.parse(Allmusic.CONFIG.apiAddress + "/song/info").newBuilder()
                .addQueryParameter("id", String.valueOf(id));
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

    public static String getMusicUrl(int id) {
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

    public static MusicObj getMusicInfo(int id) {
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
}
