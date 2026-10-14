package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRose

enum class Priority(
    val label: String,
    val color: Color
) {
    HIGH("High", NeonRose),
    MEDIUM("Medium", NeonAmber),
    LOW("Low", NeonCyan);

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: when {
                    value.contains("high", true) || value.contains("urgent", true) || value.contains("critical", true) -> HIGH
                    value.contains("low", true) -> LOW
                    else -> MEDIUM
                }
        }
    }
}
