package com.nbmusic.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 谱面解析测试：DSL → Song 模型。
 */
class SongParserTest {

    private Song parse(String text) {
        StringBuilder cause = new StringBuilder();
        Song s = SongParser.parse(text, cause);
        assertNotNull(s, "解析失败: " + cause);
        return s;
    }

    private static final String TWINKLE = """
            name 小星星
            tempo 100
            volume 8
            # 主旋律
            track flute
            | C4 C4 G4 G4 A4 A4@2 | G4@2 F4 F4 E4 E4 D4 D4@2 |
            track bass BASS_GUITAR
            | A3@2 A3@2 B3@2 B3@2 |
            """;

    @Test
    void parsesNameTempoVolume() {
        Song s = parse(TWINKLE);
        assertEquals("小星星", s.name);
        assertEquals(100, s.tempo);
        assertEquals(8, s.volume);
        assertEquals(12, s.ticksPerBeat, "100BPM -> 每拍 12 tick");
    }

    @Test
    void parsesTracksAndInstruments() {
        Song s = parse(TWINKLE);
        assertEquals(2, s.tracks.size());
        assertEquals("FLUTE", s.tracks.get(0).instrument, "flute 别名解析为 FLUTE");
        assertEquals("BASS_GUITAR", s.tracks.get(1).instrument, "材质名解析为乐器");
        assertEquals("bass", s.tracks.get(1).name, "双参 track 名称");
    }

    @Test
    void notePlacementOnTickAxis() {
        Song s = parse(TWINKLE);
        Song.Track mel = s.tracks.get(0);
        // 第 1 小节: C4 C4 G4 G4 A4 A4@2 -> 6 拍 = 72 tick；第 2 小节从 tick 72 开始
        assertEquals(6, mel.notes.get(0).semitone, "C4");
        assertEquals(0, mel.notes.get(0).startTick);
        assertEquals(12, mel.notes.get(1).startTick, "第二个 C4 在 1 拍后");
        assertEquals(48, mel.notes.get(4).startTick, "A4 在 4 拍后");
        // @2 时值：A4@2 占 2 拍
        Song.NoteEvent a4 = mel.notes.get(5);
        assertEquals(15, a4.semitone, "A4 = 15");
        assertEquals(60, a4.startTick, "第 6 个音符在 5 拍后");
        assertEquals(24, a4.durationTicks, "@2 时值 = 2 拍 × 12");
        // 第二小节第一个音符 G4@2 在 tick 84（第一小节 7 拍 × 12）
        Song.NoteEvent g4 = mel.notes.get(6);
        assertEquals(13, g4.semitone, "G4 = 13");
        assertEquals(84, g4.startTick, "第二小节从 84 tick 开始");
    }

    @Test
    void restsAdvanceCursor() {
        Song s = parse("""
                name t
                track flute
                | C4 . D4 . |
                """);
        Song.Track t = s.tracks.get(0);
        assertEquals(2, t.notes.size());
        assertEquals(0, t.notes.get(0).startTick);
        assertEquals(24, t.notes.get(1).startTick, "休止 1 拍后 D4 在 2 拍处");
    }

    @Test
    void totalTicksAndEventCount() {
        Song s = parse(TWINKLE);
        assertTrue(s.totalTicks() >= 96, "总时长 ≥ 两小节");
        assertEquals(6 + 7 + 4, s.eventCount(), "主旋律 13 + 低音 4");
    }

    @Test
    void rejectsNoteBeforeTrack() {
        StringBuilder cause = new StringBuilder();
        Song s = SongParser.parse("| C4 |", cause);
        assertNull(s, "track 之前的音符应拒绝");
        assertTrue(cause.length() > 0, "给出原因");
    }

    @Test
    void rejectsEmptyOrGarbage() {
        assertNull(SongParser.parse("", null));
        assertNull(SongParser.parse("track flute", null), "无音符应拒绝");
        assertNull(SongParser.parse("garbage line here", null), "无法识别行应拒绝");
    }

    @Test
    void defaultNameWhenMissing() {
        Song s = parse("track flute\n| C4 |");
        assertEquals("未命名", s.name);
        assertEquals(100, s.tempo, "默认 100 BPM");
        assertEquals(10, s.volume, "默认音量 10");
    }

    @Test
    void multiBarNotesChainAcrossBars() {
        Song s = parse("""
                name x
                track flute
                | C4 | C4 | C4 |
                """);
        Song.Track t = s.tracks.get(0);
        assertEquals(3, t.notes.size());
        assertEquals(0, t.notes.get(0).startTick);
        assertEquals(12, t.notes.get(1).startTick);
        assertEquals(24, t.notes.get(2).startTick);
    }

    @Test
    void sharpFlatNotesParse() {
        Song s = parse("name x\ntrack flute\n| F#4 Bb4 C#5 |");
        Song.Track t = s.tracks.get(0);
        assertEquals(12, t.notes.get(0).semitone, "F#4 = 12");
        assertEquals(16, t.notes.get(1).semitone, "Bb4 = 16");
        assertEquals(19, t.notes.get(2).semitone, "C#5 = 19");
    }
}
