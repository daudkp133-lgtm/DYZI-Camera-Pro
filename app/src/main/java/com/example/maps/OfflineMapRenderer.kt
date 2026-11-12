package com.example.maps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.LocationData
import com.example.model.MapStyleType
import kotlin.math.abs

object OfflineMapRenderer {

    /**
     * Generates a high-fidelity Google Maps default road view snapshot
     * matching the exact style of the reference inspection photo:
     * - Authentic Google Maps pastel road/land palette
     * - Winding river / waterbody
     * - Main road (e.g. Samar Bagh Rd) and secondary street network
     * - Locality & POI labels (e.g. Samar Bagh Jandool, bazar, café)
     * - Authentic Google red teardrop marker pin with shadow at exact coordinate center
     * - Iconic multi-color "Google" logo at the bottom-left corner
     */
    fun generateOfflineMapSnapshot(
        width: Int,
        height: Int,
        location: LocationData,
        mapStyle: MapStyleType = MapStyleType.NORMAL,
        zoom: Float = 15f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Google Maps Default Colors
        val isDefaultRoadView = mapStyle == MapStyleType.NORMAL || mapStyle == MapStyleType.TERRAIN
        val landBgColor = if (isDefaultRoadView) Color.rgb(235, 241, 246) else Color.rgb(20, 32, 24)
        val waterColor = if (isDefaultRoadView) Color.rgb(122, 185, 238) else Color.rgb(45, 95, 120)
        val roadCasingColor = if (isDefaultRoadView) Color.rgb(205, 216, 226) else Color.rgb(55, 65, 75)
        val roadFillColor = if (isDefaultRoadView) Color.WHITE else Color.rgb(70, 85, 95)
        val roadSecondaryColor = if (isDefaultRoadView) Color.rgb(248, 250, 252) else Color.rgb(60, 70, 80)
        val labelDarkColor = if (isDefaultRoadView) Color.rgb(75, 85, 99) else Color.rgb(220, 225, 230)
        val labelPoiBlue = if (isDefaultRoadView) Color.rgb(26, 115, 232) else Color.rgb(100, 180, 255)

        // 2. Base Landmass
        canvas.drawColor(landBgColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 3. Winding River (just like the river flowing along the left/bottom in reference photo)
        paint.style = Paint.Style.FILL
        paint.color = waterColor

        val riverPath = Path().apply {
            moveTo(0f, h * 0.15f)
            cubicTo(w * 0.28f, h * 0.35f, w * 0.12f, h * 0.65f, w * 0.18f, h * 0.85f)
            cubicTo(w * 0.20f, h * 0.95f, w * 0.28f, h, w * 0.35f, h)
            lineTo(0f, h)
            lineTo(0f, h * 0.15f)
            close()
        }
        canvas.drawPath(riverPath, paint)

        // Subtle river shore highlight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.008f
        paint.color = if (isDefaultRoadView) Color.argb(60, 255, 255, 255) else Color.argb(40, 255, 255, 255)
        canvas.drawPath(riverPath, paint)

        // 4. Secondary Street Grid / Network (Light gray subtle strokes)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.018f
        paint.color = roadSecondaryColor

        // Cross street paths
        val gridPath = Path().apply {
            // East-West secondary roads
            moveTo(w * 0.15f, h * 0.22f)
            lineTo(w, h * 0.28f)

            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w, h * 0.42f)

            moveTo(w * 0.25f, h * 0.72f)
            lineTo(w, h * 0.68f)

            // North-South connecting paths
            moveTo(w * 0.65f, 0f)
            cubicTo(w * 0.68f, h * 0.35f, w * 0.75f, h * 0.65f, w * 0.72f, h)

            moveTo(w * 0.88f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.85f)
        }
        canvas.drawPath(gridPath, paint)

        // 5. Main Arterial Road (Samar Bagh Rd) with Casing & Fill
        val mainRoadPath = Path().apply {
            moveTo(w * 0.52f, h)
            cubicTo(w * 0.50f, h * 0.78f, w * 0.44f, h * 0.55f, w * 0.38f, h * 0.42f)
            cubicTo(w * 0.32f, h * 0.30f, w * 0.22f, h * 0.18f, w * 0.15f, 0f)
        }

        // Road Casing (Gray border)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.055f
        paint.color = roadCasingColor
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(mainRoadPath, paint)

        // Road Fill (Pure White)
        paint.strokeWidth = w * 0.038f
        paint.color = roadFillColor
        canvas.drawPath(mainRoadPath, paint)

        // Bridge over river (if road crosses river)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.045f
        paint.color = Color.rgb(180, 195, 210)
        canvas.drawLine(w * 0.14f, 0f, w * 0.18f, h * 0.05f, paint)

        // 6. Text Labels (Google Maps typography style)
        val typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val typefaceBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        // A. Locality Label at top center ("Samar Bagh Jandool")
        val localityName = location.locality.ifBlank { "Samar Bagh Jandool" }
        paint.style = Paint.Style.FILL
        paint.typeface = typefaceBold
        paint.textSize = w * 0.058f
        paint.color = labelDarkColor
        val locWidth = paint.measureText(localityName)
        canvas.drawText(localityName, (w - locWidth) / 2f, h * 0.16f, paint)

        // District label under locality if short
        if (location.district.isNotBlank() && location.district != localityName) {
            paint.typeface = typeface
            paint.textSize = w * 0.042f
            paint.color = Color.rgb(120, 130, 140)
            val distWidth = paint.measureText(location.district)
            canvas.drawText(location.district, (w - distWidth) / 2f, h * 0.23f, paint)
        }

        // B. Road Label ("...ar Bagh Rd") angled along the road
        paint.typeface = typeface
        paint.textSize = w * 0.036f
        paint.color = Color.rgb(110, 120, 130)
        canvas.save()
        canvas.rotate(-82f, w * 0.52f, h * 0.82f)
        val roadName = if (localityName.length > 5) "${localityName.take(8)} Rd" else "Main Rd"
        canvas.drawText(roadName, w * 0.40f, h * 0.85f, paint)
        canvas.restore()

        // C. Nearby POI Badge (e.g., shopping / hotel badge as seen in reference photo)
        val poiX = w * 0.42f
        val poiY = h * 0.65f

        // Blue circle icon badge
        paint.style = Paint.Style.FILL
        paint.color = labelPoiBlue
        canvas.drawCircle(poiX, poiY, w * 0.032f, paint)

        // Small white shopping/cart symbol or star
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(poiX, poiY, w * 0.014f, paint)

        // POI label text ("...bazar" / hotel)
        paint.typeface = typeface
        paint.textSize = w * 0.034f
        paint.color = labelPoiBlue
        val poiLabel = if (location.subLocality.isNotBlank()) location.subLocality.take(14) else "${localityName.take(10)} bazar"
        canvas.drawText(poiLabel, w * 0.08f, poiY + w * 0.012f, paint)

        // Additional nearby place label
        paint.color = labelDarkColor
        paint.textSize = w * 0.032f
        canvas.drawText("Quetta Durrani...", w * 0.03f, h * 0.53f, paint)

        // 7. Exact Location Pin Marker (Classic Google Maps Red Pin)
        // Located at the center of the card
        val pinX = w * 0.45f
        val pinY = h * 0.50f
        val pinWidth = w * 0.11f
        val pinHeight = pinWidth * 1.35f

        // Pin Drop Shadow (Dark translucent oval at the pin tip)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(110, 0, 0, 0)
        canvas.drawOval(
            RectF(pinX - pinWidth * 0.55f, pinY - pinWidth * 0.12f, pinX + pinWidth * 0.55f, pinY + pinWidth * 0.28f),
            paint
        )

        // Red Teardrop Pin Body
        val pinPath = Path().apply {
            moveTo(pinX, pinY) // Bottom tip touching ground
            cubicTo(
                pinX - pinWidth * 0.70f, pinY - pinHeight * 0.50f,
                pinX - pinWidth * 0.60f, pinY - pinHeight,
                pinX, pinY - pinHeight
            )
            cubicTo(
                pinX + pinWidth * 0.60f, pinY - pinHeight,
                pinX + pinWidth * 0.70f, pinY - pinHeight * 0.50f,
                pinX, pinY
            )
            close()
        }

        // Gradient for authentic 3D Google pin lighting
        val pinGradient = LinearGradient(
            pinX - pinWidth * 0.5f, pinY - pinHeight,
            pinX + pinWidth * 0.5f, pinY,
            Color.rgb(240, 80, 65), // lighter red highlight
            Color.rgb(215, 45, 32), // classic dark red
            Shader.TileMode.CLAMP
        )
        paint.shader = pinGradient
        canvas.drawPath(pinPath, paint)
        paint.shader = null

        // White inner circle of the pin
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(pinX, pinY - pinHeight * 0.65f, pinWidth * 0.28f, paint)

        // Inner dark dot
        paint.color = Color.rgb(215, 45, 32)
        canvas.drawCircle(pinX, pinY - pinHeight * 0.65f, pinWidth * 0.12f, paint)

        // 8. Official "Google" Multi-Color Logo at Bottom Left
        // G (Blue), o (Red), o (Yellow), g (Blue), l (Green), e (Red)
        val logoX = w * 0.05f
        val logoY = h - h * 0.05f
        val logoSize = w * 0.062f

        paint.textSize = logoSize
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)

        val letters = listOf(
            Pair("G", Color.rgb(66, 133, 244)),   // Google Blue
            Pair("o", Color.rgb(234, 67, 53)),    // Google Red
            Pair("o", Color.rgb(251, 188, 5)),    // Google Yellow
            Pair("g", Color.rgb(66, 133, 244)),   // Google Blue
            Pair("l", Color.rgb(52, 168, 83)),    // Google Green
            Pair("e", Color.rgb(234, 67, 53))     // Google Red
        )

        var curX = logoX
        for ((char, col) in letters) {
            paint.color = col
            canvas.drawText(char, curX, logoY, paint)
            curX += paint.measureText(char)
        }

        return bitmap
    }
}
