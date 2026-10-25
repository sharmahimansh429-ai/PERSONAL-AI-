package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NexusDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.NexusMemory
import com.example.data.repository.NexusRepository
import com.example.ui.components.OrbState
import com.example.voice.SpeechManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ConnectionState {
    object Checking : ConnectionState()
    data class Online(val model: String) : ConnectionState()
    object NoKey : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NexusDatabase.getInstance(application)
    val repository = NexusRepository(
        context = application,
        chatDao = database.chatDao(),
        conversationDao = database.conversationDao(),
        memoryDao = database.memoryDao()
    )

    val speechManager = SpeechManager(application)
    val ttsManager = TextToSpeechManager(application)

    // Current Conversation ID
    private val _currentConversationId = MutableStateFlow<Long>(0L)
    val currentConversationId: StateFlow<Long> = _currentConversationId.asStateFlow()

    // Conversations List
    val conversations: StateFlow<List<Conversation>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Messages for current conversation
    val currentMessages: StateFlow<List<ChatMessage>> = _currentConversationId
        .flatMapLatest { convId ->
            repository.getMessagesForConversation(convId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Memories List
    val memories: StateFlow<List<NexusMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Input & State
    val inputText = MutableStateFlow("")

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isApiKeyConfigured = MutableStateFlow(repository.isApiKeyConfigured())
    val selectedModel = MutableStateFlow(repository.getSelectedModel())
    val languageMode = MutableStateFlow(repository.getLanguageMode())
    val personaStyle = MutableStateFlow(repository.getPersonaStyle())
    val isAutoSpeak = MutableStateFlow(repository.isAutoSpeakEnabled())
    val ttsSpeed = MutableStateFlow(repository.getTtsSpeed())
    val ttsPitch = MutableStateFlow(repository.getTtsPitch())

    // Accurate Connection State (Tested via Live API Ping)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Checking)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var lastPrompt: String? = null

    // Dynamic Orb State
    val orbState: StateFlow<OrbState> = combine(
        _isGenerating,
        speechManager.speechState,
        ttsManager.isSpeaking,
        _errorMessage
    ) { generating, speechState, speaking, error ->
        when {
            error != null -> OrbState.ERROR
            generating -> OrbState.THINKING
            speechState is SpeechManager.SpeechState.Listening -> OrbState.LISTENING
            speaking -> OrbState.SPEAKING
            else -> OrbState.IDLE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrbState.IDLE)

    init {
        viewModelScope.launch {
            repository.seedInitialMemoriesIfEmpty()
            // Initialize or load latest conversation
            conversations.collect { convList ->
                if (_currentConversationId.value == 0L) {
                    if (convList.isNotEmpty()) {
                        _currentConversationId.value = convList.first().id
                    } else {
                        val newId = repository.createConversation("Session #1")
                        _currentConversationId.value = newId
                    }
                }
            }
        }

        // Check connection on startup
        checkConnection()

        // Observe speech recognition
        viewModelScope.launch {
            speechManager.speechState.collect { state ->
                when (state) {
                    is SpeechManager.SpeechState.PartialResult -> {
                        inputText.value = state.text
                    }
                    is SpeechManager.SpeechState.FinalResult -> {
                        inputText.value = state.text
                        sendMessage(state.text)
                        speechManager.resetState()
                    }
                    is SpeechManager.SpeechState.Error -> {
                        _errorMessage.value = state.message
                    }
                    else -> {}
                }
            }
        }
    }

    fun checkConnection() {
        isApiKeyConfigured.value = repository.isApiKeyConfigured()
        if (!repository.isApiKeyConfigured()) {
            _connectionState.value = ConnectionState.NoKey
            return
        }
        _connectionState.value = ConnectionState.Checking
        viewModelScope.launch {
            val result = repository.verifyConnection()
            result.onSuccess { activeModel ->
                _connectionState.value = ConnectionState.Online(activeModel)
                selectedModel.value = repository.getSelectedModel()
            }.onFailure { err ->
                val msg = err.message ?: "Connection test failed"
                _connectionState.value = ConnectionState.Error(msg)
            }
        }
    }

    fun startListening() {
        val lang = languageMode.value
        speechManager.startListening(lang)
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun toggleListening() {
        if (speechManager.speechState.value is SpeechManager.SpeechState.Listening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun speakMessage(message: ChatMessage) {
        if (ttsManager.currentlySpeakingId.value == message.id) {
            ttsManager.stop()
        } else {
            ttsManager.speak(
                text = message.text,
                messageId = message.id,
                speed = ttsSpeed.value,
                pitch = ttsPitch.value
            )
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun sendMessage(textOverride: String? = null) {
        val text = (textOverride ?: inputText.value).trim()
        if (text.isBlank() || _isGenerating.value) return

        val convId = _currentConversationId.value
        if (convId == 0L) return

        lastPrompt = text
        inputText.value = ""
        _isGenerating.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Save user message
            repository.insertMessage(
                ChatMessage(
                    conversationId = convId,
                    sender = "user",
                    text = text,
                    language = languageMode.value
                )
            )

            // Query Gemini
            val result = repository.askGemini(convId, text)
            _isGenerating.value = false

            result.onSuccess { aiReply ->
                val msgId = repository.insertMessage(
                    ChatMessage(
                        conversationId = convId,
                        sender = "nexus",
                        text = aiReply,
                        language = languageMode.value
                    )
                )

                // Update connection state to verified online
                _connectionState.value = ConnectionState.Online(repository.getSelectedModel())

                if (isAutoSpeak.value) {
                    ttsManager.speak(
                        text = aiReply,
                        messageId = msgId,
                        speed = ttsSpeed.value,
                        pitch = ttsPitch.value
                    )
                }
            }.onFailure { error ->
                val errorText = error.message ?: "An unexpected error occurred."
                _errorMessage.value = errorText
                _connectionState.value = ConnectionState.Error(errorText)
                repository.insertMessage(
                    ChatMessage(
                        conversationId = convId,
                        sender = "nexus",
                        text = "⚠️ [SYSTEM ALERT]: $errorText",
                        isError = true
                    )
                )
            }
        }
    }

    fun retryLastMessage() {
        lastPrompt?.let { prompt ->
            _errorMessage.value = null
            sendMessage(prompt)
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val count = conversations.value.size + 1
            val newId = repository.createConversation("Nexus Session #$count")
            _currentConversationId.value = newId
            ttsManager.stop()
        }
    }

    fun selectConversation(id: Long) {
        _currentConversationId.value = id
        ttsManager.stop()
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    _currentConversationId.value = remaining.first().id
                } else {
                    val newId = repository.createConversation("Nexus Session #1")
                    _currentConversationId.value = newId
                }
            }
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearAllConversations()
            val newId = repository.createConversation("Nexus Session #1")
            _currentConversationId.value = newId
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // Memories
    fun addMemory(key: String, fact: String, category: String) {
        viewModelScope.launch {
            repository.insertMemory(
                NexusMemory(
                    key = key.trim(),
                    fact = fact.trim(),
                    category = category,
                    isActive = true
                )
            )
        }
    }

    fun updateMemory(memory: NexusMemory) {
        viewModelScope.launch {
            repository.updateMemory(memory)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun toggleMemoryActive(memory: NexusMemory) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isActive = !memory.isActive))
        }
    }

    // Settings
    fun updateCustomApiKey(key: String) {
        repository.setCustomApiKey(key)
        isApiKeyConfigured.value = repository.isApiKeyConfigured()
        checkConnection()
    }

    fun updateModel(model: String) {
        repository.setSelectedModel(model)
        selectedModel.value = repository.getSelectedModel()
        checkConnection()
    }

    fun updateLanguageMode(mode: String) {
        repository.setLanguageMode(mode)
        languageMode.value = mode
    }

    fun updatePersonaStyle(style: String) {
        repository.setPersonaStyle(style)
        personaStyle.value = style
    }

    fun updateAutoSpeak(enabled: Boolean) {
        repository.setAutoSpeakEnabled(enabled)
        isAutoSpeak.value = enabled
    }

    fun updateTtsSettings(speed: Float, pitch: Float) {
        repository.setTtsSpeed(speed)
        repository.setTtsPitch(pitch)
        ttsSpeed.value = speed
        ttsPitch.value = pitch
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
