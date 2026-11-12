package com.example.rendering

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.ExifInterface
import com.example.maps.OfflineMapRenderer
import com.example.model.DateTimeData
import com.example.model.ExportFormat
import com.example.model.ImageQualityOption
import com.example.model.LocationData
import com.example.model.MapSize
import com.example.model.StampBackgroundStyle
import com.example.model.StampConfig
import com.example.model.StampPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object StampBitmapRenderer {

    suspend fun renderAndSaveStampedImage(
        context: Context,
        rawImageFile: File,
        location: LocationData,
        dateTime: DateTimeData,
        stampConfig: StampConfig,
        mapBitmapOverride: Bitmap? = null,
        qualityOption: ImageQualityOption = ImageQualityOption.VERY_HIGH,
        exportFormat: ExportFormat = ExportFormat.JPEG,
        saveToPublicGallery: Boolean = true
    ): Pair<File, File?> = withContext(Dispatchers.IO) {
        // 1. Decode original image with orientation correction
        val orientedBitmap = decodeAndCorrectOrientation(rawImageFile)

        // 2. Render stamp on canvas
        val stampedBitmap = renderStampOnBitmap(
            sourceBitmap = orientedBitmap,
            location = location,
            dateTime = dateTime,
            config = stampConfig,
            mapBitmapOverride = mapBitmapOverride
        )

        // 3. Save stamped image
        val outputDir = File(context.filesDir, "stamped_photos").apply { mkdirs() }
        val filename = "GPSCAM_${System.currentTimeMillis()}.${exportFormat.extension}"
        val stampedFile = File(outputDir, filename)

        val compressFormat = if (exportFormat == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }

        FileOutputStream(stampedFile).use { fos ->
            stampedBitmap.compress(compressFormat, qualityOption.qualityPercent, fos)
        }

        // 4. Optionally save to Public MediaStore
        if (saveToPublicGallery) {
            saveToMediaStore(context, stampedFile, exportFormat)
        }

        // Cleanup
        if (stampedBitmap != orientedBitmap) {
            stampedBitmap.recycle()
        }
        orientedBitmap.recycle()

        Pair(stampedFile, rawImageFile)
    }

    suspend fun renderFromUri(
        context: Context,
        inputUri: Uri,
        location: LocationData,
        dateTime: DateTimeData,
        stampConfig: StampConfig,
        mapBitmapOverride: Bitmap? = null,
        qualityOption: ImageQualityOption = ImageQualityOption.VERY_HIGH,
        exportFormat: ExportFormat = ExportFormat.JPEG
    ): File = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(inputUri)
        val tempRawFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempRawFile).use { out ->
            inputStream?.copyTo(out)
        }

        val orientedBitmap = decodeAndCorrectOrientation(tempRawFile)
        val stampedBitmap = renderStampOnBitmap(
            sourceBitmap = orientedBitmap,
            location = location,
            dateTime = dateTime,
            config = stampConfig,
            mapBitmapOverride = mapBitmapOverride
        )

        val outputDir = File(context.filesDir, "edited_photos").apply { mkdirs() }
        val filename = "EDITED_${System.currentTimeMillis()}.${exportFormat.extension}"
        val stampedFile = File(outputDir, filename)

        val compressFormat = if (exportFormat == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }

        FileOutputStream(stampedFile).use { fos ->
            stampedBitmap.compress(compressFormat, qualityOption.qualityPercent, fos)
        }

        saveToMediaStore(context, stampedFile, exportFormat)

        if (stampedBitmap != orientedBitmap) {
            stampedBitmap.recycle()
        }
        orientedBitmap.recycle()
        tempRawFile.delete()

        stampedFile
    }

    fun renderStampOnBitmap(
        sourceBitmap: Bitmap,
        location: LocationData,
        dateTime: DateTimeData,
        config: StampConfig,
        mapBitmapOverride: Bitmap? = null
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Draw original photo
        canvas.drawBitmap(sourceBitmap, 0f, 0f, null)

        val baseDimension = min(width, height)
        val padding = if (config.useZeroMargins) 0f else baseDimension * 0.035f

        // A. Draw Map if enabled
        if (config.showMap) {
            val mapRatio = when (config.mapSize) {
                MapSize.SMALL -> 0.24f
                MapSize.MEDIUM -> 0.31f
                MapSize.LARGE -> 0.38f
            }
            val mapW = (width * mapRatio).toInt()
            val mapH = (mapW * 1.08f).toInt()

            val mapBmp = mapBitmapOverride ?: OfflineMapRenderer.generateOfflineMapSnapshot(
                width = mapW,
                height = mapH,
                location = location,
                mapStyle = config.mapStyle,
                zoom = config.mapZoom
            )

            val (mapX, mapY) = calculateCoordinatesForPosition(
                position = config.mapPosition,
                elementW = mapW.toFloat(),
                elementH = mapH.toFloat(),
                canvasW = width.toFloat(),
                canvasH = height.toFloat(),
                padding = padding
            )

            // Draw map card
            val mapRect = RectF(mapX, mapY, mapX + mapW, mapY + mapH)
            val cornerRadius = if (config.useZeroMargins) 0f else baseDimension * 0.02f

            // Map Card Shadow (only if non-zero margin)
            if (!config.useZeroMargins) {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(120, 0, 0, 0)
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    RectF(mapX + 4f, mapY + 6f, mapX + mapW + 4f, mapY + mapH + 6f),
                    cornerRadius, cornerRadius, shadowPaint
                )
            }

            // Clip and draw map bitmap
            canvas.save()
            val clipPath = android.graphics.Path().apply {
                if (cornerRadius > 0f) {
                    addRoundRect(mapRect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
                } else {
                    addRect(mapRect, android.graphics.Path.Direction.CW)
                }
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(mapBmp, null, mapRect, null)

            // Border around map
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255)
                strokeWidth = max(2f, baseDimension * 0.003f)
                style = Paint.Style.STROKE
            }
            if (cornerRadius > 0f) {
                canvas.drawRoundRect(mapRect, cornerRadius, cornerRadius, borderPaint)
            } else {
                canvas.drawRect(mapRect, borderPaint)
            }
            canvas.restore()
        }

        // B. Draw Timestamp & Location Text
        val lines = config.getLines(location, dateTime)
        if (lines.isNotEmpty()) {
            val textSize = baseDimension * 0.038f * config.textSizeMultiplier
            val lineSpacing = textSize * 1.35f

            val typeface = if (config.isMonospace) {
                Typeface.MONOSPACE
            } else if (config.isBold) {
                Typeface.DEFAULT_BOLD
            } else {
                Typeface.DEFAULT
            }

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { Color.parseColor(config.textColorHex) }.getOrDefault(Color.WHITE)
                this.textSize = textSize
                this.typeface = typeface
            }

            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                this.textSize = textSize
                this.typeface = typeface
            }

            // Measure bounds
            var maxLineWidth = 0f
            for (line in lines) {
                val w = textPaint.measureText(line)
                if (w > maxLineWidth) maxLineWidth = w
            }
            val totalTextHeight = lines.size * lineSpacing

            val (textX, textY) = calculateCoordinatesForPosition(
                position = config.timestampPosition,
                elementW = maxLineWidth,
                elementH = totalTextHeight,
                canvasW = width.toFloat(),
                canvasH = height.toFloat(),
                padding = padding
            )

            // Background card if enabled
            if (config.backgroundStyle != StampBackgroundStyle.TRANSPARENT_SHADOW) {
                val bgPadding = if (config.useZeroMargins) 0f else baseDimension * 0.02f
                val canvasWidth = width.toFloat()
                val canvasHeight = height.toFloat()
                val bgRect = if (config.useZeroMargins) {
                    when (config.timestampPosition) {
                        StampPosition.BOTTOM_RIGHT -> RectF(canvasWidth - maxLineWidth - baseDimension * 0.02f, canvasHeight - totalTextHeight - baseDimension * 0.02f, canvasWidth, canvasHeight)
                        StampPosition.BOTTOM_LEFT -> RectF(0f, canvasHeight - totalTextHeight - baseDimension * 0.02f, maxLineWidth + baseDimension * 0.02f, canvasHeight)
                        StampPosition.TOP_RIGHT -> RectF(canvasWidth - maxLineWidth - baseDimension * 0.02f, 0f, canvasWidth, totalTextHeight + baseDimension * 0.02f)
                        StampPosition.TOP_LEFT -> RectF(0f, 0f, maxLineWidth + baseDimension * 0.02f, totalTextHeight + baseDimension * 0.02f)
                        StampPosition.BOTTOM_BANNER -> RectF(0f, canvasHeight - totalTextHeight - baseDimension * 0.02f, canvasWidth, canvasHeight)
                    }
                } else {
                    RectF(
                        textX - bgPadding,
                        textY - bgPadding,
                        textX + maxLineWidth + bgPadding,
                        textY + totalTextHeight + bgPadding
                    )
                }
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = when (config.backgroundStyle) {
                        StampBackgroundStyle.DARK_CARD -> Color.argb(190, 7, 14, 24)
                        StampBackgroundStyle.SOLID_BLACK -> Color.BLACK
                        StampBackgroundStyle.CYAN_ACCENT_CARD -> Color.argb(210, 13, 27, 42)
                        else -> Color.argb(160, 0, 0, 0)
                    }
                    style = Paint.Style.FILL
                }
                val bgRadius = if (config.useZeroMargins) 0f else baseDimension * 0.015f
                canvas.drawRoundRect(bgRect, bgRadius, bgRadius, bgPaint)
            }

            // Draw each line with drop shadow for supreme legibility (like user reference!)
            // For right-positioned timestamps, align flush to the right edge with zero margin
            val isRightAligned = config.timestampPosition == StampPosition.BOTTOM_RIGHT || config.timestampPosition == StampPosition.TOP_RIGHT
            val rightEdge = width.toFloat() - (if (config.useZeroMargins) max(2f, baseDimension * 0.005f) else padding)

            var currentY = textY + textSize
            for (line in lines) {
                val lineWidth = textPaint.measureText(line)
                val lineX = if (isRightAligned) {
                    rightEdge - lineWidth
                } else {
                    textX
                }

                if (config.backgroundStyle == StampBackgroundStyle.TRANSPARENT_SHADOW) {
                    val shadowOffset = max(2f, baseDimension * 0.003f)
                    // Multi-direction shadow for high contrast over any background
                    canvas.drawText(line, lineX + shadowOffset, currentY + shadowOffset, shadowPaint)
                    canvas.drawText(line, lineX - 1f, currentY, shadowPaint)
                    canvas.drawText(line, lineX, currentY - 1f, shadowPaint)
                }
                canvas.drawText(line, lineX, currentY, textPaint)
                currentY += lineSpacing
            }
        }

        return resultBitmap
    }

    private fun calculateCoordinatesForPosition(
        position: StampPosition,
        elementW: Float,
        elementH: Float,
        canvasW: Float,
        canvasH: Float,
        padding: Float
    ): Pair<Float, Float> {
        return when (position) {
            StampPosition.BOTTOM_LEFT -> Pair(padding, canvasH - padding - elementH)
            StampPosition.BOTTOM_RIGHT -> Pair(canvasW - padding - elementW, canvasH - padding - elementH)
            StampPosition.TOP_LEFT -> Pair(padding, padding)
            StampPosition.TOP_RIGHT -> Pair(canvasW - padding - elementW, padding)
            StampPosition.BOTTOM_BANNER -> Pair((canvasW - elementW) / 2f, canvasH - padding - elementH)
        }
    }

    private fun decodeAndCorrectOrientation(file: File): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image file: ${file.absolutePath}")

        val exif = runCatching { ExifInterface(file.absolutePath) }.getOrNull()
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return rawBitmap
        }

        val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        if (rotated != rawBitmap) {
            rawBitmap.recycle()
        }
        return rotated
    }

    private fun saveToMediaStore(context: Context, file: File, format: ExportFormat) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GPSCamPro")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri).use { out ->
                    if (out != null) {
                        file.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
            }
        } catch (_: Exception) {}
    }
}
