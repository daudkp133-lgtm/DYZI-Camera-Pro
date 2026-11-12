package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DateFormatOption
import com.example.model.DateTimeData
import com.example.model.TimeFormatOption
import com.example.model.TimeSource
import com.example.ui.viewmodel.GpsCamViewModel
import java.util.TimeZone

@Composable
fun DateTimeStudioScreen(
    viewModel: GpsCamViewModel,
    onNavigateBack: () -> Unit
) {
    val currentDateTime by viewModel.dateTimeData.collectAsState()

    var isManual by remember { mutableStateOf(currentDateTime.source == TimeSource.MANUAL) }
    var selectedDateFormat by remember { mutableStateOf(currentDateTime.dateFormat) }
    var selectedTimeFormat by remember { mutableStateOf(currentDateTime.timeFormat) }
    var selectedTzId by remember { mutableStateOf(currentDateTime.timezoneId) }

    var year by remember { mutableIntStateOf(currentDateTime.manualYear) }
    var month by remember { mutableIntStateOf(currentDateTime.manualMonth) }
    var day by remember { mutableIntStateOf(currentDateTime.manualDay) }
    var hour by remember { mutableIntStateOf(currentDateTime.manualHour) }
    var minute by remember { mutableIntStateOf(currentDateTime.manualMinute) }
    var second by remember { mutableIntStateOf(currentDateTime.manualSecond) }

    // Preview data constructed on the fly
    val previewData = remember(isManual, selectedDateFormat, selectedTimeFormat, selectedTzId, year, month, day, hour, minute, second) {
        DateTimeData(
            source = if (isManual) TimeSource.MANUAL else TimeSource.DEVICE,
            dateFormat = selectedDateFormat,
            timeFormat = selectedTimeFormat,
            timezoneId = selectedTzId,
            manualYear = year,
            manualMonth = month,
            manualDay = day,
            manualHour = hour,
            manualMinute = minute,
            manualSecond = second
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "DATE & TIME STUDIO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Live Stamp Preview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00D2FC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LIVE STAMP PREVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D2FC)
                    )
                    Text(
                        text = previewData.formatFormattedDateTime(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Timezone: $selectedTzId (${if (isManual) "Manual Custom" else "Device Auto"})",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Automatic vs Manual Toggle Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isManual) "MANUAL CUSTOM TIME" else "AUTOMATIC DEVICE TIME",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isManual) Color(0xFFF59E0B) else Color(0xFF10B981)
                        )
                        Text(
                            text = if (isManual) "Time is fixed for all shots until changed" else "Synced to Android system clock",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Switch(
                        checked = isManual,
                        onCheckedChange = { isManual = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00D2FC),
                            checkedTrackColor = Color(0xFF0D1B2A)
                        )
                    )
                }
            }
        }

        // Manual Pickers (Only visible when isManual is true)
        if (isManual) {
            item {
                Text(
                    text = "CUSTOM CALENDAR & CLOCK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D2FC)
                )
            }

            // Year / Month / Day
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberPickerBox("Day", day, 1..31, Modifier.weight(1f)) { day = it }
                    NumberPickerBox("Month", month, 1..12, Modifier.weight(1f)) { month = it }
                    NumberPickerBox("Year", year, 2020..2035, Modifier.weight(1.3f)) { year = it }
                }
            }

            // Hour / Minute / Second
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberPickerBox("Hour (0-23)", hour, 0..23, Modifier.weight(1f)) { hour = it }
                    NumberPickerBox("Minute", minute, 0..59, Modifier.weight(1f)) { minute = it }
                    NumberPickerBox("Second", second, 0..59, Modifier.weight(1f)) { second = it }
                }
            }
        }

        // Date Format Selection
        item {
            Text(
                text = "DATE FORMAT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D2FC)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateFormatOption.values().forEach { option ->
                    val isSelected = selectedDateFormat == option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1B2E4B) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00D2FC) else Color(0xFF243B5A),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDateFormat = option }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = option.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = option.pattern, color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Time Format Selection
        item {
            Text(
                text = "TIME FORMAT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D2FC)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeFormatOption.values().forEach { option ->
                    val isSelected = selectedTimeFormat == option
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1B2E4B) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00D2FC) else Color(0xFF243B5A),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTimeFormat = option }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = option.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = if (option.is24Hour) "24-Hour" else "12-Hour AM/PM", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Action Buttons: Reset & Save
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val now = java.util.Calendar.getInstance()
                        isManual = false
                        year = now.get(java.util.Calendar.YEAR)
                        month = now.get(java.util.Calendar.MONTH) + 1
                        day = now.get(java.util.Calendar.DAY_OF_MONTH)
                        hour = now.get(java.util.Calendar.HOUR_OF_DAY)
                        minute = now.get(java.util.Calendar.MINUTE)
                        second = now.get(java.util.Calendar.SECOND)
                        viewModel.resetToDeviceTime()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Clock", color = Color.White)
                }

                Button(
                    onClick = {
                        viewModel.setDateTimeData(previewData)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_datetime_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply to Camera", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NumberPickerBox(
    label: String,
    value: Int,
    range: IntRange,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243B5A))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF132238))
                        .clickable {
                            if (value > range.first) onValueChange(value - 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "-", color = Color(0xFF00D2FC), fontWeight = FontWeight.Bold)
                }

                Text(
                    text = value.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF132238))
                        .clickable {
                            if (value < range.last) onValueChange(value + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = Color(0xFF00D2FC), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
