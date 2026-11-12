package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.model.CapturedPhoto
import com.example.model.LocationData
import com.example.ui.viewmodel.GpsCamViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GpsCamViewModel,
    onOpenInEditor: (CapturedPhoto) -> Unit
) {
    val context = LocalContext.current
    val allPhotos by viewModel.allPhotos.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isGridView by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var viewingPhoto by remember { mutableStateOf<CapturedPhoto?>(null) }

    val categories = listOf("All", "Camera", "Edited", "Favorites")

    val filteredPhotos = remember(allPhotos, selectedTabIndex, searchQuery) {
        allPhotos.filter { photo ->
            val matchesCategory = when (selectedTabIndex) {
                1 -> !photo.isEdited
                2 -> photo.isEdited
                3 -> photo.isFavorite
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    photo.presetName.contains(searchQuery, ignoreCase = true) ||
                    photo.address.contains(searchQuery, ignoreCase = true) ||
                    photo.inspectionType?.contains(searchQuery, ignoreCase = true) == true ||
                    photo.siteId?.contains(searchQuery, ignoreCase = true) == true
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INSPECTION GALLERY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredPhotos.size} photos recorded",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Row {
                IconButton(onClick = { isGridView = true }) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Grid View",
                        tint = if (isGridView) Color(0xFF00D2FC) else Color(0xFF94A3B8)
                    )
                }
                IconButton(onClick = { isGridView = false }) {
                    Icon(
                        imageVector = Icons.Default.ViewList,
                        contentDescription = "List View",
                        tint = if (!isGridView) Color(0xFF00D2FC) else Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by site, address, preset...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D2FC),
                unfocusedBorderColor = Color(0xFF243B5A)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Categories Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Color(0xFF00D2FC)
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Photos Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Capture a photo with GPS CAM or edit an existing one.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    PhotoGridCard(
                        photo = photo,
                        onPhotoClick = { viewingPhoto = photo },
                        onToggleFavorite = { viewModel.toggleFavorite(photo) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    PhotoListCard(
                        photo = photo,
                        onPhotoClick = { viewingPhoto = photo },
                        onToggleFavorite = { viewModel.toggleFavorite(photo) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    viewingPhoto?.let { photo ->
        PhotoDetailSheet(
            photo = photo,
            onDismiss = { viewingPhoto = null },
            onToggleFavorite = { viewModel.toggleFavorite(photo) },
            onDelete = {
                viewModel.deletePhoto(photo)
                viewingPhoto = null
            },
            onOpenInEditor = {
                viewingPhoto = null
                onOpenInEditor(photo)
            },
            onShare = {
                val file = File(photo.filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Inspection Photo"))
            }
        )
    }
}

@Composable
private fun PhotoGridCard(
    photo: CapturedPhoto,
    onPhotoClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPhotoClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = "Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top favorite button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color(0x88000000), CircleShape)
            ) {
                Icon(
                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (photo.isFavorite) Color(0xFFEF4444) else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom pill
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xCC070E18))
                    .padding(8.dp)
            ) {
                Text(
                    text = photo.presetName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = String.format("%.4f, %.4f", photo.latitude, photo.longitude),
                    fontSize = 10.sp,
                    color = Color(0xFF00D2FC),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PhotoListCard(
    photo: CapturedPhoto,
    onPhotoClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPhotoClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = File(photo.filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = photo.presetName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = LocationData.toDmsString(photo.latitude, true) + " " + LocationData.toDmsString(photo.longitude, false),
                    fontSize = 11.sp,
                    color = Color(0xFF00D2FC)
                )
                Text(
                    text = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US).format(Date(photo.timestampMillis)),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (photo.isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailSheet(
    photo: CapturedPhoto,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenInEditor: () -> Unit,
    onShare: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D1B2A)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // High Resolution Image Preview
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = File(photo.filePath),
                        contentDescription = "Inspection photo full",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Action Toolbar (Share, Edit, Favorite, Delete)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onShare) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF00D2FC))
                    }
                    IconButton(onClick = onOpenInEditor) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Re-edit Stamp", tint = Color(0xFF10B981))
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (photo.isFavorite) Color(0xFFEF4444) else Color.White
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }

            // Inspection Telemetry Details
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("VERIFIED CAPTURE TELEMETRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00D2FC))
                        DetailRow("Coordinates", String.format("%.6f, %.6f", photo.latitude, photo.longitude))
                        DetailRow("DMS", LocationData.toDmsString(photo.latitude, true) + " " + LocationData.toDmsString(photo.longitude, false))
                        DetailRow("Address", photo.address)
                        DetailRow("Captured Time", SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.US).format(Date(photo.timestampMillis)))
                        photo.altitude?.let { DetailRow("Altitude", "${it.toInt()} meters") }
                        photo.siteId?.let { DetailRow("Site ID", it) }
                        photo.technician?.let { DetailRow("Technician", it) }
                        photo.inspectionType?.let { DetailRow("Inspection Type", it) }
                        DetailRow("Source Type", if (photo.isEdited) "Edited Image" else "Live Camera")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
