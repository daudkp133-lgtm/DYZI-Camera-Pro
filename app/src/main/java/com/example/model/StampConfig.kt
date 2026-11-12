package com.example.model

enum class StampPosition {
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_BANNER
}

enum class MapSize {
    SMALL, // ~20% width
    MEDIUM, // ~28% width
    LARGE // ~36% width
}

enum class MapStyleType {
    NORMAL,
    SATELLITE,
    HYBRID,
    TERRAIN
}

enum class CoordinateFormat {
    DECIMAL, // 34.9546N 71.6865E
    DECIMAL_DEGREES, // 34.9546°N 71.6865°E
    DMS // 34°57'16.6"N 71°41'11.4"E
}

enum class StampBackgroundStyle {
    TRANSPARENT_SHADOW, // As seen in user reference photo: transparent with black text shadow
    DARK_CARD, // Translucent dark rounded card
    SOLID_BLACK,
    CYAN_ACCENT_CARD
}

data class StampConfig(
    val id: String = "default_preset",
    val name: String = "Generator Maintenance",
    // Map settings
    val showMap: Boolean = true,
    val mapPosition: StampPosition = StampPosition.BOTTOM_LEFT,
    val mapSize: MapSize = MapSize.MEDIUM,
    val mapStyle: MapStyleType = MapStyleType.NORMAL,
    val mapZoom: Float = 15f,
    // Timestamp settings
    val timestampPosition: StampPosition = StampPosition.BOTTOM_RIGHT,
    val backgroundStyle: StampBackgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
    val textColorHex: String = "#FFFFFF",
    val isBold: Boolean = true,
    val isMonospace: Boolean = false,
    val textSizeMultiplier: Float = 1.0f,
    // Field toggles
    val showDateTime: Boolean = true,
    val showCoordinates: Boolean = true,
    val coordinateFormat: CoordinateFormat = CoordinateFormat.DECIMAL,
    val showAltitude: Boolean = true,
    val showAccuracy: Boolean = false,
    val showAddress: Boolean = true,
    val showDistrictProvince: Boolean = true,
    val showPostalCode: Boolean = true,
    // Custom inspection fields
    val showCustomFields: Boolean = false,
    val projectName: String = "",
    val siteId: String = "",
    val technicianName: String = "",
    val companyName: String = "DYZI",
    val inspectionType: String = "Generator Maintenance",
    val customNotes: String = "",
    // Logo / Watermark
    val showLogoWatermark: Boolean = false,
    // Zero margins with borders (flush against bottom and side edges)
    val useZeroMargins: Boolean = true
) {
    fun getLines(location: LocationData, dateTime: DateTimeData): List<String> {
        val lines = mutableListOf<String>()

        if (showDateTime) {
            lines.add(dateTime.formatFormattedDateTime())
        }

        if (showCoordinates) {
            val coordStr = when (coordinateFormat) {
                CoordinateFormat.DECIMAL -> location.formatDecimalCompact()
                CoordinateFormat.DECIMAL_DEGREES -> location.formatDecimal()
                CoordinateFormat.DMS -> location.formatDMS()
            }
            lines.add(coordStr)
        }

        if (showAddress && location.locality.isNotBlank()) {
            lines.add(location.locality)
        }

        if (showDistrictProvince) {
            if (location.district.isNotBlank()) {
                lines.add(location.district)
            }
            if (location.province.isNotBlank()) {
                lines.add(location.province)
            }
        }

        val footerParts = mutableListOf<String>()
        if (showPostalCode && location.postalCode.isNotBlank()) {
            footerParts.add(location.postalCode)
        }
        if (showAltitude && location.altitude != null) {
            footerParts.add("Alt: ${location.altitude.toInt()}m")
        }
        if (showAccuracy && location.accuracy != null) {
            footerParts.add("Acc: ±${location.accuracy.toInt()}m")
        }
        if (footerParts.isNotEmpty()) {
            lines.add(footerParts.joinToString("  •  "))
        }

        if (showCustomFields) {
            if (companyName.isNotBlank() || inspectionType.isNotBlank()) {
                val header = listOfNotNull(
                    companyName.takeIf { it.isNotBlank() },
                    inspectionType.takeIf { it.isNotBlank() }
                ).joinToString(" | ")
                lines.add(header)
            }
            if (siteId.isNotBlank() || technicianName.isNotBlank()) {
                val details = listOfNotNull(
                    siteId.takeIf { it.isNotBlank() }?.let { "Site: $it" },
                    technicianName.takeIf { it.isNotBlank() }?.let { "Tech: $it" }
                ).joinToString("  ")
                lines.add(details)
            }
            if (projectName.isNotBlank()) {
                lines.add("Proj: $projectName")
            }
            if (customNotes.isNotBlank()) {
                lines.add("Note: $customNotes")
            }
        }

        return lines
    }
}
