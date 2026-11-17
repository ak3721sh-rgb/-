package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.MindCalloutBox
import com.example.ui.components.MindCalloutType
import com.example.ui.components.PredictionTrackerPanel
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.LemonYellowPrimary
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun ChapterDetailScreen(
    chapterId: Int,
    viewModel: MainViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }
    val lessonsFlow = remember(chapterId) { viewModel.getLessonsForChapter(chapterId) }
    val chapterLessons by lessonsFlow.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()
    val interaction = LocalInteractionController.current
    val readerSize = interaction.settings.readerFontSizeSp

    val quizLesson = chapterLessons.firstOrNull { it.quizQuestion.isNotBlank() }
    var selectedQuizAnswer by remember(chapterId) { mutableIntStateOf(-1) }
    var quizSubmitted by remember(chapterId) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = chapter?.title ?: "جزئیات فصل $chapterId",
                subtitle = "مدرس: اکبر شیرزادخانی",
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
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.2f), LemonYellowGlow)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Sky,
                    shapeRadius = 22.dp,
                    extrusionDepth = 4.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = SkyBluePrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = chapter?.subtitle ?: "",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "مدرس دوره: اکبر شیرزادخانی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = chapter?.introduction ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (readerSize - 1f).coerceAtLeast(15f).sp,
                                lineHeight = (readerSize * 1.55f).sp
                            ),
                            color = TextPrimaryLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "اهداف یادگیری این فصل:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeLightPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chapter?.learningObjectives ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Real Lessons List for This Chapter (with TECHNIQUE / EXERCISE / CONCEPT badges)
                if (chapterLessons.isNotEmpty()) {
                    Text(
                        text = "درس‌ها و تمرین‌های این فصل (${chapterLessons.size} مورد):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    chapterLessons.forEach { lesson ->
                        val (badgeText, badgeTone) = when (lesson.contentType) {
                            "TECHNIQUE" -> "تکنیک عملی" to Mind3DChipTone.Peach
                            "EXERCISE" -> "تمرین و فعالیت" to Mind3DChipTone.Mint
                            else -> "مفهوم علمی" to Mind3DChipTone.Sky
                        }
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = if (lesson.isCompleted) Mind3DCardTone.Mint else Mind3DCardTone.WarmIvory,
                            onClick = { viewModel.navigateTo(ScreenDestination.LessonDetail(lesson.id)) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (lesson.isCompleted) MintPrimary else SkyBluePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lesson.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Mind3DChip(
                                        text = badgeText,
                                        onClick = { viewModel.navigateTo(ScreenDestination.LessonDetail(lesson.id)) },
                                        isSelected = true,
                                        tone = badgeTone
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "ورود به درس",
                                    tint = TextSecondaryLight
                                )
                            }
                        }
                    }
                }

                // Interactive Group Activity Guide + 7-Day Tracker on Chapter 1
                if (chapterId == 1) {
                    MindCalloutBox(
                        type = MindCalloutType.Practice,
                        title = "راهنمای فعالیت گروهی فصل اول",
                        content = "هر هفته یک تجربه واقعی یا پیش‌بینی روزانه را طبق پروتکل فصل اول ثبت کنید، مشاهدات عینی را از برداشت‌ها تفکیک نمایید و در پایان هفته نتایج گروهی را بدون جهت‌گیری بررسی کنید.",
                        modifier = Modifier.fillMaxWidth()
                    )
                    PredictionTrackerPanel(viewModel = viewModel)
                }

                // Dynamic Interactive Chapter Quiz Card (backed by real LessonEntity question when available)
                if (quizLesson != null) {
                    val options = listOf(
                        quizLesson.quizOption1,
                        quizLesson.quizOption2,
                        quizLesson.quizOption3,
                        quizLesson.quizOption4
                    ).filter { it.isNotBlank() }

                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 22.dp,
                        extrusionDepth = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = LemonYellowPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "آزمون ارزیابی یادگیری فصل $chapterId",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = quizLesson.quizQuestion,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryLight
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            options.forEachIndexed { index, option ->
                                val isSelected = selectedQuizAnswer == index
                                val isCorrect = index == quizLesson.quizCorrectIndex

                                val optionTone = when {
                                    quizSubmitted && isCorrect -> Mind3DCardTone.Mint
                                    quizSubmitted && isSelected && !isCorrect -> Mind3DCardTone.Peach
                                    isSelected -> Mind3DCardTone.Sky
                                    else -> Mind3DCardTone.WarmIvory
                                }

                                Mind3DCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    tone = optionTone,
                                    shapeRadius = 14.dp,
                                    extrusionDepth = if (isSelected) 2.dp else 3.5.dp,
                                    borderWidth = if (isSelected) 2.dp else 1.dp,
                                    onClick = if (!quizSubmitted) {
                                        { selectedQuizAnswer = index }
                                    } else null
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (quizSubmitted && isCorrect) Color(0xFF1B5E20) else TextPrimaryLight,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!quizSubmitted) {
                                Mind3DButton(
                                    text = "ثبت پاسخ آزمون فصل",
                                    onClick = {
                                        if (selectedQuizAnswer != -1) {
                                            quizSubmitted = true
                                            if (selectedQuizAnswer == quizLesson.quizCorrectIndex) {
                                                viewModel.markLessonCompleted(quizLesson.id)
                                            }
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Lemon,
                                    enabled = selectedQuizAnswer != -1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "تحلیل علمی: ${quizLesson.quizExplanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Related Book & Game Shortcut Row
                Mind3DButton(
                    text = if (chapterId == 1) "مطالعه کتاب اول (سرفصل‌های ۱۶گانه)" else "مطالعه کتاب فصل $chapterId",
                    onClick = { viewModel.navigateTo(ScreenDestination.BookReader(bookId = chapterId, pageNumber = 1)) },
                    variant = Mind3DButtonVariant.Sky,
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.fillMaxWidth()
                )

                Mind3DButton(
                    text = "اجرای بازی مرتبط با این فصل",
                    onClick = {
                        val targetGame = when (chapterId) {
                            1 -> "game_guess_obs"
                            2 -> "game_flight"
                            3 -> "game_zener"
                            4 -> "game_emotion"
                            else -> "game_symbol"
                        }
                        viewModel.navigateTo(ScreenDestination.GamePlay(targetGame))
                    },
                    variant = Mind3DButtonVariant.Orange,
                    icon = Icons.Default.SportsEsports,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
