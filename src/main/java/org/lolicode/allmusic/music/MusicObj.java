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
    public int id;
    public String url;
    public int dt;
    protected FreeTrialInfoObj freeTrialInfo;
    public String player;  // who ordered this song
}
