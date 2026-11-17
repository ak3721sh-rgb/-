package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.Mind3DAvatarButton
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

/**
 * Official Instructor & Course Identity Page.
 * Reached whenever tapping the instructor portrait or official app logo.
 * Contains ONLY verified factual identity & direct official contact routes.
 */
@Composable
fun InstructorProfileScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "معرفی مدرس و طراح دوره",
                subtitle = "اکبر شیرزادخانی • هنر ارتباط ذهن‌ها",
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
                            Color(0xFFF0FDFA),
                            OrangeLightSoft.copy(alpha = 0.22f),
                            LemonYellowGlow.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Card with Instructor Studio Portrait + Official Logo
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Turquoise,
                    shapeRadius = 26.dp,
                    extrusionDepth = 5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Mind3DAvatarButton(
                                drawableRes = R.drawable.mentor_studio,
                                contentDescription = "تصویر رسمی مدرس اکبر شیرزادخانی",
                                onClick = {},
                                size = 112.dp,
                                isCircular = false,
                                cornerRadius = 24.dp,
                                borderColor = LogoGold,
                                extrusionColor = Color(0xFF9A7B1C),
                                glowColor = SkyBlueLight,
                                testTag = "instructor_profile_photo"
                            )

                            Mind3DAvatarButton(
                                drawableRes = R.drawable.official_logo,
                                contentDescription = "لوگوی رسمی دوره هنر ارتباط ذهن‌ها",
                                onClick = {},
                                size = 86.dp,
                                isCircular = true,
                                borderColor = LogoGold,
                                extrusionColor = Color(0xFF0F766E),
                                glowColor = Color(0xFF2DD4BF),
                                testTag = "instructor_profile_logo"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اکبر شیرزادخانی",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.testTag("instructor_profile_name")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.72f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "مدرس و طراح دوره «هنر ارتباط ذهن‌ها»",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "عنوان کامل مجموعه آموزشی: هنر ارتباط ذهن‌ها؛ از تله‌پاتی تا شهود و همدلی",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Verified Factual Description Card
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.WarmIvory,
                    shapeRadius = 22.dp,
                    extrusionDepth = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "درباره دوره و رویکرد آموزشی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }

                        Text(
                            text = "این برنامه محیط اختصاصی آموزش، تمرین‌های عملی، کتابخانه «کتاب‌های مدرس»، آزمون‌ها، دفترچه ثبت تجربه‌های شهودی و سنجش پیشرفت تمرکز و همدلی تحت نظارت جناب اکبر شیرزادخانی است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight,
                            lineHeight = 24.sp
                        )

                        Text(
                            text = "رویکرد دوره بر تفکیک دقیق تجربه‌های ذهنی از حدس تصادفی، تقویت توجه آگاهانه، آرام‌سازی ذهن، شنیدن فعال، ثبت صادقانه نتایج و احترام به چارچوب‌های اخلاقی و علمی استوار است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Official Contact Channels Card
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Sky,
                    shapeRadius = 22.dp,
                    extrusionDepth = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "اطلاعات تماس مستقیم مدرس",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )

                        ProfileContactRow(label = "شماره تماس مستقیم:", value = "۰۹۱۳۸۳۱۰۸۸۹")
                        ProfileContactRow(label = "ایمیل رسمی:", value = "aram96822@gmail.com")

                        Spacer(modifier = Modifier.height(4.dp))

                        Mind3DButton(
                            text = "تماس تلفنی با مدرس (۰۹۱۳۸۳۱۰۸۸۹)",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09138310889"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Emerald,
                            icon = Icons.Default.Phone,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "instructor_profile_call_btn"
                        )

                        Mind3DButton(
                            text = "ارسال پیامک به مدرس",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:09138310889"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Orange,
                            icon = Icons.Default.Sms,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "instructor_profile_sms_btn"
                        )

                        Mind3DButton(
                            text = "ارسال ایمیل به مدرس (aram96822@gmail.com)",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:aram96822@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "پیام هنرجوی دوره هنر ارتباط ذهن‌ها")
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Sky,
                            icon = Icons.Default.Email,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "instructor_profile_email_btn"
                        )
                    }
                }

                // Quick Jump Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Mind3DButton(
                        text = "کتابخانه مدرس",
                        onClick = { viewModel.navigateTo(ScreenDestination.BooksLibrary) },
                        variant = Mind3DButtonVariant.Gold,
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        height = 50.dp
                    )
                    Mind3DButton(
                        text = "صفحه ارتباط و پیام‌رسان‌ها",
                        onClick = { viewModel.navigateTo(ScreenDestination.ContactInstructor) },
                        variant = Mind3DButtonVariant.Violet,
                        icon = Icons.Default.SupportAgent,
                        modifier = Modifier.weight(1f),
                        height = 50.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileContactRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = OrangeLightPrimary)
    }
}
