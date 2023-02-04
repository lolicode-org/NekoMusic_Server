package org.lolicode.allmusic;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.lolicode.allmusic.command.MusicCommand;
import org.lolicode.allmusic.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import okhttp3.OkHttpClient;
import org.lolicode.allmusic.music.SongList;
import org.lolicode.allmusic.event.PlayerJoinCallback;
import org.lolicode.allmusic.task.PlayerJoin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class Allmusic implements DedicatedServerModInitializer {
    public static final String MOD_ID = "allmusic";
    public static final String MOD_NAME = "AllMusic";
    public static final String MOD_VERSION = "1.0.0";
    public static final Identifier ID = new Identifier("allmusic", "channel");
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final ModConfig CONFIG = new ModConfig();
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static final List<ScheduledFuture> EXECUTORS = new ArrayList<>();

    public static final SongList idleList = new SongList();
    public static final SongList playingList = new SongList();
    public static Set<String> currentVote = new HashSet<>();

    @Override
    public void onInitializeServer() {
        if (ModConfig.load()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                MusicCommand.register(dispatcher);
            });
            PlayerJoinCallback.EVENT.register(PlayerJoin::OnPlayerJoin);
            LOGGER.info("AllMusic mod loaded");
        } else {
            LOGGER.error("Failed to load mod config, mod will not work");
        }
    }
}
