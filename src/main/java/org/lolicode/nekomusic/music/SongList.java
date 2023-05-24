package org.lolicode.nekomusic.music;

import org.lolicode.nekomusic.NekoMusic;

import java.util.LinkedList;
import java.util.Random;

/**
 * The list of music that is only used in the <strong>server</strong>.
 */
public class SongList {
    public volatile LinkedList<MusicObj> songs = new LinkedList<>();

    protected volatile long id = 0;

    protected volatile boolean isPersistent = false;

    public volatile boolean isPlaying = false;

    public void add(MusicObj musicObj) {
        songs.add(musicObj);
    }

    public MusicObj get(long id) {
        return songs.stream().filter(musicObj -> musicObj.id == id).findFirst().orElse(null);
    }

    public MusicObj next() throws InterruptedException {
        if (songs.size() == 0) return null;

        MusicObj music;
        if (isPersistent) {
            music = songs.get(new Random().nextInt(songs.size()));
        } else {
            music = songs.get(0);
            songs.remove(0);
        }
        String url = Api.getMusicUrl(music);  // Don't use cached url, as the url may be expired
        LyricObj lyric = music.lyric == null ? Api.getLyric(music) : music.lyric; // use cached lyric if available
        if (url != null) {
            music.url = url;
            if (lyric != null) music.lyric = lyric;

            isPlaying = true;
            if (isPersistent)
                NekoMusic.orderList.isPlaying = false;
            else
                NekoMusic.idleList.isPlaying = false;
            return music;
        } else {
            if (isPersistent)
                songs.remove(music);  // Remove the song from the list if it's not available
            Thread.sleep(1000);
            return next();
        }
    }

    public boolean remove(MusicObj musicObj) {
        if (!songs.contains(musicObj)) return false;
        songs.remove(musicObj);
        return true;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= songs.size()) return false;
        songs.remove(index);
        return true;
    }

    public void load(SongList newSongList) {
        songs.clear();
        songs.addAll(newSongList.songs);
        id = newSongList.id;
    }

    public static void loadIdleList() {
        if (NekoMusic.CONFIG.idleList == 0) return;
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                SongList songList = Api.getSongList(NekoMusic.CONFIG.idleList);
                if (songList != null) {
                    NekoMusic.idleList.load(songList);
                    NekoMusic.idleList.isPersistent = true;
                    NekoMusic.idleList.id = NekoMusic.CONFIG.idleList;
                }
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Failed to load idle list", e);
            }
        });
    }

    public boolean hasSong(MusicObj musicObj) {
        return songs.contains(musicObj);
    }

    public boolean hasSong(long id) {
        for (MusicObj musicObj : songs) {
            if (musicObj.id == id) return true;
        }
        return false;
    }
}
