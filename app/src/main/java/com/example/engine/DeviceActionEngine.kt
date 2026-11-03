package com.example.engine

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.ActionRiskLevel
import com.example.model.DeviceAction
import com.example.model.InstalledApp
import com.example.model.SettingType
import com.example.service.RivonAccessibilityService
import org.json.JSONObject
import java.util.Locale

class DeviceActionEngine(private val context: Context) {

    data class ExecutionResult(
        val success: Boolean,
        val message: String,
        val openedApp: String? = null
    )

    fun getInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<InstalledApp>()

        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val appName = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = resolveInfo.loadIcon(pm)

            appList.add(
                InstalledApp(
                    name = appName,
                    packageName = packageName,
                    icon = icon,
                    isSystemApp = isSystem,
                    launchableActivity = resolveInfo.activityInfo.name
                )
            )
        }
        return appList.distinctBy { it.packageName }.sortedBy { it.name }
    }

    fun findAppByName(nameQuery: String): InstalledApp? {
        val apps = getInstalledApps()
        val query = nameQuery.lowercase().trim()

        // Exact match
        apps.firstOrNull { it.name.equals(query, ignoreCase = true) }?.let { return it }

        // Contains match
        apps.firstOrNull { it.name.lowercase().contains(query) }?.let { return it }

        // Package contains
        apps.firstOrNull { it.packageName.lowercase().contains(query) }?.let { return it }

        return null
    }

    fun parseActionFromInput(userText: String, aiRawResponse: String?): DeviceAction? {
        // 1. First check if AI provided an explicit action JSON block
        if (!aiRawResponse.isNullOrBlank()) {
            val jsonAction = extractActionFromJson(aiRawResponse)
            if (jsonAction != null) return jsonAction
        }

        val text = userText.trim().lowercase(Locale.ROOT)

        // 2. Multilingual parsing (English, Nepali, Hinglish)

        // Launch / Open App
        // e.g. "open instagram", "instagram khol", "instagram open gardeu", "instagram kholo", "rivon, instagram khol"
        val openKeywords = listOf("open gardeu", "open", "launch", "khol", "kholo", "chalao", "kholideu", "gardeu")
        if (openKeywords.any { text.contains(it) } && !text.contains("search") && !text.contains("message") && !text.contains("volume") && !text.contains("alarm")) {
            val cleaned = text
                .replace("rivon", "")
                .replace("open gardeu", "")
                .replace("kholideu", "")
                .replace("gardeu", "")
                .replace("kholo", "")
                .replace("khol", "")
                .replace("chalao", "")
                .replace("open", "")
                .replace("launch", "")
                .trim()

            if (cleaned.isNotBlank()) {
                if (cleaned.contains("camera")) {
                    return DeviceAction.OpenCamera()
                } else if (cleaned.contains("gallery") || cleaned.contains("photos")) {
                    return DeviceAction.LaunchApp("Gallery", "com.google.android.apps.photos")
                } else if (cleaned.contains("whatsapp")) {
                    return DeviceAction.LaunchApp("WhatsApp", "com.whatsapp")
                } else if (cleaned.contains("instagram")) {
                    return DeviceAction.LaunchApp("Instagram", "com.instagram.android")
                } else if (cleaned.contains("youtube")) {
                    return DeviceAction.LaunchApp("YouTube", "com.google.android.youtube")
                } else if (cleaned.contains("spotify")) {
                    return DeviceAction.LaunchApp("Spotify", "com.spotify.music")
                } else if (cleaned.contains("chrome")) {
                    return DeviceAction.LaunchApp("Chrome", "com.android.chrome")
                } else {
                    return DeviceAction.LaunchApp(cleaned)
                }
            }
        }

        // WhatsApp Messaging
        // e.g. "open whatsapp and message rahul", "message rahul on whatsapp saying I am coming", "rahul lai message gara"
        if (text.contains("whatsapp") || text.contains("message") || text.contains("sandesh")) {
            val whatsappPattern = Regex("(?:message|send message to|tell|bhejo|gara)\\s+([a-zA-Z0-9]+)(?:\\s+(?:on whatsapp|ko|lai))?(?:\\s+(?:saying|that|:))?\\s*(.*)")
            val match = whatsappPattern.find(text)
            if (match != null) {
                val contact = match.groupValues[1].trim()
                val message = match.groupValues[2].trim().ifBlank { "Hello!" }
                return DeviceAction.WhatsAppMessage(contactName = contact, messageText = message)
            }
        }

        // YouTube search / play
        if (text.contains("youtube")) {
            val query = text.replace(Regex("(?:open|search|play|on|youtube|for|chalao|baja)"), "").trim()
            return DeviceAction.SearchYouTube(query.ifBlank { "Trending music" })
        }

        // Spotify play
        if (text.contains("spotify") || text.contains("play music") || text.contains("geet baja")) {
            val query = text.replace(Regex("(?:play|on|spotify|geet|baja|music)"), "").trim()
            return DeviceAction.PlaySpotify(query.ifBlank { "Top Hits" })
        }

        // Volume control
        // e.g. "set volume to 50 percent", "volume 70%"
        if (text.contains("volume") || text.contains("awaaz")) {
            val numberMatch = Regex("(\\d{1,3})").find(text)
            val percent = numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 50
            return DeviceAction.AdjustVolume(percent.coerceIn(0, 100))
        }

        // Alarms
        // e.g. "set alarm for 6 AM", "set an alarm at 7:30"
        if (text.contains("alarm")) {
            val timeMatch = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?").find(text)
            if (timeMatch != null) {
                var hour = timeMatch.groupValues[1].toIntOrNull() ?: 7
                val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
                val ampm = timeMatch.groupValues[3]
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
                return DeviceAction.SetAlarm(hour = hour, minute = minute)
            }
            return DeviceAction.SetAlarm(hour = 7, minute = 0)
        }

        // Timers
        if (text.contains("timer")) {
            val minMatch = Regex("(\\d+)\\s*(?:min|minute)").find(text)
            val minutes = minMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5
            return DeviceAction.SetTimer(seconds = minutes * 60)
        }

        // Settings
        if (text.contains("wifi") || text.contains("wi-fi")) {
            return DeviceAction.OpenSettings(SettingType.WIFI)
        }
        if (text.contains("bluetooth")) {
            return DeviceAction.OpenSettings(SettingType.BLUETOOTH)
        }
        if (text.contains("location") || text.contains("gps")) {
            return DeviceAction.OpenSettings(SettingType.LOCATION)
        }

        // Web Search on Chrome
        if (text.startsWith("search") || text.contains("search for") || text.contains("khoja")) {
            val query = text.replace(Regex("(?:search|for|on google|on chrome|khoja)"), "").trim()
            if (query.isNotBlank()) {
                return DeviceAction.SearchWeb(query)
            }
        }

        // Camera
        if (text.contains("camera") || text.contains("photo khicha")) {
            return DeviceAction.OpenCamera()
        }

        // Maps
        if (text.contains("navigate") || text.contains("direction") || text.contains("maps")) {
            val dest = text.replace(Regex("(?:navigate to|directions to|open maps|maps|jaau)"), "").trim()
            return DeviceAction.NavigateMaps(dest.ifBlank { "Home" })
        }

        return null
    }

    private fun extractActionFromJson(rawText: String): DeviceAction? {
        val actionMatch = Regex("```action\\s*([\\s\\S]*?)\\s*```").find(rawText)
            ?: Regex("```json\\s*([\\s\\S]*?)\\s*```").find(rawText)

        val jsonStr = actionMatch?.groupValues?.get(1)?.trim() ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val actionName = json.optString("action")
            val params = json.optJSONObject("params") ?: JSONObject()

            when (actionName) {
                "LaunchApp" -> DeviceAction.LaunchApp(
                    appName = params.optString("appName", "App"),
                    packageName = params.optString("packageName").takeIf { it.isNotBlank() }
                )
                "WhatsAppMessage" -> DeviceAction.WhatsAppMessage(
                    contactName = params.optString("contactName", "Contact"),
                    phoneNumber = params.optString("phoneNumber").takeIf { it.isNotBlank() },
                    messageText = params.optString("messageText", "Hello"),
                    sendDirectlyViaAccessibility = params.optBoolean("sendDirectly", false)
                )
                "MakePhoneCall" -> DeviceAction.MakePhoneCall(
                    contactName = params.optString("contactName", "Contact"),
                    phoneNumber = params.optString("phoneNumber", "")
                )
                "SetAlarm" -> DeviceAction.SetAlarm(
                    hour = params.optInt("hour", 7),
                    minute = params.optInt("minute", 0),
                    message = params.optString("message", "Alarm")
                )
                "SetTimer" -> DeviceAction.SetTimer(
                    seconds = params.optInt("seconds", 300),
                    message = params.optString("message", "Timer")
                )
                "AdjustVolume" -> DeviceAction.AdjustVolume(
                    percentage = params.optInt("percentage", 50)
                )
                "OpenSettings" -> {
                    val settingStr = params.optString("settingType", "WIFI")
                    val type = try { SettingType.valueOf(settingStr) } catch (e: Exception) { SettingType.WIFI }
                    DeviceAction.OpenSettings(type)
                }
                "SearchYouTube" -> DeviceAction.SearchYouTube(
                    query = params.optString("query", "Music")
                )
                "PlaySpotify" -> DeviceAction.PlaySpotify(
                    query = params.optString("query", "Music")
                )
                "SearchWeb" -> DeviceAction.SearchWeb(
                    query = params.optString("query", "")
                )
                "OpenCamera" -> DeviceAction.OpenCamera(
                    isVideo = params.optBoolean("isVideo", false)
                )
                "NavigateMaps" -> DeviceAction.NavigateMaps(
                    destination = params.optString("destination", "Home")
                )
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun executeAction(action: DeviceAction): ExecutionResult {
        return try {
            when (action) {
                is DeviceAction.LaunchApp -> executeLaunchApp(action)
                is DeviceAction.WhatsAppMessage -> executeWhatsApp(action)
                is DeviceAction.MakePhoneCall -> executePhoneCall(action)
                is DeviceAction.SetAlarm -> executeSetAlarm(action)
                is DeviceAction.SetTimer -> executeSetTimer(action)
                is DeviceAction.AdjustVolume -> executeAdjustVolume(action)
                is DeviceAction.OpenSettings -> executeOpenSettings(action)
                is DeviceAction.SearchYouTube -> executeSearchYouTube(action)
                is DeviceAction.PlaySpotify -> executePlaySpotify(action)
                is DeviceAction.SearchWeb -> executeSearchWeb(action)
                is DeviceAction.OpenCamera -> executeOpenCamera(action)
                is DeviceAction.ComposeEmail -> executeComposeEmail(action)
                is DeviceAction.NavigateMaps -> executeNavigateMaps(action)
                is DeviceAction.ShareContent -> executeShareContent(action)
                is DeviceAction.AccessibilityInteract -> executeAccessibilityInteract(action)
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Failed to execute ${action.title}: ${e.message}")
        }
    }

    private fun executeLaunchApp(action: DeviceAction.LaunchApp): ExecutionResult {
        val pm = context.packageManager

        // If package name provided directly
        val packageName = action.packageName ?: findAppByName(action.appName)?.packageName

        if (packageName != null) {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ExecutionResult(true, "Opened ${action.appName}", action.appName)
            }
        }

        // If app is not installed, open Google Play Store listing
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${action.appName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(playStoreIntent)
            ExecutionResult(true, "${action.appName} is not installed. Opened Play Store to download.", action.appName)
        } catch (e: Exception) {
            ExecutionResult(false, "${action.appName} is not installed on this device.")
        }
    }

    private fun executeWhatsApp(action: DeviceAction.WhatsAppMessage): ExecutionResult {
        val phoneParam = action.phoneNumber?.replace("+", "")?.replace(" ", "") ?: ""
        val encodedMsg = Uri.encode(action.messageText)

        // Try direct WhatsApp URL scheme
        val uri = if (phoneParam.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$phoneParam&text=$encodedMsg")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)

            // Level 3: Accessibility automation if available
            val accessibility = RivonAccessibilityService.instance
            if (accessibility != null && action.sendDirectlyViaAccessibility) {
                // Post brief delay then attempt to click send button via accessibility
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    accessibility.clickNodeByText("Send")
                    accessibility.clickNodeById("com.whatsapp:id/send")
                }, 1800)
                ExecutionResult(true, "Sent WhatsApp message to ${action.contactName} via Accessibility.")
            } else {
                ExecutionResult(true, "Prepared WhatsApp message for ${action.contactName}. Tap Send to deliver.", "WhatsApp")
            }
        } catch (e: Exception) {
            // Fallback to standard share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, action.messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send WhatsApp Message").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            ExecutionResult(true, "Opened sharing sheet to send message to ${action.contactName}.")
        }
    }

    private fun executePhoneCall(action: DeviceAction.MakePhoneCall): ExecutionResult {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Dialing ${action.contactName} (${action.phoneNumber}).")
    }

    private fun executeSetAlarm(action: DeviceAction.SetAlarm): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, action.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, action.message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Set alarm for %02d:%02d.".format(action.hour, action.minute))
        } catch (e: Exception) {
            ExecutionResult(false, "Could not set alarm: ${e.message}")
        }
    }

    private fun executeSetTimer(action: DeviceAction.SetTimer): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, action.seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, action.message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Set timer for ${action.seconds / 60} minutes.")
        } catch (e: Exception) {
            ExecutionResult(false, "Could not set timer: ${e.message}")
        }
    }

    private fun executeAdjustVolume(action: DeviceAction.AdjustVolume): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = (maxVolume * (action.percentage / 100f)).toInt().coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, AudioManager.FLAG_SHOW_UI)
        return ExecutionResult(true, "Adjusted media volume to ${action.percentage}%.")
    }

    private fun executeOpenSettings(action: DeviceAction.OpenSettings): ExecutionResult {
        val actionString = when (action.settingType) {
            SettingType.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SettingType.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingType.LOCATION -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            SettingType.SOUND -> Settings.ACTION_SOUND_SETTINGS
            SettingType.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingType.NOTIFICATIONS -> Settings.ACTION_APP_NOTIFICATION_SETTINGS
            SettingType.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            SettingType.APPLICATION_DETAILS -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }

        val intent = Intent(actionString).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (action.settingType == SettingType.APPLICATION_DETAILS) {
                data = Uri.parse("package:${context.packageName}")
            }
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Opened ${action.settingType.displayName}.")
    }

    private fun executeSearchYouTube(action: DeviceAction.SearchYouTube): ExecutionResult {
        val intent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", action.query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Searching YouTube for \"${action.query}\"", "YouTube")
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(action.query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ExecutionResult(true, "Opened YouTube search in browser.")
        }
    }

    private fun executePlaySpotify(action: DeviceAction.PlaySpotify): ExecutionResult {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            setPackage("com.spotify.music")
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
            putExtra(SearchManager.QUERY, action.query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Playing \"${action.query}\" on Spotify.", "Spotify")
        } catch (e: Exception) {
            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(action.query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(searchIntent)
                ExecutionResult(true, "Opened Spotify search for \"${action.query}\".")
            } catch (e2: Exception) {
                ExecutionResult(false, "Spotify is not installed on this device.")
            }
        }
    }

    private fun executeSearchWeb(action: DeviceAction.SearchWeb): ExecutionResult {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, action.query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Searching Chrome for \"${action.query}\"", "Chrome")
        } catch (e: Exception) {
            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(action.query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(urlIntent)
            ExecutionResult(true, "Opened web search in browser.")
        }
    }

    private fun executeOpenCamera(action: DeviceAction.OpenCamera): ExecutionResult {
        val intent = if (action.isVideo) {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        } else {
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Opened Camera.", "Camera")
    }

    private fun executeComposeEmail(action: DeviceAction.ComposeEmail): ExecutionResult {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${action.recipient ?: ""}")
            action.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            action.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ExecutionResult(true, "Opened email composer.")
    }

    private fun executeNavigateMaps(action: DeviceAction.NavigateMaps): ExecutionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(action.destination)}")).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Navigating to ${action.destination} on Google Maps.", "Maps")
        } catch (e: Exception) {
            val webMaps = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${Uri.encode(action.destination)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webMaps)
            ExecutionResult(true, "Opened Google Maps in browser.")
        }
    }

    private fun executeShareContent(action: DeviceAction.ShareContent): ExecutionResult {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, action.text)
        }
        val chooser = Intent.createChooser(intent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ExecutionResult(true, "Opened Android sharesheet.")
    }

    private fun executeAccessibilityInteract(action: DeviceAction.AccessibilityInteract): ExecutionResult {
        val service = RivonAccessibilityService.instance
            ?: return ExecutionResult(false, "Accessibility Service is not enabled. Please enable it in Settings.")

        var success = false
        if (!action.targetText.isNullOrBlank()) {
            success = service.clickNodeByText(action.targetText)
        } else if (!action.targetViewId.isNullOrBlank()) {
            success = service.clickNodeById(action.targetViewId)
        }

        return if (success) {
            ExecutionResult(true, "Executed on-screen interaction: ${action.actionDescription}")
        } else {
            ExecutionResult(false, "Could not find target element on active screen.")
        }
    }
}
