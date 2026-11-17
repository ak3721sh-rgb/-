package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val lessons by viewModel.lessons.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val logs by viewModel.experienceLogs.collectAsState()
    val predictions by viewModel.predictionEntries.collectAsState()
    val gameResults by viewModel.gameResults.collectAsState()

    val completedPages = readingProgress?.completedPagesCount ?: 1
    val completedLessons = lessons.count { it.isCompleted }
    val masteredCardsCount = flashcards.count { it.isMastered }
    val difficultCardsCount = flashcards.count { it.isMarkedDifficult || it.errorCount > 0 }
    val totalGamesPlayed = gameResults.size
    val totalXp = userProfile?.xp ?: 0
    val correctPredictions = predictions.count { it.outcomeStatus == "CORRECT" }

    val totalUnits = (lessons.size + flashcards.size + 16).coerceAtLeast(1)
    val completedUnits = completedLessons + masteredCardsCount + completedPages.coerceAtMost(16)
    val courseProgressPercent = ((completedUnits.toFloat() / totalUnits) * 100).toInt().coerceIn(2, 100)

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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                                text = "درصد پیشرفت در دوره آموزشی",
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
                                        text = "پیشرفت واقعی",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "تمام شاخص‌های زیر بر اساس فعالیت واقعی شما محاسبه و در پایگاه داده محلی ذخیره می‌شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

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
                            title = "زمان مطالعه",
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
                            title = "درس‌های تکمیل‌شده",
                            value = "$completedLessons درس",
                            subtitle = "از ${lessons.size} درس",
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
                            subtitle = "آمار بازی‌های شناختی",
                            icon = Icons.Default.SportsEsports,
                            color = SkyBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 22.dp,
                        extrusionDepth = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LogoGold, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "پیشنهاد فعالیت بعدی برای شما:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (difficultCardsCount > 0) {
                                    "مرور کارت‌های نیازمند تمرین در بخش فلش‌کارت‌های لایتنر برای تثبیت مفاهیم دوره."
                                } else {
                                    "مطالعه ادامه سرفصل‌های کتاب اول و ثبت پیش‌بینی روزانه در فرم ۷ روزه."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                color = TextSecondaryLight
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Mind3DButton(
                                text = "شروع فعالیت پیشنهادی",
                                onClick = {
                                    if (difficultCardsCount > 0) {
                                        viewModel.navigateTo(ScreenDestination.Flashcards)
                                    } else {
                                        viewModel.navigateTo(ScreenDestination.BooksLibrary)
                                    }
                                },
                                variant = Mind3DButtonVariant.Mint,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
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
