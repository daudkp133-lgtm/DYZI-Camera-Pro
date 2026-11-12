package com.example.model

data class PresetItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val isDefault: Boolean = false,
    val config: StampConfig
) {
    companion object {
        fun getDefaultPresets(): List<PresetItem> {
            return listOf(
                PresetItem(
                    id = "generator_maint",
                    name = "Generator Maintenance",
                    category = "Maintenance",
                    description = "Coolant, radiator, alternator, fuel level & hour meter inspections",
                    isDefault = true,
                    config = StampConfig(
                        id = "generator_maint",
                        name = "Generator Maintenance",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showDateTime = true,
                        showCoordinates = true,
                        coordinateFormat = CoordinateFormat.DECIMAL,
                        showAddress = true,
                        showDistrictProvince = true,
                        showPostalCode = true,
                        showAltitude = false,
                        showCustomFields = false,
                        inspectionType = "Generator Maintenance",
                        companyName = "DYZI"
                    )
                ),
                PresetItem(
                    id = "site_inspection",
                    name = "Site Inspection",
                    category = "Engineering",
                    description = "Full site audit with coordinates, altitude, project and site ID",
                    isDefault = false,
                    config = StampConfig(
                        id = "site_inspection",
                        name = "Site Inspection",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        mapStyle = MapStyleType.NORMAL,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showDateTime = true,
                        showCoordinates = true,
                        coordinateFormat = CoordinateFormat.DECIMAL_DEGREES,
                        showAltitude = true,
                        showAccuracy = true,
                        showAddress = true,
                        showCustomFields = true,
                        inspectionType = "Site Inspection",
                        companyName = "DYZI Engineering",
                        siteId = "SITE-8268",
                        projectName = "Regional Infrastructure"
                    )
                ),
                PresetItem(
                    id = "preventive_maint",
                    name = "Preventive Maintenance",
                    category = "Industrial",
                    description = "Periodic equipment maintenance and verification record",
                    isDefault = false,
                    config = StampConfig(
                        id = "preventive_maint",
                        name = "Preventive Maintenance",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        mapStyle = MapStyleType.NORMAL,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showDateTime = true,
                        showCoordinates = true,
                        coordinateFormat = CoordinateFormat.DECIMAL,
                        showCustomFields = true,
                        inspectionType = "Preventive Maintenance",
                        technicianName = "Lead Inspector"
                    )
                ),
                PresetItem(
                    id = "repair_work",
                    name = "Repair Work",
                    category = "Service",
                    description = "Field repair ticket documentation and work order verification",
                    isDefault = false,
                    config = StampConfig(
                        id = "repair_work",
                        name = "Repair Work",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        mapStyle = MapStyleType.NORMAL,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showCustomFields = true,
                        inspectionType = "Field Repair",
                        siteId = "WO-4921"
                    )
                ),
                PresetItem(
                    id = "before_inspection",
                    name = "Before Inspection",
                    category = "Audit",
                    description = "Pre-work initial site condition documentation",
                    isDefault = false,
                    config = StampConfig(
                        id = "before_inspection",
                        name = "Before Inspection",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        mapStyle = MapStyleType.NORMAL,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showCustomFields = true,
                        inspectionType = "BEFORE Inspection"
                    )
                ),
                PresetItem(
                    id = "after_inspection",
                    name = "After Inspection",
                    category = "Audit",
                    description = "Post-work handover and completed task verification",
                    isDefault = false,
                    config = StampConfig(
                        id = "after_inspection",
                        name = "After Inspection",
                        showMap = true,
                        mapPosition = StampPosition.BOTTOM_LEFT,
                        mapSize = MapSize.MEDIUM,
                        mapStyle = MapStyleType.NORMAL,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showCustomFields = true,
                        inspectionType = "AFTER Inspection"
                    )
                ),
                PresetItem(
                    id = "personal",
                    name = "Personal",
                    category = "General",
                    description = "Clean minimalist travel & field photo timestamp",
                    isDefault = false,
                    config = StampConfig(
                        id = "personal",
                        name = "Personal",
                        showMap = false,
                        timestampPosition = StampPosition.BOTTOM_RIGHT,
                        backgroundStyle = StampBackgroundStyle.TRANSPARENT_SHADOW,
                        showDateTime = true,
                        showCoordinates = true,
                        showAddress = true,
                        showDistrictProvince = true,
                        showCustomFields = false
                    )
                )
            )
        }
    }
}
