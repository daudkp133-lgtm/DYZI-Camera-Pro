package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CapturedPhoto

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val rawFilePath: String?,
    val thumbnailPath: String?,
    val timestampMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val address: String,
    val presetName: String,
    val isFavorite: Boolean,
    val isEdited: Boolean,
    val imageWidth: Int,
    val imageHeight: Int,
    val inspectionType: String?,
    val siteId: String?,
    val technician: String?,
    val notes: String?
) {
    fun toModel(): CapturedPhoto {
        return CapturedPhoto(
            id = id,
            filePath = filePath,
            rawFilePath = rawFilePath,
            thumbnailPath = thumbnailPath,
            timestampMillis = timestampMillis,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            address = address,
            presetName = presetName,
            isFavorite = isFavorite,
            isEdited = isEdited,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            inspectionType = inspectionType,
            siteId = siteId,
            technician = technician,
            notes = notes
        )
    }

    companion object {
        fun fromModel(photo: CapturedPhoto): PhotoEntity {
            return PhotoEntity(
                id = photo.id,
                filePath = photo.filePath,
                rawFilePath = photo.rawFilePath,
                thumbnailPath = photo.thumbnailPath,
                timestampMillis = photo.timestampMillis,
                latitude = photo.latitude,
                longitude = photo.longitude,
                altitude = photo.altitude,
                address = photo.address,
                presetName = photo.presetName,
                isFavorite = photo.isFavorite,
                isEdited = photo.isEdited,
                imageWidth = photo.imageWidth,
                imageHeight = photo.imageHeight,
                inspectionType = photo.inspectionType,
                siteId = photo.siteId,
                technician = photo.technician,
                notes = photo.notes
            )
        }
    }
}
