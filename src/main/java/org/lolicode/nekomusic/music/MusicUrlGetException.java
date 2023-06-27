package org.lolicode.nekomusic.music;

import java.util.Objects;

public class MusicUrlGetException extends RuntimeException {
    private final MusicObj musicObj;
    public MusicUrlGetException(MusicObj musicObj) {
        super("Failed to get music url: " + (Objects.requireNonNull(musicObj).name == null ? "Unknown" : musicObj.name) + " (" + musicObj.id + ")");
        this.musicObj = musicObj;
    }
    
    public String getMusicDescription() {
        return (musicObj.name == null ? "Unknown" : musicObj.name) + " (" + musicObj.id + ")";
    }
}
