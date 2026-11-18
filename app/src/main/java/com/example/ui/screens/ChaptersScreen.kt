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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChapterEntity
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun ChaptersScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val chapters by viewModel.chapters.collectAsState()
    val allLessons by viewModel.lessons.collectAsState()

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "مسیر آموزشی هشت‌فصلی",
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
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.2f), LemonYellowGlow)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 20.dp,
                        extrusionDepth = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = SkyBluePrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تمامی ۸ فصل و درس‌ها برای مطالعه در دسترس هستند",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary
                                    )
                                    Text(
                                        text = "هر فصل شامل درس‌های تخصصی، تکنیک گام‌به‌گام، فعالیت گروهی و آزمون علمی است.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Mind3DButton(
                                text = "نمایش سرفصل‌ها و کارت همه درس‌ها",
                                onClick = { viewModel.navigateTo(ScreenDestination.CourseSyllabus) },
                                variant = Mind3DButtonVariant.Sky,
                                icon = Icons.Default.MenuBook,
                                height = 48.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chapters_open_syllabus_btn")
                            )
                        }
                    }
                }

                items(chapters, key = { it.id }) { chapter ->
                    val chapterLessons = allLessons.filter { it.chapterId == chapter.id }
                    val completedCount = chapterLessons.count { it.isCompleted }

                    ChapterCard(
                        chapter = chapter,
                        lessonsCount = chapterLessons.size,
                        completedCount = completedCount,
                        onClick = {
                            viewModel.navigateTo(ScreenDestination.ChapterDetail(chapter.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: ChapterEntity,
    lessonsCount: Int,
    completedCount: Int,
    onClick: () -> Unit
) {
    val tone = when (chapter.id % 4) {
        1 -> Mind3DCardTone.Sky
        2 -> Mind3DCardTone.Peach
        3 -> Mind3DCardTone.Lemon
        else -> Mind3DCardTone.Mint
    }

    Mind3DCard(
        modifier = Modifier.fillMaxWidth(),
        tone = tone,
        shapeRadius = 20.dp,
        extrusionDepth = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = chapter.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (lessonsCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedCount از $lessonsCount درس تکمیل‌شده",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MintPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "در دسترس",
                    tint = MintPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
