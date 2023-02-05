package org.lolicode.allmusic.music;

import org.lolicode.allmusic.Allmusic;

import java.util.LinkedList;
import java.util.Random;

public class SongList {
    public LinkedList<MusicObj> songs = new LinkedList<>();

    protected int id = 0;

    protected boolean isPersistent = false;

    public boolean isPlaying = false;

    public void add(MusicObj musicObj) {
        songs.add(musicObj);
    }

    public MusicObj next() {
        if (songs.size() == 0) return null;

        MusicObj music;
        if (isPersistent) {
            Random random = new Random();
            music = songs.get(random.nextInt(songs.size()));
        } else {
            music = songs.get(0);
            songs.remove(0);
        }
        String url = Api.getMusicUrl(music.id);  // Don't use cached url, as the url may be expired
        if (url != null) {
            music.url = url;

            isPlaying = true;
            if (isPersistent)
                Allmusic.orderList.isPlaying = false;
            else
                Allmusic.idleList.isPlaying = false;
            return music;
        } else {
            if (isPersistent)
                songs.remove(music);  // Remove the song from the list if it's not available
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

    public static boolean loadIdleList() {
        if (Allmusic.CONFIG.idleList == 0) return false;
        try {
            SongList songList = Api.getSongList(Allmusic.CONFIG.idleList);
            if (songList != null) {
                Allmusic.idleList.load(songList);
                Allmusic.idleList.isPersistent = true;
                Allmusic.idleList.id = Allmusic.CONFIG.idleList;
                return true;
            }
        } catch (Exception e) {
            Allmusic.LOGGER.error("Failed to load idle list", e);
        }
        return false;
    }

    public boolean hasSong(MusicObj musicObj) {
        return songs.contains(musicObj);
    }

    public boolean hasSong(int id) {
        for (MusicObj musicObj : songs) {
            if (musicObj.id == id) return true;
        }
        return false;
    }
}
