package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DateTimeData
import com.example.model.LocationData
import com.example.model.StampConfig
import com.example.ui.components.LiveStampOverlay
import com.example.ui.viewmodel.GpsCamViewModel

@Composable
fun PhotoEditorScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val context = LocalContext.current

    val initialLocation by viewModel.selectedLocation.collectAsState()
    val initialDateTime by viewModel.dateTimeData.collectAsState()
    val initialConfig by viewModel.currentStampConfig.collectAsState()
    val presets by viewModel.allPresets.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var locationState by remember { mutableStateOf(initialLocation) }
    var dateTimeState by remember { mutableStateOf(initialDateTime) }
    var configState by remember { mutableStateOf(initialConfig) }
    var isProcessing by remember { mutableStateOf(false) }

    // Undo / Redo history stacks
    val undoStack = remember { mutableStateListOf<StampConfig>() }
    val redoStack = remember { mutableStateListOf<StampConfig>() }

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            undoStack.clear()
            redoStack.clear()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                    text = "EXISTING PHOTO EDITOR",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Undo / Redo buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (undoStack.isNotEmpty()) {
                            val previous = undoStack.removeLast()
                            redoStack.add(configState)
                            configState = previous
                        }
                    },
                    enabled = undoStack.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) Color(0xFF00D2FC) else Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            val next = redoStack.removeLast()
                            undoStack.add(configState)
                            configState = next
                        }
                    },
                    enabled = redoStack.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) Color(0xFF00D2FC) else Color.Gray
                    )
                }
            }
        }

        // Preview Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Loaded Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Live Stamp
                LiveStampOverlay(
                    location = locationState,
                    dateTime = dateTimeState,
                    config = configState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Empty State Prompt to Pick Photo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Pick Photo",
                        tint = Color(0xFF00D2FC),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Import Any Photo to Add GPS Stamp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("pick_photo_button")
                    ) {
                        Text("Open Photo Picker", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Editing Controls List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedImageUri != null) {
                // Export Button
                item {
                    Button(
                        onClick = {
                            val uri = selectedImageUri ?: return@Button
                            isProcessing = true
                            viewModel.applyStampToExistingPhoto(
                                uri = uri,
                                location = locationState,
                                dateTime = dateTimeState,
                                config = configState,
                                onSuccess = {
                                    isProcessing = false
                                    onNavigateToGallery()
                                }
                            )
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_edited_photo_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Stamped Image (Keep Original)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Presets Quick Selector
                item {
                    Text(
                        text = "CHOOSE PRESET STYLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FC)
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { preset ->
                            val isSelected = configState.name == preset.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF00D2FC) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, if (isSelected) Color(0xFF00D2FC) else Color(0xFF243B5A), RoundedCornerShape(10.dp))
                                    .clickable {
                                        undoStack.add(configState)
                                        configState = preset.config
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = preset.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF070E18) else Color.White
                                )
                            }
                        }
                    }
                }

                // Location Editor Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("EDIT PHOTO LOCATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00D2FC))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = locationState.latitude.toString(),
                                    onValueChange = {
                                        it.toDoubleOrNull()?.let { lat ->
                                            locationState = locationState.copy(latitude = lat)
                                        }
                                    },
                                    label = { Text("Latitude") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = locationState.longitude.toString(),
                                    onValueChange = {
                                        it.toDoubleOrNull()?.let { lng ->
                                            locationState = locationState.copy(longitude = lng)
                                        }
                                    },
                                    label = { Text("Longitude") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = locationState.locality,
                                onValueChange = { locationState = locationState.copy(locality = it) },
                                label = { Text("Place / Locality") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Date & Time Editor Section
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("EDIT PHOTO DATE & TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00D2FC))

                            Text(
                                text = dateTimeState.formatFormattedDateTime(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val cal = java.util.Calendar.getInstance()
                                        dateTimeState = dateTimeState.copy(
                                            manualYear = cal.get(java.util.Calendar.YEAR),
                                            manualMonth = cal.get(java.util.Calendar.MONTH) + 1,
                                            manualDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132238)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Set Today", color = Color(0xFF00D2FC), fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val cal = java.util.Calendar.getInstance()
                                        dateTimeState = dateTimeState.copy(
                                            manualHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                                            manualMinute = cal.get(java.util.Calendar.MINUTE)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132238)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Set Now", color = Color(0xFF00D2FC), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
