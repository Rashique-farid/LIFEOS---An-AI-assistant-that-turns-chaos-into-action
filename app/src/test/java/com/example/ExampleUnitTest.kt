package com.example

import com.example.ai.LocalHeuristicParser
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.planner.TimelineScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLocalHeuristicParser_extractsUserScenario() {
        val rant = "I have to finish my job assignment by Friday, prepare for an interview, pay my electricity bill, and study Java for an hour every day."
        val drafts = LocalHeuristicParser.parseChaosText(rant)

        assertTrue("Should extract at least 4 action items", drafts.size >= 4)

        // Find assignment
        val assignment = drafts.find { it.title.contains("assignment", ignoreCase = true) }
        assertTrue("Assignment task should be found", assignment != null)
        assertEquals(TaskCategory.WORK, assignment?.category)

        // Find bill
        val bill = drafts.find { it.title.contains("bill", ignoreCase = true) }
        assertTrue("Bill task should be found", bill != null)
        assertEquals(TaskCategory.BILLS, bill?.category)
        assertEquals(Priority.HIGH, bill?.priority)

        // Find Java study
        val javaStudy = drafts.find { it.title.contains("Java", ignoreCase = true) }
        assertTrue("Java study task should be found", javaStudy != null)
        assertEquals(TaskCategory.LEARNING, javaStudy?.category)
        assertEquals(60, javaStudy?.durationMinutes)
    }

    @Test
    fun testTimelineScheduler_replanOverdueTasks() {
        val tasks = listOf(
            TaskEntity(
                id = 1,
                title = "Morning Meeting",
                scheduledDate = "2026-10-14",
                startTime = "09:00",
                durationMinutes = 60,
                isCompleted = false // at 11:30, this is overdue
            ),
            TaskEntity(
                id = 2,
                title = "Afternoon Workshop",
                scheduledDate = "2026-10-14",
                startTime = "14:00",
                durationMinutes = 60,
                isCompleted = false
            )
        )

        // Current time: 11:30 (690 minutes)
        val result = TimelineScheduler.replanOverdueTasks(tasks, 690)

        assertEquals(1, result.movedTaskCount)
        val replannedTask = result.updatedTasks.find { it.id == 1L }
        assertTrue(replannedTask != null)
        assertFalse(replannedTask!!.isMissed)
        // Should be rescheduled past 11:30 and before 14:00
        val newTimeMin = TimelineScheduler.parseTimeToMinutes(replannedTask.startTime)
        assertTrue("Replanned time should be >= 11:30", newTimeMin >= 690)
        assertTrue("Replanned time should not collide with 14:00 workshop", newTimeMin + replannedTask.durationMinutes <= 14 * 60)
    }
}
