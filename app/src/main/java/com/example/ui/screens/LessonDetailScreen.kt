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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalloutType
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.MindCalloutBox
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LessonDetailScreen(
    lessonId: Int,
    viewModel: MainViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val allLessons by viewModel.lessons.collectAsState()
    val lesson = allLessons.find { it.id == lessonId }
    val interaction = LocalInteractionController.current
    val readerSize = interaction.settings.readerFontSizeSp

    var selectedOption by remember(lessonId) { mutableIntStateOf(-1) }
    var submitted by remember(lessonId) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = lesson?.title ?: "جزئیات درس",
                subtitle = "فصل ${lesson?.chapterId ?: 1} • مدرس: اکبر شیرزادخانی",
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
            if (lesson == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("در حال بارگذاری درس...", color = TextPrimaryLight)
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (typeLabel, typeTone) = when (lesson.contentType) {
                                    "TECHNIQUE" -> "تکنیک عملی" to Mind3DChipTone.Peach
                                    "EXERCISE" -> "تمرین کاربردی" to Mind3DChipTone.Mint
                                    else -> "مفهوم پایه" to Mind3DChipTone.Sky
                                }
                                Mind3DChip(
                                    text = typeLabel,
                                    onClick = {},
                                    isSelected = true,
                                    tone = typeTone
                                )
                                if (lesson.isCompleted) {
                                    Mind3DChip(
                                        text = "تکمیل‌شده ✓",
                                        onClick = {},
                                        isSelected = true,
                                        icon = Icons.Default.CheckCircle,
                                        tone = Mind3DChipTone.Mint
                                    )
                                }
                            }

                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                            Text(
                                text = lesson.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = readerSize.sp,
                                    lineHeight = (readerSize * 1.68f).sp
                                ),
                                color = TextPrimaryLight
                            )
                        }
                    }

                    if (lesson.example.isNotBlank()) {
                        MindCalloutBox(
                            title = "مثال ملموس",
                            content = lesson.example,
                            type = CalloutType.Reflection
                        )
                    }

                    if (lesson.stepTechnique.isNotBlank()) {
                        MindCalloutBox(
                            title = "تکنیک گام‌به‌گام",
                            content = lesson.stepTechnique,
                            type = CalloutType.Technique
                        )
                    }

                    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Apricot) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SkyBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تمرین فردی", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                            }
                            Text(lesson.soloExercise, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = OrangeLightPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تمرین دونفره / گروهی", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                            }
                            Text(lesson.pairExercise, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                        }
                    }

                    if (lesson.quizQuestion.isNotBlank()) {
                        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MintPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ارزیابی سریع درس", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(lesson.quizQuestion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                                val options = listOf(lesson.quizOption1, lesson.quizOption2, lesson.quizOption3, lesson.quizOption4)
                                options.forEachIndexed { idx, opt ->
                                    if (opt.isNotBlank()) {
                                        val isSelected = selectedOption == idx
                                        val isCorrect = idx == lesson.quizCorrectIndex
                                        val optionTone = when {
                                            submitted && isCorrect -> Mind3DCardTone.Mint
                                            submitted && isSelected && !isCorrect -> Mind3DCardTone.Peach
                                            isSelected -> Mind3DCardTone.Sky
                                            else -> Mind3DCardTone.WarmIvory
                                        }
                                        Mind3DCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            tone = optionTone,
                                            shapeRadius = 14.dp,
                                            extrusionDepth = if (isSelected) 2.dp else 3.5.dp,
                                            borderWidth = if (isSelected) 2.dp else 1.dp,
                                            onClick = if (!submitted) {
                                                { selectedOption = idx }
                                            } else null
                                        ) {
                                            Text(
                                                text = opt,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (submitted && isCorrect) Color(0xFF1B5E20) else TextPrimaryLight,
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }
                                    }
                                }

                                if (!submitted) {
                                    Mind3DButton(
                                        text = "بررسی پاسخ",
                                        onClick = { submitted = true },
                                        variant = Mind3DButtonVariant.Lemon,
                                        enabled = selectedOption != -1,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else if (lesson.quizExplanation.isNotBlank()) {
                                    Text(
                                        text = "تحلیل: ${lesson.quizExplanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (lesson.summary.isNotBlank()) {
                        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Violet) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SpiritualVioletPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("جمع‌بندی درس", fontWeight = FontWeight.Bold, color = SpiritualVioletPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(lesson.summary, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryLight)
                            }
                        }
                    }

                    Mind3DButton(
                        text = if (lesson.isCompleted) "این درس تکمیل شده است ✓" else "ثبت تکمیل درس (+۳۵ XP)",
                        onClick = { viewModel.markLessonCompleted(lesson.id) },
                        variant = Mind3DButtonVariant.Emerald,
                        enabled = !lesson.isCompleted,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
