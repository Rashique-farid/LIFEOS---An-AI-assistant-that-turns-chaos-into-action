package com.example.data.repository

import com.example.data.dao.TaskDao
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long> = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun updateTasks(tasks: List<TaskEntity>) = taskDao.updateTasks(tasks)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun clearAll() = taskDao.clearAll()

    suspend fun seedInitialDataIfEmpty() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val defaultTasks = listOf(
            TaskEntity(
                title = "Deep Work: Finish job assignment",
                description = "Finalize project slides and submit candidate assessment ahead of Friday deadline.",
                category = TaskCategory.WORK.name,
                priority = Priority.HIGH.name,
                scheduledDate = today,
                startTime = "09:00",
                durationMinutes = 90,
                isCompleted = true,
                rawSource = "VOICE",
                extractedMeta = "Deadline: Friday 5:00 PM"
            ),
            TaskEntity(
                title = "Pay electricity bill ($84.20)",
                description = "Utility auto-pay reminder. Confirm transaction via banking app.",
                category = TaskCategory.BILLS.name,
                priority = Priority.HIGH.name,
                scheduledDate = today,
                startTime = "11:00",
                durationMinutes = 15,
                isCompleted = false,
                rawSource = "SCREENSHOT",
                extractedMeta = "Amount: $84.20 • Due: Oct 18"
            ),
            TaskEntity(
                title = "Interview Preparation & System Design",
                description = "Review distributed caching and practice behavioral STAR questions.",
                category = TaskCategory.WORK.name,
                priority = Priority.HIGH.name,
                scheduledDate = today,
                startTime = "13:30",
                durationMinutes = 60,
                isCompleted = false,
                rawSource = "VOICE",
                extractedMeta = "Focus: Concurrency & System Design"
            ),
            TaskEntity(
                title = "Study Java 21 & Concurrency",
                description = "Daily 1-hour deliberate practice: Virtual Threads and Structured Concurrency.",
                category = TaskCategory.LEARNING.name,
                priority = Priority.MEDIUM.name,
                scheduledDate = today,
                startTime = "15:30",
                durationMinutes = 60,
                isCompleted = false,
                rawSource = "VOICE",
                extractedMeta = "Daily Goal: 1 hour habit"
            ),
            TaskEntity(
                title = "Evening Recovery & Walk",
                description = "30-minute outdoor walk without screens to decompress.",
                category = TaskCategory.HEALTH.name,
                priority = Priority.LOW.name,
                scheduledDate = today,
                startTime = "18:00",
                durationMinutes = 30,
                isCompleted = false,
                rawSource = "MANUAL",
                extractedMeta = "Buffer zone"
            )
        )
        taskDao.insertTasks(defaultTasks)
    }
}
