package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraFacing
import com.example.model.ExportFormat
import com.example.model.ImageQualityOption
import com.example.model.MapStyleType
import com.example.ui.viewmodel.GpsCamViewModel

@Composable
fun SettingsScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    var showApiDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "APPLICATION SETTINGS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Section: Camera & Capture
        item {
            SettingsCategoryHeader("CAMERA & CAPTURE")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Save Original Raw Photo",
                        subtitle = "Preserves the untouched photo alongside stamped copy",
                        checked = settings.saveOriginalImage,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(saveOriginalImage = it)) }
                    )

                    SettingToggleRow(
                        title = "Save Stamped Photo",
                        subtitle = "Write permanent GPS stamp directly on output image",
                        checked = settings.saveStampedImage,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(saveStampedImage = it)) }
                    )

                    SettingToggleRow(
                        title = "Haptic Shutter Feedback",
                        subtitle = "Vibration pulse upon capturing image",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                    )
                }
            }
        }

        // Section: Output Quality & Format
        item {
            SettingsCategoryHeader("IMAGE QUALITY & EXPORT")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Compression Quality", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImageQualityOption.values().forEach { q ->
                            val isSelected = settings.imageQuality == q
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF00D2FC) else Color(0xFF132238))
                                    .clickable { viewModel.updateSettings(settings.copy(imageQuality = q)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = q.label.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF070E18) else Color.White
                                )
                            }
                        }
                    }

                    Text("File Format", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportFormat.values().forEach { fmt ->
                            val isSelected = settings.exportFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF00D2FC) else Color(0xFF132238))
                                    .clickable { viewModel.updateSettings(settings.copy(exportFormat = fmt)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fmt.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF070E18) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Google Maps API Integration Help
        item {
            SettingsCategoryHeader("MAP & SATELLITE ENGINE")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Google Maps API Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Configured via Secrets panel / .env", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = { showApiDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132238))
                        ) {
                            Text("Setup Guide", color = Color(0xFF00D2FC), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // About / Branding
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D2FC).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("GPS CAM PRO", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF00D2FC))
                    Text("by DYZI  •  Version 1.0 Pro Edition", fontSize = 11.sp, color = Color.White)
                    Text(
                        "Intended for professional field inspections, generator maintenance, civil engineering, and high-accuracy geo-documentation.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showApiDialog) {
        AlertDialog(
            onDismissRequest = { showApiDialog = false },
            title = { Text("Google Maps SDK Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("To use live Google Maps & Satellite imagery in production:")
                    Text("1. In Google Cloud Console, enable 'Maps SDK for Android'.")
                    Text("2. Create an API Key and restrict it to your package name and SHA-1 certificate.")
                    Text("3. Add the key in the AI Studio Secrets panel or .env file as MAPS_API_KEY=YOUR_KEY.")
                    Text("Note: Offline GIS vector mapping is built-in and always available even with no API key or when network is disconnected!")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showApiDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC))
                ) {
                    Text("Got it", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00D2FC),
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Spacer(modifier = Modifier.width(8.dp))
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
