package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.SpiritualVioletSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

/**
 * Dedicated screen explaining the vision, structure, scientific boundaries,
 * and authoring identity of the course "هنر ارتباط ذهن‌ها".
 */
@Composable
fun AboutCourseScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "درباره دوره هنر ارتباط ذهن‌ها",
                subtitle = "ساختار علمی و آموزشی • تألیف اکبر شیرزادخانی",
                onBackClick = { viewModel.navigateBack() },
                backButtonVariant = Mind3DIconVariant.Sky,
                showSettingsAction = true,
                onInstructorBadgeClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF6FBFF), Color(0xFFFFFFFF), Color(0xFFF5F3FF))
                    )
                )
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Introduction Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE0F2FE), Color(0xFFECFDF5), Color(0xFFFEF9C3))
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.official_logo),
                                contentDescription = "نشان رسمی دوره هنر ارتباط ذهن‌ها",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, LogoGold, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "دوره جامع «هنر ارتباط ذهن‌ها»",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "تألیف و تدریس: اکبر شیرزادخانی",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0C4A6E)
                                )
                            }
                        }
                        Text(
                            text = "این برنامه یک محیط آموزشی ساختاریافته برای تقویت توجه، آرام‌سازی ذهن، همدلی عاطفی، شهود آگاهانه و ثبت دقیق تجربه‌های تمرین‌های ذهنی است تا پیشرفت شما بر پایه تمرین منظم و مشاهدهٔ واقعی شکل بگیرد.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 25.sp,
                            color = TextPrimaryLight
                        )
                    }
                }

                // Course Modules Structure
                AboutHighlightCard(
                    title = "ساختار آموزشی و مسیر گام‌به‌گام",
                    accentBg = SkyBlueSoft,
                    iconTint = SkyBluePrimary,
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    bullets = listOf(
                        "۸ کتاب و جزوهٔ دسته‌بندی‌شده با علامت‌گذاری روشن بین متن تألیفی مدرس، محتوای تکمیلی علمی و بخش‌های در حال تکمیل.",
                        "۱۶ سرفصل آموزشی تعاملی همراه با مثال، تمرین فردی، تمرین دونفره و آزمون کوتاه تثبیت یادگیری.",
                        "تمرین‌های عملی، تکنیک‌های تمرکز، مدیتیشن و ثبت ۷ روزهٔ پیش‌بینی‌ها با قابلیت قفل متن پیش از رخداد.",
                        "مجموعه بازی‌های تمرکز، حافظهٔ دیداری و هماهنگی از جمله «پرواز تمرکز خلبانی» برای سنجش چابکی ذهن."
                    )
                )

                // Scientific & Ethical Framing
                AboutHighlightCard(
                    title = "چارچوب علمی، مسئولانه و واقع‌گرایانه",
                    accentBg = SpiritualVioletSoft,
                    iconTint = SpiritualVioletPrimary,
                    icon = Icons.Default.Shield,
                    bullets = listOf(
                        "تمام تمرین‌ها و بازی‌های این برنامه جنبهٔ آموزشی، تمرینی و شناختی دارند و جایگزین ارزیابی بالینی، مشاورهٔ پزشکی یا روان‌درمانی نیستند.",
                        "در بخش‌های شناختی میان پدیده‌های قابل بررسی علمی (مانند توجه انتخابی، خوانش نشانه‌های غیرکلامی و همدلی) و دیدگاه‌های پژوهشی تله‌پاتی تفکیک روشن انجام می‌شود.",
                        "داده‌های مطالعه، نشانک‌ها، یادداشت‌ها، فلش‌کارت‌ها و گزارش‌های تمرینی به‌صورت محلی در دستگاه شما نگهداری می‌شوند."
                    )
                )

                // Learning Outcomes
                AboutHighlightCard(
                    title = "دستاوردهای اصلی هنرجویان",
                    accentBg = MintSoft,
                    iconTint = MintPrimary,
                    icon = Icons.Default.SelfImprovement,
                    bullets = listOf(
                        "افزایش پایداری تمرکز و کاهش پراکندگی حواس در موقعیت‌های روزمره",
                        "بهبود هوش عاطفی، گوش‌دادن فعال و درک عمیق‌تر حالات دیگران",
                        "تفاوت‌گذاری دقیق بین شهود واقعی، سوگیری حافظه و حدس‌های هیجانی"
                    )
                )

                // Instructor Card & Action Buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PeachSoft.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = PeachPrimary)
                            Text(
                                text = "معرفی مدرس و دسترسی مستقیم",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                        Text(
                            text = "برای آشنایی با سوابق آموزشی، مسیر طراحی دوره و دریافت کد فعال‌سازی یا راهنمایی از استاد اکبر شیرزادخانی، از دکمه‌های زیر استفاده کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 23.sp,
                            color = TextSecondaryLight
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Mind3DButton(
                                text = "معرفی مدرس",
                                onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                                variant = Mind3DButtonVariant.Gold,
                                icon = Icons.Default.Psychology,
                                testTag = "about_instructor_btn",
                                modifier = Modifier.weight(1f)
                            )
                            Mind3DButton(
                                text = "شروع سرفصل‌ها",
                                onClick = { viewModel.navigateTo(ScreenDestination.Chapters) },
                                variant = Mind3DButtonVariant.Mint,
                                icon = Icons.Default.SportsEsports,
                                testTag = "about_chapters_btn",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Mind3DButton(
                            text = "ورود به کتابخانه و جزوه‌ها",
                            onClick = { viewModel.navigateTo(ScreenDestination.BooksLibrary) },
                            variant = Mind3DButtonVariant.Sky,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            testTag = "about_library_btn",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutHighlightCard(
    title: String,
    accentBg: Color,
    iconTint: Color,
    icon: ImageVector,
    bullets: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }
            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(16.dp)
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = TextPrimaryLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
