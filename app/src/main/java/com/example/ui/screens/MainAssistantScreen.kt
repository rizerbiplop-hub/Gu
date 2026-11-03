package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RivonOrbState
import com.example.ui.RivonViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.ActionCardView
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
import java.util.Calendar

@Composable
fun MainAssistantScreen(viewModel: RivonViewModel) {
    val orbState by viewModel.orbState.collectAsState()
    val audioRms by viewModel.audioRmsLevel.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val transcribedText by viewModel.transcribedSpeech.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val voiceEngineType by viewModel.voiceEngineType.collectAsState()
    val wakeWordEnabled by viewModel.wakeWordEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val pendingAction by viewModel.pendingActionConfirmation.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lastMessage = messages.lastOrNull()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Welcome Back"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        RivonDeepBlack,
                        RivonMidnight,
                        Color(0xFF040914)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(RivonCyan, RivonElectricBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RIVON AI",
                            color = RivonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Personal Intelligence",
                            color = RivonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.APP_MANAGER) },
                        modifier = Modifier.testTag("nav_app_manager")
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = "App Manager", tint = RivonSoftCyan)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.CHAT) },
                        modifier = Modifier.testTag("nav_chat")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = RivonSoftCyan)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.PERMISSIONS) },
                        modifier = Modifier.testTag("nav_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = RivonSoftCyan)
                    }
                }
            }

            // Status badges row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RivonStatusBadge(
                    label = selectedModel.split("/").lastOrNull()?.replace(":free", " (Free)") ?: "OpenRouter",
                    icon = Icons.Default.Psychology,
                    color = RivonCyan,
                    onClick = { viewModel.navigateTo(ScreenDestination.AI_MODELS) }
                )
                RivonStatusBadge(
                    label = if (voiceEngineType.name == "ELEVENLABS") "ElevenLabs Voice" else "Android TTS",
                    icon = Icons.Default.RecordVoiceOver,
                    color = RivonElectricBlue,
                    onClick = { viewModel.navigateTo(ScreenDestination.ELEVENLABS_VOICES) }
                )
                RivonStatusBadge(
                    label = if (wakeWordEnabled) "Wake Word: ON" else "Wake Word: OFF",
                    icon = if (wakeWordEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    color = if (wakeWordEnabled) RivonNeonGreen else RivonTextMuted,
                    onClick = { viewModel.navigateTo(ScreenDestination.WAKE_WORD) }
                )
            }

            // Notification / Status Banner
            AnimatedVisibility(visible = statusBanner != null) {
                statusBanner?.let { banner ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.clearStatusBanner() },
                        color = RivonSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RivonCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = banner,
                                color = RivonCyan,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Scrollable Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Greeting & State indicator
                Text(
                    text = "$greeting, $userName",
                    color = RivonTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (orbState) {
                        RivonOrbState.IDLE -> "How can I assist your device today?"
                        RivonOrbState.LISTENING -> "Listening to your voice..."
                        RivonOrbState.THINKING -> "Processing intelligence..."
                        RivonOrbState.SPEAKING -> "Synthesizing response..."
                        RivonOrbState.EXECUTING -> "Executing Android action..."
                        RivonOrbState.ERROR -> "Attention required"
                    },
                    color = RivonTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Central Orb
                RivonOrb(
                    state = orbState,
                    rmsAudioLevel = audioRms,
                    size = 230.dp,
                    onClick = {
                        viewModel.toggleVoiceListening()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Active waveform if speaking or listening
                if (isSpeaking || isListening) {
                    AudioWaveformIndicator(isSpeaking = isSpeaking)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Live speech transcript or last assistant answer
                if (isListening && transcribedText.isNotBlank()) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        borderColor = RivonCyan
                    ) {
                        Text(
                            text = "\"$transcribedText\"",
                            color = RivonCyan,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (lastMessage != null) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        borderColor = RivonGlassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (lastMessage.sender == com.example.model.MessageSender.USER) "You:" else "RIVON:",
                                color = if (lastMessage.sender == com.example.model.MessageSender.USER) RivonSoftCyan else RivonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastMessage.text,
                                color = RivonTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Pending Action Confirmation (if any)
                if (pendingAction != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ActionCardView(
                        planned = pendingAction!!,
                        onConfirm = { viewModel.confirmPendingAction(pendingAction!!) },
                        onCancel = { viewModel.cancelPendingAction(pendingAction!!) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Chips
                Text(
                    text = "QUICK COMMANDS",
                    color = RivonTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val quickActions = listOf(
                    "Open Instagram",
                    "Open YouTube and search relaxing music",
                    "Message Rahul on WhatsApp",
                    "Play Top Hits on Spotify",
                    "Set volume to 50 percent",
                    "Set alarm for 6 AM",
                    "Open Camera",
                    "Open Wi-Fi Settings"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (action in quickActions) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.processUserInput(action)
                                },
                            color = RivonSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RivonBorder),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = action,
                                color = RivonTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = RivonSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RivonGlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleVoiceListening() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isListening) RivonCyan else RivonElectricBlue,
                                CircleShape
                            )
                            .testTag("main_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = RivonDeepBlack
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_text_input"),
                        placeholder = {
                            Text(
                                "Ask RIVON or control apps...",
                                color = RivonTextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = RivonTextPrimary,
                            unfocusedTextColor = RivonTextPrimary,
                            cursorColor = RivonCyan
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.processUserInput(textInput)
                                textInput = ""
                            }
                        })
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.processUserInput(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier.testTag("send_button")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) RivonCyan else RivonTextMuted
                        )
                    }
                }
            }
        }
    }
}
