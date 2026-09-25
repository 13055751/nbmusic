# NBMusic —— 音符盒音乐插件

用 **Minecraft 原生音符盒（Note Block）音色**编写与播放音乐。灵感来自：
音符盒在不同材质上发出的声音不一样（羊毛=吉他、黏土=长笛、骨块=木琴、
金块=铃铛、浮冰=风铃……）——**材质即音色**，天然不缺音色库。

- **独立可用**：`/nbm play <歌曲>` 即可播放，无需放置任何音符盒方块
  （引擎直接用 `playNote` 播放原生音色）。
- **可联动 Eldoria**：Eldoria 的 `.efx` 脚本可用 `music <歌曲>` 指令，
  在施法特效中播放旋律（软依赖，未装本插件时自动跳过）。

## 快速开始

```text
/nbm play twinkle        # 播放内置《小星星》
/nbm play 欢乐颂 0.5     # 半速播放（>1 慢放）
/nbm list                # 歌曲列表
/nbm info 小星星         # 查看轨道/音色/时长
/nbm step 小星星         # 逐拍步进调试（/nbm next 前进一拍）
/nbm stop                # 停止
/nbm pause               # 暂停/继续
```

## 谱面格式（文本 DSL）

歌曲放在 `plugins/NBMusic/songs/*.txt`（首次启动自动落盘内置 3 首作参考）。
改谱后 `/nbm reload` 即生效。

```text
name 小星星              # 歌名
tempo 100                # 四分音符 BPM（默认 100）
volume 8                 # 音量 0-10（默认 10）
# 注释（# 在行首或前导空白后）
track flute              # 开始一轨：`track <乐器|材质>`（轨道名=乐器名）
| C4 C4 G4 G4 A4 A4@2 |  # 音符空格分隔；@N = 时值 N 拍；. = 休止
| G4@2 F4 F4 E4 E4 D4 D4@2 |
track bass BASS_GUITAR   # `track <轨道名> <乐器|材质>`（多轨同名乐器）
| A3@2 A3@2 B3@2 B3@2 |
```

- **音符**：音名+八度（`A4`/`F#4`/`Bb4`），范围 F#3..F#5（音符盒 25 音），越界自动夹取。
- **时值**：`@N` 后缀 = N 拍（默认 1 拍）；休止用 `.`（同样支持 `@N`）。
- **小节**：`|` 只作视觉分隔，不参与计时；音符按 tick 轴对齐（同刻发声 = 和弦）。
- **乐器**：材质名（`WOOL`/`GLASS`/`BONE_BLOCK`/`GOLD_BLOCK`…）或乐器名
  （`PIANO`/`GUITAR`/`FLUTE`/`CHIME`/`XYLOPHONE`/`BASS_GUITAR`…），未知一律回落钢琴。

### 材质 → 音色速查（音符盒机制）

| 材质 | 音色 | 材质 | 音色 |
|---|---|---|---|
| 羊毛 WOOL | 吉他 GUITAR | 黏土 CLAY | 长笛 FLUTE |
| 木板 PLANKS | 贝斯 BASS_GUITAR | 浮冰 PACKED_ICE | 风铃 CHIME |
| 玻璃 GLASS | 击鼓 STICKS | 骨块 BONE_BLOCK | 木琴 XYLOPHONE |
| 石头 STONE | 底鼓 BASS_DRUM | 铁块 IRON_BLOCK | 铁琴 IRON_XYLOPHONE |
| 沙 SAND | 小军鼓 SNARE_DRUM | 灵魂沙 SOUL_SAND | 牛铃 COW_BELL |
| 金块 GOLD_BLOCK | 铃铛 BELL | 南瓜 PUMPKIN | 迪吉里杜管 DIDGERIDOO |
| 绿宝石 EMERALD_BLOCK | 方波 BIT | 干草块 HAY_BLOCK | 班卓琴 BANJO |
| 萤石 GLOWSTONE | 电子音 PLING | 石英 QUARTZ_BLOCK | 钢琴 PIANO |

## Eldoria 联动

Eldoria 检测到本插件时，`.efx` 支持：

```text
music twinkle        # 播放歌曲（默认倍速）
music 欢乐颂 0.5     # 半速
```

实现：Eldoria 反射调用 `com.nbmusic.api.MusicApi.play(Plugin, Player, song, speed)`，
软依赖（plugin.yml softdepend: NBMusic），未装插件打警告跳过，不影响技能本身。

## 命令

| 命令 | 说明 |
|---|---|
| `/nbm play <歌曲> [倍率]` | 播放（倍率>1 慢放） |
| `/nbm stop` / `/nbm pause` | 停止 / 暂停-继续 |
| `/nbm step <歌曲>` | 逐拍步进调试（`/nbm next` 前进） |
| `/nbm list` / `/nbm info <歌曲>` | 列表 / 详情（轨道·音色·时长） |
| `/nbm reload` | 重载 songs/ 目录谱面（管理员） |
| `/nbm save <名称> <谱面...>` | 命令行保存一首歌（管理员） |
| `/nbm del <歌曲>` | 删除（内置歌可删，重启恢复） |

## 构建与测试

```bash
mvn package              # 产物 target/nbmusic-1.0.0.jar
mvn test                 # 17 项 JUnit（音名映射/谱面解析）
```

## 许可证

MIT License（[LICENSE](LICENSE)）。源码参考：Minecraft Wiki Note Block
（材质→乐器机制），实现为自研。
