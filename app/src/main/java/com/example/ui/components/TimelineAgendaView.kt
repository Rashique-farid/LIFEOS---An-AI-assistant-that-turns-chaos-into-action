package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.planner.TimelineItem
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TimelineAgendaView(
    timelineItems: List<TimelineItem>,
    onToggleComplete: (TaskEntity) -> Unit,
    onMarkMissed: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onQuickChaosClick: () -> Unit,
    onAutoReplanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (timelineItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Schedule Blocks Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the glowing mic button or dump your thoughts to turn chaos into today's timeline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = timelineItems,
            key = { item ->
                when (item) {
                    is TimelineItem.TaskBlock -> "task_${item.task.id}_${item.task.startTime}"
                    is TimelineItem.FreeBuffer -> "buffer_${item.startTime}_${item.endTime}"
                }
            }
        ) { item ->
            when (item) {
                is TimelineItem.TaskBlock -> {
                    TaskBlockCard(
                        task = item.task,
                        isCurrent = item.isCurrent,
                        isOverdue = item.isOverdue,
                        onToggleComplete = { onToggleComplete(item.task) },
                        onMarkMissed = { onMarkMissed(item.task) },
                        onDeleteTask = { onDeleteTask(item.task) },
                        onAutoReplan = onAutoReplanClick
                    )
                }
                is TimelineItem.FreeBuffer -> {
                    FreeBufferCard(
                        buffer = item,
                        onQuickAdd = onQuickChaosClick
                    )
                }
            }
        }
    }
}

@Composable
fun TaskBlockCard(
    task: TaskEntity,
    isCurrent: Boolean,
    isOverdue: Boolean,
    onToggleComplete: () -> Unit,
    onMarkMissed: () -> Unit,
    onDeleteTask: () -> Unit,
    onAutoReplan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = task.getCategoryEnum()
    val priority = task.getPriorityEnum()
    var showMenu by remember { mutableStateOf(false) }

    val borderColor = when {
        task.isCompleted -> NeonEmerald.copy(alpha = 0.5f)
        isOverdue -> NeonRose.copy(alpha = 0.8f)
        isCurrent -> NeonCyan.copy(alpha = 0.9f)
        else -> ObsidianCardBorder
    }

    val cardBg = when {
        task.isCompleted -> ObsidianCard.copy(alpha = 0.7f)
        isCurrent -> ObsidianCard
        isOverdue -> ObsidianCard
        else -> ObsidianCard
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent || isOverdue) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("task_block_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Time + Category Chip + Priority Tag + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, ObsidianCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isCurrent) NeonCyan else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.startTime} (${task.durationMinutes}m)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) NeonCyan else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(category.color.copy(alpha = 0.12f))
                            .border(0.8.dp, category.color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = category.getIcon(),
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = category.color
                            )
                        }
                    }

                    if (isCurrent && !task.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NOW",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan
                            )
                        }
                    }

                    if (isOverdue && !task.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonRose.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SLIPPED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonRose
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(ObsidianSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mark Missed / Delayed", color = NeonRose) },
                            onClick = {
                                showMenu = false
                                onMarkMissed()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Auto-Replan Slot", color = NeonCyan) },
                            onClick = {
                                showMenu = false
                                onAutoReplan()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Task", color = TextMuted) },
                            onClick = {
                                showMenu = false
                                onDeleteTask()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Title + checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (task.isCompleted) TextMuted.copy(alpha = 0.7f) else TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!task.extractedMeta.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.extractedMeta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeonCyan.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Interactive Glowing Checkbox
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (task.isCompleted) NeonEmerald.copy(alpha = 0.2f) else ObsidianBlack
                        )
                        .border(
                            1.2.dp,
                            if (task.isCompleted) NeonEmerald else ObsidianCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleComplete() }
                        .testTag("toggle_task_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isOverdue && !task.isCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonRose.copy(alpha = 0.12f))
                        .clickable { onAutoReplan() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NeonRose,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Behind schedule • Tap to auto-replan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonRose
                        )
                    }
                    Text(
                        text = "Shift Slot →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
fun FreeBufferCard(
    buffer: TimelineItem.FreeBuffer,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface.copy(alpha = 0.5f))
            .border(0.8.dp, ObsidianCardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onQuickAdd() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${buffer.startTime} – ${buffer.endTime}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• Free Focus Buffer (${buffer.durationMinutes}m)",
                    fontSize = 12.sp,
                    color = TextMuted.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "+ Add Task",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan.copy(alpha = 0.8f)
            )
        }
    }
}
