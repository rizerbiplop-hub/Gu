package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageSender
import com.example.model.RivonOrbState
import com.example.ui.RivonViewModel
import com.example.ui.components.AudioWaveformIndicator
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonOrb
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonGlassBorder
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonNeonPurple
import com.example.ui.theme.RivonSoftCyan
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary

@Composable
fun VoiceConversationScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val orbState by viewModel.orbState.collectAsState()
    val audioRms by viewModel.audioRmsLevel.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val transcribedText by viewModel.transcribedSpeech.collectAsState()
    val continuousMode by viewModel.continuousConversationMode.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val voiceEngineType by viewModel.voiceEngineType.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    val lastRivonMessage = messages.lastOrNull { it.sender == MessageSender.RIVON }
    val lastUserMessage = messages.lastOrNull { it.sender == MessageSender.USER }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F2042),
                        RivonMidnight,
                        RivonDeepBlack
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.popBack() },
                    modifier = Modifier.testTag("voice_exit_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit Voice Mode",
                        tint = RivonTextSecondary
                    )
                }

                RivonStatusBadge(
                    label = if (continuousMode) "Speech-to-Speech: Continuous" else "Single Turn",
                    icon = Icons.Default.Sync,
                    color = if (continuousMode) RivonNeonGreen else RivonCyan,
                    onClick = { viewModel.toggleContinuousConversation() },
                    modifier = Modifier.testTag("toggle_continuous_mode")
                )

                RivonStatusBadge(
                    label = if (voiceEngineType.name == "ELEVENLABS") "ElevenLabs" else "Local TTS",
                    icon = Icons.Default.RecordVoiceOver,
                    color = RivonElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Title
            Text(
                text = when (orbState) {
                    RivonOrbState.IDLE -> if (continuousMode) "Ready for voice input" else "Tap mic to talk"
                    RivonOrbState.LISTENING -> "Listening to you..."
                    RivonOrbState.THINKING -> "RIVON is thinking..."
                    RivonOrbState.SPEAKING -> "RIVON is speaking..."
                    RivonOrbState.EXECUTING -> "Automating Android action..."
                    RivonOrbState.ERROR -> "Attention required"
                },
                color = when (orbState) {
                    RivonOrbState.LISTENING -> RivonCyan
                    RivonOrbState.SPEAKING -> RivonSoftCyan
                    RivonOrbState.EXECUTING -> RivonElectricBlue
                    else -> RivonTextPrimary
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Continuous neural speech synthesis loop active",
                color = RivonTextMuted,
                fontSize = 12.sp
            )

            // Central Area with Big Orb & HUD
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // The Big Responsive Orb
                RivonOrb(
                    state = orbState,
                    rmsAudioLevel = audioRms,
                    size = 280.dp,
                    onClick = {
                        viewModel.toggleVoiceListening()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Waveform indicator
                AudioWaveformIndicator(
                    isSpeaking = isSpeaking || isListening,
                    modifier = Modifier.size(height = 30.dp, width = 80.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // User speech transcription box
                if (isListening && transcribedText.isNotBlank()) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        borderColor = RivonCyan
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("YOU ARE SAYING:", color = RivonSoftCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"$transcribedText\"",
                                color = RivonTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (lastRivonMessage != null) {
                    // Assistant response box
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        borderColor = RivonGlassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("RIVON REPLIED:", color = RivonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                if (isSpeaking) {
                                    RivonStatusBadge(label = "SPEAKING", color = RivonCyan)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastRivonMessage.text,
                                color = RivonTextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick spoken triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val voiceTriggers = listOf(
                    "Turn on flashlight",
                    "Check battery percentage",
                    "What time is it?",
                    "Take a selfie",
                    "Open Instagram",
                    "Play relaxing music on Spotify",
                    "Calculate 15 percent of 650",
                    "Tell me a clever thought"
                )
                for (trigger in voiceTriggers) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.processUserInput(trigger)
                            },
                        color = RivonSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RivonBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = trigger,
                            color = RivonTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Big Mic Control Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Brush.radialGradient(listOf(RivonCyan, RivonElectricBlue))
                            else Brush.radialGradient(listOf(RivonElectricBlue, Color(0xFF1540A0)))
                        )
                        .clickable { viewModel.toggleVoiceListening() }
                        .testTag("voice_mode_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (isListening) RivonDeepBlack else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
