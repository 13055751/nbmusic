package com.nbmusic.command;

import com.nbmusic.NBMusicPlugin;
import com.nbmusic.music.Song;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MusicCommand implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of(
            "play", "stop", "pause", "step", "next", "list", "info", "reload", "save", "del");

    private final NBMusicPlugin plugin;

    public MusicCommand(NBMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            printHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "play" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令仅限玩家使用。");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§c用法: /nbm play <歌曲> [拍速倍率]");
                    return true;
                }
                Song song = plugin.getSongLibrary().get(args[1]);
                if (song == null) {
                    player.sendMessage("§c未找到歌曲: " + args[1] + "（/nbm list 查看）");
                    return true;
                }
                double speed = 1.0;
                if (args.length >= 3) {
                    try {
                        speed = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c倍率无效: " + args[2]);
                        return true;
                    }
                }
                boolean ok = plugin.getSongPlayer().play(player, song, speed, false);
                player.sendMessage(ok
                        ? "§a♪ 播放「" + song.name + "」(" + song.tempo + " BPM, "
                        + song.tracks.size() + " 轨, " + song.eventCount() + " 音符"
                        + (speed != 1 ? ", ×" + speed : "") + ")"
                        : "§c播放失败。");
            }
            case "step" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令仅限玩家使用。");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§c用法: /nbm step <歌曲>（逐拍步进调试，/nbm next 前进）");
                    return true;
                }
                Song song = plugin.getSongLibrary().get(args[1]);
                if (song == null) {
                    player.sendMessage("§c未找到歌曲: " + args[1]);
                    return true;
                }
                boolean ok = plugin.getSongPlayer().play(player, song, 1.0, true);
                player.sendMessage(ok ? "§a逐拍调试「" + song.name + "」，用 /nbm next 前进一拍。"
                        : "§c进入步进失败。");
            }
            case "next" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令仅限玩家使用。");
                    return true;
                }
                int beat = plugin.getSongPlayer().step(player);
                player.sendMessage(beat < 0 ? "§c当前没有步进中的播放（先 /nbm step <歌曲>）。"
                        : "§a第 " + beat + " 拍。");
            }
            case "stop" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令仅限玩家使用。");
                    return true;
                }
                plugin.getSongPlayer().stop(player);
                player.sendMessage("§7播放已停止。");
            }
            case "pause" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c该命令仅限玩家使用。");
                    return true;
                }
                boolean paused = plugin.getSongPlayer().togglePause(player);
                player.sendMessage(paused ? "§7播放已暂停。" : "§a播放继续。");
            }
            case "list" -> {
                List<String> names = plugin.getSongLibrary().list();
                if (names.isEmpty()) {
                    sender.sendMessage("§7暂无歌曲。");
                    return true;
                }
                sender.sendMessage("§e[NBMusic] §7歌曲库 (" + names.size() + "):");
                for (String n : names) {
                    sender.sendMessage("  §a" + n);
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nbm info <歌曲>");
                    return true;
                }
                Song song = plugin.getSongLibrary().get(args[1]);
                if (song == null) {
                    sender.sendMessage("§c未找到歌曲: " + args[1]);
                    return true;
                }
                sender.sendMessage("§e[NBMusic] §7" + song.name
                        + " §8(" + song.tempo + " BPM, 音量 " + song.volume + ", "
                        + song.totalTicks() + " tick)");
                for (Song.Track t : song.tracks) {
                    sender.sendMessage("  §a" + t.name + " §7(" + t.instrument + ") §8- "
                            + t.notes.size() + " 音符");
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("nbmusic.admin")) {
                    sender.sendMessage("§c你没有权限执行此命令。");
                    return true;
                }
                plugin.getSongLibrary().reload();
                sender.sendMessage("§a歌曲库已重载: " + plugin.getSongLibrary().list().size() + " 首。");
            }
            case "save" -> {
                if (!sender.hasPermission("nbmusic.admin")) {
                    sender.sendMessage("§c你没有权限执行此命令。");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /nbm save <名称> <谱面文本...>（或用 /nbm reload 读取 songs/ 目录文件）");
                    return true;
                }
                StringBuilder text = new StringBuilder("name " + args[1] + "\n");
                for (int i = 2; i < args.length; i++) {
                    text.append(args[i]).append(' ');
                }
                plugin.getSongLibrary().save(args[1], text.toString());
                sender.sendMessage("§a已保存并解析: " + args[1]
                        + (plugin.getSongLibrary().get(args[1]) != null ? "（成功）" : "（解析失败，见日志）"));
            }
            case "del" -> {
                if (!sender.hasPermission("nbmusic.admin")) {
                    sender.sendMessage("§c你没有权限执行此命令。");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nbm del <歌曲>");
                    return true;
                }
                boolean ok = plugin.getSongLibrary().delete(args[1]);
                sender.sendMessage(ok ? "§7已删除歌曲: " + args[1] : "§c未找到歌曲: " + args[1]);
            }
            default -> printHelp(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }
        if (args.length == 2) {
            List<String> songs = plugin.getSongLibrary().list();
            return StringUtil.copyPartialMatches(args[1], songs, new ArrayList<>());
        }
        return List.of();
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("§e===== NBMusic 帮助 =====");
        sender.sendMessage("§7/nbm play <歌曲> [倍率] §8- 播放歌曲（倍率>1 慢放）");
        sender.sendMessage("§7/nbm stop §8- 停止播放");
        sender.sendMessage("§7/nbm pause §8- 暂停/继续");
        sender.sendMessage("§7/nbm step <歌曲> §8- 逐拍步进调试（/nbm next 前进）");
        sender.sendMessage("§7/nbm list §8- 歌曲列表");
        sender.sendMessage("§7/nbm info <歌曲> §8- 歌曲信息（轨道/音色/时长）");
        sender.sendMessage("§7/nbm reload §8- 重载 songs/ 目录谱面（管理员）");
        sender.sendMessage("§7/nbm save <名称> <谱面> §8- 命令行保存一首歌（管理员）");
    }
}
