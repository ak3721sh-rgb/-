package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.R
import com.example.data.sync.UpdateCheckResult
import com.example.ui.components.Mind3DAvatarButton
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachLight
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletLight
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.SpiritualVioletSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TurquoisePrimary
import com.example.ui.theme.TurquoiseSoft
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UpdateCheckUiState

data class InspirationalCardItem(
    val id: Int,
    val text: String,
    val source: String,
    val bgGradient: List<Color>,
    val borderColor: Color
)

private data class HomeQuickSectionItem(
    val title: String,
    val icon: ImageVector,
    val tone: Mind3DCardTone,
    val tint: Color,
    val testTag: String,
    val destination: ScreenDestination
)

private val SplashInspirationalCards = listOf(
    InspirationalCardItem(
        id = 1,
        text = "« دل می‌رود ز دستم صاحب‌دلان خدا را / دردا که راز پنهان خواهد شد آشکارا »",
        source = "حافظ شیرازی",
        bgGradient = listOf(SpiritualVioletSoft, SkyBlueSoft),
        borderColor = SpiritualVioletLight
    ),
    InspirationalCardItem(
        id = 2,
        text = "« ای برادر تو همان اندیشه‌ای / مابقی تو استخوان و ریشه‌ای »",
        source = "مولانا جلال‌الدین بلخی",
        bgGradient = listOf(MintSoft, SkyBlueSoft),
        borderColor = Color(0xFF4ADE80)
    ),
    InspirationalCardItem(
        id = 3,
        text = "« بنی‌آدم اعضای یک پیکرند / که در آفرینش ز یک گوهرند »",
        source = "سعدی شیرازی",
        bgGradient = listOf(PeachSoft, ButterYellowSoft),
        borderColor = PeachLight
    ),
    InspirationalCardItem(
        id = 4,
        text = "« همدلی از همزبانی بهتر است؛ پیوند دل‌ها زبانی فراتر از کلمات دارد. »",
        source = "روان‌شناسی شناختی و ارتباط انسانی",
        bgGradient = listOf(TurquoiseSoft, SkyBlueSoft),
        borderColor = Color(0xFF2DD4BF)
    ),
    InspirationalCardItem(
        id = 5,
        text = "« شهود، پردازش سریع و بی‌صدای خرد ناهشیار در بستر آرامش درونی است. »",
        source = "اکبر شیرزادخانی",
        bgGradient = listOf(ButterYellowSoft, PeachSoft),
        borderColor = LogoGold
    ),
    InspirationalCardItem(
        id = 6,
        text = "« هفت شهر عشق را عطار گشت / ما هنوز اندر خم یک کوچه‌ایم »",
        source = "عطار نیشابوری",
        bgGradient = listOf(SpiritualVioletSoft, PeachSoft),
        borderColor = SpiritualVioletLight
    ),
    InspirationalCardItem(
        id = 7,
        text = "« مشاهده دقیق، آغاز دانایی است؛ میان آنچه می‌بینید و آنچه می‌پندارید مرزی روشن بکشید. »",
        source = "روان‌شناسی شناخت",
        bgGradient = listOf(SkyBlueSoft, MintSoft),
        borderColor = SkyBlueLight
    ),
    InspirationalCardItem(
        id = 8,
        text = "« آرامش کانون توجه، طوفان‌های درونی اضطراب را مهار می‌کند. »",
        source = "مهارت‌های آرام‌سازی ذهن",
        bgGradient = listOf(MintSoft, ButterYellowSoft),
        borderColor = Color(0xFF16A34A)
    ),
    InspirationalCardItem(
        id = 9,
        text = "« نیت خیر و دعای خالصانه، زیباترین فرکانسی است که انسان می‌تواند به سوی دیگران ارسال کند. »",
        source = "تله‌پاتی الهی و ارتباط معنوی",
        bgGradient = listOf(SpiritualVioletSoft, ButterYellowSoft),
        borderColor = LogoGold
    ),
    InspirationalCardItem(
        id = 10,
        text = "« گوش دادن در سکوت و با تمام وجود، گرانبهاترین هدیه‌ای است که به یک انسان می‌بخشید. »",
        source = "اصول همدلی و پیوند ذهن‌ها",
        bgGradient = listOf(PeachSoft, TurquoiseSoft),
        borderColor = PeachLight
    ),
    InspirationalCardItem(
        id = 11,
        text = "« تن ز جان و جان ز تن مستور نیست / لیک کس را دید جان دستور نیست »",
        source = "مولانا",
        bgGradient = listOf(TurquoiseSoft, MintSoft),
        borderColor = Color(0xFF0D9488)
    ),
    InspirationalCardItem(
        id = 12,
        text = "« در سکوت ذهن، ندایی آرام طنین‌انداز می‌شود که در هیاهوی افکار ناپیدا بود. »",
        source = "آموزه‌های تمرکز و شهود",
        bgGradient = listOf(SkyBlueSoft, SpiritualVioletSoft),
        borderColor = SkyBlueLight
    ),
    InspirationalCardItem(
        id = 13,
        text = "« بشنو این نکته که خود را ز غم آزاده کنی / خون خوری گر طلب روزی ننهاده کنی »",
        source = "حافظ",
        bgGradient = listOf(ButterYellowSoft, MintSoft),
        borderColor = LogoGold
    ),
    InspirationalCardItem(
        id = 14,
        text = "« هر کلمه بذری در روان دیگری است؛ مراقب باشیم دانه‌های مهر و آرامش بکاریم. »",
        source = "روان‌شناسی ارتباط",
        bgGradient = listOf(PeachSoft, SpiritualVioletSoft),
        borderColor = PeachLight
    ),
    InspirationalCardItem(
        id = 15,
        text = "« چشمانت را ببند تا جهانی گسترده‌تر از دیدگان را با چشم دل تماشا کنی. »",
        source = "تجسم خلاق",
        bgGradient = listOf(SpiritualVioletSoft, TurquoiseSoft),
        borderColor = SpiritualVioletLight
    ),
    InspirationalCardItem(
        id = 16,
        text = "« امواج آلفای مغزی، دروازه هماهنگی تمرکز عمیق، شهود و تندرستی است. »",
        source = "علوم اعصاب شناختی",
        bgGradient = listOf(MintSoft, SkyBlueSoft),
        borderColor = Color(0xFF16A34A)
    ),
    InspirationalCardItem(
        id = 17,
        text = "« دوستی حقیقی پیوندی است که گذشت زمان و فاصله جغرافیایی آن را خاموش نمی‌کند. »",
        source = "پیوندهای پایدار انسانی",
        bgGradient = listOf(PeachSoft, ButterYellowSoft),
        borderColor = LogoGold
    ),
    InspirationalCardItem(
        id = 18,
        text = "« آیینه‌ای شو پاک و بی‌غبار، تا حقیقت وجود دیگران بی‌هیچ کجی در تو متجلی شود. »",
        source = "حکمت کهن",
        bgGradient = listOf(TurquoiseSoft, SkyBlueSoft),
        borderColor = Color(0xFF2DD4BF)
    ),
    InspirationalCardItem(
        id = 19,
        text = "« گر برکنم دل از تو و بردارم از تو مهر / آن مهر بر که افکنم آن دل کجا برم »",
        source = "سعدی",
        bgGradient = listOf(SpiritualVioletSoft, PeachSoft),
        borderColor = SpiritualVioletLight
    ),
    InspirationalCardItem(
        id = 20,
        text = "« خلوت معنوی، پالایشگاه روان از غبارهای اضطراب و سردرگمی است. »",
        source = "شهود و معنویت",
        bgGradient = listOf(SkyBlueSoft, ButterYellowSoft),
        borderColor = SkyBlueLight
    ),
    InspirationalCardItem(
        id = 21,
        text = "« ارتباط ذهن‌ها با احترام به حریم مقدس استقلال و آزادی دیگران آغاز می‌شود. »",
        source = "اخلاق ارتباط ذهن‌ها",
        bgGradient = listOf(MintSoft, PeachSoft),
        borderColor = Color(0xFF16A34A)
    ),
    InspirationalCardItem(
        id = 22,
        text = "« صبر و تکرار آرام تمرین‌ها، ریشه تسلط بر کانون توجه است. »",
        source = "روان‌شناسی تمرکز",
        bgGradient = listOf(ButterYellowSoft, TurquoiseSoft),
        borderColor = LogoGold
    ),
    InspirationalCardItem(
        id = 23,
        text = "« از مشاهده عینی آغاز کن تا به درک شهودی و همدلی بدون قضاوت برسی. »",
        source = "اکبر شیرزادخانی",
        bgGradient = listOf(SkyBlueSoft, PeachSoft),
        borderColor = SkyBluePrimary
    ),
    InspirationalCardItem(
        id = 24,
        text = "« دل هر ذره را که بشکافی / آفتابیش در میان بینی »",
        source = "هاتف اصفهانی",
        bgGradient = listOf(SpiritualVioletSoft, MintSoft),
        borderColor = SpiritualVioletLight
    ),
    InspirationalCardItem(
        id = 25,
        text = "« از تمرکز و شهود تا همدلی، سفری شگفت‌انگیز از شناخت خود به سوی مهرورزی است. »",
        source = "هنر ارتباط ذهن‌ها",
        bgGradient = listOf(ButterYellowSoft, SkyBlueSoft),
        borderColor = LogoGold
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplashScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val interaction = LocalInteractionController.current
    val scrollState = rememberScrollState()
    val metadata by viewModel.contentMetadata.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()

    var currentCardIndex by remember { mutableIntStateOf(0) }
    var quoteExpanded by remember(currentCardIndex) { mutableStateOf(false) }
    val currentCard = SplashInspirationalCards[currentCardIndex % SplashInspirationalCards.size]

    var showUpdateDialog by remember { mutableStateOf(false) }

    val homeQuickSections = remember {
        listOf(
            HomeQuickSectionItem("کتابخانه", Icons.Default.LibraryBooks, Mind3DCardTone.Sky, SkyBluePrimary, "home_sec_library", ScreenDestination.BooksLibrary),
            HomeQuickSectionItem("تمرین‌ها", Icons.Default.FitnessCenter, Mind3DCardTone.Mint, MintPrimary, "home_sec_practices", ScreenDestination.PracticesHub),
            HomeQuickSectionItem("تکنیک‌ها", Icons.Default.Lightbulb, Mind3DCardTone.Peach, PeachPrimary, "home_sec_techniques", ScreenDestination.TechniquesHub),
            HomeQuickSectionItem("تله‌پاتی الهی", Icons.Default.AutoAwesome, Mind3DCardTone.Violet, SpiritualVioletPrimary, "home_sec_spiritual", ScreenDestination.SpiritualSection),
            HomeQuickSectionItem("بازی‌ها", Icons.Default.SportsEsports, Mind3DCardTone.Gold, Color(0xFFB45309), "home_sec_games", ScreenDestination.GamesHub),
            HomeQuickSectionItem("فلش‌کارت‌ها", Icons.Default.Style, Mind3DCardTone.Lemon, Color(0xFFA16207), "home_sec_flashcards", ScreenDestination.Flashcards),
            HomeQuickSectionItem("ارزیابی پیشرفت", Icons.Default.Timeline, Mind3DCardTone.Turquoise, TurquoisePrimary, "home_sec_stats", ScreenDestination.ProgressStats),
            HomeQuickSectionItem("ارتباط با مدرس", Icons.Default.ContactPhone, Mind3DCardTone.Rose, Color(0xFFBE123C), "home_sec_contact", ScreenDestination.ContactInstructor)
        )
    }

    if (showUpdateDialog) {
        Dialog(
            onDismissRequest = {
                showUpdateDialog = false
                viewModel.resetUpdateCheckState()
            }
        ) {
            Mind3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("splash_update_dialog"),
                tone = Mind3DCardTone.WarmIvory,
                shapeRadius = 24.dp,
                extrusionDepth = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isOnline = viewModel.isNetworkConnected()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) MintPrimary else TurquoisePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "به‌روزرسانی و دریافت (نسخه ${BuildConfig.VERSION_NAME})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                    }

                    Text(
                        text = if (isOnline) {
                            "نسخه نصب‌شده روی دستگاه: ${BuildConfig.VERSION_NAME} (کد ${BuildConfig.VERSION_CODE}) • اینترنت متصل"
                        } else {
                            "نسخه نصب‌شده: ${BuildConfig.VERSION_NAME} (کد ${BuildConfig.VERSION_CODE}) • اتصال اینترنت برقرار نیست"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) MintPrimary else Color(0xFFB45309)
                    )

                    var dialogBrowserFeedback by remember { mutableStateOf<String?>(null) }

                    when (val state = updateCheckState) {
                        is UpdateCheckUiState.Idle,
                        is UpdateCheckUiState.Checking -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = SkyBluePrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "در حال اتصال و بررسی نسخه سرور...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight
                                )
                            }
                        }

                        is UpdateCheckUiState.Result -> {
                            when (val res = state.result) {
                                is UpdateCheckResult.UpdateAvailable -> {
                                    Text(
                                        text = "نسخه جدید ${res.manifest.versionName} (کد ${res.manifest.versionCode}) در دسترس است!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary
                                    )
                                    if (res.manifest.releaseDate.isNotBlank()) {
                                        Text(
                                            text = "تاریخ انتشار: ${res.manifest.releaseDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondaryLight
                                        )
                                    }
                                    Text(
                                        text = "تغییرات نسخه: ${res.manifest.changelog}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimaryLight
                                    )
                                    if (res.manifest.sha256.isNotBlank()) {
                                        Text(
                                            text = "SHA-256: ${res.manifest.sha256}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondaryLight
                                        )
                                    }
                                    Mind3DButton(
                                        text = "دانلود مستقیم APK در مرورگر (HTTPS)",
                                        onClick = {
                                            if (!viewModel.isNetworkConnected()) {
                                                dialogBrowserFeedback = "اتصال اینترنت دستگاه برقرار نیست."
                                            } else {
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(res.manifest.apkUrl)))
                                                    dialogBrowserFeedback = "دانلود آغاز شد؛ فایل در مرورگر گوشی باز شد و نصب فقط با تأیید شما انجام می‌شود."
                                                } catch (_: Exception) {
                                                    dialogBrowserFeedback = "مرورگر مناسبی برای باز کردن لینک دریافت یافت نشد."
                                                }
                                            }
                                        },
                                        variant = Mind3DButtonVariant.Emerald,
                                        icon = Icons.Default.Download,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 48.dp
                                    )
                                }

                                is UpdateCheckResult.UpToDate -> {
                                    Text(
                                        text = res.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MintPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!res.directApkUrl.isNullOrBlank()) {
                                        Mind3DButton(
                                            text = "دانلود مستقیم فایل APK با مرورگر",
                                            onClick = {
                                                if (!viewModel.isNetworkConnected()) {
                                                    dialogBrowserFeedback = "اتصال اینترنت دستگاه برقرار نیست."
                                                } else {
                                                    try {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(res.directApkUrl)))
                                                        dialogBrowserFeedback = "دانلود آغاز شد؛ لینک مستقیم در مرورگر گوشی باز شد."
                                                    } catch (_: Exception) {
                                                        dialogBrowserFeedback = "مرورگر مناسبی برای باز کردن لینک یافت نشد."
                                                    }
                                                }
                                            },
                                            variant = Mind3DButtonVariant.Sky,
                                            icon = Icons.Default.Download,
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            height = 48.dp
                                        )
                                    }
                                }

                                is UpdateCheckResult.Unavailable -> {
                                    Text(
                                        text = if (metadata?.apkDownloadUrl.isNullOrBlank()) {
                                            "لینک دانلود عمومی هنوز منتشر نشده است"
                                        } else {
                                            "به‌روزرسانی در دسترس نیست"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = if (metadata?.apkDownloadUrl.isNullOrBlank()) {
                                            "به‌روزرسانی در دسترس نیست — نشانی HTTPS عمومی برای دریافت مستقیم فایل APK (مثلاً GitHub Releases) هنوز در تنظیمات انتشار ثبت نشده است؛ هیچ لینک آزمایشی یا داخلی نمایش داده نمی‌شود."
                                        } else {
                                            res.reason
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    if (dialogBrowserFeedback != null) {
                        Text(
                            text = dialogBrowserFeedback!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }

                    Mind3DButton(
                        text = "مرکز دانلود و راهنمای نصب",
                        onClick = {
                            showUpdateDialog = false
                            viewModel.navigateTo(ScreenDestination.DownloadHub)
                        },
                        variant = Mind3DButtonVariant.Turquoise,
                        icon = Icons.Default.Download,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp
                    )

                    Mind3DButton(
                        text = "تنظیم نشانی در اتاق مدرس",
                        onClick = {
                            showUpdateDialog = false
                            viewModel.navigateTo(ScreenDestination.InstructorRoom)
                        },
                        variant = Mind3DButtonVariant.Sky,
                        icon = Icons.Default.AdminPanelSettings,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp
                    )

                    Mind3DButton(
                        text = "بستن",
                        onClick = {
                            showUpdateDialog = false
                            viewModel.resetUpdateCheckState()
                        },
                        variant = Mind3DButtonVariant.Neutral,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF1FBF9),
                        Color(0xFFECFDF5).copy(alpha = 0.7f),
                        Color(0xFFE0F2FE).copy(alpha = 0.6f),
                        Color(0xFFFFFBEB),
                        Color(0xFFF5F3FF)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        val isCompactScreen = maxWidth < 400.dp
        val isTabletScreen = maxWidth >= 700.dp
        val avatarSize = if (isCompactScreen) 66.dp else 82.dp

        Column(
            modifier = Modifier
                .widthIn(max = 820.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = if (isCompactScreen) 14.dp else 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Responsive Top Bar: Version Badge + Interaction Settings + Quick APK Download CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mind3DChip(
                    text = "نسخه ${BuildConfig.VERSION_NAME}",
                    onClick = { viewModel.navigateTo(ScreenDestination.DownloadHub) },
                    isSelected = true,
                    icon = Icons.Default.AutoAwesome,
                    tone = Mind3DChipTone.Violet
                )

                Mind3DIconButton(
                    icon = Icons.Default.Tune,
                    onClick = { interaction.openSettingsDialog() },
                    variant = Mind3DIconVariant.Neutral,
                    buttonSize = 48.dp,
                    contentDescription = "تنظیمات صدا، لرزش و اندازه قلم",
                    testTag = "topbar_settings_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Mind3DButton(
                text = "دانلود سریع APK",
                onClick = {
                    viewModel.checkForAppUpdate()
                    showUpdateDialog = true
                },
                variant = Mind3DButtonVariant.Gold,
                icon = Icons.Default.Download,
                singleLine = true,
                height = 48.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_download_button")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. TOP LUXURY HEADER CARD: Official Logo + Title + Mentor Studio Portrait
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.WarmIvory,
                shapeRadius = 24.dp,
                extrusionDepth = 4.5.dp,
                borderWidth = 1.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompactScreen) 12.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Mind3DAvatarButton(
                        drawableRes = R.drawable.official_logo,
                        contentDescription = "نشان رسمی هنر ارتباط ذهن‌ها — معرفی مدرس",
                        size = avatarSize,
                        cornerRadius = avatarSize / 2,
                        testTag = "splash_official_logo",
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "هنر ارتباط ذهن‌ها",
                            style = if (isCompactScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "از تله‌پاتی تا شهود و همدلی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A7B1C),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "مدرس: اکبر شیرزادخانی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "لمس نشان یا تصویر برای معرفی مدرس",
                            style = MaterialTheme.typography.labelSmall,
                            color = TurquoisePrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Mind3DAvatarButton(
                        drawableRes = R.drawable.mentor_studio,
                        contentDescription = "اکبر شیرزادخانی - مدرس دوره",
                        size = avatarSize,
                        cornerRadius = 18.dp,
                        testTag = "splash_mentor_photo",
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact Slogan Strip
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Turquoise,
                shapeRadius = 16.dp,
                extrusionDepth = 2.dp
            ) {
                Text(
                    text = "« از تمرکز و شهود تا همدلی و شناخت ارتباط انسانی »",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F766E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. COMPACT INSPIRATIONAL CARD (25 Quotes + Clean Toggle)
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Sky,
                shapeRadius = 22.dp,
                extrusionDepth = 4.dp,
                onClick = {
                    currentCardIndex = (currentCardIndex + 1) % SplashInspirationalCards.size
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(currentCard.bgGradient))
                        .border(1.4.dp, currentCard.borderColor, RoundedCornerShape(22.dp))
                        .padding(if (isCompactScreen) 14.dp else 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "کارت الهام‌بخش (${currentCard.id} از ۲۵)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.88f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "کارت بعدی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyBluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val isLongQuote = currentCard.text.length > 78
                        Text(
                            text = currentCard.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isCompactScreen) 16.sp else 17.5.sp,
                                lineHeight = 28.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight,
                            textAlign = TextAlign.Center,
                            maxLines = if (quoteExpanded || !isLongQuote) 5 else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isLongQuote) {
                            Text(
                                text = if (quoteExpanded) "جمع‌کردن متن ▲" else "ادامه متن ▼",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { quoteExpanded = !quoteExpanded }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.92f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = currentCard.source,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. PRIMARY PROMINENT PORTAL ENTRY BUTTON
            Mind3DButton(
                text = "ورود به دنیای ذهن‌ها",
                subText = "مسیر ۸ فصلی، کتابخانه، تمرین‌ها، بازی‌ها و فلش‌کارت‌ها",
                onClick = { viewModel.navigateTo(ScreenDestination.Portal) },
                variant = Mind3DButtonVariant.Sky,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("splash_enter_portal_button"),
                height = 64.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. CLEAN 8-SECTION QUICK NAVIGATION ARCHITECTURE GRID
            Text(
                text = "دسترسی سریع به ۸ بخش اصلی دوره",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val cols = if (isTabletScreen) 4 else 2
            homeQuickSections.chunked(cols).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Mind3DCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp),
                            tone = item.tone,
                            shapeRadius = 16.dp,
                            extrusionDepth = 3.dp,
                            testTag = item.testTag,
                            onClick = { viewModel.navigateTo(item.destination) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(item.tint.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.tint,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight,
                                    maxLines = 2,
                                    lineHeight = 18.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    repeat(cols - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick utility chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mind3DChip(
                    text = "معرفی مدرس",
                    onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                    isSelected = false,
                    icon = Icons.Default.Person,
                    tone = Mind3DChipTone.Sky
                )
                Mind3DChip(
                    text = "مرکز نصب APK",
                    onClick = { viewModel.navigateTo(ScreenDestination.DownloadHub) },
                    isSelected = false,
                    icon = Icons.Default.Download,
                    tone = Mind3DChipTone.Mint
                )
                Mind3DChip(
                    text = "اتاق مدیریت مدرس",
                    onClick = { viewModel.navigateTo(ScreenDestination.InstructorRoom) },
                    isSelected = false,
                    icon = Icons.Default.AdminPanelSettings,
                    tone = Mind3DChipTone.Violet
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. DIRECT CONTACT CARD (Zero mid-word line wraps at 360dp & fontScale 1.3x)
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.WarmIvory,
                shapeRadius = 22.dp,
                extrusionDepth = 3.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "راه‌های ارتباط مستقیم با مدرس",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    )
                    Text(
                        text = "مدرس و مؤلف: اکبر شیرزادخانی — ۰۹۱۳۸۳۱۰۸۸۹",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DButton(
                            text = "تماس تلفنی",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09138310889"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Mint,
                            icon = Icons.Default.Phone,
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )

                        Mind3DButton(
                            text = "ارسال پیامک",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:09138310889"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Peach,
                            icon = Icons.Default.Sms,
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Responsive messenger chips/buttons so Eitaa / Rubika / WhatsApp never wrap awkwardly
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DButton(
                            text = "ایتا",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://eitaa.com/"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Peach,
                            icon = Icons.Default.Message,
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 96.dp)
                                .testTag("home_btn_eitaa"),
                            height = 48.dp
                        )

                        Mind3DButton(
                            text = "روبیکا",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rubika.ir/"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Lemon,
                            icon = Icons.Default.Public,
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 96.dp)
                                .testTag("home_btn_rubika"),
                            height = 48.dp
                        )

                        Mind3DButton(
                            text = "واتساپ",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/989138310889"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            variant = Mind3DButtonVariant.Mint,
                            icon = Icons.Default.Message,
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 96.dp)
                                .testTag("home_btn_whatsapp"),
                            height = 48.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Mind3DButton(
                        text = "ارسال ایمیل (aram96822@gmail.com)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:aram96822@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "پیام هنرجوی هنر ارتباط ذهن‌ها")
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        variant = Mind3DButtonVariant.Sky,
                        icon = Icons.Default.Email,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        height = 48.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
