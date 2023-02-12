package org.lolicode.allmusic.music;

import java.util.List;

public class MusicObj {
    public static class ArtistObj {
        public String name;
        protected String id;
    }

    protected static class FreeTrialInfoObj {
        public int start;
        public int end;
    }

    public List<ArtistObj> ar = List.of();
    public String name;
    public long id;
    public String url;
    public long dt;
    protected long time; // should be equal to dt, to determine if the song is trial (fuck netease)
    protected FreeTrialInfoObj freeTrialInfo;
    protected byte fee;
    protected byte payed;
    public String player;  // who ordered this song
}
