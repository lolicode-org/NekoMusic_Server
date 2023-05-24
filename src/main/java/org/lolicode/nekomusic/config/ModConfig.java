package org.lolicode.nekomusic.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.music.SongList;

import java.io.*;

public class ModConfig {
    public String cookie = "";
    public long idleList = 0;
    public String apiAddress = "";
    public float voteThreshold = 0.5f;
    public int maxQuality = 320000;

    private static File configFile;

    public static void prepare() {
        if (configFile != null) return;
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "nekomusic.json");
    }

    public static void create() {
        save();
    }

    public static void save() {
        prepare();

        String config = NekoMusic.GSON.toJson(NekoMusic.CONFIG);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(config);
        } catch (IOException e) {
            NekoMusic.LOGGER.error("Failed to save mod config", e);
        }
    }

    public static boolean load() {
        prepare();

        if (!configFile.exists()) {
            create();
        }
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                ModConfig config = NekoMusic.GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    NekoMusic.CONFIG.cookie = config.cookie;
                    NekoMusic.CONFIG.idleList = config.idleList;
                    if (config.apiAddress == null || config.apiAddress.isEmpty()) {
                        throw new RuntimeException("apiAddress is null in mod config");
                    }
                    NekoMusic.CONFIG.apiAddress = config.apiAddress;
                    if (config.idleList > 0) {
                        SongList.loadIdleList();
                    }
                    return true;
                }
            } catch (IOException e) {
                NekoMusic.LOGGER.error("Failed to load mod config", e);
            } catch (RuntimeException e) {
                NekoMusic.LOGGER.error("Failed to parse mod config", e);
            }
        }

        return false;
    }

    public static void reload(MinecraftServer server, ServerCommandSource source) {
        if (load()) {
            source.sendFeedback(() -> Text.of("§aNekoMusic: Config reloaded"), true);
        } else {
            source.sendFeedback(() -> Text.of("§cNekoMusic: Config reload failed"), true);
        }
    }

    public static boolean loadLegacy() {
        File legacyFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "allmusic.json");
        if (legacyFile.exists()) {
            configFile = legacyFile;
            if (load()) {
                NekoMusic.LOGGER.warn("Found legacy config file, migrating...");
                configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "nekomusic.json");
                save();
                if (!legacyFile.delete()) {
                    NekoMusic.LOGGER.warn("Failed to delete legacy config file, you may need to delete 'allmusic.json' manually");
                }
                return true;
            }
        }
        return false;
    }
}
