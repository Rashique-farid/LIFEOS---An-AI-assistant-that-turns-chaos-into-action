package com.example.planner

import com.example.data.model.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class TimelineItem {
    data class TaskBlock(
        val task: TaskEntity,
        val isCurrent: Boolean = false,
        val isOverdue: Boolean = false
    ) : TimelineItem()

    data class FreeBuffer(
        val startTime: String,
        val endTime: String,
        val durationMinutes: Int
    ) : TimelineItem()
}

data class ReplanResult(
    val updatedTasks: List<TaskEntity>,
    val movedTaskCount: Int,
    val summaryMessage: String
)

object TimelineScheduler {

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size < 2) return 0
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0
        return hour * 60 + minute
    }

    fun formatMinutesToTime(totalMinutes: Int): String {
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    fun getCurrentTimeMinutes(): Int {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return parseTimeToMinutes(sdf.format(Date()))
    }

    fun buildTimeline(tasks: List<TaskEntity>, currentMinutes: Int): List<TimelineItem> {
        val sorted = tasks.sortedBy { parseTimeToMinutes(it.startTime) }
        val items = mutableListOf<TimelineItem>()

        var cursor = 8 * 60 // start day view at 08:00 AM

        for (task in sorted) {
            val taskStart = parseTimeToMinutes(task.startTime)
            val taskEnd = taskStart + task.durationMinutes

            // Check if there's a free buffer before this task
            if (taskStart > cursor + 15) {
                items.add(
                    TimelineItem.FreeBuffer(
                        startTime = formatMinutesToTime(cursor),
                        endTime = formatMinutesToTime(taskStart),
                        durationMinutes = taskStart - cursor
                    )
                )
            }

            val isCurrent = currentMinutes in taskStart..taskEnd && !task.isCompleted
            val isOverdue = currentMinutes > taskEnd && !task.isCompleted

            items.add(
                TimelineItem.TaskBlock(
                    task = task,
                    isCurrent = isCurrent,
                    isOverdue = isOverdue
                )
            )

            cursor = maxOf(cursor, taskEnd)
        }

        // Evening buffer if day has room
        val endOfDay = 21 * 60
        if (cursor < endOfDay) {
            items.add(
                TimelineItem.FreeBuffer(
                    startTime = formatMinutesToTime(cursor),
                    endTime = formatMinutesToTime(endOfDay),
                    durationMinutes = endOfDay - cursor
                )
            )
        }

        return items
    }

    fun replanOverdueTasks(
        allTasks: List<TaskEntity>,
        currentMinutes: Int
    ): ReplanResult {
        val overdueTasks = mutableListOf<TaskEntity>()
        val futureTasks = mutableListOf<TaskEntity>()
        val completedOrUntouched = mutableListOf<TaskEntity>()

        for (t in allTasks) {
            val taskEnd = parseTimeToMinutes(t.startTime) + t.durationMinutes
            if (!t.isCompleted && (t.isMissed || currentMinutes > taskEnd)) {
                overdueTasks.add(t)
            } else if (parseTimeToMinutes(t.startTime) >= currentMinutes) {
                futureTasks.add(t)
            } else {
                completedOrUntouched.add(t)
            }
        }

        if (overdueTasks.isEmpty()) {
            return ReplanResult(
                updatedTasks = allTasks,
                movedTaskCount = 0,
                summaryMessage = "All tasks are on track. No rescheduling needed."
            )
        }

        // Sort existing future tasks by start time
        futureTasks.sortBy { parseTimeToMinutes(it.startTime) }

        // Start slotting overdue tasks into open windows starting from currentMinutes + 15 min buffer
        var slotCursor = ((currentMinutes + 14) / 15) * 15 // round up to next 15 min mark
        val replannedList = mutableListOf<TaskEntity>()

        for (overdue in overdueTasks) {
            val duration = overdue.durationMinutes
            var placed = false

            // Try to find a slot that fits before or between future tasks
            while (!placed && slotCursor < 23 * 60) {
                val proposedEnd = slotCursor + duration

                // Check collision with any future task
                val collision = futureTasks.find { f ->
                    val fStart = parseTimeToMinutes(f.startTime)
                    val fEnd = fStart + f.durationMinutes
                    slotCursor < fEnd && proposedEnd > fStart
                }

                if (collision == null) {
                    val newTime = formatMinutesToTime(slotCursor)
                    val updated = overdue.copy(
                        startTime = newTime,
                        isMissed = false,
                        extractedMeta = "${overdue.extractedMeta?.let { "$it • " } ?: ""}Auto-Replanned to $newTime"
                    )
                    replannedList.add(updated)
                    futureTasks.add(updated)
                    futureTasks.sortBy { parseTimeToMinutes(it.startTime) }
                    slotCursor = proposedEnd + 15 // 15 min buffer
                    placed = true
                } else {
                    // Jump cursor past collision
                    val collEnd = parseTimeToMinutes(collision.startTime) + collision.durationMinutes
                    slotCursor = maxOf(slotCursor + 15, collEnd + 15)
                }
            }

            if (!placed) {
                // If late evening, slot at evening time
                val fallbackTime = formatMinutesToTime(slotCursor)
                val updated = overdue.copy(
                    startTime = fallbackTime,
                    isMissed = false,
                    extractedMeta = "${overdue.extractedMeta?.let { "$it • " } ?: ""}Pushed to Evening"
                )
                replannedList.add(updated)
            }
        }

        val allCombined = completedOrUntouched + futureTasks

        return ReplanResult(
            updatedTasks = allCombined,
            movedTaskCount = overdueTasks.size,
            summaryMessage = "Auto-Replanned ${overdueTasks.size} slipped ${if (overdueTasks.size == 1) "task" else "tasks"} into upcoming afternoon open buffers."
        )
    }
}
