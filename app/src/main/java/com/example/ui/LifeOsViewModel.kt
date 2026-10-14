package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.ai.LocalHeuristicParser
import com.example.data.db.LifeOsDatabase
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskDraft
import com.example.data.model.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.planner.ReplanResult
import com.example.planner.TimelineItem
import com.example.planner.TimelineScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardMetrics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionPercentage: Int = 0,
    val totalFocusHours: Float = 0f,
    val overdueCount: Int = 0,
    val categoryBreakdown: Map<TaskCategory, Int> = emptyMap(),
    val streakDays: Int = 5
)

data class LifeOsUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val timelineItems: List<TimelineItem> = emptyList(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isAnalyzing: Boolean = false,
    val statusBanner: String? = null,
    val extractedDrafts: List<TaskDraft> = emptyList(),
    val showChaosSheet: Boolean = false,
    val showScreenshotSheet: Boolean = false,
    val showAddManualDialog: Boolean = false,
    val showReplanModal: Boolean = false,
    val lastReplanResult: ReplanResult? = null,
    val isListeningVoice: Boolean = false,
    val liveVoiceTranscript: String = "",
    val selectedCategoryFilter: TaskCategory? = null,
    val activeTab: Int = 0, // 0: Timeline, 1: Dashboard, 2: Commitments
    val metrics: DashboardMetrics = DashboardMetrics(),
    val isAiConfigured: Boolean = GeminiService.isConfigured
)

class LifeOsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _uiState = MutableStateFlow(LifeOsUiState())
    val uiState: StateFlow<LifeOsUiState> = _uiState.asStateFlow()

    init {
        val db = LifeOsDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        viewModelScope.launch {
            repository.allTasks.collect { taskList ->
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayTasks = taskList.filter { it.scheduledDate == _uiState.value.selectedDate }
                val currentMinutes = TimelineScheduler.getCurrentTimeMinutes()
                val timeline = TimelineScheduler.buildTimeline(todayTasks, currentMinutes)

                val completedCount = todayTasks.count { it.isCompleted }
                val totalCount = todayTasks.size
                val percentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0
                val totalMinutes = todayTasks.sumOf { it.durationMinutes }
                val focusHours = String.format(Locale.US, "%.1f", totalMinutes / 60f).toFloatOrNull() ?: 0f

                val overdueCount = todayTasks.count { t ->
                    val endMin = TimelineScheduler.parseTimeToMinutes(t.startTime) + t.durationMinutes
                    !t.isCompleted && (t.isMissed || currentMinutes > endMin)
                }

                val categoryMap = mutableMapOf<TaskCategory, Int>()
                TaskCategory.entries.forEach { cat ->
                    val count = todayTasks.count { it.getCategoryEnum() == cat }
                    if (count > 0) categoryMap[cat] = count
                }

                val metrics = DashboardMetrics(
                    totalTasks = totalCount,
                    completedTasks = completedCount,
                    completionPercentage = percentage,
                    totalFocusHours = focusHours,
                    overdueCount = overdueCount,
                    categoryBreakdown = categoryMap,
                    streakDays = 5
                )

                _uiState.value = _uiState.value.copy(
                    tasks = taskList,
                    timelineItems = timeline,
                    metrics = metrics
                )
            }
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setCategoryFilter(category: TaskCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    fun openChaosSheet(initialText: String = "") {
        _uiState.value = _uiState.value.copy(
            showChaosSheet = true,
            liveVoiceTranscript = initialText,
            extractedDrafts = emptyList(),
            statusBanner = null
        )
    }

    fun closeChaosSheet() {
        _uiState.value = _uiState.value.copy(
            showChaosSheet = false,
            liveVoiceTranscript = "",
            extractedDrafts = emptyList()
        )
    }

    fun openScreenshotSheet() {
        _uiState.value = _uiState.value.copy(
            showScreenshotSheet = true,
            extractedDrafts = emptyList(),
            statusBanner = null
        )
    }

    fun closeScreenshotSheet() {
        _uiState.value = _uiState.value.copy(
            showScreenshotSheet = false,
            extractedDrafts = emptyList()
        )
    }

    fun openAddManualDialog() {
        _uiState.value = _uiState.value.copy(showAddManualDialog = true)
    }

    fun closeAddManualDialog() {
        _uiState.value = _uiState.value.copy(showAddManualDialog = false)
    }

    fun updateVoiceTranscript(text: String) {
        _uiState.value = _uiState.value.copy(liveVoiceTranscript = text)
    }

    fun setListeningVoice(isListening: Boolean) {
        _uiState.value = _uiState.value.copy(isListeningVoice = isListening)
    }

    fun setStatusBanner(msg: String?) {
        _uiState.value = _uiState.value.copy(statusBanner = msg)
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun markTaskMissed(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isMissed = true))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun autoReplanSchedule() {
        viewModelScope.launch {
            val currentTasks = _uiState.value.tasks.filter { it.scheduledDate == _uiState.value.selectedDate }
            val currentMin = TimelineScheduler.getCurrentTimeMinutes()
            val result = TimelineScheduler.replanOverdueTasks(currentTasks, currentMin)

            if (result.movedTaskCount > 0) {
                repository.updateTasks(result.updatedTasks)
            }

            _uiState.value = _uiState.value.copy(
                lastReplanResult = result,
                showReplanModal = true,
                statusBanner = result.summaryMessage
            )
        }
    }

    fun dismissReplanModal() {
        _uiState.value = _uiState.value.copy(showReplanModal = false)
    }

    fun processChaosRant(rant: String) {
        if (rant.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, statusBanner = "Transforming chaos into organized plan…")
            val (drafts, banner) = GeminiService.extractTasksFromChaos(rant)
            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                extractedDrafts = drafts,
                statusBanner = banner
            )
        }
    }

    fun processScreenshot(bitmap: Bitmap, userNotes: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, statusBanner = "Extracting dates, costs, & action items with Gemini Vision…")
            val (drafts, banner) = GeminiService.extractTasksFromScreenshot(bitmap, userNotes)
            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                extractedDrafts = drafts,
                statusBanner = banner
            )
        }
    }

    fun toggleDraftSelection(index: Int) {
        val currentDrafts = _uiState.value.extractedDrafts.toMutableList()
        if (index in currentDrafts.indices) {
            val d = currentDrafts[index]
            currentDrafts[index] = d.copy(isSelected = !d.isSelected)
            _uiState.value = _uiState.value.copy(extractedDrafts = currentDrafts)
        }
    }

    fun commitExtractedDrafts() {
        val selected = _uiState.value.extractedDrafts.filter { it.isSelected }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            val entities = selected.map { draft ->
                TaskEntity(
                    title = draft.title,
                    description = draft.description,
                    category = draft.category.name,
                    priority = draft.priority.name,
                    scheduledDate = if (draft.scheduledDate.isNotBlank()) draft.scheduledDate else _uiState.value.selectedDate,
                    startTime = draft.startTime,
                    durationMinutes = draft.durationMinutes,
                    isCompleted = false,
                    rawSource = "AI_PLAN",
                    extractedMeta = draft.extractedMeta
                )
            }
            repository.insertTasks(entities)
            _uiState.value = _uiState.value.copy(
                showChaosSheet = false,
                showScreenshotSheet = false,
                extractedDrafts = emptyList(),
                liveVoiceTranscript = "",
                statusBanner = "Added ${entities.size} tasks to your schedule."
            )
        }
    }

    fun addManualTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: Priority,
        startTime: String,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                category = category.name,
                priority = priority.name,
                scheduledDate = _uiState.value.selectedDate,
                startTime = startTime,
                durationMinutes = durationMinutes,
                isCompleted = false,
                rawSource = "MANUAL",
                extractedMeta = "Directly scheduled"
            )
            repository.insertTask(task)
            _uiState.value = _uiState.value.copy(
                showAddManualDialog = false,
                statusBanner = "Added \"$title\" to your schedule."
            )
        }
    }

    fun resetToDefaultPresetSchedule() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedInitialDataIfEmpty()
            _uiState.value = _uiState.value.copy(statusBanner = "Schedule reset to default demo plan.")
        }
    }
}
