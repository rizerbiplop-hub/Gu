package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.service.RivonWakeWordService
import com.example.ui.RivonViewModel
import com.example.ui.ScreenDestination
import com.example.ui.screens.AboutRivonScreen
import com.example.ui.screens.AiModelSelectionScreen
import com.example.ui.screens.ApiConfigScreen
import com.example.ui.screens.AppManagerScreen
import com.example.ui.screens.ChatConversationScreen
import com.example.ui.screens.ElevenLabsVoiceScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.MemorySettingsScreen
import com.example.ui.screens.PermissionManagerScreen
import com.example.ui.screens.PersonalitySettingsScreen
import com.example.ui.screens.WakeWordSettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RivonDeepBlack

class MainActivity : ComponentActivity() {

    private val viewModel: RivonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RivonDeepBlack
                ) {
                    RivonAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val isWakeTrigger = intent.getBooleanExtra(RivonWakeWordService.EXTRA_WAKE_TRIGGER, false)
        val isAssist = intent.action == Intent.ACTION_ASSIST

        if (isWakeTrigger || isAssist) {
            viewModel.toggleVoiceListening()
        }
    }
}

@Composable
fun RivonAppContent(viewModel: RivonViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ScreenDestination.MAIN -> MainAssistantScreen(viewModel = viewModel)
            ScreenDestination.CHAT -> ChatConversationScreen(viewModel = viewModel)
            ScreenDestination.AI_MODELS -> AiModelSelectionScreen(viewModel = viewModel)
            ScreenDestination.ELEVENLABS_VOICES -> ElevenLabsVoiceScreen(viewModel = viewModel)
            ScreenDestination.APP_MANAGER -> AppManagerScreen(viewModel = viewModel)
            ScreenDestination.PERSONALITY -> PersonalitySettingsScreen(viewModel = viewModel)
            ScreenDestination.WAKE_WORD -> WakeWordSettingsScreen(viewModel = viewModel)
            ScreenDestination.PERMISSIONS -> PermissionManagerScreen(viewModel = viewModel)
            ScreenDestination.MEMORY -> MemorySettingsScreen(viewModel = viewModel)
            ScreenDestination.API_CONFIG -> ApiConfigScreen(viewModel = viewModel)
            ScreenDestination.ABOUT -> AboutRivonScreen(viewModel = viewModel)
            else -> MainAssistantScreen(viewModel = viewModel)
        }
    }
}
