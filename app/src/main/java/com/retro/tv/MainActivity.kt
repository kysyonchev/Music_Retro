package com.retro.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class Screen { HOME, TOP30_PICK, PLAYER }

private data class Era(
    val title: String,      // must match "decade" values in clips.json: 70s, 80s, 90s, 00s
    val years: String,
    val subtitle: String,
    val accent: Color,
    val background: List<Color>
)

private val eras = listOf(
    Era("70s", "1970–1979", "DISCO • FUNK", Color(0xFFFFA34D), listOf(Color(0xFF542C20), Color(0xFF17100F))),
    Era("80s", "1980–1989", "NEW WAVE", Color(0xFFFF4FC8), listOf(Color(0xFF421B60), Color(0xFF11132B))),
    Era("90s", "1990–1999", "GRUNGE", Color(0xFF54E6D1), listOf(Color(0xFF123D43), Color(0xFF10191C))),
    Era("00s", "2000–2009", "Y2K • POP-PUNK", Color(0xFF7DA7FF), listOf(Color(0xFF172F63), Color(0xFF101522)))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        setContent { RetroApp() }
    }
}

@Composable
private fun RetroApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var mode by remember { mutableStateOf<Mode>(Mode.Live) }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    MaterialTheme {
        // On the player screen the background must be transparent so the video behind Compose shows.
        Box(Modifier.fillMaxSize().background(if (screen == Screen.PLAYER) Color.Transparent else Color(0xFF08090D))) {
            when (screen) {
                Screen.HOME -> HomeScreen(
                    onEra = { mode = Mode.DecadeChannel(it.title); screen = Screen.PLAYER },
                    onTop30 = { screen = Screen.TOP30_PICK },
                    onLive = { mode = Mode.Live; screen = Screen.PLAYER }
                )
                Screen.TOP30_PICK -> DecadePickScreen(
                    eyebrow = "THE COUNTDOWN",
                    title = "TOP 30 – PICK A DECADE",
                    onEra = { mode = Mode.Top30(it.title); screen = Screen.PLAYER },
                    onBack = { screen = Screen.HOME }
                )
                Screen.PLAYER -> PlaybackScreen(mode) { screen = Screen.HOME }
            }
        }
    }
}

/* ───────────────────────── HOME ───────────────────────── */

@Composable
private fun HomeScreen(onEra: (Era) -> Unit, onTop30: () -> Unit, onLive: () -> Unit) {
    val firstEraFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        firstEraFocus.requestFocus()
    }

    Row(
        Modifier.fillMaxSize().padding(horizontal = 58.dp, vertical = 38.dp),
        horizontalArrangement = Arrangement.spacedBy(42.dp)
    ) {
        Column(Modifier.width(245.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MUSIC", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
                Text("R E T R O", color = Color(0xFFB9F34A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Text("MUSIC NEVER\nGOES OUT OF STYLE.", color = Color.White, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("70s  ·  80s  ·  90s  ·  00s", color = Color(0xFF9A9BA5), fontSize = 13.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuTile("▶  PLAY LIVE", Color(0xFFB9F34A), true, onLive)
                MenuTile("TOP 30 COUNTDOWN", Color(0xFFFF355E), false, onTop30)
                Text("USE YOUR REMOTE TO SELECT", color = Color(0xFF686A75), fontSize = 10.sp)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CHOOSE YOUR DECADE", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("FOUR ERAS. COUNTLESS CLASSICS.", color = Color(0xFF858792), fontSize = 11.sp)
                }
                Text("ON AIR  ●", color = Color(0xFFFF355E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth().weight(1f).focusGroup(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                eras.forEachIndexed { index, era ->
                    EraCard(
                        era = era,
                        modifier = Modifier.weight(1f).then(if (index == 0) Modifier.focusRequester(firstEraFocus) else Modifier)
                    ) { onEra(era) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MTV RETRO  •  VERSION 1.0.1", color = Color(0xFF555762), fontSize = 10.sp)
            }
        }
    }
}

/* ───────────────────── TOP 30 DECADE PICKER ───────────────────── */

@Composable
private fun DecadePickScreen(eyebrow: String, title: String, onEra: (Era) -> Unit, onBack: () -> Unit) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100)
        firstFocus.requestFocus()
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 58.dp, vertical = 38.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(eyebrow, color = Color(0xFFFF355E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("A NEW RANDOM LIST EVERY TIME.", color = Color(0xFF858792), fontSize = 11.sp)
        }
        Row(Modifier.fillMaxWidth().weight(1f).focusGroup(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            eras.forEachIndexed { index, era ->
                EraCard(
                    era = era,
                    modifier = Modifier.weight(1f).then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier)
                ) { onEra(era) }
            }
        }
        Text("PRESS BACK TO RETURN", color = Color(0xFF686A75), fontSize = 10.sp)
    }
}

@Composable
private fun EraCard(era: Era, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val number = era.title.dropLast(1)
    val letterS = era.title.takeLast(1)

    Box(
        modifier.fillMaxHeight().clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(era.background))
            .then(if (focused) Modifier.border(3.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(Modifier.align(Alignment.BottomStart)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(number, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                Text(letterS, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(y = 6.dp), softWrap = false, maxLines = 1)
            }
            Text(era.years, color = era.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
            Spacer(Modifier.height(12.dp))
            Text(era.subtitle, color = Color(0xFFD0D0D5), fontSize = 9.sp, softWrap = false, maxLines = 1)
        }
        Text("✦", Modifier.align(Alignment.TopEnd), color = era.accent, fontSize = 28.sp)
        AnimatedVisibility(focused, Modifier.align(Alignment.TopStart), enter = fadeIn(), exit = fadeOut()) {
            Text("SELECT  →", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MenuTile(label: String, accent: Color, filled: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier.fillMaxWidth().height(48.dp).clip(shape)
            .background(if (filled || focused) accent else Color(0xFF171922))
            .border(if (focused) 2.dp else 1.dp, if (focused) Color.White else Color(0xFF343642), shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(label, color = if (filled || focused) Color(0xFF08090D) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/* ───────────────────────── PLAYBACK ───────────────────────── */

private fun makeChannel(songs: List<Clip>, ads: List<Clip>): Channel? =
    if (songs.isEmpty()) null else Channel(songs, ads)

private const val MAX_CONSECUTIVE_ERRORS = 8

private fun errorHelp(code: Int) = when (code) {
    2 -> "The video ID is invalid."
    5 -> "HTML5 player error."
    100 -> "Video not found or private."
    101, 150 -> "The owner blocks embedding this video. Pick different videos."
    153 -> "YouTube rejected the player request (missing referer/origin)."
    else -> "Check your internet connection and the IDs in clips.json."
}

@Composable
private fun PlaybackScreen(mode: Mode, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val view = LocalView.current
    val all = remember { ClipRepo.load(ctx) }

    // Keep the TV awake while playing
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Top 30: 30 random songs from the chosen decade, played #30 -> #1
    val top30 = remember(mode) {
        (mode as? Mode.Top30)?.let { m ->
            all.filter { !it.isAd && it.decade == m.decade }.shuffled().take(30)
        }
    }

    // Endless channel for Live / Decade modes
    val channel = remember(mode) {
        when (mode) {
            is Mode.Live -> makeChannel(all.filter { !it.isAd }, all.filter { it.isAd })
            is Mode.DecadeChannel -> makeChannel(
                all.filter { !it.isAd && it.decade == mode.decade },
                all.filter { it.isAd && it.decade == mode.decade }
            )
            is Mode.Top30 -> null
        }
    }

    var rank by remember(mode) { mutableStateOf(top30?.size ?: 0) }
    var current by remember(mode) { mutableStateOf(top30?.firstOrNull() ?: channel?.next()) }
    var errorStreak by remember(mode) { mutableStateOf(0) }
    var lastError by remember(mode) { mutableStateOf(0) }

    val clip = current
    if (clip == null || errorStreak >= MAX_CONSECUTIVE_ERRORS) {
        MessageScreen(
            if (clip == null) "No videos found for this selection. Add entries to clips.json."
            else "Couldn't play any videos (last YouTube error: $lastError). ${errorHelp(lastError)}",
            onBack
        )
        return
    }

    fun advance() {
        if (top30 != null) {
            if (rank <= 1) { onBack(); return }
            rank--
            current = top30[top30.size - rank]
        } else {
            current = channel?.next()
        }
    }

    Box(Modifier.fillMaxSize()) {   // transparent: the WebView sits behind Compose
        YouTubePlayer(
            videoId = clip.id,
            onEnded = { errorStreak = 0; advance() },
            onError = { code -> lastError = code; errorStreak++; advance() },   // blocked / removed video: skip
            modifier = Modifier.fillMaxSize()
        )

        if (errorStreak > 0) {
            Text(
                "Skipped a video (YouTube error $lastError)",
                color = Color(0xFFFFB74D), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)
            )
        }

        key(clip.id, rank) {
            var numberShown by remember { mutableStateOf(false) }
            var infoShown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                numberShown = true
                infoShown = true
                delay(7000)
                infoShown = false
            }

            // Top 30 number box (top-left), stays for the whole song
            if (top30 != null) {
                AnimatedVisibility(
                    numberShown, Modifier.align(Alignment.TopStart).padding(32.dp),
                    enter = scaleIn() + fadeIn(), exit = fadeOut()
                ) {
                    Box(
                        Modifier.background(Color(0xFFFF355E), RoundedCornerShape(6.dp))
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text("$rank", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Lower third: title/artist for a few seconds at the start of each clip
            AnimatedVisibility(
                infoShown, Modifier.align(Alignment.BottomStart).padding(40.dp),
                enter = fadeIn(), exit = fadeOut()
            ) {
                Column(
                    Modifier.background(Color(0xAA000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    if (clip.isAd) {
                        Text("COMMERCIAL BREAK", color = Color(0xFFB9F34A), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    } else {
                        Text(clip.title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        if (clip.artist.isNotBlank()) {
                            Text(clip.artist, color = Color.LightGray, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageScreen(message: String, onBack: () -> Unit) {
    val backFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { delay(100); backFocus.requestFocus() }

    Column(
        Modifier.fillMaxSize().padding(70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        var focused by remember { mutableStateOf(false) }
        Box(
            Modifier.clip(RoundedCornerShape(5.dp))
                .background(if (focused) Color.White else Color(0xFF20222B))
                .focusRequester(backFocus)
                .onFocusChanged { focused = it.isFocused }
                .clickable(onClick = onBack)
                .padding(horizontal = 22.dp, vertical = 13.dp)
        ) {
            Text("←  BACK TO HOME", color = if (focused) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}