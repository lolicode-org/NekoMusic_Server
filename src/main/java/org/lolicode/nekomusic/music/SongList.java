package org.lolicode.nekomusic.music;

import org.lolicode.nekomusic.NekoMusic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The list of music that is only used in the <strong>server</strong>.
 */
public class SongList {
    private final LinkedList<MusicObj> songs = new LinkedList<>();

    protected volatile long id = 0;

    protected final boolean isPersistent;

    public volatile boolean isPlaying = false;
    private final Lock lock = new ReentrantLock();

    public SongList(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public void add(MusicObj musicObj) {
        try {
            lock.lock();
            songs.add(musicObj);
        } finally {
            lock.unlock();
        }
    }

    public MusicObj get(long id) {
        try {
            lock.lock();
            return songs.stream().filter(musicObj -> musicObj.id == id).findFirst().orElse(null);
        } finally {
            lock.unlock();
        }
    }

    public MusicObj next() throws InterruptedException {
        MusicObj music;
        try {
            lock.lock();
            if (songs.size() == 0) return null;
            if (isPersistent) {
                music = songs.get(new Random().nextInt(songs.size()));
            } else {
                music = songs.pollFirst();
            }
        } finally {
            lock.unlock();
        }
//        String url = Api.getMusicUrl(music);  // Don't use cached url, as the url may be expired
        MusicObj newObj = Api.getMusicForPlay(music);
        LyricObj lyric = music.lyric == null ? Api.getLyric(music) : music.lyric; // use cached lyric if available
        if (newObj != null && newObj.url != null && !newObj.url.isBlank()) {
            music.url = newObj.url;
            if (lyric != null) music.lyric = lyric;
            music.br = newObj.br;

            isPlaying = true;
            if (isPersistent)
                NekoMusic.orderList.isPlaying = false;
            else
                NekoMusic.idleList.isPlaying = false;
            return music;
        } else {
            if (isPersistent)
                remove(music);  // Remove the song from the list if it's not available
            NekoMusic.LOGGER.error("Failed to get url of music " + music.id + ", retrying in 1 second");
            Thread.sleep(1000);
            return next();  // FIXME: if we're stuck retrying, the user might not be able to order music until it succeeds or the list is empty, or even worse, stack overflow
        }
    }

    public boolean remove(MusicObj musicObj) {
        try {
            lock.lock();
            return songs.remove(musicObj);
        } finally {
            lock.unlock();
        }
    }

    public void load(SongList newSongList) {
        try {
            lock.lock();
            songs.clear();
            songs.addAll(newSongList.songs);
            id = newSongList.id;
        } finally {
            lock.unlock();
        }
    }

    public static void loadIdleList() {
        if (NekoMusic.CONFIG.idleList == 0) {
            NekoMusic.idleList.load(new SongList(true));
            NekoMusic.idleList.id = 0;
            return;
        }
        NekoMusic.EXECUTOR.execute(() -> {
            try {
                SongList songList = Api.getSongList(NekoMusic.CONFIG.idleList);
                if (songList != null) {
                    NekoMusic.idleList.load(songList);
                    NekoMusic.idleList.id = NekoMusic.CONFIG.idleList;
                }
            } catch (Exception e) {
                NekoMusic.LOGGER.error("Failed to load idle list", e);
            }
        });
    }

    public boolean hasSong(long id) {
        try {
            lock.lock();
            return songs.stream().anyMatch(musicObj -> musicObj.id == id);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        try {
            lock.lock();
            return songs.size();
        } finally {
            lock.unlock();
        }
    }

    public List<MusicObj> getSongs() {
        try {
            lock.lock();
            return new LinkedList<>(songs);
        } finally {
            lock.unlock();
        }
    }
}
