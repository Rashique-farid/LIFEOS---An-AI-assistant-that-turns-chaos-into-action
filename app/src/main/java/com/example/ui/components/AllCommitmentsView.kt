package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AllCommitmentsView(
    tasks: List<TaskEntity>,
    selectedCategory: TaskCategory?,
    onSelectCategory: (TaskCategory?) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = if (selectedCategory == null) {
        tasks
    } else {
        tasks.filter { it.getCategoryEnum() == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("all_commitments_view")
    ) {
        // Category Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedCategory == null) NeonCyan else ObsidianCard)
                        .border(
                            1.dp,
                            if (selectedCategory == null) NeonCyan else ObsidianCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectCategory(null) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "All (${tasks.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategory == null) ObsidianBlack else TextPrimary
                    )
                }
            }

            items(TaskCategory.entries) { cat ->
                val count = tasks.count { it.getCategoryEnum() == cat }
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) cat.color else ObsidianCard)
                        .border(
                            1.dp,
                            if (isSelected) cat.color else ObsidianCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectCategory(if (isSelected) null else cat) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = cat.getIcon(),
                            contentDescription = null,
                            tint = if (isSelected) ObsidianBlack else cat.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${cat.displayName.substringBefore(" ")} ($count)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) ObsidianBlack else TextPrimary
                        )
                    }
                }
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = filtered,
                key = { it.id }
            ) { task ->
                CommitmentRowCard(
                    task = task,
                    onToggle = { onToggleComplete(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }
    }
}

@Composable
fun CommitmentRowCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val category = task.getCategoryEnum()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (task.isCompleted) NeonEmerald.copy(alpha = 0.2f) else ObsidianBlack
                    )
                    .border(
                        1.dp,
                        if (task.isCompleted) NeonEmerald else ObsidianCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.startTime} • ${task.durationMinutes}m",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${category.displayName}",
                        fontSize = 11.sp,
                        color = category.color
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
