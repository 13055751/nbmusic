package com.nbmusic;

import com.nbmusic.command.MusicCommand;
import com.nbmusic.music.SongLibrary;
import com.nbmusic.music.SongPlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * NBMusic 音符盒音乐插件。
 * <p>
 * 独立可用；与 Eldoria 软联动：Eldoria 检测到本插件存在时，.efx 的
 * `music &lt;歌曲&gt;` 指令会经 {@link com.nbmusic.api.MusicApi} 播放旋律。
 * </p>
 */
public final class NBMusicPlugin extends JavaPlugin {

    private static NBMusicPlugin instance;

    private SongLibrary songLibrary;
    private SongPlayer songPlayer;

    @Override
    public void onEnable() {
        instance = this;
        this.songLibrary = new SongLibrary(this);
        this.songPlayer = new SongPlayer(this);
        getCommand("nbm").setExecutor(new MusicCommand(this));
        getLogger().info("NBMusic 已启用，内置歌曲: " + songLibrary.list().size() + " 首");
    }

    @Override
    public void onDisable() {
        if (songPlayer != null) {
            songPlayer.stopAll();
        }
        getLogger().info("NBMusic 已停用");
    }

    public SongLibrary getSongLibrary() {
        return songLibrary;
    }

    public SongPlayer getSongPlayer() {
        return songPlayer;
    }

    public static NBMusicPlugin get() {
        return instance;
    }
}
