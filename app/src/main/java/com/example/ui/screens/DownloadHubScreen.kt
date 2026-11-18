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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.sync.AppUpdateManager
import com.example.data.sync.UpdateCheckResult
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
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UpdateCheckUiState

@Composable
fun DownloadHubScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val metadata by viewModel.contentMetadata.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()
    val configuredUrl = metadata?.apkDownloadUrl?.trim().orEmpty()
    val hasValidUrl = AppUpdateManager.isValidPublicHttpsUrl(configuredUrl)
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "به‌روزرسانی و دریافت نسخه",
                subtitle = "نسخه ${BuildConfig.VERSION_NAME} (کد ${BuildConfig.VERSION_CODE}) • مدرس: اکبر شیرزادخانی",
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
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Mind3DAvatarButton(
                    drawableRes = R.drawable.official_logo,
                    contentDescription = "نشان رسمی هنر ارتباط ذهن‌ها — نمایش معرفی مدرس",
                    onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                    size = 96.dp,
                    isCircular = true,
                    borderColor = LogoGold,
                    extrusionColor = Color(0xFF9A7B1C),
                    glowColor = SkyBlueLight,
                    testTag = "download_official_logo"
                )

                Text(
                    text = "هنر ارتباط ذهن‌ها",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )

                Text(
                    text = "MIND CONNECTION • مدرس: اکبر شیرزادخانی",
                    style = MaterialTheme.typography.labelMedium,
                    color = LogoGold,
                    fontWeight = FontWeight.Bold
                )

                // Live Version Check & Play-Compliant Browser Update Card
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = when (val state = updateCheckState) {
                        is UpdateCheckUiState.Result -> when (state.result) {
                            is UpdateCheckResult.UpdateAvailable -> Mind3DCardTone.Mint
                            is UpdateCheckResult.UpToDate -> Mind3DCardTone.Sky
                            is UpdateCheckResult.Unavailable -> Mind3DCardTone.Lemon
                        }
                        else -> if (hasValidUrl) Mind3DCardTone.Mint else Mind3DCardTone.Lemon
                    },
                    shapeRadius = 22.dp,
                    extrusionDepth = 4.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasValidUrl) Icons.Default.Security else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (hasValidUrl) MintPrimary else Color(0xFFB45309),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasValidUrl) "مسیر رسمی دریافت و به‌روزرسانی آماده است" else "لینک دانلود عمومی هنوز منتشر نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }

                        DetailRow("نسخه نصب‌شده روی گوشی:", "نسخه ${BuildConfig.VERSION_NAME} (کد ${BuildConfig.VERSION_CODE})")
                        DetailRow("نسخه محتوای آفلاین:", metadata?.contentVersion ?: "1.4.0")
                        DetailRow("سازگاری با اندروید:", "اندروید ۸ تا ۱۶ (منطبق با Play Policy)")

                        Mind3DButton(
                            text = if (updateCheckState is UpdateCheckUiState.Checking) "در حال بررسی نسخه جدید..." else "بررسی آنلاین نسخه جدید",
                            onClick = { viewModel.checkForAppUpdate() },
                            variant = Mind3DButtonVariant.Sky,
                            icon = Icons.Default.Refresh,
                            enabled = updateCheckState !is UpdateCheckUiState.Checking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("download_check_update_btn")
                        )

                        when (val checkState = updateCheckState) {
                            is UpdateCheckUiState.Checking -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.78f))
                                        .padding(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SkyBluePrimary)
                                    Text("در حال بررسی وضعیت نسخه از سرور...", style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                                }
                            }
                            is UpdateCheckUiState.Result -> {
                                when (val result = checkState.result) {
                                    is UpdateCheckResult.UpdateAvailable -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.85f))
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MintPrimary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "نسخه جدید ${result.manifest.versionName} (کد ${result.manifest.versionCode}) موجود است",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MintPrimary
                                                )
                                            }
                                            Text(result.manifest.changelog, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                                            if (result.manifest.releaseDate.isNotBlank()) {
                                                Text("تاریخ انتشار: ${result.manifest.releaseDate}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                            }
                                            if (result.manifest.sha256.isNotBlank()) {
                                                Text("SHA-256: ${result.manifest.sha256}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                                            }
                                            Mind3DButton(
                                                text = "دریافت نسخه جدید در مرورگر (HTTPS)",
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.manifest.apkUrl))
                                                    try {
                                                        context.startActivity(intent)
                                                        actionFeedback = "دانلود آغاز شد — فایل در مرورگر باز شد و نصب فقط با تأیید شما انجام می‌شود."
                                                    } catch (_: Exception) {
                                                        actionFeedback = "مرورگر مناسبی برای باز کردن لینک پیدا نشد."
                                                    }
                                                },
                                                variant = Mind3DButtonVariant.Emerald,
                                                icon = Icons.Default.Download,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    is UpdateCheckResult.UpToDate -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.85f))
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SkyBluePrimary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(result.message, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                            }
                                            if (!result.directApkUrl.isNullOrBlank()) {
                                                Mind3DButton(
                                                    text = "دانلود مستقیم فایل APK در مرورگر",
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.directApkUrl))
                                                        try {
                                                            context.startActivity(intent)
                                                            actionFeedback = "دانلود مستقیم آغاز شد — فایل در مرورگر باز شد."
                                                        } catch (_: Exception) {
                                                            actionFeedback = "مرورگر مناسبی برای باز کردن لینک پیدا نشد."
                                                        }
                                                    },
                                                    variant = Mind3DButtonVariant.Mint,
                                                    icon = Icons.Default.Download,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("download_verified_apk_btn")
                                                )
                                            }
                                        }
                                    }
                                    is UpdateCheckResult.Unavailable -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.85f))
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = result.reason,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 20.sp
                                            )
                                            if (!result.fallbackWebUrl.isNullOrBlank()) {
                                                Mind3DButton(
                                                    text = "باز کردن صفحه انتشارهای گیت‌هاب (Releases)",
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.fallbackWebUrl))
                                                        try {
                                                            context.startActivity(intent)
                                                            actionFeedback = "صفحه انتشارهای گیت‌هاب باز شد."
                                                        } catch (_: Exception) {
                                                            actionFeedback = "مرورگر مناسبی برای باز کردن لینک پیدا نشد."
                                                        }
                                                    },
                                                    variant = Mind3DButtonVariant.Gold,
                                                    icon = Icons.Default.Public,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            else -> Unit
                        }

                        if (actionFeedback != null) {
                            Text(
                                text = actionFeedback!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasValidUrl) {
                            val checkedUnavailable = (updateCheckState as? UpdateCheckUiState.Result)?.result is UpdateCheckResult.Unavailable
                            if (!checkedUnavailable) {
                                Mind3DButton(
                                    text = "بررسی سلامت لینک و دانلود سریع",
                                    onClick = {
                                        actionFeedback = null
                                        viewModel.checkForAppUpdate()
                                    },
                                    variant = Mind3DButtonVariant.Mint,
                                    icon = Icons.Default.Download,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Mind3DButton(
                                text = "کپی آدرس دانلود برای ارسال",
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(configuredUrl))
                                    actionFeedback = "آدرس دانلود در حافظه کپی شد."
                                },
                                variant = Mind3DButtonVariant.Neutral,
                                icon = Icons.Default.ContentCopy,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "برای حفظ شفافیت و امنیت، هیچ لینک آزمایشی نمایش داده نمی‌شود. مدرس می‌تواند نشانی HTTPS عمومی فایل یا مانیفست JSON به‌روزرسانی را در «اتاق مدیریت مدرس ← انتشار APK و امنیت» ثبت کند تا دانلود امن از طریق مرورگر گوشی انجام شود.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 20.sp
                            )
                            Mind3DButton(
                                text = "تنظیم نشانی دانلود در اتاق مدرس",
                                onClick = { viewModel.navigateTo(ScreenDestination.InstructorRoom) },
                                variant = Mind3DButtonVariant.Gold,
                                icon = Icons.Default.AdminPanelSettings,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = Mind3DCardTone.Apricot,
                    shapeRadius = 20.dp,
                    extrusionDepth = 3.5.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = OrangeLightPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "راهنمای به‌روزرسانی بدون حذف داده‌ها",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val steps = listOf(
                            "۱. پس از دریافت فایل APK نسخه جدید از مرورگر، روی فایل دانلودشده در پوشه Downloads لمس کنید.",
                            "۲. به دلیل یکسان بودن امضا و شناسه بسته (com.aistudio.mindconnection.akbar)، تمام یادداشت‌ها، دفتر تجربه و پیشرفت شما بدون تغییر حفظ می‌شود.",
                            "۳. در گوشی‌های سامسونگ (One UI) در صورت نیاز، مجوز مرورگر را تأیید و گزینه Update (به‌روزرسانی) را لمس کنید."
                        )
                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
    }
}
