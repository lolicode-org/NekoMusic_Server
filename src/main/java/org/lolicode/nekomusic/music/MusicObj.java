package org.lolicode.nekomusic.music;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

public class MusicObj {
    public static class ArtistObj {
        public String name;
        protected String id;
    }

    protected static class FreeTrialInfoObj {
        public String level;
        public String encodeType;
    }

    public static class AlbumObj {
        public String name;
        public String id;
        @SerializedName("picUrl")
        public String picUrl;
    }

    private static final class FreeTrialInfoAdapter implements JsonDeserializer<FreeTrialInfoObj> {
        @Override
        public MusicObj.FreeTrialInfoObj deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }
//            if (json.isJsonPrimitive()) {
//                JsonPrimitive prim = json.getAsJsonPrimitive();
//                if (prim.isString() && "null".equalsIgnoreCase(prim.getAsString().trim())) {
//                    return null;
//                }
//                // Any other primitive is illegal here; ignore
//                return null;
//            }
            if (json.isJsonObject()) {
                return ctx.deserialize(json.getAsJsonObject(), MusicObj.FreeTrialInfoObj.class);
            }
            // Unknown shape; ignore
            return null;
        }
    }

    public List<ArtistObj> ar = List.of();
    public String name;
    public long id;
    public String url;
    public long dt;
    protected long time; // should be equal to dt, to determine if the song is trial (fuck netease)
    @JsonAdapter(FreeTrialInfoAdapter.class)
    @SerializedName("freeTrialInfo")
    protected FreeTrialInfoObj freeTrialInfo;
    protected byte fee;
    protected byte payed;
    public String player;  // who ordered this song
    public LyricObj lyric;
    @SerializedName("al")
    public AlbumObj album;
    public int br;

    @SerializedName("seek_to")
    public long seekTo;
}
