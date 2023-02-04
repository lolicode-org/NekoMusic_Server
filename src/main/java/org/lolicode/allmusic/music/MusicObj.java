package org.lolicode.allmusic.music;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MusicObj {
    public class ArtistObj {
        public String name;
        protected String id;
    }

    public List<ArtistObj> ar = List.of();
    public String name;
    public int id;
    public String url;
    public int time;
    protected byte fee;
    protected byte payed;
    public String player;  // who ordered this song
}
