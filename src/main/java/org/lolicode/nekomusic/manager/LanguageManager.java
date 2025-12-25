package org.lolicode.nekomusic.manager;

import org.lolicode.nekomusic.NekoMusic;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static final Map<String, Map<String, String>> messages = new HashMap<>();

    static {
        Map<String, String> zhCN = new HashMap<>();
        zhCN.put("config.reloaded", "§eNekoMusic: 配置已重新加载");
        zhCN.put("config.reload_failed", "§cNekoMusic: 配置重新加载失败");
        zhCN.put("login.qr_prompt", "§e请打开以下链接并扫描浏览器中显示的二维码:");
        zhCN.put("login.qr_scan", "§e请扫描以下二维码登录");
        zhCN.put("login.qr_check", "§e扫描后，请运行 §b/music login check§e 继续");
        zhCN.put("login.qr_failed", "§c生成二维码失败");
        zhCN.put("login.key_failed", "§c获取登录密钥失败");
        zhCN.put("login.no_key", "§c请先获取登陆二维码");
        zhCN.put("login.key_expired", "§c登录密钥已过期，请重新获取二维码");
        zhCN.put("login.qr_waiting_scan", "§c登陆失败: 请先扫描二维码");
        zhCN.put("login.qr_waiting_confirm", "§c登陆失败: 请在手机上确认登陆");
        zhCN.put("login.status_failed", "§c检查登录状态失败: 未知错误");
        zhCN.put("login.status_internal_error", "§c检查登录状态失败: 内部错误");
        zhCN.put("login.status_not_logged_in", "§e登录状态: §c未登录");
        zhCN.put("login.status_logged_in", "§e登录状态: §a已登录");
        zhCN.put("login.user_info_failed", "§c获取用户信息失败");
        zhCN.put("login.user_id", "§e用户ID: §b");
        zhCN.put("login.nickname", "§e昵称: §b");
        zhCN.put("login.vip_type", "§eVIP类型: §b");
        zhCN.put("music.playing", "§e正在播放: §a");
        zhCN.put("music.vote_count", "§e正在投票切歌: §a");
        zhCN.put("music.ordered", "§e已添加: §a");
        zhCN.put("music.get_info_failed", "§c获取歌曲信息失败");
        zhCN.put("music.ordered_already", "§c这首歌已在播放列表中");
        zhCN.put("music.banned", "§c这首歌已被封禁。");
        zhCN.put("music.deleted", "§e已删除: §a");
        zhCN.put("music.invalid_index", "§c无效的索引或ID");
        zhCN.put("music.no_permission", "§c你没有足够的权限");
        zhCN.put("music.delete_failed", "§c删除失败");
        zhCN.put("music.no_music", "§c播放列表为空");
        zhCN.put("music.playlist", "§e播放列表: \n");
        zhCN.put("music.current", "§e正在播放: §a");
        zhCN.put("music.click_next", "§c下一首");
        zhCN.put("music.click_delete", "§c删除");
        zhCN.put("music.click_ban", "§c封禁");
        zhCN.put("search.no_search_result", "§c没有搜索结果");
        zhCN.put("search.search_result_header", "§a标题 §e-§9 艺术家 §e- §d专辑" + "\n");
        zhCN.put("search.search_failed", "§c搜索失败");
        zhCN.put("music.add", "点击添加到播放列表");
        zhCN.put("music.add_now", "点击添加到播放列表，并跳过空闲列表");
        zhCN.put("music.add_force_now", "点击添加到播放列表，并立即播放");
        zhCN.put("music.unban", "解禁");
        zhCN.put("music.ban", "封禁");
        zhCN.put("music.working", "§b请稍候...");
        zhCN.put("music.ban_already", "§c这首歌已被封禁");
        zhCN.put("music.invalid_id", "§c无效的ID");
        zhCN.put("music.ban_success", "§a封禁成功");
        zhCN.put("music.unknown_error", "§c未知错误");
        zhCN.put("music.unban_success", "§a解禁成功");
        zhCN.put("music.unban_not_banned", "§c这首歌没有被封禁");
        zhCN.put("music.get_url_failed", "§c获取 %s 的URL失败，请检查信息是否正确。如果是付费歌曲，请确保你已使用有效的VIP账号登录");
        zhCN.put("player.default", "默认");
        zhCN.put("list.previous_page", "上一页");
        zhCN.put("list.next_page", "下一页");

        Map<String, String> enUS = new HashMap<>();
        enUS.put("config.reloaded", "§eNekoMusic: Config reloaded");
        enUS.put("config.reload_failed", "§cNekoMusic: Failed to reload config");
        enUS.put("login.qr_prompt", "§ePlease open the following link and scan the QR code displayed in the browser:");
        enUS.put("login.qr_scan", "§ePlease scan the following QR code to login");
        enUS.put("login.qr_check", "§eAfter scanning, run §b/music login check§e to continue");
        enUS.put("login.qr_failed", "§cFailed to generate QR code");
        enUS.put("login.key_failed", "§cFailed to get login key");
        enUS.put("login.no_key", "§cPlease get login QR code first");
        enUS.put("login.key_expired", "§cLogin key expired, please get QR code again");
        enUS.put("login.qr_waiting_scan", "§cLogin failed: Please scan the QR code first");
        enUS.put("login.qr_waiting_confirm", "§cLogin failed: Please confirm login on your phone");
        enUS.put("login.status_failed", "§cFailed to check login status: Unknown error");
        enUS.put("login.status_internal_error", "§cFailed to check login status: Internal error");
        enUS.put("login.status_not_logged_in", "§eLogin status: §cNot logged in");
        enUS.put("login.status_logged_in", "§eLogin status: §aLogged in");
        enUS.put("login.user_info_failed", "§cFailed to get user info");
        enUS.put("login.user_id", "§eUser ID: §b");
        enUS.put("login.nickname", "§eNickname: §b");
        enUS.put("login.vip_type", "§eVIP Type: §b");
        enUS.put("music.playing", "§eNow playing: §a");
        enUS.put("music.vote_count", "§eVoting to skip: §a");
        enUS.put("music.ordered", "§eOrdered: §a");
        enUS.put("music.get_info_failed", "§cFailed to get song info");
        enUS.put("music.ordered_already", "§cThis song is already in the playlist");
        enUS.put("music.banned", "§cThis song has been banned.");
        enUS.put("music.deleted", "§eDeleted: §a");
        enUS.put("music.invalid_index", "§cInvalid index or ID");
        enUS.put("music.no_permission", "§cYou don't have enough permission");
        enUS.put("music.delete_failed", "§cFailed to delete");
        enUS.put("music.no_music", "§cPlaylist is empty");
        enUS.put("music.playlist", "§ePlaylist: \n");
        enUS.put("music.current", "§eNow playing: §a");
        enUS.put("music.click_next", "§cNext");
        enUS.put("music.click_delete", "§cDelete");
        enUS.put("music.click_ban", "§cBan");
        enUS.put("search.no_search_result", "§cNo search result");
        enUS.put("search.search_result_header", "§aTitle §e-§9 Artist §e- §dAlbum" + "\n");
        enUS.put("search.search_failed", "§cSearch failed");
        enUS.put("music.add", "Click to add to playlist");
        enUS.put("music.add_now", "Click to add to playlist and skip idle list");
        enUS.put("music.add_force_now", "Click to add to playlist and play immediately");
        enUS.put("music.unban", "Unban");
        enUS.put("music.ban", "Ban");
        enUS.put("music.working", "§bWorking...");
        enUS.put("music.ban_already", "§cThis song has been banned");
        enUS.put("music.invalid_id", "§cInvalid ID");
        enUS.put("music.ban_success", "§aBan success");
        enUS.put("music.unknown_error", "§cUnknown error");
        enUS.put("music.unban_success", "§aUnban success");
        enUS.put("music.unban_not_banned", "§cThis song is not banned");
        enUS.put("music.get_url_failed", "§cFailed to get URL of %s, please check if the information is correct. If it's a premium song, make sure you have logged in with a valid VIP account");
        enUS.put("player.default", "Default");
        enUS.put("list.previous_page", "Previous Page");
        enUS.put("list.next_page", "Next Page");

        messages.put("zh-CN", zhCN);
        messages.put("en-US", enUS);
    }

    public static String getMessage(String key) {
        String language = NekoMusic.CONFIG.language;
        return messages.getOrDefault(language, messages.get("zh-CN")).getOrDefault(key, key);
    }

    public static String getMessage(String key, Object... args) {
        String message = getMessage(key);
        return String.format(message, args);
    }
}