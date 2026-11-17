package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.PredictionEntryEntity
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PredictionTrackerPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.predictionEntries.collectAsState()
    var selectedDay by remember { mutableIntStateOf(1) }

    val dayEntries = remember(entries, selectedDay) { entries.filter { it.dayNumber == selectedDay } }
    val latestDayEntry = dayEntries.firstOrNull()

    var editingId by remember { mutableIntStateOf(0) }
    var dateTimeText by remember { mutableStateOf("") }
    var predictionText by remember { mutableStateOf("") }
    var confidencePercent by remember { mutableIntStateOf(70) }
    var outcomeStatus by remember { mutableStateOf("PENDING") }
    var actualOutcomeText by remember { mutableStateOf("") }
    var reflectionNote by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }
    var savedBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedDay, latestDayEntry) {
        if (latestDayEntry != null) {
            editingId = latestDayEntry.id
            dateTimeText = latestDayEntry.dateTimeText
            predictionText = latestDayEntry.predictionText
            confidencePercent = latestDayEntry.confidencePercent
            outcomeStatus = latestDayEntry.outcomeStatus
            actualOutcomeText = latestDayEntry.actualOutcomeText
            reflectionNote = latestDayEntry.reflectionNote
            isLocked = latestDayEntry.isPredictionLocked
        } else {
            editingId = 0
            val nowStr = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
            dateTimeText = nowStr
            predictionText = ""
            confidencePercent = 70
            outcomeStatus = "PENDING"
            actualOutcomeText = ""
            reflectionNote = ""
            isLocked = false
        }
        savedBanner = null
    }

    val correctCount = remember(entries) { entries.count { it.outcomeStatus == "CORRECT" } }
    val incorrectCount = remember(entries) { entries.count { it.outcomeStatus == "INCORRECT" } }
    val unknownCount = remember(entries) { entries.count { it.outcomeStatus == "UNKNOWN" } }
    val decided = correctCount + incorrectCount
    val accuracyRate = if (decided > 0) (correctCount * 100) / decided else 0

    Mind3DCard(
        modifier = modifier.fillMaxWidth(),
        tone = Mind3DCardTone.Sky,
        shapeRadius = 22.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = SkyBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "فرم تمرین ۷ روزه پیش‌بینی و ثبت دقیق تجربه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "قانون صداقت علمی: پیش‌بینی خود را پیش از وقوع ثبت کنید. پس از انتخاب نتیجه (درست / نادرست / نامشخص)، متن پیش‌بینی قفل می‌شود تا خطای سوگیری پس‌نگر رخ ندهد.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cumulative Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatColumn("درست", "$correctCount", MintPrimary)
                SummaryStatColumn("نادرست", "$incorrectCount", PeachPrimary)
                SummaryStatColumn("نامشخص", "$unknownCount", SpiritualVioletPrimary)
                SummaryStatColumn("دقت", if (decided > 0) "$accuracyRate%" else "-", SkyBluePrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day 1..7 Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..7).toList(), key = { it }) { day ->
                    val hasEntry = entries.any { it.dayNumber == day }
                    Mind3DChip(
                        text = if (hasEntry) "روز $day ✓" else "روز $day",
                        onClick = { selectedDay = day },
                        isSelected = selectedDay == day,
                        tone = if (hasEntry) Mind3DChipTone.Mint else Mind3DChipTone.Sky
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "روز $selectedDay از ۷",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                Mind3DChip(
                    text = "ثبت مورد جدید برای روز $selectedDay",
                    onClick = {
                        editingId = 0
                        dateTimeText = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
                        predictionText = ""
                        confidencePercent = 70
                        outcomeStatus = "PENDING"
                        actualOutcomeText = ""
                        reflectionNote = ""
                        isLocked = false
                        savedBanner = null
                    },
                    tone = Mind3DChipTone.Peach,
                    isSelected = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dateTimeText,
                onValueChange = { if (!isLocked) dateTimeText = it },
                label = { Text("تاریخ و ساعت ثبت پیش‌بینی") },
                readOnly = isLocked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "متن پیش‌بینی پس از ثبت رخداد قفل شده و غیرقابل تغییر است.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            OutlinedTextField(
                value = predictionText,
                onValueChange = { if (!isLocked) predictionText = it },
                label = { Text("متن پیش‌بینی یا دریافت ذهنی پیش از وقوع") },
                placeholder = { Text("جزئیات حسی، تصویری یا حدس خود را پیش از مشاهده نتیجه بنویسید...") },
                readOnly = isLocked,
                enabled = !isLocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prediction_text_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    disabledTextColor = TextPrimaryLight,
                    disabledBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "میزان اطمینان اولیه: $confidencePercent%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
            Slider(
                value = confidencePercent.toFloat(),
                onValueChange = { if (!isLocked) confidencePercent = it.toInt() },
                valueRange = 0f..100f,
                enabled = !isLocked,
                colors = SliderDefaults.colors(
                    thumbColor = SkyBluePrimary,
                    activeTrackColor = SkyBluePrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "وضعیت رخداد واقعی (پس از وقوع انتخاب کنید):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mind3DChip(
                    text = "در انتظار رخداد",
                    onClick = { if (!isLocked) outcomeStatus = "PENDING" },
                    isSelected = outcomeStatus == "PENDING",
                    icon = Icons.Default.Pending,
                    tone = Mind3DChipTone.Sky
                )
                Mind3DChip(
                    text = "درست",
                    onClick = { outcomeStatus = "CORRECT" },
                    isSelected = outcomeStatus == "CORRECT",
                    icon = Icons.Default.CheckCircle,
                    tone = Mind3DChipTone.Mint
                )
                Mind3DChip(
                    text = "نادرست",
                    onClick = { outcomeStatus = "INCORRECT" },
                    isSelected = outcomeStatus == "INCORRECT",
                    icon = Icons.Default.Lock,
                    tone = Mind3DChipTone.Peach
                )
                Mind3DChip(
                    text = "نامشخص",
                    onClick = { outcomeStatus = "UNKNOWN" },
                    isSelected = outcomeStatus == "UNKNOWN",
                    icon = Icons.Default.HelpOutline,
                    tone = Mind3DChipTone.Violet
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = actualOutcomeText,
                onValueChange = { actualOutcomeText = it },
                label = { Text("نتیجه واقعی مشاهده‌شده") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = reflectionNote,
                onValueChange = { reflectionNote = it },
                label = { Text("تحلیل و یادداشت شخصی") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2
            )

            if (savedBanner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = savedBanner!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MintPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Mind3DButton(
                    text = if (outcomeStatus != "PENDING") "ثبت نتیجه و قفل پیش‌بینی" else "ذخیره پیش‌بینی روز $selectedDay",
                    onClick = {
                        if (predictionText.isNotBlank()) {
                            viewModel.savePredictionEntry(
                                PredictionEntryEntity(
                                    id = editingId,
                                    dayNumber = selectedDay,
                                    dateTimeText = dateTimeText,
                                    predictionText = predictionText,
                                    isPredictionLocked = outcomeStatus != "PENDING" || isLocked,
                                    confidencePercent = confidencePercent,
                                    outcomeStatus = outcomeStatus,
                                    actualOutcomeText = actualOutcomeText,
                                    reflectionNote = reflectionNote
                                )
                            )
                            if (outcomeStatus != "PENDING") isLocked = true
                            savedBanner = "پیش‌بینی روز $selectedDay با موفقیت در حافظه آفلاین ثبت شد."
                        }
                    },
                    variant = Mind3DButtonVariant.Mint,
                    icon = Icons.Default.Save,
                    enabled = predictionText.isNotBlank(),
                    modifier = Modifier.weight(1f).testTag("save_prediction_button")
                )

                if (latestDayEntry != null) {
                    Mind3DIconButton(
                        icon = Icons.Default.Delete,
                        onClick = { viewModel.deletePredictionEntry(latestDayEntry) },
                        variant = Mind3DIconVariant.Danger,
                        contentDescription = "حذف ثبت فعلی"
                    )
                }
            }

            if (dayEntries.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سوابق ثبت‌شده روز $selectedDay (${dayEntries.size} مورد):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight
                )
                dayEntries.forEach { entry ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "${entry.dateTimeText} — ${
                                    when (entry.outcomeStatus) {
                                        "CORRECT" -> "درست ✓"
                                        "INCORRECT" -> "نادرست ✗"
                                        "UNKNOWN" -> "نامشخص"
                                        else -> "در انتظار رخداد"
                                    }
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                            Text(
                                text = entry.predictionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
    }
}
