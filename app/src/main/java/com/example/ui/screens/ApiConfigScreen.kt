package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RivonViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ApiConfigScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val coroutineScope = rememberCoroutineScope()
    val openRouterKey by viewModel.openRouterKey.collectAsState()
    val elevenLabsKey by viewModel.elevenLabsKey.collectAsState()

    var editOpenRouterKey by remember(openRouterKey) { mutableStateOf(openRouterKey) }
    var editElevenLabsKey by remember(elevenLabsKey) { mutableStateOf(elevenLabsKey) }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivonDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Bar
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
                    modifier = Modifier.testTag("api_config_back_button")
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
                        text = "API Key Configuration",
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "OpenRouter AI & ElevenLabs Voice Credentials",
                        color = RivonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = RivonNeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Encrypted Local Storage",
                            color = RivonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your API keys are stored strictly in private Android sandboxed DataStore on this device and are only transmitted to official OpenRouter and ElevenLabs HTTPS endpoints.",
                        color = RivonTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // OpenRouter API Key Field
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OpenRouter API Key",
                            color = RivonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        RivonStatusBadge(
                            label = if (editOpenRouterKey.isNotBlank()) "CONFIGURED" else "MISSING",
                            color = if (editOpenRouterKey.isNotBlank()) RivonNeonGreen else RivonTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editOpenRouterKey,
                        onValueChange = { editOpenRouterKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("openrouter_key_input"),
                        placeholder = { Text("sk-or-v1-...", color = RivonTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RivonCyan,
                            unfocusedBorderColor = RivonBorder,
                            focusedTextColor = RivonTextPrimary,
                            unfocusedTextColor = RivonTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.preferences.setOpenRouterKey(editOpenRouterKey)
                                testStatusMessage = "OpenRouter Key Saved!"
                                viewModel.refreshModelsAndVoices()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RivonElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save OpenRouter Key")
                    }
                }
            }

            // ElevenLabs API Key Field
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ElevenLabs API Key",
                            color = RivonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        RivonStatusBadge(
                            label = if (editElevenLabsKey.isNotBlank()) "CONFIGURED" else "MISSING",
                            color = if (editElevenLabsKey.isNotBlank()) RivonNeonGreen else RivonTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editElevenLabsKey,
                        onValueChange = { editElevenLabsKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("elevenlabs_key_input"),
                        placeholder = { Text("sk_...", color = RivonTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RivonCyan,
                            unfocusedBorderColor = RivonBorder,
                            focusedTextColor = RivonTextPrimary,
                            unfocusedTextColor = RivonTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.preferences.setElevenLabsKey(editElevenLabsKey)
                                testStatusMessage = "ElevenLabs Key Saved!"
                                viewModel.refreshModelsAndVoices()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RivonElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save ElevenLabs Key")
                    }
                }
            }

            if (testStatusMessage != null) {
                Text(
                    text = testStatusMessage!!,
                    color = RivonNeonGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
