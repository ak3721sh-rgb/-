package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.PredictionTrackerPanel
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

/**
 * Dedicated Practice Hub (تمرین‌ها): collects solo/pair exercises, book practice boxes,
 * quick Journal entry, and the 7-day real prediction accuracy log.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticesScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val lessons by viewModel.lessons.collectAsState()
    val pages by viewModel.allBookPages.collectAsState()
    val interaction = LocalInteractionController.current
    val readerSize = interaction.settings.readerFontSizeSp

    var filterMode by remember { mutableStateOf("ALL") } // ALL, SOLO, PAIR, TRACKER
    val practiceLessons = remember(lessons) {
        lessons.filter { it.soloExercise.isNotBlank() || it.pairExercise.isNotBlank() }
    }
    val bookExercises = remember(pages) {
        pages.filter { !it.reflectionExercise.isNullOrBlank() }
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "تمرین‌های کاربردی و ثبت تجربه",
                subtitle = "تمرین‌های فردی، دونفره و فرم پیش‌بینی ۷ روزه",
                backButtonVariant = Mind3DIconVariant.Mint,
                showSettingsAction = true,
                onBackClick = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(MintSoft.copy(alpha = 0.7f), SkyBlueSoft.copy(alpha = 0.55f), ButterYellowSoft.copy(alpha = 0.35f))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 22.dp,
                        extrusionDepth = 3.5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MintPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مرکز تمرین‌های گام‌به‌گام هنر ارتباط ذهن‌ها",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                            Text(
                                text = "در این بخش تمام تمرین‌های فردی، دونفره و پروتکل ثبت ۷ روزه دوره در دسترس شماست تا با مشاهده عینی و بدون قضاوت تمرین کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 22.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Mind3DButton(
                                    text = "دفترچه تجربه",
                                    onClick = { viewModel.navigateTo(ScreenDestination.Journal) },
                                    variant = Mind3DButtonVariant.Sky,
                                    icon = Icons.Default.EditNote,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    height = 46.dp
                                )
                                Mind3DButton(
                                    text = "فرم ۷ روزه",
                                    onClick = { filterMode = "TRACKER" },
                                    variant = Mind3DButtonVariant.Gold,
                                    icon = Icons.Default.TrackChanges,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    height = 46.dp
                                )
                            }
                        }
                    }
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DChip(
                            text = "همه تمرین‌ها (${practiceLessons.size})",
                            isSelected = filterMode == "ALL",
                            onClick = { filterMode = "ALL" },
                            icon = Icons.Default.AutoAwesome,
                            tone = Mind3DChipTone.Mint,
                            testTag = "practice_filter_all"
                        )
                        Mind3DChip(
                            text = "تمرین فردی",
                            isSelected = filterMode == "SOLO",
                            onClick = { filterMode = "SOLO" },
                            icon = Icons.Default.Person,
                            tone = Mind3DChipTone.Sky,
                            testTag = "practice_filter_solo"
                        )
                        Mind3DChip(
                            text = "تمرین دونفره",
                            isSelected = filterMode == "PAIR",
                            onClick = { filterMode = "PAIR" },
                            icon = Icons.Default.Group,
                            tone = Mind3DChipTone.Peach,
                            testTag = "practice_filter_pair"
                        )
                        Mind3DChip(
                            text = "ثبت ۷ روزه",
                            isSelected = filterMode == "TRACKER",
                            onClick = { filterMode = "TRACKER" },
                            icon = Icons.Default.TrackChanges,
                            tone = Mind3DChipTone.Gold,
                            testTag = "practice_filter_tracker"
                        )
                    }
                }

                if (filterMode == "TRACKER") {
                    item {
                        PredictionTrackerPanel(viewModel = viewModel)
                    }
                } else {
                    val displayedLessons = practiceLessons.filter { lesson ->
                        when (filterMode) {
                            "SOLO" -> lesson.soloExercise.isNotBlank()
                            "PAIR" -> lesson.pairExercise.isNotBlank()
                            else -> true
                        }
                    }

                    items(displayedLessons, key = { "prac_lesson_${it.id}" }) { lesson ->
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.WarmIvory,
                            shapeRadius = 20.dp,
                            extrusionDepth = 3.5.dp,
                            onClick = { viewModel.navigateTo(ScreenDestination.LessonDetail(lesson.id)) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "فصل ${lesson.chapterId} • ${lesson.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "مشاهده درس",
                                        tint = TextSecondaryLight
                                    )
                                }

                                if (filterMode != "PAIR" && lesson.soloExercise.isNotBlank()) {
                                    Text(
                                        text = "تمرین فردی:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary
                                    )
                                    Text(
                                        text = lesson.soloExercise,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = (readerSize - 1f).coerceAtLeast(14f).sp,
                                            lineHeight = (readerSize * 1.55f).sp
                                        ),
                                        color = TextPrimaryLight
                                    )
                                }

                                if (filterMode != "SOLO" && lesson.pairExercise.isNotBlank()) {
                                    Text(
                                        text = "تمرین دونفره و گروهی:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PeachPrimary
                                    )
                                    Text(
                                        text = lesson.pairExercise,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = (readerSize - 1f).coerceAtLeast(14f).sp,
                                            lineHeight = (readerSize * 1.55f).sp
                                        ),
                                        color = TextPrimaryLight
                                    )
                                }
                            }
                        }
                    }

                    if (filterMode == "ALL" && bookExercises.isNotEmpty()) {
                        item {
                            Text(
                                text = "تمرین‌های کتاب‌خوان (${bookExercises.size} مورد):",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }

                        items(bookExercises, key = { "prac_page_${it.id}" }) { page ->
                            Mind3DCard(
                                modifier = Modifier.fillMaxWidth(),
                                tone = Mind3DCardTone.Mint,
                                shapeRadius = 18.dp,
                                extrusionDepth = 3.dp,
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.BookReader(page.bookId, page.pageNumber))
                                }
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "کتاب ${page.bookId} • صفحه ${page.pageNumber} — ${page.sectionTitle}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary
                                    )
                                    Text(
                                        text = page.reflectionExercise.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimaryLight,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Techniques Hub (تکنیک‌ها): aggregates step-by-step course techniques
 * from lessons and book pages with clear visual hierarchy and direct navigation to context.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TechniquesScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val lessons by viewModel.lessons.collectAsState()
    val pages by viewModel.allBookPages.collectAsState()
    val interaction = LocalInteractionController.current
    val readerSize = interaction.settings.readerFontSizeSp

    var sourceFilter by remember { mutableStateOf("ALL") } // ALL, LESSONS, BOOKS

    val techniqueLessons = remember(lessons) {
        lessons.filter { it.stepTechnique.isNotBlank() }
    }
    val techniquePages = remember(pages) {
        pages.filter { !it.techniqueBox.isNullOrBlank() }
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "تکنیک‌های عملی و گام‌به‌گام",
                subtitle = "روش‌های تمرکز، تنفس، آلفا و همدلی بدون قضاوت",
                backButtonVariant = Mind3DIconVariant.Peach,
                showSettingsAction = true,
                onBackClick = { viewModel.navigateBack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(PeachSoft.copy(alpha = 0.55f), ButterYellowSoft.copy(alpha = 0.45f), SkyBlueSoft.copy(alpha = 0.45f))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Peach,
                        shapeRadius = 22.dp,
                        extrusionDepth = 3.5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PeachPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مجموعه تکنیک‌های آموزشی استاد اکبر شیرزادخانی",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                            Text(
                                text = "مرور سریع دستورالعمل‌های مرحله‌ای تنفس، پاکسازی حواس‌پرتی، تفکیک مشاهده از برداشت و تمرین‌های همدلی.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DChip(
                            text = "همه تکنیک‌ها (${techniqueLessons.size + techniquePages.size})",
                            isSelected = sourceFilter == "ALL",
                            onClick = { sourceFilter = "ALL" },
                            icon = Icons.Default.AutoAwesome,
                            tone = Mind3DChipTone.Peach,
                            testTag = "tech_filter_all"
                        )
                        Mind3DChip(
                            text = "تکنیک فصل‌ها (${techniqueLessons.size})",
                            isSelected = sourceFilter == "LESSONS",
                            onClick = { sourceFilter = "LESSONS" },
                            icon = Icons.Default.Lightbulb,
                            tone = Mind3DChipTone.Gold,
                            testTag = "tech_filter_lessons"
                        )
                        Mind3DChip(
                            text = "نکات کتاب‌ها (${techniquePages.size})",
                            isSelected = sourceFilter == "BOOKS",
                            onClick = { sourceFilter = "BOOKS" },
                            icon = Icons.Default.MenuBook,
                            tone = Mind3DChipTone.Sky,
                            testTag = "tech_filter_books"
                        )
                    }
                }

                if (sourceFilter != "BOOKS") {
                    items(techniqueLessons, key = { "tech_lesson_${it.id}" }) { lesson ->
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.Lemon,
                            shapeRadius = 20.dp,
                            extrusionDepth = 3.5.dp,
                            onClick = { viewModel.navigateTo(ScreenDestination.LessonDetail(lesson.id)) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "فصل ${lesson.chapterId} • ${lesson.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "باز کردن درس",
                                        tint = Color(0xFF92400E)
                                    )
                                }
                                Text(
                                    text = lesson.stepTechnique,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = (readerSize - 1f).coerceAtLeast(14f).sp,
                                        lineHeight = (readerSize * 1.55f).sp
                                    ),
                                    color = TextPrimaryLight
                                )
                            }
                        }
                    }
                }

                if (sourceFilter != "LESSONS") {
                    items(techniquePages, key = { "tech_page_${it.id}" }) { page ->
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.WarmIvory,
                            shapeRadius = 20.dp,
                            extrusionDepth = 3.5.dp,
                            onClick = {
                                viewModel.navigateTo(ScreenDestination.BookReader(page.bookId, page.pageNumber))
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "کتاب ${page.bookId} • صفحه ${page.pageNumber} — ${page.sectionTitle}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "مشاهده در کتابخوان",
                                        tint = SkyBluePrimary
                                    )
                                }
                                Text(
                                    text = page.techniqueBox.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = (readerSize - 1f).coerceAtLeast(14f).sp,
                                        lineHeight = (readerSize * 1.55f).sp
                                    ),
                                    color = TextPrimaryLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
