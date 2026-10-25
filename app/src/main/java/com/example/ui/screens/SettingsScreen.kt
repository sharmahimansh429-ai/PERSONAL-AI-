package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NexusBorderHairline
import com.example.ui.theme.NexusCardDark
import com.example.ui.theme.NexusDeepBlack
import com.example.ui.theme.NexusObsidian
import com.example.ui.theme.NexusPureBlack
import com.example.ui.theme.NexusSilver
import com.example.ui.theme.NexusSilverBright
import com.example.ui.theme.NexusSilverDark
import com.example.ui.theme.NexusSilverMuted
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTitanium
import com.example.viewmodel.ConnectionState
import com.example.viewmodel.NexusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val personaStyle by viewModel.personaStyle.collectAsState()
    val isAutoSpeak by viewModel.isAutoSpeak.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    val ttsPitch by viewModel.ttsPitch.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var customKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }

    val models = listOf("gemini-3.5-flash", "gemini-3.8-flash", "gemini-3.1-pro-preview")
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val languages = listOf("Bilingual (Auto)", "English Primary", "Hindi Primary (हिंदी)")
    var langDropdownExpanded by remember { mutableStateOf(false) }

    val personas = listOf("Futuristic AI", "Jarvis Companion", "Concise Professional", "Creative Thinker")
    var personaDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusPureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexusSilverBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "SETTINGS",
                        color = NexusSilverBright,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        color = NexusSilver,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = NexusSilver,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Section 1: API Key & Intelligence Provider
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusCardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isApiKeyConfigured) NexusBorderHairline else NeonRed.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (isApiKeyConfigured) NexusTitanium else NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini API Connection",
                            color = NexusSilverBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val statusText = when (val conn = connectionState) {
                        is ConnectionState.Checking -> "CHECKING..."
                        is ConnectionState.NoKey -> "UNCONFIGURED"
                        is ConnectionState.Error -> "DISCONNECTED"
                        is ConnectionState.Online -> "VERIFIED ONLINE"
                    }
                    val statusColor = when (connectionState) {
                        is ConnectionState.Checking -> NeonAmber
                        is ConnectionState.NoKey -> NeonRed
                        is ConnectionState.Error -> NeonRed
                        is ConnectionState.Online -> NeonEmerald
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (val conn = connectionState) {
                        is ConnectionState.Online -> "Verified connected to ${conn.model}. Official Gemini API integration is active."
                        is ConnectionState.Checking -> "Sending test telemetry ping to verify Gemini API connection..."
                        is ConnectionState.Error -> "Connection check failed: ${conn.message}"
                        is ConnectionState.NoKey -> "No API key detected. Set GEMINI_API_KEY in the Secrets panel in AI Studio or enter a key below."
                    },
                    color = NexusSilver,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customKeyInput,
                    onValueChange = { customKeyInput = it },
                    label = { Text("Manual API Key Override (Optional)") },
                    placeholder = { Text("AIzaSy...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexusSilverBright,
                        unfocusedTextColor = NexusSilverBright,
                        focusedBorderColor = NexusSilver,
                        unfocusedBorderColor = NexusBorderHairline,
                        focusedContainerColor = NexusSurfaceDark,
                        unfocusedContainerColor = NexusSurfaceDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { isKeyVisible = !isKeyVisible },
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                    ) {
                        Text(
                            text = if (isKeyVisible) "Hide Key" else "Reveal Key",
                            color = NexusSilver,
                            fontSize = 11.5.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (customKeyInput.isNotBlank()) {
                                viewModel.updateCustomApiKey(customKeyInput)
                                customKeyInput = ""
                                Toast.makeText(context, "API Key saved successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusTitanium, contentColor = NexusDeepBlack),
                        enabled = customKeyInput.isNotBlank()
                    ) {
                        Text("Apply Key", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.checkConnection() },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTitanium)
                ) {
                    Text("Verify Neural Connection", color = NexusTitanium, fontSize = 12.sp)
                }
            }
        }

        // Section 2: AI Model Selection
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusCardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = NexusTitanium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Model Engine",
                        color = NexusSilverBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select the Gemini neural model running Nexus AI.",
                    color = NexusSilver,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = modelDropdownExpanded,
                    onExpandedChange = { modelDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexusSilverBright,
                            unfocusedTextColor = NexusSilverBright,
                            focusedBorderColor = NexusSilver,
                            unfocusedBorderColor = NexusBorderHairline,
                            focusedContainerColor = NexusSurfaceDark,
                            unfocusedContainerColor = NexusSurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = modelDropdownExpanded,
                        onDismissRequest = { modelDropdownExpanded = false }
                    ) {
                        models.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m, fontWeight = FontWeight.Bold)
                                        Text(
                                            when (m) {
                                                "gemini-3.5-flash" -> "Recommended • Fast, ultra-reliable multimodal chat"
                                                "gemini-3.8-flash" -> "Deep reasoning model with auto-fallback"
                                                else -> "Deep analytical thinking"
                                            },
                                            fontSize = 11.sp,
                                            color = NexusSilver
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.updateModel(m)
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Bilingual Support & Language Mode
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusCardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = NexusTitanium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Language Mode (हिंदी / English)",
                        color = NexusSilverBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Nexus seamlessly understands and responds in both Hindi and English. Choose primary conversational bias.",
                    color = NexusSilver,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = langDropdownExpanded,
                    onExpandedChange = { langDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = languageMode,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexusSilverBright,
                            unfocusedTextColor = NexusSilverBright,
                            focusedBorderColor = NexusSilver,
                            unfocusedBorderColor = NexusBorderHairline,
                            focusedContainerColor = NexusSurfaceDark,
                            unfocusedContainerColor = NexusSurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = langDropdownExpanded,
                        onDismissRequest = { langDropdownExpanded = false }
                    ) {
                        languages.forEach { l ->
                            DropdownMenuItem(
                                text = { Text(l) },
                                onClick = {
                                    viewModel.updateLanguageMode(l)
                                    langDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Voice & Speech Engine (TTS)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusCardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = NexusTitanium,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-Read Responses",
                            color = NexusSilverBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = isAutoSpeak,
                        onCheckedChange = { viewModel.updateAutoSpeak(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NexusTitanium,
                            checkedTrackColor = NexusSurfaceElevated,
                            uncheckedThumbColor = NexusSilverDark,
                            uncheckedTrackColor = NexusDeepBlack
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Speech Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", color = NexusSilver, fontSize = 13.sp)
                    Text(String.format(java.util.Locale.US, "%.1fx", ttsSpeed), color = NexusTitanium, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = ttsSpeed,
                    onValueChange = { viewModel.updateTtsSettings(it, ttsPitch) },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = NexusTitanium,
                        activeTrackColor = NexusSilver,
                        inactiveTrackColor = NexusSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Speech Pitch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Pitch", color = NexusSilver, fontSize = 13.sp)
                    Text(String.format(java.util.Locale.US, "%.1fx", ttsPitch), color = NexusTitanium, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = ttsPitch,
                    onValueChange = { viewModel.updateTtsSettings(ttsSpeed, it) },
                    valueRange = 0.6f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = NexusTitanium,
                        activeTrackColor = NexusSilver,
                        inactiveTrackColor = NexusSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.ttsManager.speak(
                            text = "Hello! I am Nexus AI. नमस्ते, मैं नेक्सस एआई हूँ। How may I assist you today?",
                            speed = ttsSpeed,
                            pitch = ttsPitch
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTitanium)
                ) {
                    Text("Test Bilingual Voice Output", color = NexusTitanium, fontSize = 13.sp)
                }
            }
        }

        // Section 5: AI Persona Style
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusCardDark,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NexusTitanium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Persona Archetype",
                        color = NexusSilverBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = personaDropdownExpanded,
                    onExpandedChange = { personaDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = personaStyle,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personaDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexusSilverBright,
                            unfocusedTextColor = NexusSilverBright,
                            focusedBorderColor = NexusSilver,
                            unfocusedBorderColor = NexusBorderHairline,
                            focusedContainerColor = NexusSurfaceDark,
                            unfocusedContainerColor = NexusSurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = personaDropdownExpanded,
                        onDismissRequest = { personaDropdownExpanded = false }
                    ) {
                        personas.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    viewModel.updatePersonaStyle(p)
                                    personaDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // About Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NexusSurfaceDark,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEXUS AI v1.0",
                    color = NexusSilverBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Futuristic Personal Intelligence • Built with Jetpack Compose & Gemini",
                    color = NexusSilverMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
