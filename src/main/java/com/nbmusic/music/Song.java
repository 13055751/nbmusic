package com.nbmusic.music;

import java.util.ArrayList;
import java.util.List;

/**
 * 一首歌曲（纯数据模型）：节拍 + 若干轨道，每条轨道是按时序排列的音符事件。
 * <p>
 * 播放时按 tick 轴对齐：音符 startTick 相同则同刻发声（和弦）。
 * 纯 Java 无 Bukkit 依赖（乐器以 Instrument 枚举名保存，播放时才解析）。
 * </p>
 */
public final class Song {

    public String name;
    /** 四分音符 BPM。 */
    public int tempo = 100;
    /** 音量 0-10（默认 10）。 */
    public int volume = 10;
    /** 每拍 tick 数 = 1200 / tempo。 */
    public int ticksPerBeat;
    public List<Track> tracks = new ArrayList<>();

    /** 一个音符事件。 */
    public static final class NoteEvent {
        /** 音符盒半音 0..24（F#3..F#5）。 */
        public int semitone;
        /** 起始 tick（全局轴）。 */
        public int startTick;
        /** 持续 tick（仅用于可视化/统计；播放是瞬态音）。 */
        public int durationTicks;

        public NoteEvent() {
        }

        public NoteEvent(int semitone, int startTick, int durationTicks) {
            this.semitone = semitone;
            this.startTick = startTick;
            this.durationTicks = durationTicks;
        }
    }

    /** 一条轨道：一个音色 + 一串音符。 */
    public static final class Track {
        public String name;
        /** Instrument 枚举名（见 NoteMapper.instrumentFor）。 */
        public String instrument;
        public List<NoteEvent> notes = new ArrayList<>();

        public Track() {
        }

        public Track(String name, String instrument) {
            this.name = name;
            this.instrument = instrument;
        }
    }

    public void recalc() {
        ticksPerBeat = Math.max(1, Math.round(1200.0f / Math.max(1, tempo)));
    }

    /** 总时长（tick）。 */
    public int totalTicks() {
        int max = 0;
        for (Track t : tracks) {
            for (NoteEvent n : t.notes) {
                max = Math.max(max, n.startTick + n.durationTicks);
            }
        }
        return max;
    }

    /** 事件总数。 */
    public int eventCount() {
        int n = 0;
        for (Track t : tracks) {
            n += t.notes.size();
        }
        return n;
    }
}
