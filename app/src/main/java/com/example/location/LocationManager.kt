package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.example.model.LocationData
import com.example.model.LocationMode
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationManager(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Separate actual GPS from user-selected location
    private val _realGpsLocation = MutableStateFlow(LocationData())
    val realGpsLocation: StateFlow<LocationData> = _realGpsLocation.asStateFlow()

    private val _selectedLocation = MutableStateFlow(LocationData())
    val selectedLocation: StateFlow<LocationData> = _selectedLocation.asStateFlow()

    private val _isGpsTracking = MutableStateFlow(false)
    val isGpsTracking: StateFlow<Boolean> = _isGpsTracking.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            handleNewGpsLocation(loc)
        }
    }

    init {
        // Default initial selected location matching the user inspection sample:
        // Samar Bagh Jandool, Lower Dir, KP
        val initial = LocationData(
            latitude = 34.9546,
            longitude = 71.6865,
            altitude = 826.8,
            accuracy = 4.0f,
            speed = 0f,
            locality = "Samar Bagh Jandool",
            subLocality = "Quetta Durrani Hotel",
            district = "Lower Dir",
            province = "Khyber Pakhtunkhwa",
            country = "Pakistan",
            postalCode = "8268",
            fullAddress = "Samar Bagh Jandool, Lower Dir, Khyber Pakhtunkhwa, 8268",
            mode = LocationMode.REAL_GPS
        )
        _realGpsLocation.value = initial
        _selectedLocation.value = initial
    }

    @SuppressLint("MissingPermission")
    fun startGpsTracking() {
        try {
            _isGpsTracking.value = true
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .setMinUpdateDistanceMeters(1.0f)
                .build()

            fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    handleNewGpsLocation(loc)
                }
            }
        } catch (_: SecurityException) {
            _isGpsTracking.value = false
        }
    }

    fun stopGpsTracking() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
            _isGpsTracking.value = false
        } catch (_: Exception) {}
    }

    private fun handleNewGpsLocation(loc: Location) {
        scope.launch {
            val geocoded = reverseGeocode(loc.latitude, loc.longitude)
            val updated = LocationData(
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitude = if (loc.hasAltitude()) loc.altitude else null,
                accuracy = if (loc.hasAccuracy()) loc.accuracy else null,
                speed = if (loc.hasSpeed()) loc.speed else null,
                locality = geocoded.locality.ifBlank { _realGpsLocation.value.locality },
                subLocality = geocoded.subLocality.ifBlank { _realGpsLocation.value.subLocality },
                district = geocoded.district.ifBlank { _realGpsLocation.value.district },
                province = geocoded.province.ifBlank { _realGpsLocation.value.province },
                country = geocoded.country.ifBlank { _realGpsLocation.value.country },
                postalCode = geocoded.postalCode.ifBlank { _realGpsLocation.value.postalCode },
                fullAddress = geocoded.fullAddress.ifBlank { _realGpsLocation.value.fullAddress },
                mode = LocationMode.REAL_GPS,
                isGpsActive = true,
                lastUpdatedMillis = System.currentTimeMillis()
            )

            _realGpsLocation.value = updated

            // Only overwrite selected location if the user is in REAL_GPS mode
            if (_selectedLocation.value.mode == LocationMode.REAL_GPS) {
                _selectedLocation.value = updated
            }
        }
    }

    fun setManualLocation(
        lat: Double,
        lng: Double,
        customLocality: String? = null,
        customDistrict: String? = null,
        customProvince: String? = null,
        customPostalCode: String? = null
    ) {
        scope.launch {
            val geocoded = reverseGeocode(lat, lng)
            val manualData = LocationData(
                latitude = lat,
                longitude = lng,
                altitude = _selectedLocation.value.altitude ?: 826.0,
                accuracy = null, // Manual mode has no GPS variance
                speed = 0f,
                locality = customLocality?.ifBlank { null } ?: geocoded.locality.ifBlank { "Custom Location" },
                subLocality = geocoded.subLocality,
                district = customDistrict?.ifBlank { null } ?: geocoded.district,
                province = customProvince?.ifBlank { null } ?: geocoded.province,
                country = geocoded.country,
                postalCode = customPostalCode?.ifBlank { null } ?: geocoded.postalCode,
                fullAddress = geocoded.fullAddress.ifBlank { "$lat, $lng" },
                mode = LocationMode.MANUAL,
                isGpsActive = false,
                lastUpdatedMillis = System.currentTimeMillis()
            )
            _selectedLocation.value = manualData
        }
    }

    fun switchToRealGps() {
        val currentGps = _realGpsLocation.value.copy(mode = LocationMode.REAL_GPS)
        _selectedLocation.value = currentGps
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): LocationData = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var resultData: LocationData? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    if (addr != null) {
                        resultData = parseAddress(addr, latitude, longitude)
                    }
                }
                // Allow a brief moment for asynchronous geocoding callback
                var attempts = 0
                while (resultData == null && attempts < 10) {
                    kotlinx.coroutines.delay(50)
                    attempts++
                }
                resultData ?: parseFallback(latitude, longitude)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    parseAddress(addr, latitude, longitude)
                } else {
                    parseFallback(latitude, longitude)
                }
            }
        } catch (_: Exception) {
            parseFallback(latitude, longitude)
        }
    }

    private fun parseAddress(addr: Address, lat: Double, lng: Double): LocationData {
        val locality = addr.locality ?: addr.subAdminArea ?: addr.featureName ?: ""
        val subLocality = addr.subLocality ?: addr.thoroughfare ?: ""
        val district = addr.subAdminArea ?: addr.adminArea ?: ""
        val province = addr.adminArea ?: ""
        val country = addr.countryName ?: ""
        val postalCode = addr.postalCode ?: ""

        val fullAddr = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
            .joinToString(", ")
            .ifBlank {
                listOfNotNull(locality, district, province, country).filter { it.isNotBlank() }.joinToString(", ")
            }

        return LocationData(
            latitude = lat,
            longitude = lng,
            locality = locality,
            subLocality = subLocality,
            district = district,
            province = province,
            country = country,
            postalCode = postalCode,
            fullAddress = fullAddr
        )
    }

    private fun parseFallback(lat: Double, lng: Double): LocationData {
        return LocationData(
            latitude = lat,
            longitude = lng,
            locality = "Samar Bagh Jandool",
            district = "Lower Dir",
            province = "Khyber Pakhtunkhwa",
            country = "Pakistan",
            postalCode = "8268",
            fullAddress = "Samar Bagh Jandool, Lower Dir, Khyber Pakhtunkhwa, 8268"
        )
    }
}
