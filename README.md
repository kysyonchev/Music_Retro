<p align="center">
  <img src="docs/banner.png" alt="Music Retro" width="720">
</p>

<h1 align="center">Music Retro</h1>

<p align="center">
  <b>Endless music-video channels for your Android TV, one decade at a time.</b><br>
  70s · 80s · 90s · 00s &nbsp;—&nbsp; shuffle, countdowns, and old-school commercial breaks.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%20TV-3DDC84">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF">
  <img alt="Version" src="https://img.shields.io/badge/version-1.0.1-FF355E">
</p>

---

## What is it?

Music Retro turns your TV into a retro music channel. Pick a decade (or let it play everything),
sit back, and it plays YouTube music videos back to back. Every few songs it cuts to a
"commercial break", just like TV used to. No accounts, no menus to fight with: it is built for a remote.

## Features

- **Play Live** – a never-ending shuffle across all four decades.
- **Decade channels** – 70s, 80s, 90s or 00s only.
- **Top 30 Countdown** – 30 random songs from a decade, played from #30 down to #1 with a rank badge.
- **Commercial breaks** – a break after every 4 songs, using vintage commercials you choose.
- **Smart shuffle** – every video plays once before any repeats, and never twice in a row.
- **Skips broken videos** – blocked or removed videos are skipped automatically.
- **Lower-third info** – song title and artist appear for a few seconds at the start of each clip.
- **TV-first** – D-pad navigation, TV launcher banner, screen stays awake while playing.

## How it works

```
 clips.json ──► ClipRepo ──► ShuffleBag ──► Channel (song, song, song, song, AD, ...)
                                                │
 Home screen ──► Live / Decade / Top 30 ────────┘
                                                ▼
                                    YouTubePlayer (IFrame API in a WebView)
```

| File | Role |
|------|------|
| `MainActivity.kt` | All screens (Home, Top 30 picker, Player) and remote-control navigation |
| `ClipData.kt` | `Clip` model, `ClipRepo` (loads `clips.json`), `ShuffleBag`, `Channel`, `Mode` |
| `YouTubePlayer.kt` | Plays videos with the official YouTube IFrame Player API inside a WebView |
| `assets/clips.json` | The song and commercial list |

## Requirements

- Android Studio (recent version) with the Android SDK installed
- An Android TV device or emulator (tested on a Sony Bravia 4K, Android 12)
- Internet connection on the TV
- Python 3 (only to regenerate the song list)

Gradle dependency needed for the player (add to `app/build.gradle(.kts)`):

```kotlin
implementation("androidx.webkit:webkit:1.12.1")
```

## Getting started

1. Clone the repo and open it in Android Studio.
2. Let Gradle sync.
3. Make sure `app/src/main/assets/clips.json` exists (see **The song list** below).
4. Run on an Android TV emulator or a TV, or build an APK:

   ```bash
   ./gradlew assembleDebug
   # APK: app/build/outputs/apk/debug/app-debug.apk
   ```

### Installing on a real TV

**Over the network (easiest):** enable *Developer options → Network/ADB debugging* on the TV, then:

```bash
adb connect <TV-IP-ADDRESS>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**With a USB stick:** copy the APK to the stick, plug it into the TV, and open it with a file
manager app. You may need to allow *Install unknown apps* for that file manager.

If the new version doesn't seem to change, uninstall the old app first, then install again.

## The song list (`clips.json`)

Each entry looks like this:

```json
{"id":"hTWKbfoikeg","title":"Smells Like Teen Spirit","artist":"Nirvana","decade":"90s","type":"song"}
```

| Field | Meaning |
|-------|---------|
| `id` | The YouTube video ID (the part after `v=` in the URL) |
| `title`, `artist` | Shown in the lower third |
| `decade` | `70s`, `80s`, `90s` or `00s` |
| `type` | `song` or `ad` (a commercial) |

### Generate it automatically

`tools/build_clips.py` builds a list from the
[akbargherbal/youtube-music-hits](https://huggingface.co/datasets/akbargherbal/youtube-music-hits)
dataset: the most-viewed music videos of each decade, with duplicate uploads removed.

```bash
pip install pandas pyarrow datasets
python tools/build_clips.py                  # 400 songs per decade, 5 commercial slots each
python tools/build_clips.py --per-decade 500 --ads 8
```

Copy the resulting `clips.json` into `app/src/main/assets/`. Extra songs beyond what you need are
a useful buffer, because some videos block embedding and get skipped.

### Add commercials

The script creates placeholder commercial entries such as `REPLACE_70s_AD1`.
Replace each placeholder `id` with a real YouTube video ID, and give every commercial its own ID.
Placeholders that still start with `REPLACE` are ignored, so the app works without them.

To change how often breaks happen, edit `adEvery` in `Channel` (`ClipData.kt`, default `4`).

## Remote control

| Button | Action |
|--------|--------|
| D-pad | Move between tiles |
| OK / Select | Choose |
| Back | Return to the home screen |

## Troubleshooting

| Problem | What to try |
|---------|-------------|
| Audio but **black screen** | On the Sony Bravia used for testing, a WebView placed inside Compose's `AndroidView` played sound without showing the picture. This app adds the WebView as a normal view *behind* the Compose UI, and the player screen's background is transparent so the video shows through. Keep that setup if you modify the layout. |
| "Skipped a video (YouTube error 101 or 150)" | The video owner blocks embedding. The app skips it; replace the ID if you like. |
| YouTube error 153 | YouTube rejected the player request. The player sets an https origin (`APP_ORIGIN` in `YouTubePlayer.kt`); keep it. |
| "Couldn't play any videos" | 8 videos in a row failed. Check the TV's internet connection and the IDs in `clips.json`. |
| Nothing plays at all | Update **Android System WebView** from the TV's Play Store. |
| Need more detail | Run from Android Studio and filter Logcat by the tag `RETROTV`. |

The player also asks YouTube to serve H.264 video, which is the safest format on TVs.

## Project layout

```
app/src/main/
├── AndroidManifest.xml
├── assets/clips.json
├── java/com/retro/tv/
│   ├── MainActivity.kt
│   ├── ClipData.kt
│   └── YouTubePlayer.kt
└── res/                    # launcher icon + TV banner
tools/build_clips.py        # builds clips.json
docs/                       # images used in this README
```

The internal package name is still `com.retro.tv`; only the visible app name changed to **Music Retro**.

## Legal

Music Retro is an independent hobby project. It is **not affiliated with or endorsed by MTV, Paramount,
YouTube or Google**. Videos are streamed from YouTube through its official embedded player
(IFrame Player API) and are never downloaded or stored by the app. All videos and music belong to their
owners and are subject to the [YouTube Terms of Service](https://www.youtube.com/t/terms). Choose
commercials and songs you are comfortable embedding.

## Credits

- Song metadata: [akbargherbal/youtube-music-hits](https://huggingface.co/datasets/akbargherbal/youtube-music-hits)
  (MIT license), derived from Wikidata and YouTube view counts.
- Built with Kotlin and Jetpack Compose.

## License

Add a `LICENSE` file of your choice (for example MIT) before publishing if you want others to reuse the code.
