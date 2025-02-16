package org.lolicode.nekomusic.manager;

import org.lolicode.nekomusic.NekoMusic;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        Map<String, String> enUS = new HashMap<>();
        enUS.put("config.reloaded", "NekoMusic: Config reloaded");
        enUS.put("config.reload_failed", "NekoMusic: Config reload failed");

        Map<String, String> zhCN = new HashMap<>();
        zhCN.put("config.reloaded", "NekoMusic: 配置已重新加载");
        zhCN.put("config.reload_failed", "NekoMusic: 配置重新加载失败");

        messages.put("en-US", enUS);
        messages.put("zh-CN", zhCN);
    }

    public static String getMessage(String key) {
        String language = NekoMusic.CONFIG.language;
        return messages.getOrDefault(language, messages.get("zh-CN")).getOrDefault(key, key);
    }
}