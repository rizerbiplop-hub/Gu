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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.AiModel
import com.example.ui.RivonViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonNeonPurple
import com.example.ui.theme.RivonSoftCyan
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import com.example.ui.theme.RivonWarningAmber
import kotlinx.coroutines.launch

@Composable
fun AiModelSelectionScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val coroutineScope = rememberCoroutineScope()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModelId by viewModel.selectedModel.collectAsState()
    val autoFallback by viewModel.preferences.autoFallbackFlow.collectAsState(initial = true)

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyFree by remember { mutableStateOf(false) }

    val filteredModels = remember(availableModels, searchQuery, filterOnlyFree) {
        availableModels.filter { model ->
            val matchesQuery = searchQuery.isBlank() ||
                    model.name.contains(searchQuery, ignoreCase = true) ||
                    model.id.contains(searchQuery, ignoreCase = true) ||
                    model.provider.contains(searchQuery, ignoreCase = true)
            val matchesFree = !filterOnlyFree || model.isFree
            matchesQuery && matchesFree
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivonDeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
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
                        modifier = Modifier.testTag("models_back_button")
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
                            text = "AI Intelligence Brain",
                            color = RivonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "OpenRouter Model Catalog",
                            color = RivonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshModelsAndVoices() },
                    modifier = Modifier.testTag("refresh_models_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Catalog",
                        tint = RivonCyan
                    )
                }
            }
        }

        // Search & Auto-fallback Card
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_search_input"),
                placeholder = {
                    Text("Search models or providers...", color = RivonTextMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RivonTextMuted)
                },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Fallback & Free filter settings card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intelligent Free Fallback",
                                color = RivonTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Auto switch to backup free models if primary encounters rate limits.",
                                color = RivonTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoFallback,
                            onCheckedChange = { checked ->
                                coroutineScope.launch {
                                    viewModel.preferences.setAutoFallback(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RivonCyan,
                                checkedTrackColor = RivonElectricBlue
                            ),
                            modifier = Modifier.testTag("auto_fallback_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show only Free Models",
                            color = RivonTextSecondary,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = filterOnlyFree,
                            onCheckedChange = { filterOnlyFree = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RivonNeonGreen,
                                checkedTrackColor = RivonSurfaceElevated
                            ),
                            modifier = Modifier.testTag("filter_free_switch")
                        )
                    }
                }
            }
        }

        // Models List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredModels, key = { it.id }) { model ->
                ModelItemCard(
                    model = model,
                    isSelected = model.id == selectedModelId,
                    onSelect = {
                        coroutineScope.launch {
                            viewModel.preferences.setSelectedModel(model.id)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ModelItemCard(
    model: AiModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .border(
                1.dp,
                if (isSelected) RivonCyan else RivonBorder,
                RoundedCornerShape(14.dp)
            )
            .testTag("model_card_${model.id.replace('/', '_')}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RivonSurfaceElevated else RivonMidnight
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column {
                        Text(
                            text = model.name,
                            color = if (isSelected) RivonCyan else RivonTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${model.provider} • ID: ${model.id}",
                            color = RivonTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RivonStatusBadge(
                        label = if (model.isFree) "FREE" else "PAID",
                        color = if (model.isFree) RivonNeonGreen else RivonWarningAmber
                    )
                }
            }

            if (model.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = model.description,
                    color = RivonTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Context: ${(model.contextLength / 1024)}k tokens",
                    color = RivonSoftCyan,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isSelected) "ACTIVE BRAIN" else "Tap to switch",
                    color = if (isSelected) RivonCyan else RivonTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
