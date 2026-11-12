package com.example.model

enum class CameraFacing {
    BACK,
    FRONT
}

enum class GridType {
    NONE,
    RULE_OF_THIRDS,
    GOLDEN_RATIO,
    CROSSHAIR
}

enum class ImageQualityOption(val qualityPercent: Int, val label: String) {
    HIGH(85, "High (85%)"),
    VERY_HIGH(95, "Very High (95%)"),
    MAXIMUM(100, "Maximum (100%)")
}

enum class ExportFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png")
}

data class AppSettings(
    val defaultCamera: CameraFacing = CameraFacing.BACK,
    val defaultGrid: GridType = GridType.RULE_OF_THIRDS,
    val defaultMapType: MapStyleType = MapStyleType.NORMAL,
    val defaultLocationMode: LocationMode = LocationMode.REAL_GPS,
    val imageQuality: ImageQualityOption = ImageQualityOption.VERY_HIGH,
    val exportFormat: ExportFormat = ExportFormat.JPEG,
    val saveOriginalImage: Boolean = true,
    val saveStampedImage: Boolean = true,
    val defaultPresetId: String = "generator_maint",
    val darkTheme: Boolean = true,
    val shutterSound: Boolean = true,
    val hapticFeedback: Boolean = true
)
