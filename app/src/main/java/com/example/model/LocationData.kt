package com.example.model

import java.util.Locale
import kotlin.math.absoluteValue

enum class LocationMode {
    REAL_GPS,
    MANUAL
}

data class LocationData(
    val latitude: Double = 34.9546,
    val longitude: Double = 71.6865,
    val altitude: Double? = 826.8, // in meters
    val accuracy: Float? = 4.2f, // in meters
    val speed: Float? = 0f, // in m/s
    val locality: String = "Samar Bagh Jandool",
    val subLocality: String = "Quetta Durrani Hotel",
    val district: String = "Lower Dir",
    val province: String = "Khyber Pakhtunkhwa",
    val country: String = "Pakistan",
    val postalCode: String = "8268",
    val fullAddress: String = "Samar Bagh Jandool, Lower Dir, Khyber Pakhtunkhwa, 8268",
    val mode: LocationMode = LocationMode.REAL_GPS,
    val isGpsActive: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    fun formatDecimal(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f°%s %.4f°%s", latitude.absoluteValue, latDir, longitude.absoluteValue, lngDir)
    }

    fun formatDecimalCompact(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lngDir = if (longitude >= 0) "E" else "W"
        return String.format(Locale.US, "%.4f%s %.4f%s", latitude.absoluteValue, latDir, longitude.absoluteValue, lngDir)
    }

    fun formatDMS(): String {
        return "${toDmsString(latitude, true)} ${toDmsString(longitude, false)}"
    }

    fun getShortAddress(): String {
        val parts = listOfNotNull(
            locality.takeIf { it.isNotBlank() },
            district.takeIf { it.isNotBlank() }
        )
        return if (parts.isNotEmpty()) parts.joinToString(", ") else formatDecimal()
    }

    companion object {
        fun toDmsString(coord: Double, isLat: Boolean): String {
            val direction = if (isLat) {
                if (coord >= 0) "N" else "S"
            } else {
                if (coord >= 0) "E" else "W"
            }
            val abs = coord.absoluteValue
            val deg = abs.toInt()
            val minFull = (abs - deg) * 60.0
            val min = minFull.toInt()
            val sec = (minFull - min) * 60.0
            return String.format(Locale.US, "%d°%02d'%04.1f\"%s", deg, min, sec, direction)
        }

        fun isValidCoordinate(lat: Double, lng: Double): Boolean {
            return lat in -90.0..90.0 && lng in -180.0..180.0
        }
    }
}
