package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LifeOsViewModel
import com.example.ui.components.AllCommitmentsView
import com.example.ui.components.ChaosCaptureDock
import com.example.ui.components.ExecutiveDashboardView
import com.example.ui.components.ManualTaskDialog
import com.example.ui.components.ReplanDialog
import com.example.ui.components.ScreenshotAnalysisSheet
import com.example.ui.components.TimelineAgendaView
import com.example.ui.components.TopExecutiveBar
import com.example.ui.components.VoiceCaptureSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LifeOsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOsApp(viewModel: LifeOsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val chaosSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenshotSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = ObsidianBlack,
        topBar = {
            Column {
                TopExecutiveBar(
                    isAiLive = uiState.isAiConfigured,
                    overdueCount = uiState.metrics.overdueCount,
                    onAutoReplanClick = { viewModel.autoReplanSchedule() },
                    onResetDemoClick = { viewModel.resetToDefaultPresetSchedule() }
                )

                // Sub-tabs: 0 = Timeline Agenda, 1 = Executive Dashboard, 2 = Commitments
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        title = "Timeline",
                        icon = Icons.Default.CalendarToday,
                        isSelected = uiState.activeTab == 0,
                        badgeText = if (uiState.metrics.overdueCount > 0) "${uiState.metrics.overdueCount} Slipped" else null,
                        onClick = { viewModel.setActiveTab(0) },
                        modifier = Modifier.weight(1f),
                        testTag = "tab_timeline"
                    )
                    TabButton(
                        title = "Dashboard",
                        icon = Icons.Default.Dashboard,
                        isSelected = uiState.activeTab == 1,
                        badgeText = "${uiState.metrics.completionPercentage}%",
                        onClick = { viewModel.setActiveTab(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "tab_dashboard"
                    )
                    TabButton(
                        title = "All Tasks",
                        icon = Icons.Default.Checklist,
                        isSelected = uiState.activeTab == 2,
                        badgeText = "${uiState.tasks.size}",
                        onClick = { viewModel.setActiveTab(2) },
                        modifier = Modifier.weight(1f),
                        testTag = "tab_commitments"
                    )
                }

                // Temporary status banner notification
                AnimatedVisibility(
                    visible = !uiState.statusBanner.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.12f))
                            .border(0.8.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.statusBanner ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            ChaosCaptureDock(
                onVoiceClick = { viewModel.openChaosSheet() },
                onScreenshotClick = { viewModel.openScreenshotSheet() },
                onManualAddClick = { viewModel.openAddManualDialog() },
                onQuickSubmit = { text ->
                    viewModel.openChaosSheet(text)
                    viewModel.processChaosRant(text)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0 -> {
                    TimelineAgendaView(
                        timelineItems = uiState.timelineItems,
                        onToggleComplete = { viewModel.toggleTaskComplete(it) },
                        onMarkMissed = { viewModel.markTaskMissed(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onQuickChaosClick = { viewModel.openChaosSheet() },
                        onAutoReplanClick = { viewModel.autoReplanSchedule() }
                    )
                }
                1 -> {
                    ExecutiveDashboardView(
                        metrics = uiState.metrics,
                        onNavigateTimeline = { viewModel.setActiveTab(0) }
                    )
                }
                2 -> {
                    AllCommitmentsView(
                        tasks = uiState.tasks,
                        selectedCategory = uiState.selectedCategoryFilter,
                        onSelectCategory = { viewModel.setCategoryFilter(it) },
                        onToggleComplete = { viewModel.toggleTaskComplete(it) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                }
            }
        }
    }

    // Voice Capture Sheet
    if (uiState.showChaosSheet) {
        VoiceCaptureSheet(
            sheetState = chaosSheetState,
            isAnalyzing = uiState.isAnalyzing,
            transcriptText = uiState.liveVoiceTranscript,
            isListening = uiState.isListeningVoice,
            extractedDrafts = uiState.extractedDrafts,
            statusBanner = uiState.statusBanner,
            onTranscriptChange = { viewModel.updateVoiceTranscript(it) },
            onListeningChanged = { viewModel.setListeningVoice(it) },
            onProcessChaos = { viewModel.processChaosRant(it) },
            onToggleDraft = { viewModel.toggleDraftSelection(it) },
            onCommitDrafts = { viewModel.commitExtractedDrafts() },
            onDismiss = { viewModel.closeChaosSheet() }
        )
    }

    // Screenshot Analysis Sheet
    if (uiState.showScreenshotSheet) {
        ScreenshotAnalysisSheet(
            sheetState = screenshotSheetState,
            isAnalyzing = uiState.isAnalyzing,
            extractedDrafts = uiState.extractedDrafts,
            statusBanner = uiState.statusBanner,
            onProcessScreenshot = { bitmap, notes -> viewModel.processScreenshot(bitmap, notes) },
            onToggleDraft = { viewModel.toggleDraftSelection(it) },
            onCommitDrafts = { viewModel.commitExtractedDrafts() },
            onDismiss = { viewModel.closeScreenshotSheet() }
        )
    }

    // Manual Task Dialog
    if (uiState.showAddManualDialog) {
        ManualTaskDialog(
            onDismiss = { viewModel.closeAddManualDialog() },
            onSaveTask = { title, desc, cat, prio, start, duration ->
                viewModel.addManualTask(title, desc, cat, prio, start, duration)
            }
        )
    }

    // Replan Confirmation Modal
    if (uiState.showReplanModal) {
        ReplanDialog(
            replanResult = uiState.lastReplanResult,
            onDismiss = { viewModel.dismissReplanModal() }
        )
    }
}

@Composable
fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ObsidianCard else ObsidianSurface)
            .border(
                1.dp,
                if (isSelected) NeonCyan.copy(alpha = 0.8f) else ObsidianCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) NeonCyan.copy(alpha = 0.2f) else ObsidianBlack
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NeonCyan else TextMuted
                    )
                }
            }
        }
    }
}
