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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoordinateFormat
import com.example.model.MapSize
import com.example.model.MapStyleType
import com.example.model.StampBackgroundStyle
import com.example.model.StampConfig
import com.example.model.StampPosition
import com.example.ui.components.LiveStampOverlay
import com.example.ui.viewmodel.GpsCamViewModel

@Composable
fun StampDesignerScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit
) {
    val location by viewModel.selectedLocation.collectAsState()
    val dateTime by viewModel.dateTimeData.collectAsState()
    val currentConfig by viewModel.currentStampConfig.collectAsState()

    var configState by remember { mutableStateOf(currentConfig) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    text = "STAMP DESIGNER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Button(
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_preset_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color(0xFF070E18), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Preset", color = Color(0xFF070E18), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Live Canvas Preview of Stamp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFF050B14))
                .border(1.dp, Color(0xFF243B5A))
        ) {
            LiveStampOverlay(
                location = location,
                dateTime = dateTime,
                config = configState,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "LIVE CANVAS PREVIEW", fontSize = 9.sp, color = Color(0xFF00D2FC), fontWeight = FontWeight.Bold)
            }
        }

        // Configuration Controls
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Apply Immediately Button
            item {
                Button(
                    onClick = {
                        viewModel.updateStampConfig(configState)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_stamp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply to Live Camera", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Section 1: Map Element Settings
            item {
                SectionHeader("MAP PANEL SETTINGS")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleRow(
                            label = "Show Map on Stamp",
                            checked = configState.showMap,
                            onCheckedChange = { configState = configState.copy(showMap = it) }
                        )

                        if (configState.showMap) {
                            Text("Map Position", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            PositionSelector(
                                selected = configState.mapPosition,
                                onSelect = { configState = configState.copy(mapPosition = it) }
                            )

                            Text("Map Size", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MapSize.values().forEach { size ->
                                    SelectablePill(
                                        label = size.name,
                                        isSelected = configState.mapSize == size,
                                        onClick = { configState = configState.copy(mapSize = size) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Text("Map Imagery Style", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SelectablePill(
                                        label = "Google Default Road View (Attached Left)",
                                        isSelected = configState.mapStyle == MapStyleType.NORMAL,
                                        onClick = { configState = configState.copy(mapStyle = MapStyleType.NORMAL) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        Pair("Satellite", MapStyleType.SATELLITE),
                                        Pair("Hybrid", MapStyleType.HYBRID),
                                        Pair("Terrain", MapStyleType.TERRAIN)
                                    ).forEach { (lbl, style) ->
                                        SelectablePill(
                                            label = lbl,
                                            isSelected = configState.mapStyle == style,
                                            onClick = { configState = configState.copy(mapStyle = style) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Timestamp Position & Style
            item {
                SectionHeader("TIMESTAMP & TEXT POSITION")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToggleRow(
                            label = "Zero Margins (Flush with Picture Edges)",
                            checked = configState.useZeroMargins,
                            onCheckedChange = { configState = configState.copy(useZeroMargins = it) }
                        )

                        Text("Text Position", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        PositionSelector(
                            selected = configState.timestampPosition,
                            onSelect = { configState = configState.copy(timestampPosition = it) }
                        )

                        Text("Background Card Style", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StampBackgroundStyle.values().forEach { bgStyle ->
                                SelectablePill(
                                    label = when (bgStyle) {
                                        StampBackgroundStyle.TRANSPARENT_SHADOW -> "Transparent (Black Text Shadow - User Default)"
                                        StampBackgroundStyle.DARK_CARD -> "Dark Rounded Card"
                                        StampBackgroundStyle.SOLID_BLACK -> "Solid Black Box"
                                        StampBackgroundStyle.CYAN_ACCENT_CARD -> "Cyan Border Card"
                                    },
                                    isSelected = configState.backgroundStyle == bgStyle,
                                    onClick = { configState = configState.copy(backgroundStyle = bgStyle) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Text("Text Color", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("#FFFFFF", "#FFFF00", "#00D2FC", "#10B981", "#F59E0B", "#EF4444").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            2.dp,
                                            if (configState.textColorHex == hex) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { configState = configState.copy(textColorHex = hex) }
                                )
                            }
                        }

                        ToggleRow(
                            label = "Bold Typography",
                            checked = configState.isBold,
                            onCheckedChange = { configState = configState.copy(isBold = it) }
                        )

                        ToggleRow(
                            label = "Monospace Font",
                            checked = configState.isMonospace,
                            onCheckedChange = { configState = configState.copy(isMonospace = it) }
                        )
                    }
                }
            }

            // Section 3: Field Toggles
            item {
                SectionHeader("DATA FIELDS ON STAMP")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToggleRow(
                            label = "Date & Time",
                            checked = configState.showDateTime,
                            onCheckedChange = { configState = configState.copy(showDateTime = it) }
                        )
                        ToggleRow(
                            label = "GPS Coordinates",
                            checked = configState.showCoordinates,
                            onCheckedChange = { configState = configState.copy(showCoordinates = it) }
                        )
                        ToggleRow(
                            label = "Locality / Address",
                            checked = configState.showAddress,
                            onCheckedChange = { configState = configState.copy(showAddress = it) }
                        )
                        ToggleRow(
                            label = "District & Province",
                            checked = configState.showDistrictProvince,
                            onCheckedChange = { configState = configState.copy(showDistrictProvince = it) }
                        )
                        ToggleRow(
                            label = "Postal Code",
                            checked = configState.showPostalCode,
                            onCheckedChange = { configState = configState.copy(showPostalCode = it) }
                        )
                        ToggleRow(
                            label = "Altitude",
                            checked = configState.showAltitude,
                            onCheckedChange = { configState = configState.copy(showAltitude = it) }
                        )
                    }
                }
            }

            // Section 4: Custom Inspection Fields
            item {
                SectionHeader("CUSTOM INSPECTION & SITE FIELDS")
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ToggleRow(
                            label = "Enable Custom Inspection Info",
                            checked = configState.showCustomFields,
                            onCheckedChange = { configState = configState.copy(showCustomFields = it) }
                        )

                        if (configState.showCustomFields) {
                            OutlinedTextField(
                                value = configState.companyName,
                                onValueChange = { configState = configState.copy(companyName = it) },
                                label = { Text("Company Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = configState.inspectionType,
                                onValueChange = { configState = configState.copy(inspectionType = it) },
                                label = { Text("Inspection Type (e.g. Generator Maintenance)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = configState.siteId,
                                onValueChange = { configState = configState.copy(siteId = it) },
                                label = { Text("Site ID / Unit Code") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = configState.technicianName,
                                onValueChange = { configState = configState.copy(technicianName = it) },
                                label = { Text("Technician / Inspector Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = configState.projectName,
                                onValueChange = { configState = configState.copy(projectName = it) },
                                label = { Text("Project Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = configState.customNotes,
                                onValueChange = { configState = configState.copy(customNotes = it) },
                                label = { Text("Custom Notes") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        var presetName by remember { mutableStateOf(configState.name) }
        var category by remember { mutableStateOf("Custom") }
        var description by remember { mutableStateOf("Custom user created stamp configuration") }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save As Reusable Preset", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStampConfig(configState.copy(name = presetName))
                        viewModel.saveCurrentConfigAsPreset(presetName, category, description)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC))
                ) {
                    Text("Save", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00D2FC),
        letterSpacing = 1.sp
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D2FC),
                checkedTrackColor = Color(0xFF0D1B2A)
            )
        )
    }
}

@Composable
private fun PositionSelector(
    selected: StampPosition,
    onSelect: (StampPosition) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            Pair("BL", StampPosition.BOTTOM_LEFT),
            Pair("BR", StampPosition.BOTTOM_RIGHT),
            Pair("TL", StampPosition.TOP_LEFT),
            Pair("TR", StampPosition.TOP_RIGHT),
            Pair("Banner", StampPosition.BOTTOM_BANNER)
        ).forEach { (label, pos) ->
            SelectablePill(
                label = label,
                isSelected = selected == pos,
                onClick = { onSelect(pos) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF00D2FC) else Color(0xFF132238))
            .border(
                1.dp,
                if (isSelected) Color(0xFF00D2FC) else Color(0xFF243B5A),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF070E18) else Color.White
        )
    }
}
