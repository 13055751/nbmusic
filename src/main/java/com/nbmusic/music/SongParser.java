package com.nbmusic.music;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本谱解析器（纯逻辑，可单元测试）。
 * <p>
 * 谱面格式（按行）：
 * <pre>
 *   # 注释
 *   name 小星星
 *   tempo 120            # 四分音符 BPM（默认 100）
 *   volume 8             # 音量 0-10（默认 10）
 *   track flute          # 新建/切换轨道：`track &lt;乐器|材质&gt;` 或 `track &lt;名称&gt; &lt;乐器|材质&gt;`
 *   | C4 C4 G4 G4 A4 A4@2 | G4 F4 E4 D4 |
 * </pre>
 * 音符 token：音名+八度（A4 / F#4 / Bb3，范围 F#3..F#5，越界夹取）；
 * `@N` 后缀 = 时值 N 拍（默认 1 拍）；`.` = 休止（同样支持 @N）。
 * `|` 只作小节分隔（视觉），不参与计时。
 * </p>
 */
public final class SongParser {

    private SongParser() {
    }

    /** 解析谱面文本；失败返回 null 并给出原因（经 cause 输出）。 */
    public static Song parse(String text, StringBuilder cause) {
        if (text == null || text.isBlank()) {
            if (cause != null) {
                cause.append("谱面为空");
            }
            return null;
        }
        Song song = new Song();
        song.recalc();
        Song.Track current = null;
        int barTick = 0; // 当前轨道内部已累计 tick（跨小节累加，不重置）
        boolean anyNote = false;
        String[] lines = text.split("\n");
        for (int ln = 0; ln < lines.length; ln++) {
            String raw = lines[ln];
            // 注释：# 仅在行首或前导空白后才算（F#4 等升号音名里的 # 不是注释）
            int hash = raw.indexOf('#');
            if (hash >= 0 && (hash == 0 || Character.isWhitespace(raw.charAt(hash - 1)))) {
                raw = raw.substring(0, hash);
            }
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            String lower = line.toLowerCase(java.util.Locale.ROOT);
            try {
                if (lower.equals("|") || line.startsWith("|")) {
                    // 小节行：解析音符 token
                    String body = line.replace("|", "");
                    if (current == null) {
                        if (cause != null) {
                            cause.append("第 " + (ln + 1) + " 行: 音符出现在 track 之前");
                        }
                        return null;
                    }
                    int advance = parseTokens(body, current, barTick, song.ticksPerBeat);
                    barTick += advance;
                    if (advance > 0) {
                        anyNote = true;
                    }
                    continue;
                }
                if (lower.startsWith("name ")) {
                    song.name = line.substring(5).trim();
                    continue;
                }
                if (lower.startsWith("tempo ")) {
                    int t = parseInt(line.substring(6).trim(), 100);
                    song.tempo = Math.max(20, Math.min(400, t));
                    song.recalc();
                    continue;
                }
                if (lower.startsWith("volume ")) {
                    song.volume = Math.max(0, Math.min(10, parseInt(line.substring(7).trim(), 10)));
                    continue;
                }
                if (lower.startsWith("track ")) {
                    String[] parts = line.substring(6).trim().split("\\s+");
                    if (parts.length == 0 || parts[0].isEmpty()) {
                        continue;
                    }
                    if (parts.length == 1) {
                        String instr = NoteMapper.instrumentFor(parts[0]);
                        current = new Song.Track(instr, instr);
                    } else {
                        String instr = NoteMapper.instrumentFor(parts[1]);
                        current = new Song.Track(parts[0], instr);
                    }
                    song.tracks.add(current);
                    barTick = 0;
                    continue;
                }
                if (cause != null) {
                    cause.append("第 " + (ln + 1) + " 行无法识别: " + line);
                }
                return null;
            } catch (NumberFormatException e) {
                if (cause != null) {
                    cause.append("第 " + (ln + 1) + " 行数值错误: " + line);
                }
                return null;
            }
        }
        if (song.name == null || song.name.isBlank()) {
            song.name = "未命名";
        }
        if (!anyNote || song.tracks.isEmpty()) {
            if (cause != null) {
                cause.append("谱面没有音符");
            }
            return null;
        }
        return song;
    }

    /** 解析一串音符 token，返回本段占用的拍数。 */
    private static int parseTokens(String body, Song.Track track, int startTick, int ticksPerBeat) {
        int cursor = startTick;
        String[] tokens = body.trim().split("\\s+");
        for (String tok : tokens) {
            if (tok.isEmpty()) {
                continue;
            }
            // 时值：@N
            int durationBeats = 1;
            String noteTok = tok;
            int at = tok.indexOf('@');
            if (at >= 0) {
                durationBeats = Math.max(1, parseInt(tok.substring(at + 1), 1));
                noteTok = tok.substring(0, at);
            }
            int durTicks = durationBeats * ticksPerBeat;
            if (noteTok.equals(".") || noteTok.isEmpty()) {
                cursor += durTicks; // 休止
                continue;
            }
            int semitone = NoteMapper.semitone(noteTok);
            track.notes.add(new Song.NoteEvent(semitone, cursor, durTicks));
            cursor += durTicks;
        }
        return cursor - startTick;
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
