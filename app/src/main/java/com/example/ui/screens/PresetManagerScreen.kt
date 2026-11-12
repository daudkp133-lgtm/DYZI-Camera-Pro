package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresetItem
import com.example.ui.viewmodel.GpsCamViewModel

@Composable
fun PresetManagerScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit,
    onEditPresetInDesigner: (PresetItem) -> Unit
) {
    val presets by viewModel.allPresets.collectAsState()
    val activePresetId by viewModel.activePresetId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "SAVED PRESETS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "SELECT OR CUSTOMIZE STAMP TEMPLATES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D2FC),
                    letterSpacing = 1.sp
                )
            }

            items(presets) { preset ->
                val isActive = preset.id == activePresetId
                PresetCard(
                    preset = preset,
                    isActive = isActive,
                    onSelect = { viewModel.selectPreset(preset) },
                    onEdit = {
                        viewModel.selectPreset(preset)
                        onEditPresetInDesigner(preset)
                    },
                    onDuplicate = {
                        viewModel.saveCurrentConfigAsPreset(
                            name = "${preset.name} (Copy)",
                            category = preset.category,
                            description = preset.description
                        )
                    },
                    onDelete = {
                        if (presets.size > 1) {
                            viewModel.deletePreset(preset)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: PresetItem,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF0D1B2A) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Color(0xFF00D2FC) else Color(0xFF243B5A)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00D2FC).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = preset.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D2FC)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = preset.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Text(
                text = preset.description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            // Features chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                val cfg = preset.config
                FeatureMiniBadge(text = if (cfg.showMap) "Map (${cfg.mapSize.name})" else "No Map")
                FeatureMiniBadge(text = "Pos: ${cfg.timestampPosition.name.take(2)}")
                if (cfg.showCustomFields) {
                    FeatureMiniBadge(text = "Custom Fields")
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit in Designer", tint = Color(0xFF00D2FC), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FeatureMiniBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF132238))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}
