package com.example.ai

import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskDraft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object LocalHeuristicParser {

    fun parseChaosText(chaosInput: String): List<TaskDraft> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cleanInput = chaosInput.trim()
        if (cleanInput.isBlank()) return emptyList()

        // Split by typical conversational conjunctions and punctuation
        val rawChunks = cleanInput.split(
            Regex("(?i)(?:,\\s*(?:and\\s+|also\\s+|then\\s+)?|\\s+and\\s+also\\s+|\\s+and\\s+|;\\s*|\\n+|\\.\\s+)")
        ).map { it.trim() }.filter { it.length > 3 }

        val startTimes = listOf("09:00", "11:00", "13:30", "15:30", "17:00", "19:00", "20:30")
        val drafts = mutableListOf<TaskDraft>()

        rawChunks.forEachIndexed { index, chunk ->
            // Clean common prefixes
            var cleanedChunk = chunk
                .replace(Regex("(?i)^(and\\s+|also\\s+|then\\s+|I\\s+have\\s+to|I\\s+need\\s+to|I\\s+must|Don't\\s+forget\\s+to|Remember\\s+to|Also\\s+need\\s+to|Please)\\s+"), "")
                .trim()
            if (cleanedChunk.isNotEmpty()) {
                cleanedChunk = cleanedChunk.replaceFirstChar { it.uppercase() }
            }

            // Determine category
            val category = when {
                cleanedChunk.contains("bill", true) || cleanedChunk.contains("pay", true) || cleanedChunk.contains("money", true) || cleanedChunk.contains("rent", true) || cleanedChunk.contains("$", true) -> TaskCategory.BILLS
                cleanedChunk.contains("interview", true) || cleanedChunk.contains("assignment", true) || cleanedChunk.contains("job", true) || cleanedChunk.contains("client", true) || cleanedChunk.contains("presentation", true) || cleanedChunk.contains("meeting", true) -> TaskCategory.WORK
                cleanedChunk.contains("study", true) || cleanedChunk.contains("java", true) || cleanedChunk.contains("read", true) || cleanedChunk.contains("course", true) || cleanedChunk.contains("learn", true) || cleanedChunk.contains("code", true) -> TaskCategory.LEARNING
                cleanedChunk.contains("gym", true) || cleanedChunk.contains("workout", true) || cleanedChunk.contains("run", true) || cleanedChunk.contains("doctor", true) || cleanedChunk.contains("meds", true) -> TaskCategory.HEALTH
                cleanedChunk.contains("urgent", true) || cleanedChunk.contains("asap", true) || cleanedChunk.contains("emergency", true) -> TaskCategory.URGENT
                else -> TaskCategory.PERSONAL
            }

            // Determine priority
            val priority = when {
                cleanedChunk.contains("urgent", true) || cleanedChunk.contains("asap", true) || cleanedChunk.contains("friday", true) || cleanedChunk.contains("deadline", true) || category == TaskCategory.BILLS || category == TaskCategory.URGENT -> Priority.HIGH
                cleanedChunk.contains("maybe", true) || cleanedChunk.contains("sometime", true) || cleanedChunk.contains("low", true) -> Priority.LOW
                else -> Priority.MEDIUM
            }

            // Extract duration
            val durationMinutes = when {
                cleanedChunk.contains("hour every day", true) || cleanedChunk.contains("an hour", true) || cleanedChunk.contains("1 hour", true) || cleanedChunk.contains("one hour", true) -> 60
                cleanedChunk.contains("2 hours", true) || cleanedChunk.contains("two hours", true) -> 120
                cleanedChunk.contains("30 min", true) || cleanedChunk.contains("half an hour", true) -> 30
                cleanedChunk.contains("15 min", true) || category == TaskCategory.BILLS -> 15
                category == TaskCategory.WORK -> 75
                category == TaskCategory.LEARNING -> 60
                else -> 45
            }

            // Extract metadata tags
            val metaParts = mutableListOf<String>()
            if (cleanedChunk.contains("friday", true)) metaParts.add("Due Friday")
            if (cleanedChunk.contains("every day", true) || cleanedChunk.contains("daily", true)) metaParts.add("Daily Routine")
            if (cleanedChunk.contains("tomorrow", true)) metaParts.add("Target: Tomorrow")
            if (cleanedChunk.contains("bill", true)) metaParts.add("Finance item")

            val meta = if (metaParts.isNotEmpty()) metaParts.joinToString(" • ") else "Auto-scheduled"

            val timeSlot = startTimes.getOrElse(index) {
                val hour = 9 + (index * 2) % 12
                String.format(Locale.getDefault(), "%02d:00", hour)
            }

            drafts.add(
                TaskDraft(
                    title = cleanedChunk,
                    description = "Extracted from natural thoughts: $cleanedChunk",
                    category = category,
                    priority = priority,
                    scheduledDate = todayStr,
                    startTime = timeSlot,
                    durationMinutes = durationMinutes,
                    extractedMeta = meta,
                    isSelected = true
                )
            )
        }

        return drafts
    }
}
