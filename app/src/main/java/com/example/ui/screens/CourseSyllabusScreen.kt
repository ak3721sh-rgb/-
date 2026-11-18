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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.LessonEntity
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

private enum class SyllabusFilter(val label: String, val tone: Mind3DChipTone) {
    ALL("همه درس‌ها", Mind3DChipTone.Sky),
    CONCEPT("مفاهیم علمی", Mind3DChipTone.Violet),
    TECHNIQUE("تکنیک‌ها", Mind3DChipTone.Peach),
    EXERCISE("تمرین‌ها", Mind3DChipTone.Mint),
    PENDING("تکمیل‌نشده", Mind3DChipTone.Gold)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseSyllabusScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val chapters by viewModel.chapters.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SyllabusFilter.ALL) }

    val totalLessonsCount = lessons.size
    val completedLessonsCount = remember(lessons) { lessons.count { it.isCompleted } }
    val progressFraction = if (totalLessonsCount > 0) {
        completedLessonsCount.toFloat() / totalLessonsCount.toFloat()
    } else {
        0f
    }

    val lessonsByChapter = remember(chapters, lessons, searchQuery, selectedFilter) {
        val query = searchQuery.trim()
        chapters.sortedBy { it.id }.mapNotNull { chapter ->
            val chapterMatchesQuery = query.isBlank() ||
                chapter.title.contains(query, ignoreCase = true) ||
                chapter.subtitle.contains(query, ignoreCase = true)

            val matchingLessons = lessons.filter { lesson ->
                lesson.chapterId == chapter.id &&
                    when (selectedFilter) {
                        SyllabusFilter.ALL -> true
                        SyllabusFilter.CONCEPT -> lesson.contentType == "CONCEPT"
                        SyllabusFilter.TECHNIQUE -> lesson.contentType == "TECHNIQUE"
                        SyllabusFilter.EXERCISE -> lesson.contentType == "EXERCISE"
                        SyllabusFilter.PENDING -> !lesson.isCompleted
                    } &&
                    (chapterMatchesQuery ||
                        lesson.title.contains(query, ignoreCase = true) ||
                        lesson.content.contains(query, ignoreCase = true))
            }.sortedBy { it.id }

            if (matchingLessons.isNotEmpty()) chapter to matchingLessons else null
        }
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "سرفصل‌های آموزشی دوره",
                subtitle = "هنر ارتباط ذهن‌ها • مدرس: اکبر شیرزادخانی",
                backButtonVariant = Mind3DIconVariant.Sky,
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
                        listOf(SkyBlueSoft, MintSoft.copy(alpha = 0.45f), LemonYellowGlow)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .testTag("syllabus_lazy_column"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "syllabus_header_summary") {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.WarmIvory,
                        shapeRadius = 22.dp,
                        extrusionDepth = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(SkyBlueSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = SkyBluePrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "نقشه کامل درس‌های «هنر ارتباط ذهن‌ها»",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryLight
                                        )
                                        Text(
                                            text = "${chapters.size} سرفصل اصلی • $totalLessonsCount درس تعاملی",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintSoft)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$completedLessonsCount از $totalLessonsCount",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = MintPrimary,
                                trackColor = Color(0xFFE2E8F0),
                                strokeCap = StrokeCap.Round
                            )

                            Text(
                                text = "روی هر کارت درس لمس کنید تا متن کامل، مثال کاربردی، تکنیک گام‌به‌گام و آزمون کوتاه همان درس باز شود.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                item(key = "syllabus_search_filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در عنوان سرفصل یا متن درس‌ها...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SkyBluePrimary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("syllabus_search_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SyllabusFilter.entries.forEach { filter ->
                                Mind3DChip(
                                    text = filter.label,
                                    isSelected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    tone = filter.tone,
                                    height = 44.dp,
                                    testTag = "syllabus_filter_${filter.name}"
                                )
                            }
                        }
                    }
                }

                if (lessonsByChapter.isEmpty()) {
                    item(key = "syllabus_empty_state") {
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.WarmIvory,
                            shapeRadius = 20.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = TextSecondaryLight,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "درسی با این عبارت یا فیلتر پیدا نشد.",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Mind3DButton(
                                    text = "نمایش همه درس‌ها",
                                    onClick = {
                                        searchQuery = ""
                                        selectedFilter = SyllabusFilter.ALL
                                    },
                                    variant = Mind3DButtonVariant.Sky,
                                    height = 48.dp
                                )
                            }
                        }
                    }
                } else {
                    lessonsByChapter.forEach { (chapter, chapterLessons) ->
                        item(key = "chapter_header_${chapter.id}") {
                            SyllabusChapterHeaderCard(
                                chapter = chapter,
                                completedCount = chapterLessons.count { it.isCompleted },
                                totalCount = chapterLessons.size,
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.ChapterDetail(chapter.id))
                                }
                            )
                        }

                        items(
                            items = chapterLessons,
                            key = { lesson -> "lesson_${lesson.id}" }
                        ) { lesson ->
                            SyllabusLessonCard(
                                lesson = lesson,
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.LessonDetail(lesson.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyllabusChapterHeaderCard(
    chapter: ChapterEntity,
    completedCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    Mind3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        tone = Mind3DCardTone.Sky,
        shapeRadius = 18.dp,
        extrusionDepth = 3.dp,
        testTag = "syllabus_chapter_header_${chapter.id}",
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${chapter.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkyBluePrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapter.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
            }
        }
    }
}

@Composable
private fun SyllabusLessonCard(
    lesson: LessonEntity,
    onClick: () -> Unit
) {
    val (typeLabel, badgeColor, cardTone, icon) = when (lesson.contentType) {
        "TECHNIQUE" -> Quadruple("تکنیک کاربردی", PeachPrimary, Mind3DCardTone.Peach, Icons.Default.Lightbulb)
        "EXERCISE" -> Quadruple("تمرین عملی", MintPrimary, Mind3DCardTone.Mint, Icons.Default.FitnessCenter)
        else -> Quadruple("مفهوم علمی", SpiritualVioletPrimary, Mind3DCardTone.Violet, Icons.Default.Psychology)
    }

    Mind3DCard(
        modifier = Modifier.fillMaxWidth(),
        tone = if (lesson.isCompleted) Mind3DCardTone.Mint else Mind3DCardTone.WarmIvory,
        shapeRadius = 18.dp,
        extrusionDepth = 3.5.dp,
        testTag = "syllabus_lesson_card_${lesson.id}",
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                if (lesson.isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "درس تکمیل‌شده",
                            tint = MintPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "تکمیل‌شده",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MintPrimary
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "مطالعه درس",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SkyBlueSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "باز کردن درس",
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                lineHeight = 24.sp
            )

            Text(
                text = lesson.content,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            if (!lesson.stepTechnique.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (cardTone == Mind3DCardTone.Peach) Color(0xFFFFF7ED)
                            else Color(0xFFF8FAFC)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تکنیک کلیدی: ${lesson.stepTechnique.lineSequence().firstOrNull().orEmpty()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
