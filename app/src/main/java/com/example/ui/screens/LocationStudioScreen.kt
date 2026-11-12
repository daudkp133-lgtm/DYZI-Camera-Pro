package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.location.CoordinateUtils
import com.example.maps.InteractiveGoogleMapView
import com.example.model.LocationData
import com.example.model.LocationMode
import com.example.model.MapStyleType
import com.example.ui.viewmodel.GpsCamViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationStudioScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val realGpsLocation by viewModel.realGpsLocation.collectAsState()

    var selectedTabIndex by remember {
        mutableIntStateOf(if (selectedLocation.mode == LocationMode.REAL_GPS) 0 else 1)
    }

    val tabs = listOf("Real GPS", "Manual Coords", "Map Selector")

    // Location Permission Launcher
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.locationManager.startGpsTracking()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                text = "LOCATION STUDIO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Mode Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF00D2FC)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> RealGpsTab(
                gpsData = realGpsLocation,
                isCurrentMode = selectedLocation.mode == LocationMode.REAL_GPS,
                onActivateRealGps = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.switchToRealGps()
                    } else {
                        locationPermLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onRefreshGps = {
                    viewModel.locationManager.startGpsTracking()
                }
            )
            1 -> ManualCoordsTab(
                currentLocation = selectedLocation,
                onSaveManual = { lat, lng, locality, district, province, postalCode ->
                    viewModel.setManualCoordinates(lat, lng, locality, district, province, postalCode)
                    onNavigateBack()
                }
            )
            2 -> MapSelectorTab(
                currentLocation = selectedLocation,
                onConfirmLocation = { lat, lng ->
                    viewModel.setManualCoordinates(lat, lng)
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
private fun RealGpsTab(
    gpsData: LocationData,
    isCurrentMode: Boolean,
    onActivateRealGps: () -> Unit,
    onRefreshGps: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DEVICE HARDWARE SENSORS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D2FC)
                            )
                            Text(
                                text = if (gpsData.isGpsActive) "GPS Locked (High Accuracy)" else "GPS Standby",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onRefreshGps,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF132238))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh GPS",
                                tint = Color(0xFF00D2FC)
                            )
                        }
                    }

                    // Coordinate Display
                    InfoRow(label = "Latitude", value = String.format("%.6f°", gpsData.latitude))
                    InfoRow(label = "Longitude", value = String.format("%.6f°", gpsData.longitude))
                    InfoRow(label = "DMS Format", value = gpsData.formatDMS())
                    InfoRow(label = "Altitude", value = gpsData.altitude?.let { "${it.toInt()} meters" } ?: "N/A")
                    InfoRow(label = "Accuracy", value = gpsData.accuracy?.let { "±${it.toInt()} m" } ?: "N/A")
                    InfoRow(label = "Speed", value = CoordinateUtils.formatSpeed(gpsData.speed))
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "REVERSE GEOCODED ADDRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FC)
                    )
                    InfoRow(label = "Locality", value = gpsData.locality)
                    InfoRow(label = "Sub-locality", value = gpsData.subLocality)
                    InfoRow(label = "District", value = gpsData.district)
                    InfoRow(label = "Province / State", value = gpsData.province)
                    InfoRow(label = "Country", value = gpsData.country)
                    InfoRow(label = "Postal Code", value = gpsData.postalCode)
                }
            }
        }

        item {
            Button(
                onClick = onActivateRealGps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentMode) Color(0xFF10B981) else Color(0xFF00D2FC)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isCurrentMode) Icons.Default.Check else Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color(0xFF070E18)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCurrentMode) "REAL GPS IS CURRENTLY ACTIVE" else "SET AS ACTIVE CAMERA LOCATION",
                    color = Color(0xFF070E18),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ManualCoordsTab(
    currentLocation: LocationData,
    onSaveManual: (Double, Double, String, String, String, String) -> Unit
) {
    var latText by remember { mutableStateOf(currentLocation.latitude.toString()) }
    var lngText by remember { mutableStateOf(currentLocation.longitude.toString()) }
    var localityText by remember { mutableStateOf(currentLocation.locality) }
    var districtText by remember { mutableStateOf(currentLocation.district) }
    var provinceText by remember { mutableStateOf(currentLocation.province) }
    var postalCodeText by remember { mutableStateOf(currentLocation.postalCode) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "MANUAL COORDINATES & ADDRESS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D2FC)
            )
            Text(
                text = "Manual coordinates will stay fixed and will not be overwritten by GPS updates.",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        item {
            val clipboardManager = LocalClipboardManager.current
            val coroutineScope = rememberCoroutineScope()
            var shareLinkInput by remember { mutableStateOf("") }
            var isResolving by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D2FC).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShareLocation,
                            contentDescription = null,
                            tint = Color(0xFF00D2FC),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DIRECT GOOGLE MAPS SHARE / LINK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D2FC)
                        )
                    }

                    Text(
                        text = "Paste a Google Maps shared link, coordinates, or text to auto-fit camera coordinates:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    OutlinedTextField(
                        value = shareLinkInput,
                        onValueChange = { shareLinkInput = it },
                        placeholder = { Text("https://maps.app.goo.gl/... or coordinates") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00D2FC),
                            unfocusedBorderColor = Color(0xFF243B5A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) shareLinkInput = clip
                            }
                        ) {
                            Text("Paste Clipboard", color = Color(0xFF00D2FC), fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isResolving = true
                                    val parsed = com.example.location.GoogleMapsShareParser.parseSharedText(shareLinkInput)
                                    isResolving = false
                                    if (parsed != null) {
                                        latText = parsed.latitude.toString()
                                        lngText = parsed.longitude.toString()
                                        parsed.placeName?.let { localityText = it }
                                    } else {
                                        errorMessage = "Could not parse location from this link"
                                    }
                                }
                            },
                            enabled = shareLinkInput.isNotBlank() && !isResolving,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC))
                        ) {
                            Text(
                                text = if (isResolving) "Auto-Fitting..." else "Auto-Fit Coordinates",
                                color = Color(0xFF070E18),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Latitude (-90 to 90)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D2FC),
                        unfocusedBorderColor = Color(0xFF243B5A)
                    )
                )

                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("Longitude (-180 to 180)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D2FC),
                        unfocusedBorderColor = Color(0xFF243B5A)
                    )
                )
            }
        }

        item {
            OutlinedTextField(
                value = localityText,
                onValueChange = { localityText = it },
                label = { Text("Locality / Place Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D2FC),
                    unfocusedBorderColor = Color(0xFF243B5A)
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = districtText,
                    onValueChange = { districtText = it },
                    label = { Text("District / City") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D2FC),
                        unfocusedBorderColor = Color(0xFF243B5A)
                    )
                )

                OutlinedTextField(
                    value = postalCodeText,
                    onValueChange = { postalCodeText = it },
                    label = { Text("Postal Code") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D2FC),
                        unfocusedBorderColor = Color(0xFF243B5A)
                    )
                )
            }
        }

        item {
            OutlinedTextField(
                value = provinceText,
                onValueChange = { provinceText = it },
                label = { Text("Province / State / Country") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D2FC),
                    unfocusedBorderColor = Color(0xFF243B5A)
                )
            )
        }

        errorMessage?.let { err ->
            item {
                Text(text = err, color = Color(0xFFEF4444), fontSize = 13.sp)
            }
        }

        item {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (lat == null || lat !in -90.0..90.0) {
                        errorMessage = "Latitude must be between -90 and +90"
                        return@Button
                    }
                    if (lng == null || lng !in -180.0..180.0) {
                        errorMessage = "Longitude must be between -180 and +180"
                        return@Button
                    }
                    errorMessage = null
                    onSaveManual(lat, lng, localityText, districtText, provinceText, postalCodeText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_manual_coords_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Manual Location", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MapSelectorTab(
    currentLocation: LocationData,
    onConfirmLocation: (Double, Double) -> Unit
) {
    var pinnedLat by remember { mutableStateOf(currentLocation.latitude) }
    var pinnedLng by remember { mutableStateOf(currentLocation.longitude) }
    var currentMapStyle by remember { mutableStateOf(MapStyleType.NORMAL) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map with interactive marker
        InteractiveGoogleMapView(
            location = currentLocation.copy(latitude = pinnedLat, longitude = pinnedLng),
            mapStyle = currentMapStyle,
            isInteractive = true,
            onLocationChanged = { lat, lng ->
                pinnedLat = lat
                pinnedLng = lng
            },
            modifier = Modifier.fillMaxSize()
        )

        // Map Style Switcher (Top Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCC0D1B2A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapStyleType.values().forEach { style ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentMapStyle == style) Color(0xFF00D2FC) else Color.Transparent)
                        .clickable { currentMapStyle = style }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = style.name.take(3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMapStyle == style) Color(0xFF070E18) else Color.White
                    )
                }
            }
        }

        // Bottom Info & Confirm Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2070E18)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DRAG PIN OR TAP MAP TO SELECT SPOT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D2FC)
                )
                Text(
                    text = String.format("%.6f, %.6f", pinnedLat, pinnedLng),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { onConfirmLocation(pinnedLat, pinnedLng) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_map_location_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Map Location", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
