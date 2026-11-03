package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RivonViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonSoftCyan
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary

@Composable
fun AboutRivonScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

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
                    modifier = Modifier.testTag("about_back_button")
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
                        text = "About RIVON AI",
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Your Personal Android Intelligence",
                        color = RivonCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Big Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(listOf(RivonCyan, RivonElectricBlue)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp
                )
            }

            Text(
                text = "RIVON AI",
                color = RivonTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )

            Text(
                text = "Version 1.0.0 (Production Core)",
                color = RivonCyan,
                fontSize = 13.sp
            )

            Text(
                text = "Next-generation Android Intelligence combining real cross-application phone automation, OpenRouter frontier AI reasoning, and ElevenLabs neural speech synthesis.",
                color = RivonTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Spec Cards
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = RivonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Architecture Highlights", color = RivonTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Native Jetpack Compose Material 3 UI\n• Multi-level Android Action Engine (Intents, Deep Links, Accessibility)\n• OpenRouter Multilingual Intelligence (English, Nepali, Hinglish)\n• ElevenLabs Ultra-Realistic Neural Speech Synthesis\n• Local Room SQLite persistent encrypted memory", color = RivonTextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = RivonNeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zero Data Collection Policy", color = RivonTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("RIVON does not collect, log, or sell private telemetry, chats, passwords, or personal identity. All actions require your explicit user authorization.", color = RivonTextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
