package com.nbmusic.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 音符映射测试：音名→半音（F#3..F#5 范围）与材质→乐器。
 */
class NoteMapperTest {

    @Test
    void semitone_anchorNotes() {
        assertEquals(0, NoteMapper.semitone("F#3"), "F#3 = 最低音 0");
        assertEquals(6, NoteMapper.semitone("C4"), "C4 = 6");
        assertEquals(12, NoteMapper.semitone("F#4"), "F#4 = 12");
        assertEquals(15, NoteMapper.semitone("A4"), "A4 = 15（中央 A）");
        assertEquals(18, NoteMapper.semitone("C5"), "C5 = 18");
        assertEquals(24, NoteMapper.semitone("F#5"), "F#5 = 最高音 24");
    }

    @Test
    void semitone_stepwise() {
        assertEquals(1, NoteMapper.semitone("G3"), "G3 = 1");
        assertEquals(2, NoteMapper.semitone("G#3"), "G#3 = 2");
        assertEquals(2, NoteMapper.semitone("Ab3"), "Ab3 = 2（等音）");
        assertEquals(13, NoteMapper.semitone("G4"), "G4 = 13");
        assertEquals(14, NoteMapper.semitone("G#4"), "G#4 = 14");
    }

    @Test
    void semitone_clampsOutOfRange() {
        assertEquals(0, NoteMapper.semitone("C2"), "过低夹到 F#3");
        assertEquals(0, NoteMapper.semitone("A2"), "过低夹到 F#3");
        assertEquals(24, NoteMapper.semitone("C7"), "过高夹到 F#5");
        assertEquals(24, NoteMapper.semitone("G6"), "过高夹到 F#5");
        assertEquals(24, NoteMapper.semitone("G5"), "G5 = 25 -> 24");
    }

    @Test
    void semitone_invalidDefaultsToFsharp() {
        assertEquals(0, NoteMapper.semitone("xyz"), "非法音名 -> 0");
        assertEquals(0, NoteMapper.semitone(null), "null -> 0");
    }

    @Test
    void nameOf_roundTrip() {
        for (int s = 0; s <= 24; s += 3) {
            String name = NoteMapper.nameOf(s);
            assertEquals(s, NoteMapper.semitone(name), "roundtrip " + name);
        }
        assertEquals("F#3", NoteMapper.nameOf(0));
        assertEquals("C4", NoteMapper.nameOf(6));
        assertEquals("A4", NoteMapper.nameOf(15));
        assertEquals("F#5", NoteMapper.nameOf(24));
    }

    @Test
    void instrument_materialMapping() {
        assertEquals("GUITAR", NoteMapper.instrumentFor("WOOL"), "羊毛 = 吉他");
        assertEquals("GUITAR", NoteMapper.instrumentFor("RED_WOOL"), "红羊毛 = 吉他");
        assertEquals("STICKS", NoteMapper.instrumentFor("GLASS"), "玻璃 = 击鼓声");
        assertEquals("BASS_DRUM", NoteMapper.instrumentFor("STONE"), "石头 = 底鼓");
        assertEquals("SNARE_DRUM", NoteMapper.instrumentFor("SAND"), "沙 = 小军鼓");
        assertEquals("BELL", NoteMapper.instrumentFor("GOLD_BLOCK"), "金块 = 铃铛");
        assertEquals("FLUTE", NoteMapper.instrumentFor("CLAY"), "黏土 = 长笛");
        assertEquals("CHIME", NoteMapper.instrumentFor("PACKED_ICE"), "浮冰 = 风铃");
        assertEquals("XYLOPHONE", NoteMapper.instrumentFor("BONE_BLOCK"), "骨块 = 木琴");
        assertEquals("IRON_XYLOPHONE", NoteMapper.instrumentFor("IRON_BLOCK"), "铁块 = 铁琴");
        assertEquals("COW_BELL", NoteMapper.instrumentFor("SOUL_SAND"), "灵魂沙 = 牛铃");
        assertEquals("DIDGERIDOO", NoteMapper.instrumentFor("PUMPKIN"), "南瓜 = 迪吉里杜管");
        assertEquals("BIT", NoteMapper.instrumentFor("EMERALD_BLOCK"), "绿宝石 = 方波");
        assertEquals("BANJO", NoteMapper.instrumentFor("HAY_BLOCK"), "干草块 = 班卓琴");
        assertEquals("PLING", NoteMapper.instrumentFor("GLOWSTONE"), "萤石 = 电子音");
        assertEquals("BASS_GUITAR", NoteMapper.instrumentFor("OAK_PLANKS"), "木板 = 贝斯");
        assertEquals("PIANO", NoteMapper.instrumentFor("QUARTZ_BLOCK"), "石英 = 钢琴");
    }

    @Test
    void instrument_directEnumAndUnknown() {
        assertEquals("GUITAR", NoteMapper.instrumentFor("GUITAR"), "枚举名原样");
        assertEquals("CHIME", NoteMapper.instrumentFor("chime"), "大小写不敏感");
        assertEquals("PIANO", NoteMapper.instrumentFor("bogus_material"), "未知 -> 钢琴");
        assertEquals("PIANO", NoteMapper.instrumentFor(null), "null -> 钢琴");
    }
}
