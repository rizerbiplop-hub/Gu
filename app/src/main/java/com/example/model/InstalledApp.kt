package com.example.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val launchableActivity: String? = null
)
