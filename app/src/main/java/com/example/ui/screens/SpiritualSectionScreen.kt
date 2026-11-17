package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.theme.ButterYellowGlow
import com.example.ui.theme.ButterYellowLight
import com.example.ui.theme.ButterYellowPrimary
import com.example.ui.theme.LogoGold
import com.example.ui.theme.LogoGoldLight
import com.example.ui.theme.SpiritualVioletAura
import com.example.ui.theme.SpiritualVioletLight
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.SpiritualVioletSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LocalInteractionController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritualSectionScreen(viewModel: MainViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val scrollState = rememberScrollState()
    val interaction = LocalInteractionController.current
    val readerSize = interaction.settings.readerFontSizeSp

    val auraScale = if (interaction.effectiveReducedMotion) {
        1f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "celestial_aura")
        val animVal by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura_pulse"
        )
        animVal
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "تله‌پاتی الهی و تجربه‌های معنوی",
                subtitle = "خلوت روحانی، نیت خیر و اتصال به سرچشمه آفرینش",
                backButtonVariant = Mind3DIconVariant.Violet,
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
                        listOf(
                            Color(0xFFEDE9FE),
                            Color(0xFFE0F2FE),
                            Color(0xFFFEF3C7).copy(alpha = 0.7f),
                            Color(0xFFFDF4FF)
                        )
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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Celestial Light Halo Emblem
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(auraScale)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.White,
                                    ButterYellowGlow,
                                    LogoGoldLight,
                                    SpiritualVioletLight.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(2.5.dp, LogoGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "نور معنوی",
                        tint = SpiritualVioletPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Text(
                    text = "« هُوَ مَعَكُمْ أَيْنَ مَا كُنْتُمْ »",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SpiritualVioletPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "او با شماست، هر جا که باشید — حضور پیوسته رحمت و نور الهی در دل مؤمنان",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center
                )

                // Philosophical & Spiritual Foundations Card
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Violet,
                    shapeRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SpiritualVioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = SpiritualVioletPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مفهوم تله‌پاتی الهی در نگاه مدرس",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpiritualVioletPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "تله‌پاتی الهی، ادعای کشف اسرار پنهان جهان یا توانایی غیبی شخصی نیست؛ بلکه بالاترین مرتبه آرامش روانی و تسلیم در برابر اراده و حکمت خداوند است.\n\nهنگامی که انسان ذهن خود را از کینه، حسد، قضاوت و حرص پاک می‌سازد، ارتعاشات قلب او با فرکانس مهر، نیایش و هدایت الهی هم‌نوا می‌شود. در این خلوت معنوی، الهاماتی سرشار از صلح و روشن‌بینی در جان انسان پدیدار می‌گردد.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = readerSize.sp,
                                lineHeight = (readerSize * 1.65f).sp
                            ),
                            color = TextPrimaryLight
                        )
                    }
                }

                // Practice 1: Goodwill Intention (تمرین نیت خیر و دعای خالصانه از راه دور)
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Rose,
                    shapeRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تکنیک پاکت نورانی مهربانی (حمایت عاطفی از راه دور)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val steps = listOf(
                            "۱. در محیطی آرام و بدون مزاحمت بنشینید و ۳ تنفس ۴-۷-۸ انجام دهید.",
                            "۲. چهره فردی که نیاز به آرامش، شفا یا محبت دارد (دوست، والدین، بیمار) را با لبخند مجسم کنید.",
                            "۳. هاله‌ای از نور طلایی و رحمت الهی را دور او تصور کنید و با تمام وجود آرزوی سلامت و آرامش قلبی نمایید.",
                            "۴. هیچ وابستگی یا اصراری به نتیجه فوری نداشته باشید؛ پیام را با حس سپاسگزاری به دست خدا بسپارید.",
                            "۵. احساس سبکی و رهایی عمیق در قفسه سینه خود را حس کرده و لبخند بزنید."
                        )

                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp
                                ),
                                color = TextPrimaryLight,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // Practice 2: Spiritual Silence Meditation (مراقبه سکوت و تماشای رحمت)
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Gold,
                    shapeRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = LogoGold,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مراقبه سکوت و آرامش قلبی با پروردگار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpiritualVioletPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "به مدت ۱۰ دقیقه هرگونه گفتگوی ذهنی درونی را متوقف کنید. مانند آیینه‌ای شفاف، فقط حضور خدا را در تک‌تک سلول‌های بدن حس کنید. هر زمان فکری آمد، بدون درگیری بگویید «یا رحمان، یا رحیم» و دوباره به سکوت بازگردید.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = readerSize.sp,
                                lineHeight = (readerSize * 1.62f).sp
                            ),
                            color = TextPrimaryLight
                        )
                    }
                }

                // Action Buttons
                Mind3DButton(
                    text = "مطالعه کتاب پنجم (شهود و تجربه‌های معنوی)",
                    onClick = { viewModel.navigateTo(ScreenDestination.BookReader(bookId = 5, pageNumber = 1)) },
                    variant = Mind3DButtonVariant.Violet,
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.fillMaxWidth()
                )

                Mind3DButton(
                    text = "ثبت الهام یا تجربه معنوی در دفترچه",
                    onClick = { viewModel.navigateTo(ScreenDestination.Journal) },
                    variant = Mind3DButtonVariant.Gold,
                    icon = Icons.Default.EditNote,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
