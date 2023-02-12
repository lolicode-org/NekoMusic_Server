package org.lolicode.allmusic.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.lolicode.allmusic.Allmusic;
import org.lolicode.allmusic.music.SongList;

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
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "allmusic.json");
    }

    public static void create() {
        save();
    }

    public static void save() {
        prepare();

        String config = Allmusic.GSON.toJson(Allmusic.CONFIG);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(config);
        } catch (IOException e) {
            Allmusic.LOGGER.error("Failed to save mod config", e);
        }
    }

    public static boolean load() {
        prepare();

        if (!configFile.exists()) {
            create();
        }
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                ModConfig config = Allmusic.GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    Allmusic.CONFIG.cookie = config.cookie;
                    Allmusic.CONFIG.idleList = config.idleList;
                    if (config.apiAddress == null || config.apiAddress.isEmpty()) {
                        throw new RuntimeException("apiAddress is null in config.json");
                    }
                    Allmusic.CONFIG.apiAddress = config.apiAddress;
                    if (config.idleList > 0) {
                        SongList.loadIdleList();
                    }
                    return true;
                }
            } catch (IOException e) {
                Allmusic.LOGGER.error("Failed to load mod config", e);
            } catch (RuntimeException e) {
                Allmusic.LOGGER.error("Failed to parse mod config", e);
            }
        }

        return false;
    }

    public static void reload(MinecraftServer server, ServerCommandSource source) {
        if (load()) {
            source.sendFeedback(Text.of("§aAllMusic: Config reloaded"), true);
        } else {
            source.sendFeedback(Text.of("§cAllMusic: Config reload failed"), true);
        }
    }

}
