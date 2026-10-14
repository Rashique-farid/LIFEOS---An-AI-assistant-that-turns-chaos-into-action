package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.PERSONAL.name,
    val priority: String = Priority.MEDIUM.name,
    val scheduledDate: String, // "YYYY-MM-DD"
    val startTime: String,     // "HH:mm" (24h)
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val rawSource: String = "MANUAL", // "VOICE", "SCREENSHOT", "MANUAL", "AI_PLAN"
    val extractedMeta: String? = null, // e.g. "$84.20 electricity bill due Friday"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getCategoryEnum(): TaskCategory = TaskCategory.fromString(category)
    fun getPriorityEnum(): Priority = Priority.fromString(priority)
}

data class TaskDraft(
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val scheduledDate: String = "",
    val startTime: String = "09:00",
    val durationMinutes: Int = 45,
    val extractedMeta: String? = null,
    val isSelected: Boolean = true
)
