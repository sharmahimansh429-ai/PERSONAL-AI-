package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.ApiKeyWarningBanner
import com.example.ui.components.NexusConnectionIndicator
import com.example.ui.components.NexusOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NexusBorderHairline
import com.example.ui.theme.NexusCardDark
import com.example.ui.theme.NexusCharcoal
import com.example.ui.theme.NexusDeepBlack
import com.example.ui.theme.NexusPureBlack
import com.example.ui.theme.NexusSilver
import com.example.ui.theme.NexusSilverBright
import com.example.ui.theme.NexusSilverMuted
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTitanium
import com.example.ui.theme.ParticleCyan
import com.example.viewmodel.ConnectionState
import com.example.viewmodel.NexusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: NexusViewModel,
    onOpenMenu: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.currentMessages.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val soundLevel by viewModel.speechManager.soundLevel.collectAsState()
    val speakingId by viewModel.ttsManager.currentlySpeakingId.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    var showMicRationale by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Permission launcher for RECORD_AUDIO
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice input.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Auto scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusPureBlack)
            .imePadding()
    ) {
        // Clean Minimal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = NexusSilverBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEXUS",
                    color = NexusSilverBright,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSpeaking) {
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(NexusSurfaceDark, CircleShape)
                            .border(1.dp, NeonRed, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speaking",
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Small connection indicator light: green when connected, red when offline, grey while checking
                NexusConnectionIndicator(
                    state = connectionState,
                    onIndicatorClick = {
                        if (connectionState is ConnectionState.Error || connectionState is ConnectionState.NoKey) {
                            viewModel.checkConnection()
                        }
                    },
                    modifier = Modifier.testTag("connection_indicator")
                )
            }
        }

        // Api Key Alert if missing
        if (!isApiKeyConfigured || connectionState is ConnectionState.NoKey) {
            ApiKeyWarningBanner(onConfigureClick = onNavigateToSettings)
        }

        // Retry banner if the last message was an error
        val lastMessage = messages.lastOrNull()
        if (lastMessage?.isError == true && !isGenerating) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = Color(0xFF1A1114),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last transmission failed. Tap to retry.",
                        color = Color(0xFFFFB3B8),
                        fontSize = 12.sp
                    )
                    OutlinedButton(
                        onClick = { viewModel.retryLastMessage() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTitanium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontSize = 12.sp)
                    }
                }
            }
        }

        // Center Messages & 3D Particle Core area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Completely clean initial screen: no greetings, no sample prompts, only the 3D particle entity
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NexusOrb(
                        orbState = orbState,
                        soundLevel = soundLevel,
                        orbSize = 250.dp,
                        onClick = {
                            if (orbState == OrbState.LISTENING) {
                                viewModel.stopListening()
                            } else {
                                showMicRationale = true
                            }
                        }
                    )
                }
            } else {
                // Conversation list with compact top particle core bar
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (orbState == OrbState.LISTENING) {
                                    viewModel.stopListening()
                                } else {
                                    showMicRationale = true
                                }
                            },
                        color = NexusCharcoal.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NexusOrb(
                                orbState = orbState,
                                soundLevel = soundLevel,
                                orbSize = 48.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "NEXUS",
                                color = NexusSilverBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.createNewSession() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Session",
                                    tint = NexusTitanium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageCard(
                                message = message,
                                isSpeaking = speakingId == message.id,
                                onSpeakClick = { viewModel.speakMessage(message) },
                                onDeleteClick = { viewModel.deleteMessage(message.id) },
                                onRetryClick = { viewModel.retryLastMessage() }
                            )
                        }

                        if (isGenerating) {
                            item {
                                NexusThinkingBubble()
                            }
                        }
                    }
                }
            }
        }

        // Bottom input bar with navigation bar inset padding
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = NexusPureBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Microphone button
                val isListening = orbState == OrbState.LISTENING
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            showMicRationale = true
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.dp, if (isListening) ParticleCyan else NexusBorderHairline, CircleShape)
                        .testTag("mic_button"),
                    shape = CircleShape,
                    containerColor = if (isListening) ParticleCyan.copy(alpha = 0.25f) else NexusSurfaceDark,
                    contentColor = if (isListening) ParticleCyan else NexusSilverBright
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.inputText.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = {
                        Text(
                            "Message NEXUS...",
                            color = NexusSilverMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexusSilverBright,
                        unfocusedTextColor = NexusSilverBright,
                        focusedBorderColor = NexusSilver,
                        unfocusedBorderColor = NexusBorderHairline,
                        focusedContainerColor = NexusCardDark,
                        unfocusedContainerColor = NexusCardDark
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Send Button
                FloatingActionButton(
                    onClick = { viewModel.sendMessage() },
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.dp, NexusBorderHairline, CircleShape)
                        .testTag("send_button"),
                    shape = CircleShape,
                    containerColor = if (inputText.isNotBlank() && !isGenerating) NexusTitanium else NexusSurfaceDark,
                    contentColor = if (inputText.isNotBlank() && !isGenerating) NexusDeepBlack else NexusSilverMuted
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NexusTitanium,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Microphone Permission Rationale Dialog
    if (showMicRationale) {
        AlertDialog(
            onDismissRequest = { showMicRationale = false },
            containerColor = NexusCardDark,
            title = {
                Text(
                    text = "VOICE INPUT ACCESS",
                    color = NexusSilverBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "NEXUS utilizes Android Speech Recognition to transcribe voice in English and Hindi. Audio is processed locally on device.",
                    color = NexusSilver,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicRationale = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusTitanium, contentColor = NexusDeepBlack)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showMicRationale = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                ) {
                    Text("Cancel", color = NexusSilverBright)
                }
            }
        )
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isUser = message.sender == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.isError) NeonRed.copy(alpha = 0.2f)
                        else NexusSurfaceDark
                    )
                    .border(
                        1.dp,
                        if (message.isError) NeonRed else NexusBorderHairline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (message.isError) NeonRed else NexusTitanium,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .testTag(if (isUser) "user_message_bubble" else "nexus_message_bubble"),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) NexusSurfaceDark
            else if (message.isError) Color(0xFF1E1014)
            else NexusCardDark,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) NexusBorderHairline
                else if (message.isError) NeonRed.copy(alpha = 0.5f)
                else NexusBorderHairline
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header inside bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else if (message.isError) "SYSTEM ALERT" else "NEXUS",
                        color = if (isUser) NexusTitanium
                        else if (message.isError) NeonRed
                        else NexusSilverBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = timeString,
                        color = NexusSilverMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                SelectionContainer {
                    Text(
                        text = message.text,
                        color = if (message.isError) Color(0xFFFFB3B8) else NexusSilverBright,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                // Retry action button if message failed
                if (message.isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onRetryClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTitanium),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = NexusTitanium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 11.5.sp, color = NexusTitanium)
                        }
                    }
                }

                // Action Bar for Assistant Messages (TTS Speak, Copy)
                if (!isUser && !message.isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Nexus Message", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = NexusSilver,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    if (isSpeaking) NexusSurfaceElevated else Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop Speech" else "Read Aloud",
                                tint = if (isSpeaking) NexusSilverBright else NexusSilver,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NexusThinkingBubble() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinkingAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = NexusCardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline.copy(alpha = alpha))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = ParticleCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NEXUS synthesizing response...",
                    color = NexusSilver,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
