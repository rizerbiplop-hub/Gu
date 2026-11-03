package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedFactEntity
import com.example.ui.RivonViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonErrorRed
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary

@Composable
fun MemorySettingsScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val savedFacts by viewModel.savedFacts.collectAsState()
    var newFactKey by remember { mutableStateOf("") }
    var newFactValue by remember { mutableStateOf("") }
    var showAddCard by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.popBack() },
                        modifier = Modifier.testTag("memory_back_button")
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
                            text = "Memory Core (Room)",
                            color = RivonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Locally encrypted persistent facts & history",
                            color = RivonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showAddCard = !showAddCard },
                    modifier = Modifier.testTag("add_memory_fact_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Memory",
                        tint = RivonCyan
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Memory Overview Card
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = RivonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "What RIVON Remembers",
                                color = RivonTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RIVON only retains information you explicitly authorize. Everything is stored strictly inside local device SQLite (Room Database) and never sold or synchronized without consent.",
                            color = RivonTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Add Fact Form
            if (showAddCard) {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = RivonCyan
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Record Important Knowledge",
                                color = RivonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newFactKey,
                                onValueChange = { newFactKey = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Topic (e.g. Favorite genre, Nickname)", color = RivonTextMuted, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RivonCyan,
                                    unfocusedBorderColor = RivonBorder,
                                    focusedTextColor = RivonTextPrimary,
                                    unfocusedTextColor = RivonTextPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newFactValue,
                                onValueChange = { newFactValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Information (e.g. Synthwave music)", color = RivonTextMuted, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RivonCyan,
                                    unfocusedBorderColor = RivonBorder,
                                    focusedTextColor = RivonTextPrimary,
                                    unfocusedTextColor = RivonTextPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (newFactKey.isNotBlank() && newFactValue.isNotBlank()) {
                                        viewModel.saveFact(newFactKey.trim(), newFactValue.trim())
                                        newFactKey = ""
                                        newFactValue = ""
                                        showAddCard = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RivonElectricBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save to Memory")
                            }
                        }
                    }
                }
            }

            // Clear All Conversation History Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED KNOWLEDGE & FACTS (${savedFacts.size})",
                        color = RivonTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RivonSurfaceElevated,
                            contentColor = RivonErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Chat Log", fontSize = 11.sp)
                    }
                }
            }

            if (savedFacts.isEmpty()) {
                item {
                    Text(
                        text = "No saved facts yet. Tap + to record preferences or contact nicknames.",
                        color = RivonTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            items(savedFacts, key = { it.id }) { fact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, RivonBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = RivonMidnight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fact.key,
                                color = RivonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = fact.value,
                                color = RivonTextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteFact(fact.id) },
                            modifier = Modifier.testTag("delete_fact_${fact.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Fact",
                                tint = RivonTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
