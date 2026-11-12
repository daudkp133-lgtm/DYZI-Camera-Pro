package com.example.model

data class CapturedPhoto(
    val id: Long = 0,
    val filePath: String, // Stamped output image path
    val rawFilePath: String? = null, // Original raw photo if saved
    val thumbnailPath: String? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val address: String,
    val presetName: String,
    val isFavorite: Boolean = false,
    val isEdited: Boolean = false, // True if created via existing photo editor
    val imageWidth: Int = 1920,
    val imageHeight: Int = 1080,
    val inspectionType: String? = null,
    val siteId: String? = null,
    val technician: String? = null,
    val notes: String? = null
)
