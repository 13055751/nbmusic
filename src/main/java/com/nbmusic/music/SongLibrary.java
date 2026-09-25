package com.nbmusic.music;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌曲库：内置歌曲（resources/songs/*.txt，首次启动落盘）+ 玩家自定义（songs/ 目录）。
 * 每次载入都重新解析文本谱（数据驱动，改谱即生效）。
 */
public final class SongLibrary {

    private final Plugin plugin;
    private final File dir;
    private final Map<String, Song> songs = new LinkedHashMap<>();

    public SongLibrary(Plugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "songs");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().warning("创建歌曲目录失败: " + dir.getPath());
        }
        ensureBuiltin();
        reload();
    }

    /** 内置歌曲首次启动落盘（供参考/编辑）。 */
    private void ensureBuiltin() {
        for (String builtin : new String[]{"twinkle", "ode_to_joy", "happy_birthday"}) {
            File f = new File(dir, builtin + ".txt");
            if (!f.exists()) {
                plugin.saveResource("songs/" + builtin + ".txt", false);
            }
        }
    }

    /** 重载全部歌曲（/nbm reload）。 */
    public void reload() {
        songs.clear();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".txt"));
        if (files == null) {
            return;
        }
        for (File f : files) {
            try {
                String text = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                StringBuilder cause = new StringBuilder();
                Song song = SongParser.parse(text, cause);
                if (song == null) {
                    plugin.getLogger().warning("歌曲解析失败: " + f.getName() + " -> " + cause);
                    continue;
                }
                if (song.name == null || song.name.isBlank()) {
                    song.name = f.getName().replace(".txt", "");
                }
                songs.put(song.name, song);
            } catch (IOException e) {
                plugin.getLogger().warning("歌曲读取失败: " + f.getName() + " -> " + e.getMessage());
            }
        }
    }

    /** 保存一首歌（覆盖同名）。 */
    public void save(String name, String text) {
        File f = new File(dir, safe(name) + ".txt");
        try {
            Files.writeString(f.toPath(), text, StandardCharsets.UTF_8);
            reload();
        } catch (IOException e) {
            plugin.getLogger().warning("歌曲保存失败: " + e.getMessage());
        }
    }

    /** 删除一首歌（内置歌曲也可删，下次启动恢复）。 */
    public boolean delete(String name) {
        File f = new File(dir, safe(name) + ".txt");
        boolean ok = f.delete();
        if (ok) {
            reload();
        }
        return ok;
    }

    public Song get(String name) {
        if (name == null) {
            return null;
        }
        for (Map.Entry<String, Song> e : songs.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) {
                return e.getValue();
            }
        }
        return null;
    }

    public List<String> list() {
        List<String> names = new ArrayList<>(songs.keySet());
        names.sort(Comparator.naturalOrder());
        return names;
    }

    private static String safe(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
