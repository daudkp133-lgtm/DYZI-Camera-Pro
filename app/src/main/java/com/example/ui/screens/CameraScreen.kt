package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.model.GridType
import com.example.model.PresetItem
import com.example.ui.components.LiveStampOverlay
import com.example.ui.viewmodel.GpsCamViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraScreen(
    viewModel: GpsCamViewModel,
    onNavigateToGallery: () -> Unit,
    onOpenLocationDialog: () -> Unit,
    onOpenDateTimeDialog: () -> Unit,
    onOpenStampDesigner: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        PermissionDeniedView(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
        return
    }

    val location by viewModel.selectedLocation.collectAsState()
    val dateTime by viewModel.dateTimeData.collectAsState()
    val stampConfig by viewModel.currentStampConfig.collectAsState()
    val presets by viewModel.allPresets.collectAsState()
    val activePresetId by viewModel.activePresetId.collectAsState()
    val photos by viewModel.allPhotos.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val flashMode by viewModel.cameraManager.flashMode.collectAsState()
    val gridType by viewModel.gridType.collectAsState()

    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
    var zoomLevel by remember { mutableFloatStateOf(0f) }
    var showZoomSlider by remember { mutableStateOf(false) }
    var showMapsLinkDialog by remember { mutableStateOf(false) }

    // Tap to focus animation indicator
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusScale = remember { Animatable(1.5f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Viewfinder
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewInstance = this
                    coroutineScope.launch {
                        viewModel.cameraManager.initializeCamera(lifecycleOwner, this@apply)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        focusPoint = offset
                        previewViewInstance?.let { pv ->
                            viewModel.cameraManager.tapToFocus(pv, offset.x, offset.y)
                        }
                        coroutineScope.launch {
                            focusScale.snapTo(1.5f)
                            focusScale.animateTo(1.0f, animationSpec = tween(250))
                            kotlinx.coroutines.delay(1000)
                            focusPoint = null
                        }
                    }
                }
        )

        // Grid Lines Overlay
        if (gridType != GridType.NONE) {
            GridOverlay(gridType = gridType)
        }

        // Animated Tap to Focus Ring
        focusPoint?.let { pt ->
            Box(
                modifier = Modifier
                    .offset { IntOffset((pt.x - 30).toInt(), (pt.y - 30).toInt()) }
                    .size((60 * focusScale.value).dp)
                    .border(2.dp, Color(0xFF00D2FC), CircleShape)
            )
        }

        // Live Stamp Overlay matching the actual output!
        LiveStampOverlay(
            location = location,
            dateTime = dateTime,
            config = stampConfig,
            modifier = Modifier.fillMaxSize()
        )

        // Top Toolbar
        TopCameraBar(
            flashMode = flashMode,
            gridType = gridType,
            onToggleFlash = { viewModel.cameraManager.cycleFlashMode() },
            onToggleGrid = { viewModel.cycleGrid() },
            onSwitchCamera = {
                previewViewInstance?.let { pv ->
                    viewModel.cameraManager.toggleCamera(lifecycleOwner, pv)
                }
            },
            onToggleZoom = { showZoomSlider = !showZoomSlider },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Zoom Slider Overlay
        AnimatedVisibility(
            visible = showZoomSlider,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC0D1B2A))
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = "${(zoomLevel * 3 + 1).toInt()}x",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = zoomLevel,
                    onValueChange = {
                        zoomLevel = it
                        viewModel.cameraManager.setZoom(it)
                    },
                    modifier = Modifier.height(140.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00D2FC),
                        activeTrackColor = Color(0xFF00D2FC)
                    )
                )
            }
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick preset switcher chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = preset.id == activePresetId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF00D2FC) else Color(0x990D1B2A)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00D2FC) else Color(0x66FFFFFF),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectPreset(preset) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF070E18) else Color.White
                        )
                    }
                }
            }

            // Quick Actions Bar: Location / Maps Link / DateTime / Stamp Designer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionChip(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    onClick = onOpenLocationDialog
                )
                QuickActionChip(
                    icon = Icons.Default.AddLocationAlt,
                    label = "Maps Link",
                    onClick = { showMapsLinkDialog = true }
                )
                QuickActionChip(
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    onClick = onOpenDateTimeDialog
                )
                QuickActionChip(
                    icon = Icons.Default.Tune,
                    label = "Design",
                    onClick = onOpenStampDesigner
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shutter & Gallery Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x990D1B2A))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToGallery() }
                        .testTag("camera_gallery_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val latestPhoto = photos.firstOrNull()
                    if (latestPhoto != null) {
                        AsyncImage(
                            model = File(latestPhoto.filePath),
                            contentDescription = "Gallery",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else Color(0xFF00D2FC))
                        .clickable(enabled = !isCapturing) {
                            viewModel.capturePhotoWithStamp { photo ->
                                // Optional haptic or callback
                            }
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Flip Camera Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x990D1B2A))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            previewViewInstance?.let { pv ->
                                viewModel.cameraManager.toggleCamera(lifecycleOwner, pv)
                            }
                        }
                        .testTag("camera_flip_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }

    if (showMapsLinkDialog) {
        val clipboardManager = LocalClipboardManager.current
        var inputLink by remember { mutableStateOf("") }
        var isParsing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showMapsLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShareLocation,
                        contentDescription = null,
                        tint = Color(0xFF00D2FC),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Auto-Fit From Google Maps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste any link or text shared from Google Maps (e.g. maps.app.goo.gl or coordinates):",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    OutlinedTextField(
                        value = inputLink,
                        onValueChange = { inputLink = it },
                        placeholder = { Text("Paste Google Maps link or text...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D2FC),
                            unfocusedBorderColor = Color(0xFF243B5A)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    inputLink = clip
                                }
                            }
                        ) {
                            Text("Paste from Clipboard", color = Color(0xFF00D2FC), fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF132238))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Tip: You can also tap 'Share' directly inside Google Maps and pick GPS CAM PRO to auto-fit the camera location instantly!",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputLink.isNotBlank()) {
                            isParsing = true
                            viewModel.importGoogleMapsLocation(inputLink) { success ->
                                isParsing = false
                                if (success) {
                                    showMapsLinkDialog = false
                                }
                            }
                        }
                    },
                    enabled = inputLink.isNotBlank() && !isParsing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC))
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(
                            color = Color(0xFF070E18),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Auto-Fit Location", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMapsLinkDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF0D1B2A)
        )
    }
}

@Composable
private fun TopCameraBar(
    flashMode: Int,
    gridType: GridType,
    onToggleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x88000000))
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    },
                    contentDescription = "Flash",
                    tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) Color(0xFFF59E0B) else Color.White
                )
            }

            // Grid Lines
            IconButton(
                onClick = onToggleGrid,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x88000000))
            ) {
                Icon(
                    imageVector = Icons.Default.Grid3x3,
                    contentDescription = "Grid",
                    tint = if (gridType != GridType.NONE) Color(0xFF00D2FC) else Color.White
                )
            }
        }

        // Zoom toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x88000000))
                .clickable { onToggleZoom() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "ZOOM",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC0D1B2A))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF00D2FC),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GridOverlay(gridType: GridType) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gridColor = Color.White.copy(alpha = 0.25f)

        when (gridType) {
            GridType.RULE_OF_THIRDS -> {
                drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), 1.5f)
                drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), 1.5f)
                drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), 1.5f)
                drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), 1.5f)
            }
            GridType.CROSSHAIR -> {
                val cx = w / 2f
                val cy = h / 2f
                drawLine(Color(0xFF00D2FC).copy(alpha = 0.5f), Offset(cx - 30f, cy), Offset(cx + 30f, cy), 2f)
                drawLine(Color(0xFF00D2FC).copy(alpha = 0.5f), Offset(cx, cy - 30f), Offset(cx, cy + 30f), 2f)
                drawCircle(Color(0xFF00D2FC).copy(alpha = 0.3f), radius = 40f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
            }
            GridType.GOLDEN_RATIO -> {
                val phi = 0.618f
                drawLine(gridColor, Offset(w * (1 - phi), 0f), Offset(w * (1 - phi), h), 1.5f)
                drawLine(gridColor, Offset(w * phi, 0f), Offset(w * phi, h), 1.5f)
                drawLine(gridColor, Offset(0f, h * (1 - phi)), Offset(w, h * (1 - phi)), 1.5f)
                drawLine(gridColor, Offset(0f, h * phi), Offset(w, h * phi), 1.5f)
            }
            GridType.NONE -> {}
        }
    }
}

@Composable
private fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070E18))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Camera Permission Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "GPS CAM PRO requires camera access to capture inspection photos and apply real-time GPS coordinates.",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC))
            ) {
                Text("Grant Camera Access", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
            }
        }
    }
}
