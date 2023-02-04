package org.lolicode.allmusic.music;

import org.lolicode.allmusic.Allmusic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SongList {
    public LinkedList<MusicObj> songs = new LinkedList<>();

    protected int id = 0;

    protected boolean isPersistent = false;

    public void add(MusicObj musicObj) {
        songs.add(musicObj);
    }

    public MusicObj next() {
        if (songs.size() == 0) return null;
        if (isPersistent) {
            Random random = new Random();
            return Api.getMusic(songs.get(random.nextInt(songs.size())).id);  // Don't use cached url, as the url may be expired
        }
        MusicObj musicObj = songs.get(0);
        songs.remove(0);
        return Api.getMusic(musicObj.id);
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
}
