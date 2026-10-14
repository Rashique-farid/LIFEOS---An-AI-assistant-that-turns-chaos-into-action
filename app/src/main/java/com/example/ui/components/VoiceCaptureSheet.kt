package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.VoiceAssistantHandler
import com.example.data.model.TaskDraft
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCaptureSheet(
    sheetState: SheetState,
    isAnalyzing: Boolean,
    transcriptText: String,
    isListening: Boolean,
    extractedDrafts: List<TaskDraft>,
    statusBanner: String?,
    onTranscriptChange: (String) -> Unit,
    onListeningChanged: (Boolean) -> Unit,
    onProcessChaos: (String) -> Unit,
    onToggleDraft: (Int) -> Unit,
    onCommitDrafts: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var voiceError by remember { mutableStateOf<String?>(null) }

    val voiceHandler = remember {
        VoiceAssistantHandler(
            context = context,
            onTranscriptUpdate = { text ->
                onTranscriptChange(text)
            },
            onListeningStateChanged = { listening ->
                onListeningChanged(listening)
            },
            onError = { err ->
                voiceError = err
                onListeningChanged(false)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHandler.stopListening()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceError = null
            voiceHandler.startListening()
        } else {
            voiceError = "Microphone permission required for speech recognition. You can still type or choose a sample rant below."
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Voice-to-Action",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Speak naturally — LIFEOS extracts tasks & deadlines",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Pulsing Visualizer Center Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow ring
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.25f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isListening) listOf(NeonCyan, NeonPurple)
                                else listOf(ObsidianCard, ObsidianBlack)
                            )
                        )
                        .border(
                            1.5.dp,
                            if (isListening) NeonCyan else ObsidianCardBorder,
                            CircleShape
                        )
                        .clickable {
                            if (isListening) {
                                voiceHandler.stopListening()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    voiceError = null
                                    voiceHandler.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("record_speech_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Speaking",
                        tint = if (isListening) ObsidianBlack else NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = if (isListening) "Listening… Speak your schedule rant" else "Tap microphone to speak or edit text below",
                style = MaterialTheme.typography.bodySmall,
                color = if (isListening) NeonCyan else TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (voiceError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voiceError ?: "",
                    fontSize = 12.sp,
                    color = NeonAmber,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset Rants for immediate testing
            Text(
                text = "Quick Sample Rants:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VoiceAssistantHandler.SAMPLE_RANTS) { rant ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianCard)
                            .border(0.8.dp, ObsidianCardBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                onTranscriptChange(rant)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rant.take(38) + "…",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transcript input area
            OutlinedTextField(
                value = transcriptText,
                onValueChange = onTranscriptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transcript_input_field"),
                placeholder = {
                    Text(
                        "e.g. I have to finish my job assignment by Friday, prepare for an interview, pay my electricity bill, and study Java for an hour every day.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianBlack,
                    unfocusedContainerColor = ObsidianBlack,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = { onProcessChaos(transcriptText) },
                enabled = transcriptText.isNotBlank() && !isAnalyzing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("turn_chaos_to_action_submit")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ObsidianBlack,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyzing with AI…",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turn Chaos Into Action",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (!statusBanner.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusBanner,
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Extracted Drafts Section
            if (extractedDrafts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Action Items (${extractedDrafts.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(extractedDrafts) { index, draft ->
                        DraftCard(
                            draft = draft,
                            onToggle = { onToggleDraft(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCommitDrafts,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonEmerald,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("commit_drafts_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule Selected Tasks to Today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DraftCard(
    draft: TaskDraft,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (draft.isSelected) draft.category.color.copy(alpha = 0.5f) else ObsidianCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (draft.isSelected) ObsidianCard else ObsidianBlack
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (draft.isSelected) NeonCyan else ObsidianBlack
                    )
                    .border(
                        1.dp,
                        if (draft.isSelected) NeonCyan else ObsidianCardBorder,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (draft.isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${draft.startTime} • ${draft.durationMinutes}m",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${draft.category.displayName}",
                        fontSize = 11.sp,
                        color = draft.category.color
                    )
                }
                if (!draft.extractedMeta.isNullOrBlank()) {
                    Text(
                        text = draft.extractedMeta,
                        fontSize = 11.sp,
                        color = NeonCyan.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
