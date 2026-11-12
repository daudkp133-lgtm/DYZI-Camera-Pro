package com.example

import com.example.location.GoogleMapsShareParser
import com.example.model.CoordinateFormat
import com.example.model.DateFormatOption
import com.example.model.DateTimeData
import com.example.model.LocationData
import com.example.model.PresetItem
import com.example.model.TimeFormatOption
import com.example.model.TimeSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCoordinateFormatting() {
        val loc = LocationData(
            latitude = 34.9546,
            longitude = 71.6865,
            locality = "Samar Bagh Jandool",
            district = "Lower Dir",
            province = "Khyber Pakhtunkhwa",
            postalCode = "8268"
        )

        assertEquals("34.9546N 71.6865E", loc.formatDecimalCompact())
        assertTrue(LocationData.isValidCoordinate(34.9546, 71.6865))
        assertFalse(LocationData.isValidCoordinate(95.0, 71.6865))
        assertFalse(LocationData.isValidCoordinate(34.9546, 195.0))
    }

    @Test
    fun testDateTimeFormatting() {
        val dt = DateTimeData(
            source = TimeSource.MANUAL,
            dateFormat = DateFormatOption.DAY_MONTH_YEAR_DASH,
            timeFormat = TimeFormatOption.TIME_12H_SEC,
            timezoneId = "UTC",
            manualYear = 2026,
            manualMonth = 11,
            manualDay = 10,
            manualHour = 20,
            manualMinute = 15,
            manualSecond = 55
        )

        val formatted = dt.formatFormattedDateTime()
        assertEquals("10-Nov-2026 8:15:55 pm", formatted)
    }

    @Test
    fun testGeneratorMaintenancePresetLines() {
        val presets = PresetItem.getDefaultPresets()
        val genPreset = presets.first { it.id == "generator_maint" }

        val loc = LocationData(
            latitude = 34.9546,
            longitude = 71.6865,
            locality = "Samar Bagh Jandool",
            district = "Lower Dir",
            province = "Khyber Pakhtunkhwa",
            postalCode = "8268"
        )

        val dt = DateTimeData(
            source = TimeSource.MANUAL,
            dateFormat = DateFormatOption.DAY_MONTH_YEAR_DASH,
            timeFormat = TimeFormatOption.TIME_12H_SEC,
            timezoneId = "UTC",
            manualYear = 2026,
            manualMonth = 11,
            manualDay = 10,
            manualHour = 20,
            manualMinute = 15,
            manualSecond = 55
        )

        val lines = genPreset.config.getLines(loc, dt)
        assertTrue(lines.contains("10-Nov-2026 8:15:55 pm"))
        assertTrue(lines.contains("34.9546N 71.6865E"))
        assertTrue(lines.contains("Samar Bagh Jandool"))
        assertTrue(lines.contains("Lower Dir"))
        assertTrue(lines.contains("Khyber Pakhtunkhwa"))
    }

    @Test
    fun testGoogleMapsShareParsingDirectUrl() = runBlocking {
        val sharedUrl = "https://www.google.com/maps?q=34.9546,71.6865"
        val parsed = GoogleMapsShareParser.parseSharedText(sharedUrl)
        assertNotNull(parsed)
        assertEquals(34.9546, parsed!!.latitude, 0.001)
        assertEquals(71.6865, parsed.longitude, 0.001)
    }

    @Test
    fun testGoogleMapsShareParsingWithDroppedPin() = runBlocking {
        val sharedText = "Dropped pin near Samar Bagh Jandool\nhttps://maps.google.com/maps?q=34.9546,71.6865"
        val parsed = GoogleMapsShareParser.parseSharedText(sharedText)
        assertNotNull(parsed)
        assertEquals(34.9546, parsed!!.latitude, 0.001)
        assertEquals(71.6865, parsed.longitude, 0.001)
        assertEquals("Samar Bagh Jandool", parsed.placeName)
    }

    @Test
    fun testGoogleMapsDmsParsing() = runBlocking {
        val dmsText = "34°57'16.6\"N 71°41'11.4\"E"
        val parsed = GoogleMapsShareParser.parseSharedText(dmsText)
        assertNotNull(parsed)
        assertEquals(34.9546, parsed!!.latitude, 0.005)
        assertEquals(71.6865, parsed.longitude, 0.005)
    }
}
