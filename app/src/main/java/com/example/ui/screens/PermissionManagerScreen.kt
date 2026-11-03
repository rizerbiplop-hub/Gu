package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.RivonAccessibilityService
import com.example.ui.RivonViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RivonStatusBadge
import com.example.ui.theme.RivonBorder
import com.example.ui.theme.RivonCyan
import com.example.ui.theme.RivonDeepBlack
import com.example.ui.theme.RivonElectricBlue
import com.example.ui.theme.RivonMidnight
import com.example.ui.theme.RivonNeonGreen
import com.example.ui.theme.RivonNeonPurple
import com.example.ui.theme.RivonSurface
import com.example.ui.theme.RivonSurfaceElevated
import com.example.ui.theme.RivonTextMuted
import com.example.ui.theme.RivonTextPrimary
import com.example.ui.theme.RivonTextSecondary
import com.example.ui.theme.RivonErrorRed
import com.example.ui.theme.RivonWarningAmber

@Composable
fun PermissionManagerScreen(viewModel: RivonViewModel) {
    BackHandler {
        viewModel.popBack()
    }

    val context = LocalContext.current
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var contactsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var phoneGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
    }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        contactsGranted = granted
    }

    val phoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        phoneGranted = granted
    }

    val isAccessibilityActive = RivonAccessibilityService.isServiceRunning()

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
                    modifier = Modifier.testTag("permissions_back_button")
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
                        text = "System Permissions & Settings",
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Security, Integrations & API Config",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Navigation Hub
            Text(
                text = "RIVON CONTROL PANELS",
                color = RivonTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavButton(
                    label = "API Keys",
                    icon = Icons.Default.Key,
                    color = RivonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.API_CONFIG) }
                )
                QuickNavButton(
                    label = "Personality",
                    icon = Icons.Default.Psychology,
                    color = RivonElectricBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.PERSONALITY) }
                )
                QuickNavButton(
                    label = "Memory",
                    icon = Icons.Default.Settings,
                    color = RivonNeonPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.MEMORY) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Permission list
            Text(
                text = "ANDROID DEVICE CAPABILITIES",
                color = RivonTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // 1. Microphone
            PermissionStatusCard(
                title = "Microphone & Speech Recognition",
                description = "Required to speak natural voice commands to RIVON and convert speech to text.",
                icon = Icons.Default.Mic,
                isGranted = micGranted,
                actionLabel = "Grant Permission",
                onAction = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )

            // 2. Accessibility Service (Level 3 Control)
            PermissionStatusCard(
                title = "Level 3 Accessibility Service",
                description = "Allows RIVON to click buttons and type text into active apps (like WhatsApp) upon your explicit voice request.",
                icon = Icons.Default.Accessibility,
                isGranted = isAccessibilityActive,
                actionLabel = "Open Accessibility Settings",
                onAction = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )

            // 3. Contacts
            PermissionStatusCard(
                title = "Contacts Directory Access",
                description = "Resolves natural names into phone numbers for WhatsApp messages and phone calls.",
                icon = Icons.Default.Contacts,
                isGranted = contactsGranted,
                actionLabel = "Grant Permission",
                onAction = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
            )

            // 4. Phone Calling
            PermissionStatusCard(
                title = "Direct Phone Dialing",
                description = "Required when commanding RIVON to call contacts directly.",
                icon = Icons.Default.Call,
                isGranted = phoneGranted,
                actionLabel = "Grant Permission",
                onAction = { phoneLauncher.launch(Manifest.permission.CALL_PHONE) }
            )

            // 5. Alarms & System Controls
            PermissionStatusCard(
                title = "Alarms, Timers & Audio Settings",
                description = "Enables setting alarms, countdown timers, and controlling device media volume.",
                icon = Icons.Default.Alarm,
                isGranted = true, // Normal install-time permissions declared in manifest
                actionLabel = null,
                onAction = {}
            )

            // About RIVON button
            OutlinedButton(
                onClick = { viewModel.navigateTo(ScreenDestination.ABOUT) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("about_rivon_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, RivonCyan)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = RivonCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("About RIVON AI Architecture", color = RivonCyan)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickNavButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = RivonSurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = RivonTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PermissionStatusCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isGranted) RivonNeonGreen.copy(alpha = 0.4f) else RivonBorder,
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = RivonMidnight)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isGranted) RivonNeonGreen.copy(alpha = 0.15f) else RivonSurfaceElevated,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isGranted) RivonNeonGreen else RivonTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        color = RivonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                RivonStatusBadge(
                    label = if (isGranted) "ENABLED" else "DISABLED",
                    color = if (isGranted) RivonNeonGreen else RivonWarningAmber
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = RivonTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (!isGranted && actionLabel != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RivonElectricBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp)
                }
            }
        }
    }
}
