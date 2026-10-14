package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotAnalysisSheet(
    sheetState: SheetState,
    isAnalyzing: Boolean,
    extractedDrafts: List<TaskDraft>,
    statusBanner: String?,
    onProcessScreenshot: (Bitmap, String) -> Unit,
    onToggleDraft: (Int) -> Unit,
    onCommitDrafts: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userNotes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                if (bitmap != null) {
                    onProcessScreenshot(bitmap, userNotes)
                }
            } catch (_: Exception) {}
        }
    }

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
                            .background(NeonPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Screenshot Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Extract dates, bills, prices & action items",
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

            // Photo picker button / preview
            if (selectedBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(ObsidianBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, ObsidianCardBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(16.dp)
                        .testTag("pick_screenshot_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose Screenshot from Photos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bill receipt, email notice, or chat deadline",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Demo Screenshot buttons
            Text(
                text = "Or Test with Sample Screenshots:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Preset 1: Electricity Bill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianCard)
                        .border(0.8.dp, ObsidianCardBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            val dummyBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                            selectedBitmap = dummyBitmap
                            onProcessScreenshot(
                                dummyBitmap,
                                "Electricity Utility Bill: Account #98321. Amount Due: $84.20 by Oct 18. Auto-pay required."
                            )
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚡ Electricity Bill",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                        Text(
                            text = "$84.20 Due",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Preset 2: Interview confirmation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianCard)
                        .border(0.8.dp, ObsidianCardBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            val dummyBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                            selectedBitmap = dummyBitmap
                            onProcessScreenshot(
                                dummyBitmap,
                                "Calendar invite: Senior Android Engineer Technical Interview at 2:00 PM Thursday on Google Meet."
                            )
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💼 Interview Invite",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "2:00 PM Meet",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User context notes
            OutlinedTextField(
                value = userNotes,
                onValueChange = { userNotes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Optional instructions (e.g. Schedule payment before Friday)",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                },
                maxLines = 2,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianBlack,
                    unfocusedContainerColor = ObsidianBlack,
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = ObsidianCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NeonPurple,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Extracting dates, prices & actions…",
                        fontSize = 13.sp,
                        color = NeonPurple,
                        fontWeight = FontWeight.SemiBold
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

            // Extracted Drafts
            if (extractedDrafts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Commitments (${extractedDrafts.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
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
                        .testTag("commit_screenshot_drafts_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commit Extracted Tasks to Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
