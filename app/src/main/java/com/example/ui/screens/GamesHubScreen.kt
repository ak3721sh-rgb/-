package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.LemonYellowPrimary
import com.example.ui.theme.LogoGold
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

data class GameMeta(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val MindCognitiveGames = listOf(
    GameMeta(
        id = "game_flight",
        title = "۱. پرواز تمرکز خلبانی",
        category = "شبیه‌ساز کابین و توجه چندگانه",
        description = "شبیه‌ساز آموزشی HUD کابین خلبان، هدایت نشانگر پرواز روی اهداف، اجتناب از موانع و هشدارها",
        icon = Icons.Default.AirplanemodeActive,
        color = SkyBluePrimary
    ),
    GameMeta(
        id = "game_symbol",
        title = "۲. شکار نماد",
        category = "توجه دیداری و تمرکز سریع",
        description = "آشکارسازی نماد هدف، پنهان‌سازی و تشخیص سریع میان نمادهای انحرافی",
        icon = Icons.Default.Category,
        color = OrangeLightPrimary
    ),
    GameMeta(
        id = "game_memory",
        title = "۳. حافظه تصویری",
        category = "حافظه کاری و بازخوانی الگو",
        description = "به‌خاطرسپاری الگوی کاشی‌های روشن‌شده در ماتریس شبکه و بازسازی آن",
        icon = Icons.Default.GridOn,
        color = LemonYellowPrimary
    ),
    GameMeta(
        id = "game_tunnel",
        title = "۴. تونل تمرکز",
        category = "تمرکز مستمر و تعادل حرکتی",
        description = "هدایت پیوسته نشانگر درون تونل پرپیچ‌وخم بدون برخورد با دیواره‌ها",
        icon = Icons.Default.Timeline,
        color = Color(0xFF2E7D32)
    ),
    GameMeta(
        id = "game_ear",
        title = "۵. گوش شنوا",
        category = "شنوایی فعال و تفکیک فرکانس",
        description = "تشخیص فرکانس‌ها و تعداد پالس‌های صوتی محیطی با بازخورد هوشمند",
        icon = Icons.Default.Hearing,
        color = SkyBluePrimary
    ),
    GameMeta(
        id = "game_zener",
        title = "۶. فرستنده و دریافت‌کننده",
        category = "آزمایش دونفره و تله‌پاتی زنر",
        description = "پروتکل ۵ نماد زنر، ثبت حدس‌های خام دریافت‌کننده و مقایسه با احتمال تصادفی ۲۰٪",
        icon = Icons.Default.SwapHoriz,
        color = LogoGold
    ),
    GameMeta(
        id = "game_emotion",
        title = "۷. آینه احساس",
        category = "همدلی و هوش هیجانی",
        description = "تشخیص احساسات چهره و موقعیت، بدون قضاوت شتابزده درباره حالات دیگران",
        icon = Icons.Default.Face,
        color = OrangeLightPrimary
    ),
    GameMeta(
        id = "game_guess_obs",
        title = "۸. حدس یا مشاهده؟",
        category = "تفکر انتقادی و شناخت شناختی",
        description = "تفکیک شواهد عینی، برداشت‌های ذهنی و حدس‌های شهودی در موقعیت‌های مختلف",
        icon = Icons.Default.Visibility,
        color = SkyBluePrimary
    ),
    GameMeta(
        id = "game_distraction",
        title = "۹. شکار حواس‌پرتی",
        category = "بازداری پاسخ و مقاومت در برابر خطا",
        description = "رد کردن محرک‌های چشمک‌زن مزاحم و لمس سریع نماد هدف اصلی",
        icon = Icons.Default.CenterFocusStrong,
        color = Color(0xFFD84315)
    ),
    GameMeta(
        id = "game_path",
        title = "۱۰. حافظه مسیر",
        category = "جهت‌یابی و توالی فضایی",
        description = "تماشای توالی گام‌های مسیر و بازسازی کامل آن با دقت لمسی",
        icon = Icons.Default.Route,
        color = Color(0xFF1565C0)
    ),
    GameMeta(
        id = "game_reaction",
        title = "۱۱. چالش سرعت واکنش",
        category = "سرعت پردازش عصبی (میلی‌ثانیه)",
        description = "پاسخ سریع به تغییر سیگنال نوری و ثبت میانگین زمان عکس‌العمل",
        icon = Icons.Default.Bolt,
        color = LemonYellowPrimary
    ),
    GameMeta(
        id = "game_story",
        title = "۱۲. داستان ناتمام",
        category = "مهارت حل تعارض و همدلی اجتماعی",
        description = "تحلیل سناریوهای ارتباطی و انتخاب بهترین پاسخ همدلانه بین‌فردی",
        icon = Icons.Default.MenuBook,
        color = Color(0xFF2E7D32)
    )
)

@Composable
fun GamesHubScreen(viewModel: MainViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "اتاق ۱۲ بازی شناختی و تمرکز",
                subtitle = "بازی‌های استاندارد، تعاملی و ثبت امتیاز",
                backButtonVariant = Mind3DIconVariant.Peach,
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
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                val columns = if (maxWidth >= 660.dp) 2 else 1
                val gameRows = MindCognitiveGames.chunked(columns)

                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxSize()
                        .testTag("games_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Cockpit Featured Hero Card
                    item {
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.IceCobalt,
                            shapeRadius = 22.dp,
                            extrusionDepth = 4.5.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(SkyBluePrimary.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AirplanemodeActive,
                                                contentDescription = null,
                                                tint = SkyBluePrimary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "بازی ویژه: پرواز تمرکز خلبانی",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimaryLight
                                            )
                                            Text(
                                                text = "شبیه‌ساز کابین پرواز جهت سنجش تمرکز و واکنش",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SkyBluePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "هدایت تعاملی نشانگر روی اهداف هوایی، مدیریت هشدارها، حفظ کانون توجه و اندازه‌گیری دقیق دقت و زمان واکنش (صرفاً شبیه‌سازی آموزشی تمرکز ذهنی).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Mind3DButton(
                                    text = "ورود به کابین خلبانی",
                                    onClick = { viewModel.navigateTo(ScreenDestination.GamePlay("game_flight")) },
                                    variant = Mind3DButtonVariant.Gold,
                                    icon = Icons.Default.AirplanemodeActive,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    items(gameRows) { rowGames ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowGames.forEach { game ->
                                GameListItem(
                                    game = game,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.navigateTo(ScreenDestination.GamePlay(game.id)) }
                                )
                            }
                            if (rowGames.size < columns) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameListItem(
    game: GameMeta,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tone = when (game.id) {
        "game_flight" -> Mind3DCardTone.Sky
        "game_symbol", "game_distraction" -> Mind3DCardTone.Peach
        "game_memory", "game_reaction" -> Mind3DCardTone.Lemon
        "game_tunnel", "game_story" -> Mind3DCardTone.Mint
        "game_zener" -> Mind3DCardTone.Gold
        "game_emotion" -> Mind3DCardTone.Rose
        else -> Mind3DCardTone.Turquoise
    }

    Mind3DCard(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 114.dp),
        tone = tone,
        shapeRadius = 20.dp,
        extrusionDepth = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 114.dp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(game.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = game.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = game.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = game.color,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SkyBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "شروع",
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
