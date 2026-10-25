package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NexusMemory
import com.example.ui.components.CyberChip
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
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
import com.example.viewmodel.NexusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onOpenMenu: () -> Unit = {}
) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<NexusMemory?>(null) }
    var deletingMemoryId by remember { mutableStateOf<Long?>(null) }

    val categories = listOf("All", "Personal", "Language", "Preference", "Work", "General")

    val filteredMemories = memories.filter { mem ->
        val matchesCategory = selectedCategoryFilter == "All" || mem.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                mem.key.contains(searchQuery, ignoreCase = true) ||
                mem.fact.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexusPureBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                            text = "MEMORIES",
                            color = NexusSilverBright,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "${memories.count { it.isActive }} ACTIVE CONTEXT NODES",
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

            // Description card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = NexusCardDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NexusTitanium,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nexus automatically injects these active memories into every prompt so your assistant remembers your context, name, and language preferences.",
                        color = NexusSilver,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("memory_search_field"),
                placeholder = {
                    Text("Search memories...", color = NexusSilverMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NexusSilver
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
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Surface(
                        modifier = Modifier.clickable { selectedCategoryFilter = cat },
                        color = if (isSelected) NexusTitanium else NexusSurfaceDark,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NexusTitanium else NexusBorderHairline
                        )
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) NexusDeepBlack else NexusSilverBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Memories List
            if (filteredMemories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = NexusSilverDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No memories matching '$searchQuery'" else "No memories recorded yet",
                            color = NexusSilver,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTitanium)
                        ) {
                            Text("Teach Nexus a Fact", color = NexusTitanium)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMemories, key = { it.id }) { memory ->
                        MemoryItemCard(
                            memory = memory,
                            onToggleActive = { viewModel.toggleMemoryActive(memory) },
                            onEdit = { editingMemory = memory },
                            onDelete = { deletingMemoryId = memory.id }
                        )
                    }
                }
            }
        }

        // Add Memory Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .border(1.dp, NexusBorderHairline, CircleShape)
                .testTag("add_memory_fab"),
            shape = CircleShape,
            containerColor = NexusTitanium,
            contentColor = NexusDeepBlack
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Memory",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        MemoryEditorDialog(
            title = "ADD NEURAL MEMORY",
            initialKey = "",
            initialFact = "",
            initialCategory = "Personal",
            onDismiss = { showAddDialog = false },
            onConfirm = { key, fact, category ->
                viewModel.addMemory(key, fact, category)
                showAddDialog = false
            }
        )
    }

    // Edit Memory Dialog
    editingMemory?.let { memory ->
        MemoryEditorDialog(
            title = "EDIT NEURAL MEMORY",
            initialKey = memory.key,
            initialFact = memory.fact,
            initialCategory = memory.category,
            onDismiss = { editingMemory = null },
            onConfirm = { key, fact, category ->
                viewModel.updateMemory(
                    memory.copy(
                        key = key,
                        fact = fact,
                        category = category
                    )
                )
                editingMemory = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingMemoryId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingMemoryId = null },
            containerColor = NexusCardDark,
            title = {
                Text(
                    text = "DELETE MEMORY?",
                    color = NexusSilverBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "Nexus AI will erase this fact from its long-term neural memory. This action cannot be undone.",
                    color = NexusSilver,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemory(id)
                        deletingMemoryId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deletingMemoryId = null },
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                ) {
                    Text("Cancel", color = NexusSilverBright)
                }
            }
        )
    }
}

@Composable
private fun MemoryItemCard(
    memory: NexusMemory,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (memory.category.lowercase()) {
        "language" -> NeonEmerald
        "personal" -> NexusTitanium
        "preference" -> CyberViolet
        "work" -> NexusSilverBright
        else -> NexusSilver
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_card_${memory.id}"),
        color = NexusCardDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (memory.isActive) NexusSilverDark else NexusBorderHairline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Chip
                Surface(
                    color = NexusSurfaceDark,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
                ) {
                    Text(
                        text = memory.category.uppercase(),
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (memory.isActive) "IN CONTEXT" else "DISABLED",
                        color = if (memory.isActive) NexusTitanium else NexusSilverDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = memory.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NexusTitanium,
                            checkedTrackColor = NexusSurfaceElevated,
                            uncheckedThumbColor = NexusSilverDark,
                            uncheckedTrackColor = NexusDeepBlack
                        ),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key
            Text(
                text = memory.key,
                color = NexusSilverBright,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Fact Details
            Text(
                text = memory.fact,
                color = NexusSilver,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit memory",
                        tint = NexusTitanium,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete memory",
                        tint = NeonRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryEditorDialog(
    title: String,
    initialKey: String,
    initialFact: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (key: String, fact: String, category: String) -> Unit
) {
    var keyText by remember { mutableStateOf(initialKey) }
    var factText by remember { mutableStateOf(initialFact) }
    var categoryText by remember { mutableStateOf(initialCategory) }

    val categories = listOf("Personal", "Language", "Preference", "Work", "General")
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexusCardDark,
        title = {
            Text(
                text = title,
                color = NexusSilverBright,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Key / Label
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Memory Label (e.g. My Name, Work, Tone)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexusSilverBright,
                        unfocusedTextColor = NexusSilverBright,
                        focusedBorderColor = NexusSilver,
                        unfocusedBorderColor = NexusBorderHairline
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexusSilverBright,
                            unfocusedTextColor = NexusSilverBright,
                            focusedBorderColor = NexusSilver,
                            unfocusedBorderColor = NexusBorderHairline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoryText = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fact details
                OutlinedTextField(
                    value = factText,
                    onValueChange = { factText = it },
                    label = { Text("Fact / Detail") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexusSilverBright,
                        unfocusedTextColor = NexusSilverBright,
                        focusedBorderColor = NexusSilver,
                        unfocusedBorderColor = NexusBorderHairline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyText.isNotBlank() && factText.isNotBlank()) {
                        onConfirm(keyText, factText, categoryText)
                    }
                },
                enabled = keyText.isNotBlank() && factText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NexusTitanium, contentColor = NexusDeepBlack)
            ) {
                Text("Save to Memory", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
            ) {
                Text("Cancel", color = NexusSilverBright)
            }
        }
    )
}
