package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CoordinateFormat
import com.example.model.MapSize
import com.example.model.MapStyleType
import com.example.model.PresetItem
import com.example.model.StampBackgroundStyle
import com.example.model.StampConfig
import com.example.model.StampPosition

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isDefault: Boolean,
    // Flattened config fields for robust SQLite storage
    val showMap: Boolean,
    val mapPosition: String,
    val mapSize: String,
    val mapStyle: String,
    val mapZoom: Float,
    val timestampPosition: String,
    val backgroundStyle: String,
    val textColorHex: String,
    val isBold: Boolean,
    val isMonospace: Boolean,
    val textSizeMultiplier: Float,
    val showDateTime: Boolean,
    val showCoordinates: Boolean,
    val coordinateFormat: String,
    val showAltitude: Boolean,
    val showAccuracy: Boolean,
    val showAddress: Boolean,
    val showDistrictProvince: Boolean,
    val showPostalCode: Boolean,
    val showCustomFields: Boolean,
    val projectName: String,
    val siteId: String,
    val technicianName: String,
    val companyName: String,
    val inspectionType: String,
    val customNotes: String,
    val showLogoWatermark: Boolean
) {
    fun toModel(): PresetItem {
        return PresetItem(
            id = id,
            name = name,
            category = category,
            description = description,
            isDefault = isDefault,
            config = StampConfig(
                id = id,
                name = name,
                showMap = showMap,
                mapPosition = runCatching { StampPosition.valueOf(mapPosition) }.getOrDefault(StampPosition.BOTTOM_LEFT),
                mapSize = runCatching { MapSize.valueOf(mapSize) }.getOrDefault(MapSize.MEDIUM),
                mapStyle = runCatching { MapStyleType.valueOf(mapStyle) }.getOrDefault(MapStyleType.NORMAL),
                mapZoom = mapZoom,
                timestampPosition = runCatching { StampPosition.valueOf(timestampPosition) }.getOrDefault(StampPosition.BOTTOM_RIGHT),
                backgroundStyle = runCatching { StampBackgroundStyle.valueOf(backgroundStyle) }.getOrDefault(StampBackgroundStyle.TRANSPARENT_SHADOW),
                textColorHex = textColorHex,
                isBold = isBold,
                isMonospace = isMonospace,
                textSizeMultiplier = textSizeMultiplier,
                showDateTime = showDateTime,
                showCoordinates = showCoordinates,
                coordinateFormat = runCatching { CoordinateFormat.valueOf(coordinateFormat) }.getOrDefault(CoordinateFormat.DECIMAL),
                showAltitude = showAltitude,
                showAccuracy = showAccuracy,
                showAddress = showAddress,
                showDistrictProvince = showDistrictProvince,
                showPostalCode = showPostalCode,
                showCustomFields = showCustomFields,
                projectName = projectName,
                siteId = siteId,
                technicianName = technicianName,
                companyName = companyName,
                inspectionType = inspectionType,
                customNotes = customNotes,
                showLogoWatermark = showLogoWatermark
            )
        )
    }

    companion object {
        fun fromModel(preset: PresetItem): PresetEntity {
            val cfg = preset.config
            return PresetEntity(
                id = preset.id,
                name = preset.name,
                category = preset.category,
                description = preset.description,
                isDefault = preset.isDefault,
                showMap = cfg.showMap,
                mapPosition = cfg.mapPosition.name,
                mapSize = cfg.mapSize.name,
                mapStyle = cfg.mapStyle.name,
                mapZoom = cfg.mapZoom,
                timestampPosition = cfg.timestampPosition.name,
                backgroundStyle = cfg.backgroundStyle.name,
                textColorHex = cfg.textColorHex,
                isBold = cfg.isBold,
                isMonospace = cfg.isMonospace,
                textSizeMultiplier = cfg.textSizeMultiplier,
                showDateTime = cfg.showDateTime,
                showCoordinates = cfg.showCoordinates,
                coordinateFormat = cfg.coordinateFormat.name,
                showAltitude = cfg.showAltitude,
                showAccuracy = cfg.showAccuracy,
                showAddress = cfg.showAddress,
                showDistrictProvince = cfg.showDistrictProvince,
                showPostalCode = cfg.showPostalCode,
                showCustomFields = cfg.showCustomFields,
                projectName = cfg.projectName,
                siteId = cfg.siteId,
                technicianName = cfg.technicianName,
                companyName = cfg.companyName,
                inspectionType = cfg.inspectionType,
                customNotes = cfg.customNotes,
                showLogoWatermark = cfg.showLogoWatermark
            )
        }
    }
}
