package com.nbmusic.api;

import com.nbmusic.NBMusicPlugin;
import com.nbmusic.music.Song;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * 公开 API（供其他插件如 Eldoria 联动调用；反射加载，软依赖）。
 * <p>
 * Eldoria 侧用法（.efx music 指令）：当 nbmusic 插件存在时，
 * `Class.forName("com.nbmusic.api.MusicApi")` 反射调用
 * `play(Plugin caller, Player player, String songName, double speed)`。
 * </p>
 */
public final class MusicApi {

    private MusicApi() {
    }

    /** 播放一首歌（对目标玩家）。返回是否成功。 */
    public static boolean play(Plugin caller, Player player, String songName, double speed) {
        NBMusicPlugin plugin = NBMusicPlugin.get();
        if (plugin == null || player == null) {
            return false;
        }
        Song song = plugin.getSongLibrary().get(songName);
        if (song == null) {
            return false;
        }
        return plugin.getSongPlayer().play(player, song, speed > 0 ? speed : 1.0, false);
    }

    /** 停止播放。 */
    public static boolean stop(Player player) {
        NBMusicPlugin plugin = NBMusicPlugin.get();
        if (plugin == null || player == null) {
            return false;
        }
        plugin.getSongPlayer().stop(player);
        return true;
    }

    /** 歌曲列表。 */
    public static List<String> list() {
        NBMusicPlugin plugin = NBMusicPlugin.get();
        return plugin == null ? List.of() : plugin.getSongLibrary().list();
    }
}
