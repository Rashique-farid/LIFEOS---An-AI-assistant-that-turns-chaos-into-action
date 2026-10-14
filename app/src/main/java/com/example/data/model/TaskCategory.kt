package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose

enum class TaskCategory(
    val displayName: String,
    val color: Color
) {
    WORK("Work & Career", NeonCyan),
    BILLS("Bills & Finance", NeonAmber),
    LEARNING("Learning & Study", NeonPurple),
    HEALTH("Health & Fitness", NeonEmerald),
    PERSONAL("Personal & Home", NeonIndigo),
    URGENT("Critical & Urgent", NeonRose);

    fun getIcon(): ImageVector {
        return when (this) {
            WORK -> Icons.Default.Work
            BILLS -> Icons.Default.AttachMoney
            LEARNING -> Icons.Default.Book
            HEALTH -> Icons.Default.FitnessCenter
            PERSONAL -> Icons.Default.Home
            URGENT -> Icons.Default.PriorityHigh
        }
    }

    companion object {
        fun fromString(value: String): TaskCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: when {
                    value.contains("bill", true) || value.contains("pay", true) || value.contains("finance", true) -> BILLS
                    value.contains("job", true) || value.contains("interview", true) || value.contains("work", true) || value.contains("assignment", true) -> WORK
                    value.contains("study", true) || value.contains("java", true) || value.contains("read", true) || value.contains("learn", true) -> LEARNING
                    value.contains("gym", true) || value.contains("workout", true) || value.contains("doctor", true) -> HEALTH
                    value.contains("urgent", true) || value.contains("deadline", true) || value.contains("asap", true) -> URGENT
                    else -> PERSONAL
                }
        }
    }
}
