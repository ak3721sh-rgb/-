package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TurquoisePrimary
import com.example.ui.theme.TurquoiseSoft
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun ProgressStatsScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val userProfile by viewModel.userProfile.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val logs by viewModel.experienceLogs.collectAsState()
    val predictions by viewModel.predictionEntries.collectAsState()
    val gameResults by viewModel.gameResults.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

    val completedPages = readingProgress?.completedPagesCount ?: 0
    val completedLessons = lessons.count { it.isCompleted }
    val remainingLessons = (lessons.size - completedLessons).coerceAtLeast(0)
    val studiedChapterIds = remember(lessons) {
        lessons.filter { it.isCompleted }.map { it.chapterId }.toSet()
    }
    val studiedChaptersCount = studiedChapterIds.size.coerceAtLeast(
        if ((readingProgress?.lastReadBookId ?: 1) > 1 || completedPages > 1) 1 else 0
    )

    val masteredCardsCount = flashcards.count { it.isMastered }
    val difficultCardsCount = flashcards.count { it.isMarkedDifficult || it.errorCount > 0 }
    val totalGamesPlayed = gameResults.size
    val totalXp = userProfile?.xp ?: 0
    val correctPredictions = predictions.count { it.outcomeStatus == "CORRECT" }

    val totalUnits = (lessons.size + flashcards.size + 16).coerceAtLeast(1)
    val completedUnits = completedLessons + masteredCardsCount + completedPages.coerceAtMost(16)
    val courseProgressPercent = ((completedUnits.toFloat() / totalUnits) * 100).toInt().coerceIn(0, 100)

    val lastBookId = readingProgress?.lastReadBookId ?: 1
    val lastPageNumber = readingProgress?.lastReadPageNumber ?: 1
    val lastActivityTitle = readingProgress?.lastActivityTitle ?: "کتاب اول: مبانی ارتباط ذهن‌ها"

    val recommendation = remember(difficultCardsCount, remainingLessons, totalGamesPlayed, logs.size, predictions.size) {
        when {
            difficultCardsCount > 0 -> Triple(
                "مرور $difficultCardsCount فلش‌کارت نیازمند تمرین",
                "علت پیشنهاد: تعداد $difficultCardsCount کارت در صف مرور لایتنر شما قرار دارد.",
                ScreenDestination.Flashcards
            )
            remainingLessons > 0 && completedLessons < 3 -> Triple(
                "ادامه درس‌های باقی‌مانده ($remainingLessons درس)",
                "علت پیشنهاد: تکمیل مفاهیم پایه پیش‌نیاز تمرین‌های عملی بعدی است.",
                ScreenDestination.Chapters
            )
            totalGamesPlayed == 0 -> Triple(
                "انجام اولین بازی شناختی و سنجش تمرکز",
                "علت پیشنهاد: هنوز نتیجه‌ای از ۱۲ بازی شناختی برای ترسیم نمودار پیشرفت ثبت نشده است.",
                ScreenDestination.GamesHub
            )
            predictions.isEmpty() && logs.isEmpty() -> Triple(
                "ثبت اولین پیش‌بینی در فرم ۷ روزه یا دفترچه تجربه",
                "علت پیشنهاد: ثبت مکتوب پیش‌بینی پیش از وقوع، دقت مشاهده عینی شما را مشخص می‌کند.",
                ScreenDestination.Journal
            )
            else -> Triple(
                "ادامه مطالعه آخرین صفحه کتاب درسی",
                "علت پیشنهاد: مسیر مطالعه و تمرین‌های شما متوازن است؛ صفحه $lastPageNumber کتاب $lastBookId آماده ادامه است.",
                ScreenDestination.BookReader(lastBookId, lastPageNumber)
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("بازنشانی آمار و تاریخچه تمرین‌ها", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "آیا مطمئن هستید که می‌خواهید آمار XP، سوابق بازی‌های شناختی، وضعیت درس‌ها و تسلط فلش‌کارت‌ها را به حالت اولیه برگردانید؟\nنکته: یادداشت‌ها و نشانک‌های کتاب شما حفظ می‌شوند.",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetUserProgressData()
                        showResetConfirm = false
                    },
                    modifier = Modifier.testTag("stats_reset_confirm_btn")
                ) {
                    Text("بله، بازنشانی شود", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("انصراف", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "کارنامه و نمودار پیشرفت",
                subtitle = "آمار واقعی ذخیره‌شده در دستگاه شما",
                backButtonVariant = Mind3DIconVariant.Turquoise,
                onBackClick = { viewModel.navigateBack() },
                showSettingsAction = true
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SkyBlueSoft,
                            TurquoiseSoft.copy(alpha = 0.25f),
                            MintSoft.copy(alpha = 0.2f),
                            ButterYellowSoft.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("stats_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Overall Course Progress Ring
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.WarmIvory,
                        shapeRadius = 24.dp,
                        extrusionDepth = 4.5.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "درصد پیشرفت واقعی در دوره آموزشی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { courseProgressPercent / 100f },
                                    modifier = Modifier.size(120.dp),
                                    strokeWidth = 10.dp,
                                    color = SkyBluePrimary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$courseProgressPercent%",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary
                                    )
                                    Text(
                                        text = "پیشرفت ثبت‌شده",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "فصل‌های دارای مطالعه: $studiedChaptersCount از ${chapters.size} فصل • تمام شاخص‌ها صرفاً از فعالیت واقعی شما محاسبه می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 2. Resume From Last Point Card
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Sky,
                        shapeRadius = 20.dp,
                        extrusionDepth = 3.5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "ادامه از آخرین نقطه مطالعه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = "$lastActivityTitle — صفحه $lastPageNumber",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight
                            )
                            Mind3DButton(
                                text = "ادامه مطالعه صفحه $lastPageNumber",
                                onClick = { viewModel.navigateTo(ScreenDestination.BookReader(lastBookId, lastPageNumber)) },
                                variant = Mind3DButtonVariant.Sky,
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.fillMaxWidth(),
                                height = 48.dp
                            )
                        }
                    }
                }

                // 3. Detailed Metric Cards Grid
                item {
                    Text(
                        text = "شاخص‌های عملکردی و یادگیری",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = "امتیاز کل XP",
                            value = "$totalXp",
                            subtitle = "سطح ${(totalXp / 250) + 1}",
                            icon = Icons.Default.AutoAwesome,
                            color = LogoGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "زمان مطالعه فعال",
                            value = "${readingProgress?.totalReadingMinutes ?: 0} دقیقه",
                            subtitle = "$completedPages صفحه ثبت‌شده",
                            icon = Icons.Default.Schedule,
                            color = SkyBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = "فلش‌کارت‌های آموخته",
                            value = "$masteredCardsCount کارت",
                            subtitle = "از ${flashcards.size} کارت",
                            icon = Icons.Default.Style,
                            color = MintPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "نیازمند مرور",
                            value = "$difficultCardsCount کارت",
                            subtitle = "تمرین مجدد لایتنر",
                            icon = Icons.Default.Warning,
                            color = PeachPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = "وضعیت درس‌ها",
                            value = "$completedLessons انجام‌شده",
                            subtitle = "$remainingLessons درس باقیمانده از ${lessons.size}",
                            icon = Icons.Default.MenuBook,
                            color = TurquoisePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "پیش‌بینی ۷ روزه",
                            value = "${predictions.size} ثبت",
                            subtitle = "$correctPredictions پیش‌بینی درست",
                            icon = Icons.Default.TrackChanges,
                            color = SpiritualVioletPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = "دفترچه تجربه",
                            value = "${logs.size} تجربه",
                            subtitle = "ثبت‌های آزاد",
                            icon = Icons.Default.CheckCircle,
                            color = MintPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "بازی‌های اجراشده",
                            value = "$totalGamesPlayed دور",
                            subtitle = "نتایج بازی‌های شناختی",
                            icon = Icons.Default.SportsEsports,
                            color = SkyBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Real Progress History Chart (Compose Canvas) & Recent Cognitive Game Summary
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.WarmIvory,
                        shapeRadius = 22.dp,
                        extrusionDepth = 3.5.dp,
                        testTag = "progress_history_chart_card"
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نمودار تاریخچه پیشرفت بازی‌های شناختی",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }

                            val recentGames = remember(gameResults) { gameResults.take(8).reversed() }
                            if (recentGames.isEmpty()) {
                                Text(
                                    text = "هنوز هیچ دور بازی برای رسم نمودار تاریخچه ثبت نشده است. با انجام هر بازی شناختی، ستون واقعی دقت و امتیاز شما در اینجا ظاهر می‌شود.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight,
                                    lineHeight = 20.sp
                                )
                            } else {
                                val maxScore = (recentGames.maxOfOrNull { it.score } ?: 100).coerceAtLeast(100)
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(12.dp)
                                ) {
                                    val barCount = recentGames.size
                                    val slotWidth = size.width / barCount.coerceAtLeast(1)
                                    val barWidth = (slotWidth * 0.54f).coerceAtMost(36.dp.toPx())

                                    // Reference horizontal grid line
                                    drawLine(
                                        color = Color(0xFFCBD5E1),
                                        start = Offset(0f, size.height * 0.5f),
                                        end = Offset(size.width, size.height * 0.5f),
                                        strokeWidth = 1.5f
                                    )

                                    recentGames.forEachIndexed { idx, result ->
                                        val ratio = (result.score.toFloat() / maxScore.toFloat()).coerceIn(0.08f, 1f)
                                        val barH = size.height * ratio
                                        val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                                        drawRoundRect(
                                            color = if (result.accuracyPercent >= 75f) Color(0xFF16A34A) else Color(0xFF0284C7),
                                            topLeft = Offset(left, size.height - barH),
                                            size = Size(barWidth, barH),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                    }
                                }

                                // Summary list of latest 3 cognitive runs
                                recentGames.takeLast(3).reversed().forEach { res ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(res.gameTitle, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${res.score} XP • دقت ${res.accuracyPercent.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkyBluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Smart Recommendation With Clear Reasoning
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 22.dp,
                        extrusionDepth = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LogoGold, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "پیشنهاد هوشمند فعالیت بعدی:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                            Text(
                                text = recommendation.first,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = recommendation.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 20.sp
                            )
                            Mind3DButton(
                                text = "شروع فعالیت پیشنهادی",
                                onClick = { viewModel.navigateTo(recommendation.third) },
                                variant = Mind3DButtonVariant.Mint,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 6. Reset Stats With Confirmation Dialog
                item {
                    Mind3DButton(
                        text = "بازنشانی آمار پیشرفت و سابقه بازی‌ها",
                        onClick = { showResetConfirm = true },
                        variant = Mind3DButtonVariant.Danger,
                        icon = Icons.Default.DeleteSweep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stats_reset_button"),
                        height = 48.dp
                    )
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val cardTone = when (color) {
        LogoGold -> Mind3DCardTone.LemonChiffon
        SkyBluePrimary -> Mind3DCardTone.Sky
        MintPrimary -> Mind3DCardTone.Mint
        OrangeLightPrimary, PeachPrimary -> Mind3DCardTone.Apricot
        SpiritualVioletPrimary -> Mind3DCardTone.Violet
        else -> Mind3DCardTone.Turquoise
    }

    Mind3DCard(
        modifier = modifier.defaultMinSize(minHeight = 134.dp),
        tone = cardTone,
        shapeRadius = 20.dp,
        extrusionDepth = 3.dp,
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 134.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = TextPrimaryLight, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
            }
        }
    }
}
