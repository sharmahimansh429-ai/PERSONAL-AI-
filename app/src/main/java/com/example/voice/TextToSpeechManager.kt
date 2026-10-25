package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<Long?>(null)
    val currentlySpeakingId: StateFlow<Long?> = _currentlySpeakingId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }
            })
        }
    }

    fun speak(text: String, messageId: Long? = null, speed: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isInitialized || tts == null) return

        stop()

        val cleanText = cleanMarkdownForSpeech(text)
        if (cleanText.isBlank()) return

        // Detect Hindi characters (Devanagari Unicode block \u0900-\u097F)
        val hasHindi = cleanText.any { it.code in 0x0900..0x097F }
        val targetLocale = if (hasHindi) Locale("hi", "IN") else Locale.US

        try {
            tts?.language = targetLocale
            tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            _currentlySpeakingId.value = messageId
            val utteranceId = "utterance_${messageId ?: System.currentTimeMillis()}"
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            _isSpeaking.value = true
        } catch (_: Exception) {
            _isSpeaking.value = false
            _currentlySpeakingId.value = null
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }

    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), "Code omitted.")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown links [title](url) -> title
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            // Remove headers #, ##, etc.
            .replace(Regex("#{1,6}\\s*"), "")
            // Remove bold/italics
            .replace(Regex("[*_~]{1,3}"), "")
            // Remove blockquotes
            .replace(Regex("^>\\s*", RegexOption.MULTILINE), "")
            // Replace multiple newlines with period
            .replace(Regex("\n+"), ". ")
            .trim()
    }
}
