package com.example.location

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CoordinateUtils {

    fun parseCoordinate(input: String): Pair<Double, Double>? {
        val clean = input.trim()
        if (clean.isBlank()) return null

        // Try comma or space separated decimal: "34.9546, 71.6865" or "34.9546 71.6865"
        val parts = clean.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val lat = parts[0].toDoubleOrNull()
            val lng = parts[1].toDoubleOrNull()
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                return Pair(lat, lng)
            }
        }
        return null
    }

    /**
     * Calculates distance between two coordinates in meters
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatSpeed(speedMps: Float?): String {
        if (speedMps == null || speedMps < 0.1f) return "0.0 km/h"
        val kmh = speedMps * 3.6f
        return String.format(Locale.US, "%.1f km/h", kmh)
    }
}
