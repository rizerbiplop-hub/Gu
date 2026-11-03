package com.example.model

enum class ActionRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class ActionExecutionStatus {
    PENDING_CONFIRMATION,
    EXECUTING,
    SUCCESS,
    FAILED,
    CANCELLED
}

sealed class DeviceAction(
    val title: String,
    val description: String,
    val riskLevel: ActionRiskLevel
) {
    data class LaunchApp(
        val appName: String,
        val packageName: String? = null
    ) : DeviceAction(
        title = "Open Application",
        description = "Open $appName",
        riskLevel = ActionRiskLevel.LOW
    )

    data class WhatsAppMessage(
        val contactName: String,
        val phoneNumber: String? = null,
        val messageText: String,
        val sendDirectlyViaAccessibility: Boolean = false
    ) : DeviceAction(
        title = "WhatsApp Message",
        description = "Send message to $contactName: \"$messageText\"",
        riskLevel = ActionRiskLevel.HIGH
    )

    data class MakePhoneCall(
        val contactName: String,
        val phoneNumber: String
    ) : DeviceAction(
        title = "Phone Call",
        description = "Call $contactName ($phoneNumber)",
        riskLevel = ActionRiskLevel.HIGH
    )

    data class SetAlarm(
        val hour: Int,
        val minute: Int,
        val message: String = "Alarm"
    ) : DeviceAction(
        title = "Set Alarm",
        description = "Set alarm for %02d:%02d".format(hour, minute),
        riskLevel = ActionRiskLevel.MEDIUM
    )

    data class SetTimer(
        val seconds: Int,
        val message: String = "Timer"
    ) : DeviceAction(
        title = "Set Timer",
        description = "Set timer for ${seconds / 60} min ${seconds % 60} sec",
        riskLevel = ActionRiskLevel.LOW
    )

    data class AdjustVolume(
        val percentage: Int
    ) : DeviceAction(
        title = "Adjust Media Volume",
        description = "Set volume to $percentage%",
        riskLevel = ActionRiskLevel.MEDIUM
    )

    data class OpenSettings(
        val settingType: SettingType
    ) : DeviceAction(
        title = "Open Settings",
        description = "Open ${settingType.displayName}",
        riskLevel = ActionRiskLevel.LOW
    )

    data class SearchWeb(
        val query: String
    ) : DeviceAction(
        title = "Web Search",
        description = "Search web for \"$query\"",
        riskLevel = ActionRiskLevel.LOW
    )

    data class SearchYouTube(
        val query: String
    ) : DeviceAction(
        title = "YouTube Search",
        description = "Search YouTube for \"$query\"",
        riskLevel = ActionRiskLevel.LOW
    )

    data class PlaySpotify(
        val query: String
    ) : DeviceAction(
        title = "Play on Spotify",
        description = "Play \"$query\" on Spotify",
        riskLevel = ActionRiskLevel.LOW
    )

    data class OpenCamera(
        val isVideo: Boolean = false
    ) : DeviceAction(
        title = "Open Camera",
        description = if (isVideo) "Open Video Recorder" else "Open Camera",
        riskLevel = ActionRiskLevel.LOW
    )

    data class ComposeEmail(
        val recipient: String? = null,
        val subject: String? = null,
        val body: String? = null
    ) : DeviceAction(
        title = "Compose Email",
        description = "Compose email to ${recipient ?: "recipient"}",
        riskLevel = ActionRiskLevel.MEDIUM
    )

    data class NavigateMaps(
        val destination: String
    ) : DeviceAction(
        title = "Google Maps Navigation",
        description = "Navigate to $destination",
        riskLevel = ActionRiskLevel.LOW
    )

    data class ShareContent(
        val text: String
    ) : DeviceAction(
        title = "Share Text",
        description = "Share: $text",
        riskLevel = ActionRiskLevel.LOW
    )

    data class AccessibilityInteract(
        val actionDescription: String,
        val targetText: String? = null,
        val targetViewId: String? = null
    ) : DeviceAction(
        title = "On-Screen Assist",
        description = actionDescription,
        riskLevel = ActionRiskLevel.MEDIUM
    )
}

enum class SettingType(val displayName: String) {
    WIFI("Wi-Fi Settings"),
    BLUETOOTH("Bluetooth Settings"),
    LOCATION("Location Settings"),
    SOUND("Sound Settings"),
    DISPLAY("Display Settings"),
    NOTIFICATIONS("Notification Settings"),
    ACCESSIBILITY("Accessibility Settings"),
    APPLICATION_DETAILS("App Settings")
}

data class PlannedAction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val action: DeviceAction,
    var status: ActionExecutionStatus = ActionExecutionStatus.PENDING_CONFIRMATION,
    var resultMessage: String? = null
)
