package com.example.maps

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.LocationData
import com.example.model.MapStyleType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun InteractiveGoogleMapView(
    location: LocationData,
    mapStyle: MapStyleType = MapStyleType.NORMAL,
    zoom: Float = 15f,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onLocationChanged: ((Double, Double) -> Unit)? = null,
    onMapReadyCallback: ((GoogleMap) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { mv ->
            mv.getMapAsync { gMap ->
                googleMapInstance = gMap
                onMapReadyCallback?.invoke(gMap)

                try {
                    gMap.uiSettings.isZoomControlsEnabled = isInteractive
                    gMap.uiSettings.isScrollGesturesEnabled = isInteractive
                    gMap.uiSettings.isZoomGesturesEnabled = isInteractive
                    gMap.uiSettings.isRotateGesturesEnabled = isInteractive

                    // Map Style
                    gMap.mapType = when (mapStyle) {
                        MapStyleType.NORMAL -> GoogleMap.MAP_TYPE_NORMAL
                        MapStyleType.SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
                        MapStyleType.HYBRID -> GoogleMap.MAP_TYPE_HYBRID
                        MapStyleType.TERRAIN -> GoogleMap.MAP_TYPE_TERRAIN
                    }

                    val target = LatLng(location.latitude, location.longitude)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))

                    // Marker
                    currentMarker?.remove()
                    val marker = gMap.addMarker(
                        MarkerOptions()
                            .position(target)
                            .title(location.locality.ifBlank { "Selected Location" })
                            .draggable(isInteractive)
                    )
                    currentMarker = marker

                    if (isInteractive && onLocationChanged != null) {
                        gMap.setOnMapClickListener { clickedLatLng ->
                            marker?.position = clickedLatLng
                            onLocationChanged(clickedLatLng.latitude, clickedLatLng.longitude)
                        }

                        gMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                            override fun onMarkerDragStart(m: Marker) {}
                            override fun onMarkerDrag(m: Marker) {}
                            override fun onMarkerDragEnd(m: Marker) {
                                onLocationChanged(m.position.latitude, m.position.longitude)
                            }
                        })
                    }
                } catch (e: Exception) {
                    hasError = true
                }
            }
        }

        // Update camera position when location changes externally
        LaunchedEffect(location.latitude, location.longitude, mapStyle) {
            val gMap = googleMapInstance ?: return@LaunchedEffect
            val target = LatLng(location.latitude, location.longitude)
            gMap.animateCamera(CameraUpdateFactory.newLatLng(target))
            currentMarker?.position = target
            gMap.mapType = when (mapStyle) {
                MapStyleType.NORMAL -> GoogleMap.MAP_TYPE_NORMAL
                MapStyleType.SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
                MapStyleType.HYBRID -> GoogleMap.MAP_TYPE_HYBRID
                MapStyleType.TERRAIN -> GoogleMap.MAP_TYPE_TERRAIN
            }
        }

        if (hasError) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0D1B2A))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Offline Mode • Vector GIS Map Active",
                    color = Color(0xFF00D2FC),
                    fontSize = 11.sp
                )
            }
        }
    }
}
