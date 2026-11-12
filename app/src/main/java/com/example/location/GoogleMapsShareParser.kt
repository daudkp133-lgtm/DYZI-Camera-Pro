package com.example.location

import com.example.model.LocationData
import com.example.model.LocationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedMapLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null
)

object GoogleMapsShareParser {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Regular expressions for various Google Maps URL and text formats
    private val AT_COORDS_REGEX = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
    private val Q_COORDS_REGEX = Pattern.compile("[?&](?:q|query|ll|loc)=(-?\\d+\\.\\d+)[,%20]+(-?\\d+\\.\\d+)")
    private val PLACE_COORDS_REGEX = Pattern.compile("/place/(-?\\d+\\.\\d+)[,%20]+(-?\\d+\\.\\d+)")
    private val GEO_URI_REGEX = Pattern.compile("geo:(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
    private val RAW_DECIMAL_REGEX = Pattern.compile("(-?\\d{1,2}\\.\\d{3,})[\\s,]+(-?\\d{1,3}\\.\\d{3,})")
    private val DMS_REGEX = Pattern.compile("(\\d+)°(?:(\\d+)'(?:([\\d.]+)\")?)?\\s*([NS])\\s+(\\d+)°(?:(\\d+)'(?:([\\d.]+)\")?)?\\s*([EW])", Pattern.CASE_INSENSITIVE)

    suspend fun parseSharedText(input: String): ParsedMapLocation? = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return@withContext null

        // 1. Check if there is an explicit place name in the shared text (e.g. "Dropped pin near Samar Bagh")
        var placeName: String? = null
        val lines = trimmed.lines().map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            if (!line.startsWith("http", ignoreCase = true) && !line.startsWith("geo:", ignoreCase = true)) {
                if (line.contains("near", ignoreCase = true) || line.contains("pin", ignoreCase = true) || line.length in 3..40) {
                    placeName = line.replace(Regex("^(Dropped pin|near|Pin dropped)\\s*(near|at)?\\s*", RegexOption.IGNORE_CASE), "")
                        .trim()
                    if (placeName.isBlank()) placeName = null
                    break
                }
            }
        }

        // 2. Direct coordinate search in input text
        val directCoords = extractCoordinatesFromText(trimmed)
        if (directCoords != null) {
            return@withContext directCoords.copy(placeName = placeName ?: directCoords.placeName)
        }

        // 3. Search for Google Maps links (e.g. https://maps.app.goo.gl/... or https://goo.gl/maps/...)
        val urlRegex = Pattern.compile("https?://[^\\s]+")
        val matcher = urlRegex.matcher(trimmed)
        if (matcher.find()) {
            val url = matcher.group()
            // Check direct URL pattern first
            val urlCoords = extractCoordinatesFromUrl(url)
            if (urlCoords != null) {
                return@withContext urlCoords.copy(placeName = placeName)
            }

            // Resolve shortened link via redirect
            val resolvedCoords = resolveShortenedUrl(url)
            if (resolvedCoords != null) {
                return@withContext resolvedCoords.copy(placeName = placeName ?: resolvedCoords.placeName)
            }
        }

        null
    }

    private fun extractCoordinatesFromText(text: String): ParsedMapLocation? {
        // Raw decimal coordinates (e.g. 34.9546, 71.6865)
        val decMatcher = RAW_DECIMAL_REGEX.matcher(text)
        if (decMatcher.find()) {
            val lat = decMatcher.group(1)?.toDoubleOrNull()
            val lng = decMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        // DMS coordinates (e.g. 34°57'16.6"N 71°41'11.4"E)
        val dmsMatcher = DMS_REGEX.matcher(text)
        if (dmsMatcher.find()) {
            val latDeg = dmsMatcher.group(1)?.toDoubleOrNull() ?: 0.0
            val latMin = dmsMatcher.group(2)?.toDoubleOrNull() ?: 0.0
            val latSec = dmsMatcher.group(3)?.toDoubleOrNull() ?: 0.0
            val latDir = dmsMatcher.group(4) ?: "N"

            val lngDeg = dmsMatcher.group(5)?.toDoubleOrNull() ?: 0.0
            val lngMin = dmsMatcher.group(6)?.toDoubleOrNull() ?: 0.0
            val lngSec = dmsMatcher.group(7)?.toDoubleOrNull() ?: 0.0
            val lngDir = dmsMatcher.group(8) ?: "E"

            var lat = latDeg + (latMin / 60.0) + (latSec / 3600.0)
            if (latDir.equals("S", ignoreCase = true)) lat = -lat

            var lng = lngDeg + (lngMin / 60.0) + (lngSec / 3600.0)
            if (lngDir.equals("W", ignoreCase = true)) lng = -lng

            if (LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        // Geo URI (geo:34.9546,71.6865)
        val geoMatcher = GEO_URI_REGEX.matcher(text)
        if (geoMatcher.find()) {
            val lat = geoMatcher.group(1)?.toDoubleOrNull()
            val lng = geoMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        return null
    }

    private fun extractCoordinatesFromUrl(url: String): ParsedMapLocation? {
        // @lat,lng
        val atMatcher = AT_COORDS_REGEX.matcher(url)
        if (atMatcher.find()) {
            val lat = atMatcher.group(1)?.toDoubleOrNull()
            val lng = atMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        // ?q=lat,lng
        val qMatcher = Q_COORDS_REGEX.matcher(url)
        if (qMatcher.find()) {
            val lat = qMatcher.group(1)?.toDoubleOrNull()
            val lng = qMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        // /place/lat,lng
        val placeMatcher = PLACE_COORDS_REGEX.matcher(url)
        if (placeMatcher.find()) {
            val lat = placeMatcher.group(1)?.toDoubleOrNull()
            val lng = placeMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lng != null && LocationData.isValidCoordinate(lat, lng)) {
                return ParsedMapLocation(lat, lng)
            }
        }

        return null
    }

    private fun resolveShortenedUrl(shortUrl: String): ParsedMapLocation? {
        return try {
            val request = Request.Builder()
                .url(shortUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val finalUrl = response.request.url.toString()
                extractCoordinatesFromUrl(finalUrl) ?: run {
                    // Try parsing HTML if available or redirect history
                    val prior = response.priorResponse?.header("Location")
                    if (prior != null) extractCoordinatesFromUrl(prior) else null
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
