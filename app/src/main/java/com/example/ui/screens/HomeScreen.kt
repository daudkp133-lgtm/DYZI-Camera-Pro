package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LocationMode
import com.example.ui.viewmodel.GpsCamViewModel
import java.io.File

@Composable
fun HomeScreen(
    viewModel: GpsCamViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToLocationStudio: () -> Unit,
    onNavigateToDateTimeStudio: () -> Unit,
    onNavigateToStampDesigner: () -> Unit,
    onNavigateToPhotoEditor: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val location by viewModel.selectedLocation.collectAsState()
    val dateTime by viewModel.dateTimeData.collectAsState()
    val stampConfig by viewModel.currentStampConfig.collectAsState()
    val photos by viewModel.allPhotos.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & Branding
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GPS CAM ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "PRO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00D2FC)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00D2FC).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DYZI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D2FC)
                            )
                        }
                    }
                    Text(
                        text = "Capture • Locate • Customize",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF00D2FC),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Live GPS / Location Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToLocationStudio() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00D2FC), Color(0xFF0077B6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (location.mode == LocationMode.REAL_GPS) "REAL GPS ACTIVE" else "MANUAL LOCATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (location.mode == LocationMode.REAL_GPS) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (location.mode == LocationMode.REAL_GPS) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                            )
                        }

                        Text(
                            text = location.formatDecimalCompact(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = location.getShortAddress(),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit Location",
                        tint = Color(0xFF00D2FC),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Active Preset Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToPresets() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D1B2A)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D2FC).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF132238)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Preset",
                            tint = Color(0xFF00D2FC),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE STAMP PRESET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D2FC)
                        )
                        Text(
                            text = stampConfig.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = dateTime.formatFormattedDateTime(),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Text(
                        text = "CHANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FC),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00D2FC).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Main Action Tiles Grid
        item {
            Text(
                text = "STUDIO MODULES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = "Live Camera",
                    subtitle = "Real-time GPS overlay",
                    icon = Icons.Default.PhotoCamera,
                    accentColor = Color(0xFF00D2FC),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_live_camera"),
                    onClick = onNavigateToCamera
                )

                ActionTile(
                    title = "Edit Photo",
                    subtitle = "Stamp existing image",
                    icon = Icons.Default.Edit,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_edit_photo"),
                    onClick = onNavigateToPhotoEditor
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = "Location Studio",
                    subtitle = "GPS, Manual & Map",
                    icon = Icons.Default.MyLocation,
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_location_studio"),
                    onClick = onNavigateToLocationStudio
                )

                ActionTile(
                    title = "Date & Time",
                    subtitle = "Manual & Auto clock",
                    icon = Icons.Default.AccessTime,
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_datetime_studio"),
                    onClick = onNavigateToDateTimeStudio
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = "Stamp Designer",
                    subtitle = "Visual layout editor",
                    icon = Icons.Default.ColorLens,
                    accentColor = Color(0xFF818CF8),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_stamp_designer"),
                    onClick = onNavigateToStampDesigner
                )

                ActionTile(
                    title = "Saved Presets",
                    subtitle = "Industry inspection templates",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = Color(0xFFA78BFA),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_saved_presets"),
                    onClick = onNavigateToPresets
                )
            }
        }

        // Recent Photos Carousel
        if (photos.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CAPTURES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "VIEW ALL (${photos.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FC),
                        modifier = Modifier.clickable { onNavigateToGallery() }
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos.take(6)) { photo ->
                        Card(
                            modifier = Modifier
                                .size(120.dp, 150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToGallery() },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = File(photo.filePath),
                                    contentDescription = "Captured photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color(0xCC070E18))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = photo.presetName,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}
