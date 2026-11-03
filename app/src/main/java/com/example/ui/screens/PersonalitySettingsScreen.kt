package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LanguageMode
import com.example.model.PersonalityStyle
import com.example.ui.RivonViewModel
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import kotlinx.coroutines.launch

@Composable
fun PersonalitySettingsScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val coroutineScope = rememberCoroutineScope()
    val activeStyle by viewModel.personalityStyle.collectAsState()
    val activeLanguage by viewModel.languageMode.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()
    var editName by remember(currentUserName) { mutableStateOf(currentUserName) }

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
                    modifier = Modifier.testTag("personality_back_button")
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
                        text = "Personality & Tone",
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Customize RIVON's Demeanor & Languages",
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
            // User Call-sign Name
            Text(
                text = "CALL-SIGN / NAME",
                color = RivonTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = editName,
                onValueChange = {
                    editName = it
                    coroutineScope.launch {
                        viewModel.preferences.setUserName(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_name_input"),
                label = { Text("What should RIVON call you?", color = RivonTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RivonCyan,
                    unfocusedBorderColor = RivonBorder,
                    focusedTextColor = RivonTextPrimary,
                    unfocusedTextColor = RivonTextPrimary,
                    cursorColor = RivonCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Personality Style Cards
            Text(
                text = "PERSONALITY ARCHETYPE",
                color = RivonTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            for (style in PersonalityStyle.entries) {
                val isSelected = style == activeStyle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch {
                                viewModel.preferences.setPersonalityStyle(style)
                            }
                        }
                        .border(
                            1.dp,
                            if (isSelected) RivonCyan else RivonBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("style_${style.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RivonSurfaceElevated else RivonMidnight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                                    contentDescription = null,
                                    tint = RivonDeepBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = style.displayName,
                                color = if (isSelected) RivonCyan else RivonTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = style.subtitle,
                                color = RivonTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Language Mode
            Text(
                text = "MULTILINGUAL INTERACTION",
                color = RivonTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            for (lang in LanguageMode.entries) {
                val isSelected = lang == activeLanguage
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch {
                                viewModel.preferences.setLanguageMode(lang)
                            }
                        }
                        .border(
                            1.dp,
                            if (isSelected) RivonElectricBlue else RivonBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("lang_${lang.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RivonSurfaceElevated else RivonMidnight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(RivonElectricBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = lang.displayName,
                                color = if (isSelected) RivonElectricBlue else RivonTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lang.promptHint,
                                color = RivonTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
