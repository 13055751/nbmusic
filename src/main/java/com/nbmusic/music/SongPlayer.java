package com.nbmusic.music;

import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 歌曲播放器：逐 tick 调度音符（playNote，纯音符盒音色，无需放置方块）。
 * <p>
 * 调试友好：倍速（speed &gt;1 慢放）/ 暂停 / 逐 tick 步进（stepMode，配合 step()）。
 * </p>
 */
public final class SongPlayer {

    private final org.bukkit.plugin.Plugin plugin;
    private final Map<UUID, Playing> playing = new HashMap<>();

    public SongPlayer(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPlaying(Player player) {
        return playing.containsKey(player.getUniqueId());
    }

    public void stop(Player player) {
        Playing p = playing.remove(player.getUniqueId());
        if (p != null && p.task != null) {
            p.task.cancel();
        }
    }

    public void stopAll() {
        for (Playing p : playing.values()) {
            if (p.task != null) {
                p.task.cancel();
            }
        }
        playing.clear();
    }

    /** 暂停/继续；返回当前是否暂停。 */
    public boolean togglePause(Player player) {
        Playing p = playing.get(player.getUniqueId());
        if (p == null) {
            return false;
        }
        p.paused = !p.paused;
        return p.paused;
    }

    /** 步进模式下前进一拍（一次播放一拍音符）；返回当前拍数。 */
    public int step(Player player) {
        Playing p = playing.get(player.getUniqueId());
        if (p == null || !p.stepMode) {
            return -1;
        }
        p.playBeat();
        return p.beat;
    }

    /**
     * 播放歌曲。
     *
     * @param speed    拍速倍率：1=原速，0.5=半速（慢），2=双速；&lt;=0 用 1
     * @param stepMode true=逐拍步进（不自动推进，用 step()）
     */
    public boolean play(Player player, Song song, double speed, boolean stepMode) {
        stop(player);
        if (song == null || song.tracks.isEmpty()) {
            return false;
        }
        Playing p = new Playing(player, song, speed > 0 ? speed : 1.0, stepMode);
        playing.put(player.getUniqueId(), p);
        p.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> p.tick(), 0L, 1L);
        return true;
    }

    /** 单次播放状态机。 */
    private static final class Playing {
        final Player player;
        final Song song;
        final double speed;
        final boolean stepMode;
        final int totalTicks;
        double tick = 0;
        int beat = 0;
        boolean paused = false;
        double acc = 0;
        org.bukkit.scheduler.BukkitTask task;

        Playing(Player player, Song song, double speed, boolean stepMode) {
            this.player = player;
            this.song = song;
            this.speed = speed;
            this.stepMode = stepMode;
            this.totalTicks = song.totalTicks();
        }

        void tick() {
            if (paused || stepMode) {
                return;
            }
            acc += speed;
            if (acc >= 1.0) {
                acc = 0;
                playBeat();
            }
        }

        /** 播放当前拍的全部音符并推进。 */
        void playBeat() {
            if (beat >= totalTicks) {
                finish();
                return;
            }
            int b = beat;
            Location base = player.getLocation();
            for (Song.Track t : song.tracks) {
                org.bukkit.Instrument instrument = resolveInstrument(t.instrument);
                for (Song.NoteEvent n : t.notes) {
                    if (n.startTick == b) {
                        Note note = noteOf(n.semitone);
                        if (instrument != null) {
                            player.playNote(base, instrument, note);
                        }
                    }
                }
            }
            beat++;
        }

        void finish() {
            if (task != null) {
                task.cancel();
            }
        }
    }

    private static org.bukkit.Instrument resolveInstrument(String name) {
        try {
            return org.bukkit.Instrument.valueOf(name);
        } catch (IllegalArgumentException e) {
            return org.bukkit.Instrument.PIANO;
        }
    }

    /** 半音 0..24 → Bukkit Note（F#3..F#5）。 */
    public static Note noteOf(int semitone) {
        int s = Math.max(0, Math.min(24, semitone));
        int octave = s / 12;
        Note.Tone tone = Note.Tone.values()[s % 12];
        return new Note(octave, tone, false);
    }
}
