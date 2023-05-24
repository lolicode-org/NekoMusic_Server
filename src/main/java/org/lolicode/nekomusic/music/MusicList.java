package org.lolicode.nekomusic.music;

import java.util.Arrays;

public class MusicList {
    public static class Music {
        public String name;
        public String artist;
        public String album;
    }

    public Music[] musics;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(musics).forEach(music -> {
            sb.append(music.name);
            if (music.artist != null) {
                sb.append(" - ").append(music.artist);
            }
            if (music.album != null) {
                sb.append(" (").append(music.album).append(")");
            }
            sb.append("\n");
        });
        return sb.toString();
    }

    public Boolean isEmpty() {
        return musics == null || musics.length == 0;
    }
}
