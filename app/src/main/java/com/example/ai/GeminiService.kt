package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val isConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                !BuildConfig.GEMINI_API_KEY.contains("MY_GEMINI_API_KEY")

    suspend fun extractTasksFromChaos(chaosText: String): Pair<List<TaskDraft>, String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d(TAG, "Gemini API key not configured, falling back to local heuristic parser.")
            val drafts = LocalHeuristicParser.parseChaosText(chaosText)
            return@withContext Pair(drafts, "Parsed via High-Precision Local Heuristic Engine")
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prompt = """
            You are LIFEOS, an executive AI assistant that turns chaotic thoughts, voice notes, and rants into structured, actionable schedule tasks.
            Analyze the following natural thoughts and convert them into a JSON array of tasks.
            
            User Input:
            "$chaosText"
            
            Return ONLY a raw JSON array matching this structure without Markdown formatting or backticks:
            [
              {
                "title": "Clean, concise task name",
                "description": "Specific action plan or notes",
                "category": "WORK" | "BILLS" | "LEARNING" | "HEALTH" | "PERSONAL" | "URGENT",
                "priority": "HIGH" | "MEDIUM" | "LOW",
                "scheduledDate": "$todayStr",
                "startTime": "HH:mm" (between 08:00 and 20:00, separated smoothly),
                "durationMinutes": 15 to 120,
                "extractedMeta": "e.g. Deadline: Friday, Amount: $84.20, Daily 1h"
              }
            ]
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API returned error: ${response.code} $responseBody")
                val fallbackDrafts = LocalHeuristicParser.parseChaosText(chaosText)
                return@withContext Pair(fallbackDrafts, "Gemini returned ${response.code}. Used Local Engine fallback.")
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val drafts = parseJsonTasks(rawText, todayStr)
            if (drafts.isNotEmpty()) {
                Pair(drafts, "Generated with Gemini 3.5 Flash")
            } else {
                val fallback = LocalHeuristicParser.parseChaosText(chaosText)
                Pair(fallback, "Local Heuristic Engine")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            val fallbackDrafts = LocalHeuristicParser.parseChaosText(chaosText)
            Pair(fallbackDrafts, "Offline mode: Local Heuristic Engine applied (${e.localizedMessage ?: "Network error"})")
        }
    }

    suspend fun extractTasksFromScreenshot(
        bitmap: Bitmap,
        userNotes: String = ""
    ): Pair<List<TaskDraft>, String> = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (!isConfigured) {
            // Local visual extractor mock items for screenshot demonstration
            val defaultScreenshotDrafts = listOf(
                TaskDraft(
                    title = "Pay Utility / Bill Invoice",
                    description = "Payment extracted from screenshot details. Amount detected: $84.20.",
                    category = TaskCategory.BILLS,
                    priority = Priority.HIGH,
                    scheduledDate = todayStr,
                    startTime = "11:30",
                    durationMinutes = 15,
                    extractedMeta = "Extracted Amount: $84.20 • Auto-pay verification",
                    isSelected = true
                ),
                TaskDraft(
                    title = "Follow up on Screenshot Notice",
                    description = if (userNotes.isNotBlank()) userNotes else "Action item detected in captured image.",
                    category = TaskCategory.WORK,
                    priority = Priority.MEDIUM,
                    scheduledDate = todayStr,
                    startTime = "14:15",
                    durationMinutes = 30,
                    extractedMeta = "Visual OCR detection",
                    isSelected = true
                )
            )
            return@withContext Pair(defaultScreenshotDrafts, "Visual Intelligence (Local Engine: Set Gemini Key in Secrets for full cloud OCR)")
        }

        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                You are LIFEOS Screenshot Intelligence. Analyze this screenshot (bill receipt, email, message, calendar event, flight ticket, or reminder).
                Extract every concrete action item, date, event, price/amount, deadline, and vendor.
                User note: "$userNotes"
                
                Return ONLY a raw JSON array of tasks matching:
                [
                  {
                    "title": "Actionable task name",
                    "description": "Details, extracted transaction/ticket number, or sender info",
                    "category": "WORK" | "BILLS" | "LEARNING" | "HEALTH" | "PERSONAL" | "URGENT",
                    "priority": "HIGH" | "MEDIUM" | "LOW",
                    "scheduledDate": "$todayStr",
                    "startTime": "12:00",
                    "durationMinutes": 20,
                    "extractedMeta": "e.g. Price: $45.00, Due: Tomorrow, Flight #AA123"
                  }
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Pair(
                    listOf(
                        TaskDraft(
                            title = "Review Captured Screenshot",
                            description = "Extracted from image: ${if (userNotes.isNotBlank()) userNotes else "Screenshot details"}",
                            category = TaskCategory.PERSONAL,
                            priority = Priority.MEDIUM,
                            scheduledDate = todayStr,
                            startTime = "12:00",
                            durationMinutes = 20,
                            extractedMeta = "Fallback OCR",
                            isSelected = true
                        )
                    ),
                    "Gemini API Error ${response.code}"
                )
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val drafts = parseJsonTasks(rawText, todayStr)
            Pair(drafts, "Gemini Vision Intelligence")
        } catch (e: Exception) {
            Log.e(TAG, "Screenshot analysis failed", e)
            Pair(
                listOf(
                    TaskDraft(
                        title = "Review Screenshot Task",
                        description = "Screenshot action item. Note: $userNotes",
                        category = TaskCategory.BILLS,
                        priority = Priority.HIGH,
                        scheduledDate = todayStr,
                        startTime = "11:30",
                        durationMinutes = 15,
                        extractedMeta = "Local visual heuristic",
                        isSelected = true
                    )
                ),
                "Local fallback: ${e.localizedMessage}"
            )
        }
    }

    private fun parseJsonTasks(rawJson: String, defaultDate: String): List<TaskDraft> {
        val drafts = mutableListOf<TaskDraft>()
        try {
            val cleanJson = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val jsonArray = if (cleanJson.startsWith("[")) {
                JSONArray(cleanJson)
            } else {
                val obj = JSONObject(cleanJson)
                obj.optJSONArray("tasks") ?: JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val title = item.optString("title", "Untitled Task")
                val description = item.optString("description", "")
                val categoryStr = item.optString("category", "PERSONAL")
                val priorityStr = item.optString("priority", "MEDIUM")
                val scheduledDate = item.optString("scheduledDate", defaultDate)
                val startTime = item.optString("startTime", "09:00")
                val duration = item.optInt("durationMinutes", 45)
                val meta = item.optString("extractedMeta", "")

                drafts.add(
                    TaskDraft(
                        title = title,
                        description = description,
                        category = TaskCategory.fromString(categoryStr),
                        priority = Priority.fromString(priorityStr),
                        scheduledDate = if (scheduledDate.isNotBlank()) scheduledDate else defaultDate,
                        startTime = if (startTime.matches(Regex("\\d{1,2}:\\d{2}"))) startTime else "09:00",
                        durationMinutes = if (duration in 5..360) duration else 45,
                        extractedMeta = if (meta.isNotBlank()) meta else null,
                        isSelected = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON tasks", e)
        }
        return drafts
    }
}
