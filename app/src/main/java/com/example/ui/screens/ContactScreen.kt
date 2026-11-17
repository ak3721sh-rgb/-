package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun ContactScreen(viewModel: MainViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "ارتباط مستقیم با مدرس",
                subtitle = "مدرس: اکبر شیرزادخانی",
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
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.25f), LemonYellowGlow)
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
                // High-Res Studio Portrait Card + Interactive Logo
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Sky,
                    shapeRadius = 24.dp,
                    extrusionDepth = 4.5.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Mind3DAvatarButton(
                                drawableRes = R.drawable.mentor_studio,
                                contentDescription = "اکبر شیرزادخانی - معرفی مدرس",
                                onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                                size = 132.dp,
                                isCircular = false,
                                cornerRadius = 24.dp,
                                borderColor = LogoGold,
                                extrusionColor = Color(0xFF9A7B1C),
                                glowColor = SkyBlueLight,
                                testTag = "contact_mentor_photo"
                            )

                            Mind3DAvatarButton(
                                drawableRes = R.drawable.official_logo,
                                contentDescription = "نشان رسمی هنر ارتباط ذهن‌ها",
                                onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                                size = 76.dp,
                                isCircular = true,
                                borderColor = LogoGold,
                                extrusionColor = Color(0xFF0369A1),
                                glowColor = SkyBlueLight,
                                testTag = "contact_official_logo"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "اکبر شیرزادخانی",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )

                        Text(
                            text = "مدرس و طراح اپلیکیشن «هنر ارتباط ذهن‌ها»",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "تلفن تماس مستقیم: ۰۹۱۳۸۳۱۰۸۸۹",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeLightPrimary
                        )

                        Text(
                            text = "ایمیل رسمی: aram96822@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryLight
                        )
                    }
                }

                // Slogan Banner
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.LemonChiffon,
                    shapeRadius = 18.dp,
                    extrusionDepth = 3.dp
                ) {
                    Text(
                        text = "« از تمرکز و شهود تا همدلی و شناخت ارتباط انسانی »\nپاسخگوی تمامی پرسش‌های علمی، دریافت کلید فعال‌سازی و مشاوره تخصصی هنرجویان.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }

                // 3D Communication Buttons
                Text(
                    text = "مسیرهای ارتباطی مستقیم:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Mind3DButton(
                        text = "تماس تلفنی با مدرس (۰۹۱۳۸۳۱۰۸۸۹)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09138310889"))
                            context.startActivity(intent)
                        },
                        variant = Mind3DButtonVariant.Emerald,
                        icon = Icons.Default.Phone,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Mind3DButton(
                        text = "ارسال پیامک به مدرس (درخواست کلید یا مشاوره)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:09138310889"))
                            context.startActivity(intent)
                        },
                        variant = Mind3DButtonVariant.Orange,
                        icon = Icons.Default.Sms,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Mind3DButton(
                        text = "ارسال ایمیل به مدرس (aram96822@gmail.com)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:aram96822@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "پیام هنرجوی دوره هنر ارتباط ذهن‌ها")
                            }
                            context.startActivity(intent)
                        },
                        variant = Mind3DButtonVariant.Sky,
                        icon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Mind3DButton(
                        text = "ارتباط در پیام‌رسان ایتا (Eitaa)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://eitaa.com/"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        variant = Mind3DButtonVariant.Orange,
                        icon = Icons.Default.Message,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Mind3DButton(
                        text = "ارتباط در پیام‌رسان روبیکا (Rubika)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rubika.ir/"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        variant = Mind3DButtonVariant.Lemon,
                        icon = Icons.Default.Public,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Mind3DButton(
                        text = "ارتباط در واتساپ (WhatsApp)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/989138310889"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        variant = Mind3DButtonVariant.Emerald,
                        icon = Icons.Default.Message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
