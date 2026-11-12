package com.example.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.DateTimeStudioScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationStudioScreen
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.PresetManagerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.GpsCamViewModel

enum class AppScreen {
    HOME,
    CAMERA,
    GALLERY,
    SETTINGS,
    LOCATION_STUDIO,
    DATETIME_STUDIO,
    STAMP_DESIGNER,
    PHOTO_EDITOR,
    PRESET_MANAGER
}

@Composable
fun AppNavigation(viewModel: GpsCamViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var previousScreen by remember { mutableStateOf(AppScreen.HOME) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openCameraEvent.collect {
            currentScreen = AppScreen.CAMERA
        }
    }

    fun navigateTo(screen: AppScreen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    fun navigateBack() {
        currentScreen = if (currentScreen in listOf(
                AppScreen.LOCATION_STUDIO,
                AppScreen.DATETIME_STUDIO,
                AppScreen.STAMP_DESIGNER,
                AppScreen.PHOTO_EDITOR,
                AppScreen.PRESET_MANAGER
            )
        ) {
            previousScreen
        } else {
            AppScreen.HOME
        }
    }

    // Handle system back gesture
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            navigateBack()
        }
    }

    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.CAMERA,
        AppScreen.GALLERY,
        AppScreen.SETTINGS
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                GpsCamBottomBar(
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        previousScreen = currentScreen
                        currentScreen = screen
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCamera = { navigateTo(AppScreen.CAMERA) },
                    onNavigateToLocationStudio = { navigateTo(AppScreen.LOCATION_STUDIO) },
                    onNavigateToDateTimeStudio = { navigateTo(AppScreen.DATETIME_STUDIO) },
                    onNavigateToStampDesigner = { navigateTo(AppScreen.STAMP_DESIGNER) },
                    onNavigateToPhotoEditor = { navigateTo(AppScreen.PHOTO_EDITOR) },
                    onNavigateToPresets = { navigateTo(AppScreen.PRESET_MANAGER) },
                    onNavigateToGallery = { navigateTo(AppScreen.GALLERY) },
                    onNavigateToSettings = { navigateTo(AppScreen.SETTINGS) }
                )

                AppScreen.CAMERA -> CameraScreen(
                    viewModel = viewModel,
                    onNavigateToGallery = { navigateTo(AppScreen.GALLERY) },
                    onOpenLocationDialog = { navigateTo(AppScreen.LOCATION_STUDIO) },
                    onOpenDateTimeDialog = { navigateTo(AppScreen.DATETIME_STUDIO) },
                    onOpenStampDesigner = { navigateTo(AppScreen.STAMP_DESIGNER) }
                )

                AppScreen.GALLERY -> GalleryScreen(
                    viewModel = viewModel,
                    onOpenInEditor = { photo ->
                        navigateTo(AppScreen.PHOTO_EDITOR)
                    }
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )

                AppScreen.LOCATION_STUDIO -> LocationStudioScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )

                AppScreen.DATETIME_STUDIO -> DateTimeStudioScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )

                AppScreen.STAMP_DESIGNER -> com.example.ui.screens.StampDesignerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )

                AppScreen.PHOTO_EDITOR -> PhotoEditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() },
                    onNavigateToGallery = { navigateTo(AppScreen.GALLERY) }
                )

                AppScreen.PRESET_MANAGER -> PresetManagerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() },
                    onEditPresetInDesigner = {
                        navigateTo(AppScreen.STAMP_DESIGNER)
                    }
                )
            }
        }
    }
}

@Composable
private fun GpsCamBottomBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Color(0xFF070E18))
            .border(0.5.dp, Color(0xFF243B5A))
            .padding(vertical = 6.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                icon = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                isSelected = currentScreen == AppScreen.HOME,
                onClick = { onTabSelected(AppScreen.HOME) },
                testTag = "nav_home"
            )

            // Prominent center camera button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00D2FC))
                    .clickable { onTabSelected(AppScreen.CAMERA) }
                    .testTag("nav_camera"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = Color(0xFF070E18),
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(
                label = "Gallery",
                icon = if (currentScreen == AppScreen.GALLERY) Icons.Filled.Collections else Icons.Outlined.Collections,
                isSelected = currentScreen == AppScreen.GALLERY,
                onClick = { onTabSelected(AppScreen.GALLERY) },
                testTag = "nav_gallery"
            )

            BottomNavItem(
                label = "Settings",
                icon = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                isSelected = currentScreen == AppScreen.SETTINGS,
                onClick = { onTabSelected(AppScreen.SETTINGS) },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF00D2FC) else Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF00D2FC) else Color(0xFF94A3B8)
        )
    }
}
