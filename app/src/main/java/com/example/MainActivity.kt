package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MemoriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NexusAiTheme
import com.example.ui.theme.NexusBorderHairline
import com.example.ui.theme.NexusCardDark
import com.example.ui.theme.NexusDeepBlack
import com.example.ui.theme.NexusPureBlack
import com.example.ui.theme.NexusSilver
import com.example.ui.theme.NexusSilverBright
import com.example.ui.theme.NexusSilverMuted
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTitanium
import com.example.viewmodel.NexusViewModel
import kotlinx.coroutines.launch

enum class NexusTab(val title: String, val icon: ImageVector) {
    CHAT("Core", Icons.Default.SmartToy),
    MEMORIES("Memories", Icons.Default.Memory),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: NexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusAiTheme {
                NexusApp(viewModel)
            }
        }
    }
}

@Composable
fun NexusApp(viewModel: NexusViewModel) {
    var currentTab by remember { mutableStateOf(NexusTab.CHAT) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Handle back button on sub-screens to return to CHAT
    BackHandler(enabled = currentTab != NexusTab.CHAT || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentTab = NexusTab.CHAT
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NexusPureBlack,
                drawerContentColor = NexusSilverBright,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(
                        BorderStroke(1.dp, NexusBorderHairline),
                        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(NexusSurfaceDark)
                                    .border(1.dp, NexusBorderHairline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = NexusTitanium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "NEXUS",
                                color = NexusSilverBright,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }

                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.close() } },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = NexusSilverMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NexusBorderHairline)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation Items
                    NexusTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) NexusSilverBright else NexusSilverMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) NexusSilverBright else NexusSilver,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = NexusSurfaceElevated,
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .testTag("drawer_${tab.name.lowercase()}")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Minimalist Footer
                    Text(
                        text = "NEXUS SYSTEM",
                        color = NexusSilverMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NexusPureBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabContentTransition"
                ) { tab ->
                    when (tab) {
                        NexusTab.CHAT -> ChatScreen(
                            viewModel = viewModel,
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } },
                            onNavigateToSettings = { currentTab = NexusTab.SETTINGS }
                        )
                        NexusTab.MEMORIES -> MemoriesScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentTab = NexusTab.CHAT },
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                        )
                        NexusTab.HISTORY -> HistoryScreen(
                            viewModel = viewModel,
                            onSelectConversation = {
                                currentTab = NexusTab.CHAT
                            },
                            onNavigateBack = { currentTab = NexusTab.CHAT },
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                        )
                        NexusTab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentTab = NexusTab.CHAT },
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        }
    }

    // Global Error Dialog
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            containerColor = NexusCardDark,
            title = {
                Text(
                    text = "NEXUS ALERT",
                    color = NeonRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = error,
                    color = NexusSilverBright,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissError() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexusTitanium,
                        contentColor = NexusDeepBlack
                    )
                ) {
                    Text("Acknowledge", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
