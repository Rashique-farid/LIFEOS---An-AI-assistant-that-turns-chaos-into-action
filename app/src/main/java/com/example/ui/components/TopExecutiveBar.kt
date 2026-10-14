package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopExecutiveBar(
    isAiLive: Boolean,
    overdueCount: Int,
    onAutoReplanClick: () -> Unit,
    onResetDemoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateDisplay = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    Surface(
        color = ObsidianBlack,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Glowing logo badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))
                                )
                            )
                            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "LIFEOS Nexus",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIFEOS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // AI Engine pill indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isAiLive) NeonCyan.copy(alpha = 0.15f) else NeonPurple.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (isAiLive) NeonCyan.copy(alpha = 0.4f) else NeonPurple.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAiLive) "Gemini Live" else "Heuristic Core",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAiLive) NeonCyan else NeonPurple
                                )
                            }
                        }
                        Text(
                            text = dateDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset demo button
                    IconButton(
                        onClick = onResetDemoClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reset_demo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Demo Data",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Auto-Replan Button with badge if overdue tasks exist
                    FilledTonalButton(
                        onClick = onAutoReplanClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (overdueCount > 0) NeonAmber.copy(alpha = 0.2f) else ObsidianCard,
                            contentColor = if (overdueCount > 0) NeonAmber else NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .border(
                                1.dp,
                                if (overdueCount > 0) NeonAmber.copy(alpha = 0.7f) else NeonCyan.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("auto_replan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (overdueCount > 0) "Replan ($overdueCount)" else "Replan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
