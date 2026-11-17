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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveSectionSquareButton
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.SectionSquareThemes
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.SpiritualVioletSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun PortalScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val scrollState = rememberScrollState()
    val readingProgress by viewModel.readingProgress.collectAsState()

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "دنیای ذهن‌ها و آموزش‌ها",
                subtitle = "مدرس: اکبر شیرزادخانی",
                backButtonVariant = Mind3DIconVariant.Sky,
                showSettingsAction = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    Mind3DChip(
                        text = "معرفی مدرس",
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                        isSelected = true,
                        icon = Icons.Default.Person,
                        tone = Mind3DChipTone.Sky
                    )
                }
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
                            PeachSoft.copy(alpha = 0.3f),
                            SpiritualVioletSoft.copy(alpha = 0.35f),
                            ButterYellowSoft.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. RESUME FROM LAST ACTIVITY BANNER
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.WarmIvory,
                    shapeRadius = 22.dp,
                    extrusionDepth = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MintSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = MintPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ادامه از آخرین فعالیت شما",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MintPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = readingProgress?.lastActivityTitle ?: "کتاب اول: مبانی ارتباط ذهن‌ها؛ تله‌پاتی چیست؟",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Mind3DButton(
                            text = "ادامه مطالعه (صفحه ${readingProgress?.lastReadPageNumber ?: 1})",
                            onClick = {
                                viewModel.navigateTo(
                                    ScreenDestination.BookReader(
                                        bookId = readingProgress?.lastReadBookId ?: 1,
                                        pageNumber = readingProgress?.lastReadPageNumber ?: 1
                                    )
                                )
                            },
                            variant = Mind3DButtonVariant.Mint,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth(),
                            height = 48.dp
                        )
                    }
                }

                // 2. FEATURED SECTION: کتاب‌های مدرس
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Sky,
                    shapeRadius = 24.dp,
                    extrusionDepth = 4.5.dp,
                    onClick = { viewModel.navigateTo(ScreenDestination.BooksLibrary) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SkyBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "کتابخانه اختصاصی «کتاب‌های مدرس»",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary
                            )
                            Text(
                                text = "۸ کتاب آموزشی تألیفی استاد اکبر شیرزادخانی همراه با فهرست سرفصل‌ها، نشانک، یادداشت صفحه و تمرین‌ها",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryLight,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // 3. SPECIAL SPIRITUAL SECTION: تله‌پاتی الهی
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Violet,
                    shapeRadius = 24.dp,
                    extrusionDepth = 4.5.dp,
                    onClick = { viewModel.navigateTo(ScreenDestination.SpiritualSection) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SpiritualVioletPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "بخش ویژه تله‌پاتی الهی و خلوت معنوی",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SpiritualVioletPrimary
                            )
                            Text(
                                text = "آسمان آرام، نورهای لطیف، نیت خیر، دعای خالصانه، آرامش با پروردگار و ارسال مهر و تندرستی از راه دور",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryLight,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // 4. MAIN MODULES GRID (All 8 Interactive Square Buttons preserved intact)
                Text(
                    text = "بخش‌های آموزشی و مهارتی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InteractiveSectionSquareButton(
                        title = "تمرین‌های کاربردی",
                        subtitle = "فردی، دونفره و ۷ روزه",
                        icon = Icons.Default.FitnessCenter,
                        theme = SectionSquareThemes.MintSage,
                        testTag = "portal_btn_practices",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.PracticesHub) }
                    )

                    InteractiveSectionSquareButton(
                        title = "تکنیک‌های عملی",
                        subtitle = "روش‌های گام‌به‌گام تمرکز",
                        icon = Icons.Default.Lightbulb,
                        theme = SectionSquareThemes.Peach,
                        testTag = "portal_btn_techniques",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.TechniquesHub) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InteractiveSectionSquareButton(
                        title = "۸ فصل دوره",
                        subtitle = "سرفصل‌ها و آزمون‌ها",
                        icon = Icons.Default.MenuBook,
                        theme = SectionSquareThemes.SkyBlue,
                        testTag = "portal_btn_chapters",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Chapters) }
                    )

                    InteractiveSectionSquareButton(
                        title = "۱۲ بازی شناختی",
                        subtitle = "کابین خلبانی و تمرکز",
                        icon = Icons.Default.SportsEsports,
                        theme = SectionSquareThemes.Peach,
                        testTag = "portal_btn_games",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.GamesHub) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InteractiveSectionSquareButton(
                        title = "فلش‌کارت‌های لایتنر",
                        subtitle = "مرور زمان‌بندی و تسلط",
                        icon = Icons.Default.Style,
                        theme = SectionSquareThemes.LemonGold,
                        testTag = "portal_btn_flashcards",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Flashcards) }
                    )

                    InteractiveSectionSquareButton(
                        title = "دفترچه تجربه شخصی",
                        subtitle = "ثبت حدس‌ها و نتایج",
                        icon = Icons.Default.EditNote,
                        theme = SectionSquareThemes.MintSage,
                        testTag = "portal_btn_journal",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Journal) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InteractiveSectionSquareButton(
                        title = "پیشرفت و آمار",
                        subtitle = "نمودار رشد و نشان‌ها",
                        icon = Icons.Default.Timeline,
                        theme = SectionSquareThemes.Turquoise,
                        testTag = "portal_btn_stats",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.ProgressStats) }
                    )

                    InteractiveSectionSquareButton(
                        title = "اتاق مدیریت مدرس",
                        subtitle = "صدور لایسنس و کنترل",
                        icon = Icons.Default.AdminPanelSettings,
                        theme = SectionSquareThemes.SoftLilac,
                        testTag = "portal_btn_instructor",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorRoom) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InteractiveSectionSquareButton(
                        title = "دانلود و نصب APK",
                        subtitle = "نسخه رسمی و راهنما",
                        icon = Icons.Default.Download,
                        theme = SectionSquareThemes.IceCobalt,
                        testTag = "portal_btn_download",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.DownloadHub) }
                    )

                    InteractiveSectionSquareButton(
                        title = "ارتباط با مدرس",
                        subtitle = "تماس، ایتا، روبیکا...",
                        icon = Icons.Default.ContactPhone,
                        theme = SectionSquareThemes.SoftRose,
                        testTag = "portal_btn_contact",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.ContactInstructor) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
