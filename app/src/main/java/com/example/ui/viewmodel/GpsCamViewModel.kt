package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraManager
import com.example.database.AppDatabase
import com.example.database.PhotoEntity
import com.example.database.PresetEntity
import com.example.location.LocationManager
import com.example.model.AppSettings
import com.example.model.CapturedPhoto
import com.example.model.DateTimeData
import com.example.model.GridType
import com.example.model.LocationData
import com.example.model.LocationMode
import com.example.model.PresetItem
import com.example.model.StampConfig
import com.example.model.TimeSource
import com.example.rendering.StampBitmapRenderer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class GpsCamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val photoDao = db.photoDao()
    private val presetDao = db.presetDao()

    val locationManager = LocationManager(application)
    val cameraManager = CameraManager(application)

    // Current State
    val selectedLocation: StateFlow<LocationData> = locationManager.selectedLocation
    val realGpsLocation: StateFlow<LocationData> = locationManager.realGpsLocation

    private val _dateTimeData = MutableStateFlow(DateTimeData())
    val dateTimeData: StateFlow<DateTimeData> = _dateTimeData.asStateFlow()

    private val _currentStampConfig = MutableStateFlow(
        PresetItem.getDefaultPresets().first().config
    )
    val currentStampConfig: StateFlow<StampConfig> = _currentStampConfig.asStateFlow()

    private val _activePresetId = MutableStateFlow("generator_maint")
    val activePresetId: StateFlow<String> = _activePresetId.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _gridType = MutableStateFlow(GridType.RULE_OF_THIRDS)
    val gridType: StateFlow<GridType> = _gridType.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Database Flows
    val allPhotos: StateFlow<List<CapturedPhoto>> = photoDao.getAllPhotos()
        .map { list -> list.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<PresetItem>> = presetDao.getAllPresets()
        .map { list ->
            if (list.isEmpty()) PresetItem.getDefaultPresets()
            else list.map { it.toModel() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PresetItem.getDefaultPresets())

    // Photo to edit in Existing Photo Editor
    private val _editorSelectedUri = MutableStateFlow<Uri?>(null)
    val editorSelectedUri: StateFlow<Uri?> = _editorSelectedUri.asStateFlow()

    init {
        locationManager.startGpsTracking()
        loadDefaultPreset()
    }

    private fun loadDefaultPreset() {
        viewModelScope.launch {
            val count = presetDao.getCount()
            val defaults = PresetItem.getDefaultPresets().map { PresetEntity.fromModel(it) }
            if (count == 0) {
                presetDao.insertPresets(defaults)
            } else {
                val defaultPreset = PresetItem.getDefaultPresets().first()
                presetDao.insertPreset(PresetEntity.fromModel(defaultPreset))
            }
            _currentStampConfig.value = PresetItem.getDefaultPresets().first().config
        }
    }

    // Preset Selection
    fun selectPreset(preset: PresetItem) {
        _activePresetId.value = preset.id
        _currentStampConfig.value = preset.config
    }

    fun updateStampConfig(updated: StampConfig) {
        _currentStampConfig.value = updated
    }

    fun saveCurrentConfigAsPreset(name: String, category: String, description: String) {
        viewModelScope.launch {
            val newId = "preset_${System.currentTimeMillis()}"
            val newPreset = PresetItem(
                id = newId,
                name = name,
                category = category.ifBlank { "Custom" },
                description = description,
                isDefault = false,
                config = _currentStampConfig.value.copy(id = newId, name = name)
            )
            presetDao.insertPreset(PresetEntity.fromModel(newPreset))
            _activePresetId.value = newId
            _toastEvent.emit("Preset '$name' saved successfully")
        }
    }

    fun deletePreset(preset: PresetItem) {
        viewModelScope.launch {
            presetDao.deletePreset(PresetEntity.fromModel(preset))
            _toastEvent.emit("Preset deleted")
        }
    }

    // Date & Time Controls
    fun setDateTimeData(data: DateTimeData) {
        _dateTimeData.value = data
    }

    fun resetToDeviceTime() {
        _dateTimeData.value = _dateTimeData.value.copy(
            source = TimeSource.DEVICE,
            timestampMillis = System.currentTimeMillis()
        )
    }

    fun setManualDateTime(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, tzId: String? = null) {
        _dateTimeData.value = _dateTimeData.value.copy(
            source = TimeSource.MANUAL,
            manualYear = year,
            manualMonth = month,
            manualDay = day,
            manualHour = hour,
            manualMinute = min,
            manualSecond = sec,
            timezoneId = tzId ?: _dateTimeData.value.timezoneId
        )
    }

    fun setManualCoordinates(
        lat: Double,
        lng: Double,
        locality: String? = null,
        district: String? = null,
        province: String? = null,
        postalCode: String? = null
    ) {
        locationManager.setManualLocation(lat, lng, locality, district, province, postalCode)
    }

    val openCameraEvent = MutableSharedFlow<Unit>()

    fun importGoogleMapsLocation(input: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val parsed = com.example.location.GoogleMapsShareParser.parseSharedText(input)
            if (parsed != null) {
                locationManager.setManualLocation(
                    lat = parsed.latitude,
                    lng = parsed.longitude,
                    customLocality = parsed.placeName
                )
                _toastEvent.emit("Location auto-fitted from Google Maps: ${String.format("%.4f, %.4f", parsed.latitude, parsed.longitude)}")
                openCameraEvent.emit(Unit)
                onComplete?.invoke(true)
            } else {
                _toastEvent.emit("Could not extract coordinates from Maps link")
                onComplete?.invoke(false)
            }
        }
    }

    fun switchToRealGps() {
        locationManager.switchToRealGps()
    }

    fun cycleGrid() {
        _gridType.value = when (_gridType.value) {
            GridType.NONE -> GridType.RULE_OF_THIRDS
            GridType.RULE_OF_THIRDS -> GridType.GOLDEN_RATIO
            GridType.GOLDEN_RATIO -> GridType.CROSSHAIR
            GridType.CROSSHAIR -> GridType.NONE
        }
    }

    // Photo Capture Flow
    fun capturePhotoWithStamp(onSuccess: (CapturedPhoto) -> Unit) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        viewModelScope.launch {
            try {
                val rawFile = cameraManager.capturePhoto()
                val loc = selectedLocation.value
                val dt = dateTimeData.value
                val cfg = currentStampConfig.value
                val settings = appSettings.value

                val (stampedFile, savedRawFile) = StampBitmapRenderer.renderAndSaveStampedImage(
                    context = getApplication(),
                    rawImageFile = rawFile,
                    location = loc,
                    dateTime = dt,
                    stampConfig = cfg,
                    qualityOption = settings.imageQuality,
                    exportFormat = settings.exportFormat,
                    saveToPublicGallery = true
                )

                val newPhoto = CapturedPhoto(
                    filePath = stampedFile.absolutePath,
                    rawFilePath = if (settings.saveOriginalImage) savedRawFile?.absolutePath else null,
                    thumbnailPath = stampedFile.absolutePath,
                    timestampMillis = dt.getEffectiveMillis(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitude,
                    address = loc.fullAddress,
                    presetName = cfg.name,
                    isFavorite = false,
                    isEdited = false,
                    inspectionType = cfg.inspectionType.takeIf { cfg.showCustomFields },
                    siteId = cfg.siteId.takeIf { cfg.showCustomFields },
                    technician = cfg.technicianName.takeIf { cfg.showCustomFields },
                    notes = cfg.customNotes.takeIf { cfg.showCustomFields }
                )

                val insertedId = photoDao.insertPhoto(PhotoEntity.fromModel(newPhoto))
                val savedModel = newPhoto.copy(id = insertedId)

                _toastEvent.emit("Stamped photo saved with GPS coordinates!")
                onSuccess(savedModel)
            } catch (e: Exception) {
                e.printStackTrace()
                _toastEvent.emit("Capture error: ${e.message}")
            } finally {
                _isCapturing.value = false
            }
        }
    }

    // Existing Photo Editing Flow
    fun setEditorSelectedUri(uri: Uri?) {
        _editorSelectedUri.value = uri
    }

    fun applyStampToExistingPhoto(
        uri: Uri,
        location: LocationData,
        dateTime: DateTimeData,
        config: StampConfig,
        onSuccess: (CapturedPhoto) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val settings = appSettings.value
                val stampedFile = StampBitmapRenderer.renderFromUri(
                    context = getApplication(),
                    inputUri = uri,
                    location = location,
                    dateTime = dateTime,
                    stampConfig = config,
                    qualityOption = settings.imageQuality,
                    exportFormat = settings.exportFormat
                )

                val photo = CapturedPhoto(
                    filePath = stampedFile.absolutePath,
                    rawFilePath = null,
                    thumbnailPath = stampedFile.absolutePath,
                    timestampMillis = dateTime.getEffectiveMillis(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    address = location.fullAddress,
                    presetName = config.name,
                    isFavorite = false,
                    isEdited = true,
                    inspectionType = config.inspectionType.takeIf { config.showCustomFields },
                    siteId = config.siteId.takeIf { config.showCustomFields },
                    technician = config.technicianName.takeIf { config.showCustomFields }
                )

                val id = photoDao.insertPhoto(PhotoEntity.fromModel(photo))
                _toastEvent.emit("Edited photo stamped and saved!")
                onSuccess(photo.copy(id = id))
            } catch (e: Exception) {
                e.printStackTrace()
                _toastEvent.emit("Failed to stamp photo: ${e.message}")
            }
        }
    }

    // Gallery Actions
    fun toggleFavorite(photo: CapturedPhoto) {
        viewModelScope.launch {
            photoDao.setFavorite(photo.id, !photo.isFavorite)
        }
    }

    fun deletePhoto(photo: CapturedPhoto) {
        viewModelScope.launch {
            runCatching { File(photo.filePath).delete() }
            photo.rawFilePath?.let { runCatching { File(it).delete() } }
            photoDao.deletePhoto(PhotoEntity.fromModel(photo))
            _toastEvent.emit("Photo removed from gallery")
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
    }

    override fun onCleared() {
        super.onCleared()
        locationManager.stopGpsTracking()
    }
}
