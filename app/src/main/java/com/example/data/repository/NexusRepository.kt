package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerateRequest
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.db.ChatDao
import com.example.data.db.ConversationDao
import com.example.data.db.MemoryDao
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.NexusMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NexusRepository(
    private val context: Context,
    private val chatDao: ChatDao,
    private val conversationDao: ConversationDao,
    private val memoryDao: MemoryDao,
    private val geminiService: GeminiApiService = GeminiApiService.create()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_CUSTOM_API_KEY = "pref_custom_api_key"
        const val PREF_SELECTED_MODEL = "pref_selected_model"
        const val PREF_LANGUAGE_MODE = "pref_language_mode"
        const val PREF_PERSONA_STYLE = "pref_persona_style"
        const val PREF_AUTO_SPEAK = "pref_auto_speak"
        const val PREF_TTS_SPEED = "pref_tts_speed"
        const val PREF_TTS_PITCH = "pref_tts_pitch"
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        const val FALLBACK_MODEL = "gemini-3.5-flash"
    }

    // Conversations Flow
    val allConversations: Flow<List<Conversation>> = conversationDao.getAllConversations()

    // Memories Flow
    val allMemories: Flow<List<NexusMemory>> = memoryDao.getAllMemories()

    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>> =
        chatDao.getMessagesForConversation(conversationId)

    suspend fun getActiveMemories(): List<NexusMemory> = withContext(Dispatchers.IO) {
        memoryDao.getActiveMemoriesList()
    }

    suspend fun seedInitialMemoriesIfEmpty() = withContext(Dispatchers.IO) {
        if (memoryDao.getMemoryCount() == 0) {
            val seeds = listOf(
                NexusMemory(
                    key = "Bilingual Fluency",
                    fact = "User communicates in both English and Hindi. Reply in Hindi (शुद्ध/सहज हिंदी) when addressed in Hindi, and English when in English.",
                    category = "Language",
                    isActive = true
                ),
                NexusMemory(
                    key = "AI Identity",
                    fact = "You are NEXUS AI: an ultra-modern, sentient-styled personal intelligence with deep reasoning and concise execution.",
                    category = "Personal",
                    isActive = true
                ),
                NexusMemory(
                    key = "Response Tone",
                    fact = "Provide insightful, accurate, and articulate answers. Use clean formatting and bullet points when explaining complex topics.",
                    category = "Preference",
                    isActive = true
                )
            )
            memoryDao.insertMemories(seeds)
        }
    }

    // Official Gemini API Key getter (checks manual override, then managed BuildConfig / ENV)
    fun getApiKey(): String {
        val custom = prefs.getString(PREF_CUSTOM_API_KEY, "")?.trim().orEmpty()
        if (custom.isNotBlank()) return custom

        val envKey = try {
            BuildConfig.ENV_GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") return envKey

        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun isApiKeyConfigured(): Boolean {
        return getApiKey().isNotBlank()
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun sanitizeModelName(model: String): String {
        var m = model.trim()
        if (m.startsWith("models/")) {
            m = m.removePrefix("models/")
        }
        if (m == "gemini-2.5-flash" || m == "gemini-2.0-flash") {
            m = DEFAULT_MODEL
        }
        return m
    }

    fun getSelectedModel(): String {
        val raw = prefs.getString(PREF_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        return sanitizeModelName(raw)
    }

    fun setSelectedModel(model: String) {
        val clean = sanitizeModelName(model)
        prefs.edit().putString(PREF_SELECTED_MODEL, clean).apply()
    }

    fun getLanguageMode(): String {
        return prefs.getString(PREF_LANGUAGE_MODE, "Bilingual (Auto)") ?: "Bilingual (Auto)"
    }

    fun setLanguageMode(mode: String) {
        prefs.edit().putString(PREF_LANGUAGE_MODE, mode).apply()
    }

    fun getPersonaStyle(): String {
        return prefs.getString(PREF_PERSONA_STYLE, "Futuristic AI") ?: "Futuristic AI"
    }

    fun setPersonaStyle(style: String) {
        prefs.edit().putString(PREF_PERSONA_STYLE, style).apply()
    }

    fun isAutoSpeakEnabled(): Boolean {
        return prefs.getBoolean(PREF_AUTO_SPEAK, false)
    }

    fun setAutoSpeakEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_AUTO_SPEAK, enabled).apply()
    }

    fun getTtsSpeed(): Float = prefs.getFloat(PREF_TTS_SPEED, 1.0f)
    fun setTtsSpeed(speed: Float) = prefs.edit().putFloat(PREF_TTS_SPEED, speed).apply()

    fun getTtsPitch(): Float = prefs.getFloat(PREF_TTS_PITCH, 1.0f)
    fun setTtsPitch(pitch: Float) = prefs.edit().putFloat(PREF_TTS_PITCH, pitch).apply()

    // Conversation management
    suspend fun createConversation(title: String = "New Nexus Session"): Long = withContext(Dispatchers.IO) {
        val conv = Conversation(
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            snippet = "Session started"
        )
        conversationDao.insertConversation(conv)
    }

    suspend fun updateConversationTitle(id: Long, title: String) = withContext(Dispatchers.IO) {
        val conv = conversationDao.getConversationById(id)
        if (conv != null) {
            conversationDao.updateConversation(conv.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteConversation(id: Long) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        conversationDao.deleteAllConversations()
    }

    // Memory Management
    suspend fun insertMemory(memory: NexusMemory): Long = withContext(Dispatchers.IO) {
        memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: NexusMemory) = withContext(Dispatchers.IO) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun deleteAllMemories() = withContext(Dispatchers.IO) {
        memoryDao.deleteAllMemories()
    }

    // Chat Message DB operations
    suspend fun insertMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        val id = chatDao.insertMessage(message)
        // Update snippet and updatedAt on the conversation
        val conv = conversationDao.getConversationById(message.conversationId)
        if (conv != null) {
            val snippet = if (message.text.length > 60) message.text.take(60) + "..." else message.text
            val newTitle = if (conv.snippet == "Session started" && message.sender == "user") {
                if (message.text.length > 30) message.text.take(30) + "..." else message.text
            } else conv.title

            conversationDao.updateConversation(
                conv.copy(
                    title = newTitle,
                    snippet = snippet,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        id
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteMessageById(messageId)
    }

    // Gemini API Query with Multi-Turn & Memory Injection
    suspend fun askGemini(
        conversationId: Long,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set GEMINI_API_KEY in AI Studio Secrets panel or enter it in App Settings.")
            )
        }

        val activeMemories = memoryDao.getActiveMemoriesList()
        val memoryBlock = if (activeMemories.isNotEmpty()) {
            buildString {
                appendLine("USER PERSONAL MEMORIES AND PREFERENCES:")
                activeMemories.forEach {
                    appendLine("- [${it.category}] ${it.key}: ${it.fact}")
                }
            }
        } else {
            "No specific user memories stored yet."
        }

        val persona = getPersonaStyle()
        val languagePreference = getLanguageMode()

        val systemInstructionText = """
            You are NEXUS AI, an elite, ultra-capable personal artificial intelligence assistant.
            Design & Persona: $persona. You are thoughtful, futuristic, insightful, fast, and helpful.
            
            LANGUAGE CAPABILITY:
            - Full bilingual proficiency in English and Hindi.
            - Current user language setting: $languagePreference.
            - If user speaks or asks in Hindi, answer in fluent, natural, respectful Hindi (Devanagari script or Hinglish if requested).
            - If user speaks in English, reply in polished, articulate English.
            - You can switch effortlessly between Hindi and English (code-switching) whenever natural.
            
            PERSONAL MEMORY SYSTEM:
            $memoryBlock
            
            Always respect and utilize these personal memories seamlessly to personalize your responses.
            Keep formatting clean with clear paragraphs, code blocks, or bullet points. Avoid filler text.
        """.trimIndent()

        // Fetch recent conversation history to provide context (last 10 messages)
        val history = chatDao.getMessagesList(conversationId)
        val recentMessages = history.takeLast(10)

        val contents = mutableListOf<GeminiContent>()
        for (msg in recentMessages) {
            val role = if (msg.sender == "user") "user" else "model"
            if (msg.text.isNotBlank() && !msg.isError) {
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // Add the current user prompt if not already present in the list
        if (contents.isEmpty() || contents.last().role != "user" || contents.last().parts.firstOrNull()?.text != userPrompt) {
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userPrompt))
                )
            )
        }

        val request = GeminiGenerateRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                role = "system",
                parts = listOf(GeminiPart(text = systemInstructionText))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                maxOutputTokens = 2048
            )
        )

        val primaryModel = getSelectedModel()
        var result = executeGeminiRequest(primaryModel, apiKey, request)

        // Automatic fallback if primary model returns 404 (model retired) or 503 (high demand)
        if (result.isFailure && primaryModel != FALLBACK_MODEL) {
            val fallbackResult = executeGeminiRequest(FALLBACK_MODEL, apiKey, request)
            if (fallbackResult.isSuccess) {
                setSelectedModel(FALLBACK_MODEL)
                result = fallbackResult
            }
        }
        result
    }

    private fun sanitizeErrorMessage(rawMessage: String): String {
        return rawMessage.replace(Regex("AIza[0-9A-Za-z\\-_]{20,}"), "[REDACTED_API_KEY]")
    }

    suspend fun executeGeminiRequest(
        model: String,
        apiKey: String,
        request: GeminiGenerateRequest
    ): Result<String> {
        return try {
            val response = geminiService.generateContent(
                model = sanitizeModelName(model),
                apiKey = apiKey,
                request = request
            )

            if (response.error != null) {
                val code = response.error.code ?: 0
                val msg = response.error.message ?: "Gemini API error code $code"
                val sanitized = sanitizeErrorMessage(msg)
                return Result.failure(Exception(sanitized))
            }

            val text = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Nexus received an empty response from Gemini ($model)."))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val code = e.code()
            val parsedMsg = if (!errorBody.isNullOrBlank()) {
                if (errorBody.contains("\"message\":")) {
                    errorBody.substringAfter("\"message\":").substringBefore("\",").trim(' ', '"', '\n', '\r', '\\')
                } else {
                    "HTTP $code: $errorBody"
                }
            } else {
                "HTTP $code (${e.message()})"
            }
            val sanitized = sanitizeErrorMessage(parsedMsg)
            Result.failure(Exception(sanitized))
        } catch (e: Exception) {
            val sanitized = sanitizeErrorMessage(e.message ?: "Network error")
            Result.failure(Exception(sanitized))
        }
    }

    suspend fun verifyConnection(): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No API key configured."))
        }
        val model = getSelectedModel()
        val testRequest = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Ping"))
                )
            ),
            generationConfig = GeminiGenerationConfig(maxOutputTokens = 5)
        )
        val result = executeGeminiRequest(model, apiKey, testRequest)
        if (result.isFailure && model != FALLBACK_MODEL) {
            val fallback = executeGeminiRequest(FALLBACK_MODEL, apiKey, testRequest)
            if (fallback.isSuccess) {
                setSelectedModel(FALLBACK_MODEL)
                return@withContext Result.success(FALLBACK_MODEL)
            }
        }
        if (result.isSuccess) Result.success(model) else result
    }
}
