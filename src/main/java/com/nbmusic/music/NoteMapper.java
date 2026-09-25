package com.nbmusic.music;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 音符映射（纯逻辑，可单元测试）：
 * <ul>
 *   <li>音名 → 音符盒半音（0..24，F#3..F#5，C4=6 / A4=15 / F#5=24）</li>
 *   <li>材质/乐器别名 → Bukkit Instrument 名（如 WOOL→GUITAR、GLASS→STICKS）</li>
 * </ul>
 * 音符盒在不同材质上发出的音色不同——「材质=音色」正是本插件的核心。
 */
public final class NoteMapper {

    private NoteMapper() {
    }

    /** C 本位音高类：C=0, C#=1, D=2, D#=3, E=4, F=5, F#=6, G=7, G#=8, A=9, A#=10, B=11。 */
    private static final Map<String, Integer> PITCH_CLASS = pitchClass();

    private static Map<String, Integer> pitchClass() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("c", 0);
        m.put("c#", 1);
        m.put("db", 1);
        m.put("d", 2);
        m.put("d#", 3);
        m.put("eb", 3);
        m.put("e", 4);
        m.put("f", 5);
        m.put("f#", 6);
        m.put("gb", 6);
        m.put("g", 7);
        m.put("g#", 8);
        m.put("ab", 8);
        m.put("a", 9);
        m.put("a#", 10);
        m.put("bb", 10);
        m.put("b", 11);
        return m;
    }

    /**
     * 音名 → 半音序号（0..24，越界夹取）。
     * 公式：semitone = 12×(octave-3) + (pc-6)，其中 pc 为 C 本位音高类。
     */
    public static int semitone(String noteName) {
        if (noteName == null) {
            return 0;
        }
        String s = noteName.trim().toLowerCase(Locale.ROOT);
        // 分离字母部分与八度数字
        int i = 0;
        while (i < s.length() && (Character.isLetter(s.charAt(i)) || s.charAt(i) == '#')) {
            i++;
        }
        String pitch = s.substring(0, i);
        int octave;
        try {
            octave = i < s.length() ? Integer.parseInt(s.substring(i)) : 4;
        } catch (NumberFormatException e) {
            octave = 4;
        }
        Integer pc = PITCH_CLASS.get(pitch);
        if (pc == null) {
            return 0;
        }
        return clamp(12 * (octave - 3) + (pc - 6), 0, 24);
    }

    /** 半音序号 → 音名（F#3..F#5；调试展示用）。八度在 C 处递增（s=6→C4，s=18→C5）。 */
    public static String nameOf(int semitone) {
        int s = clamp(semitone, 0, 24);
        int octave = 3 + (s + 6) / 12;
        int idx = s % 12;
        // 半音内音名：F#,G,G#,A,A#,B,C,C#,D,D#,E,F
        String[] names = {"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"};
        return names[idx] + octave;
    }

    /** 全部合法 Instrument 枚举名（纯字符串集合，避免测试环境加载 Bukkit 枚举）。 */
    private static final java.util.Set<String> INSTRUMENTS = java.util.Set.of(
            "PIANO", "BASS_GUITAR", "BASS_DRUM", "SNARE_DRUM", "STICKS", "GUITAR",
            "FLUTE", "BELL", "CHIME", "XYLOPHONE", "IRON_XYLOPHONE", "COW_BELL",
            "DIDGERIDOO", "BIT", "BANJO", "PLING", "TRUMPET", "HARP", "PIGLIN", "ZOMBIE");

    /** 常见材质名 → Instrument 枚举名（不含 CUSTOM_HEAD 等无默认音色）。 */
    public static String instrumentFor(String key) {
        if (key == null) {
            return "PIANO";
        }
        String k = key.trim().toUpperCase(Locale.ROOT);
        // 直接是 Instrument 枚举名则原样返回
        if (INSTRUMENTS.contains(k)) {
            return k;
        }
        // 材质/别名 → 乐器
        switch (k) {
            case "WOOL", "WHITE_WOOL", "RED_WOOL", "GUITAR" -> {
                return "GUITAR";
            }
            case "OAK_PLANKS", "PLANKS", "LOG", "BASS_GUITAR" -> {
                return "BASS_GUITAR";
            }
            case "GLASS", "GLASS_PANE", "STICKS", "HAT" -> {
                return "STICKS";
            }
            case "STONE", "COBBLESTONE", "STONE_BRICK", "DEEPSLATE", "BASS_DRUM", "BASEDRUM" -> {
                return "BASS_DRUM";
            }
            case "SAND", "GRAVEL", "SANDSTONE", "CONCRETE_POWDER", "SNARE_DRUM", "SNARE" -> {
                return "SNARE_DRUM";
            }
            case "GOLD_BLOCK", "BELL" -> {
                return "BELL";
            }
            case "CLAY", "FLUTE" -> {
                return "FLUTE";
            }
            case "PACKED_ICE", "CHIME" -> {
                return "CHIME";
            }
            case "BONE_BLOCK", "XYLOPHONE" -> {
                return "XYLOPHONE";
            }
            case "IRON_BLOCK", "IRON_XYLOPHONE" -> {
                return "IRON_XYLOPHONE";
            }
            case "SOUL_SAND", "COW_BELL" -> {
                return "COW_BELL";
            }
            case "PUMPKIN", "DIDGERIDOO" -> {
                return "DIDGERIDOO";
            }
            case "EMERALD_BLOCK", "BIT" -> {
                return "BIT";
            }
            case "HAY_BLOCK", "BANJO" -> {
                return "BANJO";
            }
            case "GLOWSTONE", "PLING" -> {
                return "PLING";
            }
            case "COPPER_BLOCK", "TRUMPET" -> {
                return "TRUMPET";
            }
            case "QUARTZ_BLOCK", "PIANO", "HARP" -> {
                return "PIANO";
            }
            default -> {
                return "PIANO";
            }
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
