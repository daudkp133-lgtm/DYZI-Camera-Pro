package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps.OfflineMapRenderer
import com.example.model.DateTimeData
import com.example.model.LocationData
import com.example.model.MapSize
import com.example.model.StampBackgroundStyle
import com.example.model.StampConfig
import com.example.model.StampPosition

@Composable
fun LiveStampOverlay(
    location: LocationData,
    dateTime: DateTimeData,
    config: StampConfig,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight

        val padding = if (config.useZeroMargins) 0.dp else 16.dp

        // 1. Render Map Component if enabled
        if (config.showMap) {
            val mapWidthDp = when (config.mapSize) {
                MapSize.SMALL -> parentWidth * 0.28f
                MapSize.MEDIUM -> parentWidth * 0.35f
                MapSize.LARGE -> parentWidth * 0.42f
            }
            val mapHeightDp = mapWidthDp * 1.08f

            val mapAlign = when (config.mapPosition) {
                StampPosition.BOTTOM_LEFT -> Alignment.BottomStart
                StampPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                StampPosition.TOP_LEFT -> Alignment.TopStart
                StampPosition.TOP_RIGHT -> Alignment.TopEnd
                StampPosition.BOTTOM_BANNER -> Alignment.BottomCenter
            }

            // Generate map snapshot bitmap in crisp Google default view
            val mapBitmap = remember(location.latitude, location.longitude, config.mapStyle, config.mapSize) {
                OfflineMapRenderer.generateOfflineMapSnapshot(
                    width = 320,
                    height = 345,
                    location = location,
                    mapStyle = config.mapStyle,
                    zoom = config.mapZoom
                )
            }

            val mapShape = if (config.useZeroMargins) {
                when (config.mapPosition) {
                    StampPosition.BOTTOM_LEFT -> RoundedCornerShape(topEnd = 6.dp)
                    StampPosition.BOTTOM_RIGHT -> RoundedCornerShape(topStart = 6.dp)
                    StampPosition.TOP_LEFT -> RoundedCornerShape(bottomEnd = 6.dp)
                    StampPosition.TOP_RIGHT -> RoundedCornerShape(bottomStart = 6.dp)
                    else -> RoundedCornerShape(0.dp)
                }
            } else {
                RoundedCornerShape(8.dp)
            }

            Box(
                modifier = Modifier
                    .align(mapAlign)
                    .padding(padding)
                    .size(mapWidthDp, mapHeightDp)
                    .then(
                        if (!config.useZeroMargins) {
                            Modifier.shadow(6.dp, mapShape)
                        } else {
                            Modifier
                        }
                    )
                    .clip(mapShape)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), mapShape)
            ) {
                Image(
                    bitmap = mapBitmap.asImageBitmap(),
                    contentDescription = "Map Location",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Render Timestamp & Inspection Data Text
        val lines = remember(location, dateTime, config) {
            config.getLines(location, dateTime)
        }

        if (lines.isNotEmpty()) {
            val textAlignment = when (config.timestampPosition) {
                StampPosition.BOTTOM_LEFT, StampPosition.TOP_LEFT -> Alignment.Start
                StampPosition.BOTTOM_RIGHT, StampPosition.TOP_RIGHT -> Alignment.End
                StampPosition.BOTTOM_BANNER -> Alignment.CenterHorizontally
            }

            val boxAlign = when (config.timestampPosition) {
                StampPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                StampPosition.BOTTOM_LEFT -> Alignment.BottomStart
                StampPosition.TOP_RIGHT -> Alignment.TopEnd
                StampPosition.TOP_LEFT -> Alignment.TopStart
                StampPosition.BOTTOM_BANNER -> Alignment.BottomCenter
            }

            val textColor = remember(config.textColorHex) {
                runCatching { Color(android.graphics.Color.parseColor(config.textColorHex)) }
                    .getOrDefault(Color.White)
            }

            val fontFamily = if (config.isMonospace) FontFamily.Monospace else FontFamily.SansSerif
            val fontWeight = if (config.isBold) FontWeight.Bold else FontWeight.Normal

            val baseFontSize = (13f * config.textSizeMultiplier).sp

            val bgModifier = when (config.backgroundStyle) {
                StampBackgroundStyle.DARK_CARD -> Modifier
                    .clip(if (config.useZeroMargins) RoundedCornerShape(topStart = 6.dp) else RoundedCornerShape(8.dp))
                    .background(Color(0xD9070E18))
                    .border(0.5.dp, Color(0xFF243B5A), if (config.useZeroMargins) RoundedCornerShape(topStart = 6.dp) else RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)

                StampBackgroundStyle.SOLID_BLACK -> Modifier
                    .clip(if (config.useZeroMargins) RoundedCornerShape(topStart = 6.dp) else RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)

                StampBackgroundStyle.CYAN_ACCENT_CARD -> Modifier
                    .clip(if (config.useZeroMargins) RoundedCornerShape(topStart = 6.dp) else RoundedCornerShape(8.dp))
                    .background(Color(0xE60D1B2A))
                    .border(1.dp, Color(0xFF00D2FC).copy(alpha = 0.6f), if (config.useZeroMargins) RoundedCornerShape(topStart = 6.dp) else RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)

                StampBackgroundStyle.TRANSPARENT_SHADOW -> Modifier
                    .padding(horizontal = if (config.useZeroMargins) 2.dp else 4.dp, vertical = 2.dp)
            }

            Box(
                modifier = Modifier
                    .align(boxAlign)
                    .padding(padding)
            ) {
                Column(
                    horizontalAlignment = textAlignment,
                    modifier = bgModifier
                ) {
                    lines.forEachIndexed { index, line ->
                        val isFirst = index == 0
                        val isCoord = index == 1 && config.showCoordinates

                        Text(
                            text = line,
                            style = TextStyle(
                                color = textColor,
                                fontSize = if (isFirst) (baseFontSize.value * 1.05f).sp else baseFontSize,
                                fontWeight = if (isFirst || isCoord) FontWeight.Bold else fontWeight,
                                fontFamily = fontFamily,
                                textAlign = when (textAlignment) {
                                    Alignment.Start -> TextAlign.Start
                                    Alignment.End -> TextAlign.End
                                    else -> TextAlign.Center
                                },
                                shadow = if (config.backgroundStyle == StampBackgroundStyle.TRANSPARENT_SHADOW) {
                                    Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 3f
                                    )
                                } else null
                            )
                        )
                    }
                }
            }
        }
    }
}
