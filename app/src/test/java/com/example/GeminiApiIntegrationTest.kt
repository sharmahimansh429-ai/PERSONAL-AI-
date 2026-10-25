package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiCandidate
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiErrorResponse
import com.example.data.api.GeminiGenerateRequest
import com.example.data.api.GeminiGenerateResponse
import com.example.data.api.GeminiPart
import com.example.data.db.ChatDao
import com.example.data.db.ConversationDao
import com.example.data.db.MemoryDao
import com.example.data.db.NexusDatabase
import com.example.data.repository.NexusRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiApiIntegrationTest {

    private lateinit var context: Context
    private lateinit var db: NexusDatabase
    private lateinit var chatDao: ChatDao
    private lateinit var convDao: ConversationDao
    private lateinit var memoryDao: MemoryDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = NexusDatabase.getInstance(context)
        chatDao = db.chatDao()
        convDao = db.conversationDao()
        memoryDao = db.memoryDao()
    }

    @Test
    fun testOfficialApiKeyConfiguration() {
        val repo = NexusRepository(context, chatDao, convDao, memoryDao)
        repo.setCustomApiKey("TEST_API_KEY_AIzaSy")

        assertEquals("TEST_API_KEY_AIzaSy", repo.getApiKey())
        assertTrue(repo.isApiKeyConfigured())
    }

    @Test
    fun testSuccessfulGeminiContentGeneration() = runBlocking {
        var usedApiKey = ""
        var usedModel = ""

        val fakeGeminiService = object : GeminiApiService {
            override suspend fun generateContent(
                model: String,
                apiKey: String,
                request: GeminiGenerateRequest
            ): GeminiGenerateResponse {
                usedApiKey = apiKey
                usedModel = model
                return GeminiGenerateResponse(
                    candidates = listOf(
                        GeminiCandidate(
                            content = GeminiContent(
                                role = "model",
                                parts = listOf(GeminiPart(text = "NEXUS online. Systems nominal."))
                            )
                        )
                    ),
                    error = null
                )
            }
        }

        val repo = NexusRepository(
            context = context,
            chatDao = chatDao,
            conversationDao = convDao,
            memoryDao = memoryDao,
            geminiService = fakeGeminiService
        )
        repo.setCustomApiKey("OFFICIAL_GEMINI_KEY")

        val convId = repo.createConversation("Test Session")
        val result = repo.askGemini(convId, "Greetings Nexus")

        assertTrue(result.isSuccess)
        assertEquals("NEXUS online. Systems nominal.", result.getOrNull())
        assertEquals("OFFICIAL_GEMINI_KEY", usedApiKey)
        assertEquals("gemini-3.5-flash", usedModel)
    }

    @Test
    fun testErrorSanitizationDoesNotExposeKey() = runBlocking {
        val fakeGeminiService = object : GeminiApiService {
            override suspend fun generateContent(
                model: String,
                apiKey: String,
                request: GeminiGenerateRequest
            ): GeminiGenerateResponse {
                return GeminiGenerateResponse(
                    candidates = null,
                    error = GeminiErrorResponse(
                        code = 400,
                        message = "API key AIzaSy1234567890abcdefghijklmn is invalid"
                    )
                )
            }
        }

        val repo = NexusRepository(
            context = context,
            chatDao = chatDao,
            conversationDao = convDao,
            memoryDao = memoryDao,
            geminiService = fakeGeminiService
        )
        repo.setCustomApiKey("AIzaSy1234567890abcdefghijklmn")

        val convId = repo.createConversation("Test Session")
        val result = repo.askGemini(convId, "Test key masking")

        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(!errorMessage.contains("AIzaSy1234567890abcdefghijklmn"))
        assertTrue(errorMessage.contains("[REDACTED_API_KEY]"))
    }
}
