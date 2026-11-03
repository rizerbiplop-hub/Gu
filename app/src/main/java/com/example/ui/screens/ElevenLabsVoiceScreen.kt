package com.example.ui.screens

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceEngineType
import com.example.model.VoiceModel
import com.example.ui.RivonViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ElevenLabsVoiceScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val coroutineScope = rememberCoroutineScope()
    val availableVoices by viewModel.availableVoices.collectAsState()
    val selectedVoiceId by viewModel.voiceId.collectAsState()
    val currentEngine by viewModel.voiceEngineType.collectAsState()
    val voiceSpeed by viewModel.voiceSpeed.collectAsState()

    var playingPreviewVoiceId by remember { mutableStateOf<String?>(null) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.release()
            previewPlayer = null
        }
    }

    fun playPreview(url: String?, voiceId: String) {
        if (url.isNullOrBlank()) {
            // Speak test sample using voice engine
            viewModel.voiceEngine.speak(
                text = "Hello! I am RIVON, your personal Android intelligence assistant.",
                preferredEngine = currentEngine,
                apiKey = viewModel.elevenLabsKey.value,
                voiceId = voiceId,
                speed = voiceSpeed
            )
            return
        }

        try {
            previewPlayer?.release()
            previewPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    playingPreviewVoiceId = voiceId
                    start()
                }
                setOnCompletionListener {
                    playingPreviewVoiceId = null
                }
                setOnErrorListener { _, _, _ ->
                    playingPreviewVoiceId = null
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            playingPreviewVoiceId = null
        }
    }

    fun stopPreview() {
        previewPlayer?.stop()
        previewPlayer?.release()
        previewPlayer = null
        playingPreviewVoiceId = null
        viewModel.stopSpeaking()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivonDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RivonMidnight,
            border = androidx.compose.foundation.BorderStroke(1.dp, RivonBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.popBack() },
                    modifier = Modifier.testTag("voices_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RivonTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Voice Synthesis Engine",
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "ElevenLabs Neural Voice & Local TTS",
                        color = RivonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Engine Selector Tabs
        val engines = listOf(VoiceEngineType.ELEVENLABS, VoiceEngineType.ANDROID_TTS)
        val selectedIndex = engines.indexOf(currentEngine).coerceAtLeast(0)

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = RivonMidnight,
            contentColor = RivonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = RivonCyan
                )
            }
        ) {
            Tab(
                selected = currentEngine == VoiceEngineType.ELEVENLABS,
                onClick = {
                    coroutineScope.launch {
                        viewModel.preferences.setVoiceEngine(VoiceEngineType.ELEVENLABS)
                    }
                },
                text = {
                    Text(
                        "ElevenLabs Neural AI",
                        color = if (currentEngine == VoiceEngineType.ELEVENLABS) RivonCyan else RivonTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = currentEngine == VoiceEngineType.ANDROID_TTS,
                onClick = {
                    coroutineScope.launch {
                        viewModel.preferences.setVoiceEngine(VoiceEngineType.ANDROID_TTS)
                    }
                },
                text = {
                    Text(
                        "Android TTS (Fallback)",
                        color = if (currentEngine == VoiceEngineType.ANDROID_TTS) RivonCyan else RivonTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            )
        }

        // Speed Control Card
        Column(modifier = Modifier.padding(16.dp)) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speech Rate Speed",
                            color = RivonTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "%.1fx".format(voiceSpeed),
                            color = RivonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { speed ->
                            coroutineScope.launch {
                                viewModel.preferences.setVoiceSpeed(speed)
                            }
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = RivonCyan,
                            activeTrackColor = RivonElectricBlue,
                            inactiveTrackColor = RivonBorder
                        ),
                        modifier = Modifier.testTag("voice_speed_slider")
                    )
                }
            }
        }

        // Voice List
        Text(
            text = "AVAILABLE VOICES",
            color = RivonTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(availableVoices, key = { it.voiceId }) { voice ->
                val isSelected = voice.voiceId == selectedVoiceId
                val isPlaying = playingPreviewVoiceId == voice.voiceId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            coroutineScope.launch {
                                viewModel.preferences.setVoiceId(voice.voiceId)
                            }
                        }
                        .border(
                            1.dp,
                            if (isSelected) RivonCyan else RivonBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .testTag("voice_item_${voice.voiceId}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RivonSurfaceElevated else RivonMidnight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(RivonCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = RivonDeepBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = voice.name,
                                        color = if (isSelected) RivonCyan else RivonTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RivonStatusBadge(
                                        label = voice.gender,
                                        color = RivonElectricBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = voice.description.ifBlank { "${voice.accent} accent • ID: ${voice.voiceId}" },
                                    color = RivonTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        // Preview Audio Button
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    stopPreview()
                                } else {
                                    playPreview(voice.previewUrl, voice.voiceId)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(RivonSurface, CircleShape)
                                .testTag("preview_voice_${voice.voiceId}")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Preview Voice",
                                tint = RivonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
