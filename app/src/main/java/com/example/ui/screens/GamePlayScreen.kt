package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LogoDarkForest
import com.example.ui.theme.LogoGold
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    gameId: String,
    viewModel: MainViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val title = when (gameId) {
        "game_flight" -> "پرواز تمرکز خلبانی"
        "game_symbol" -> "شکار نماد"
        "game_memory" -> "حافظه تصویری"
        "game_tunnel" -> "تونل تمرکز"
        "game_ear" -> "گوش شنوا"
        "game_zener" -> "فرستنده و دریافت‌کننده (زنر)"
        "game_emotion" -> "آینه احساس"
        "game_guess_obs" -> "حدس یا مشاهده؟"
        "game_distraction" -> "شکار حواس‌پرتی"
        "game_path" -> "حافظه مسیر"
        "game_reaction" -> "چالش سرعت واکنش"
        "game_story" -> "داستان ناتمام"
        else -> "بازی شناختی"
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = title,
                subtitle = "ثبت عملکرد و امتیاز مهارت",
                onBackClick = { viewModel.navigateBack() },
                showSettingsAction = true
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                when (gameId) {
                    "game_flight" -> PilotFlightGame(viewModel)
                    "game_symbol" -> SymbolHuntGame(viewModel)
                    "game_memory" -> VisualMemoryGame(viewModel)
                    "game_tunnel" -> FocusTunnelGame(viewModel)
                    "game_ear" -> AttentiveEarGame(viewModel)
                    "game_zener" -> ZenerTelepathyGame(viewModel)
                    "game_emotion" -> EmotionMirrorGame(viewModel)
                    "game_guess_obs" -> GuessOrObservationGame(viewModel)
                    "game_distraction" -> DistractionHunterGame(viewModel)
                    "game_path" -> PathMemoryGame(viewModel)
                    "game_reaction" -> ReactionSpeedGame(viewModel)
                    "game_story" -> UnfinishedStoryGame(viewModel)
                    else -> SymbolHuntGame(viewModel)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Game 1: Pilot Focus Flight (کابین پرواز تمرکز خلبانی)
// -------------------------------------------------------------
@Composable
private fun PilotFlightGame(viewModel: MainViewModel) {
    var isPlaying by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(25) }
    var score by remember { mutableIntStateOf(0) }
    var lockDurationSeconds by remember { mutableFloatStateOf(0f) }
    var missionFinished by remember { mutableStateOf(false) }

    // Crosshair position (0f to 1f normalized)
    var crosshairX by remember { mutableFloatStateOf(0.5f) }
    var crosshairY by remember { mutableFloatStateOf(0.5f) }

    // Moving Target Waypoint (normalized)
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.4f) }
    var isLockedOnTarget by remember { mutableStateOf(false) }

    // Target waypoint animation loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            score = 0
            lockDurationSeconds = 0f
            timeLeft = 25
            missionFinished = false

            while (timeLeft > 0 && isPlaying) {
                delay(1000)
                timeLeft--

                // Move target smoothly
                targetX = (0.2f + Random.nextFloat() * 0.6f)
                targetY = (0.25f + Random.nextFloat() * 0.5f)
            }

            if (timeLeft <= 0) {
                isPlaying = false
                missionFinished = true
                val accuracy = (lockDurationSeconds / 25f * 100f).coerceIn(0f, 100f)
                val finalScore = (accuracy * 10).toInt() + 100
                viewModel.recordGameScore(
                    gameId = "game_flight",
                    gameTitle = "پرواز تمرکز خلبانی",
                    score = finalScore,
                    accuracy = accuracy,
                    reactionTime = 320,
                    difficulty = "متوسط"
                )
            }
        }
    }

    // Tick checking lock-on
    LaunchedEffect(isPlaying, crosshairX, crosshairY, targetX, targetY) {
        if (isPlaying) {
            val dist = hypot(crosshairX - targetX, crosshairY - targetY)
            isLockedOnTarget = dist < 0.12f
            if (isLockedOnTarget) {
                lockDurationSeconds += 0.1f
                score += 4
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoDarkForest)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cockpit Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HUD کابین پرواز خلبانی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LogoGold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (timeLeft < 5) Color.Red else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$timeLeft ثانیه",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // HUD Cockpit Viewport (Canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, if (isLockedOnTarget) Color(0xFF00E676) else LogoGold, RoundedCornerShape(20.dp))
                .background(Color(0xFF05130E))
                .pointerInput(isPlaying) {
                    if (isPlaying) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            crosshairX = (change.position.x / size.width).coerceIn(0.08f, 0.92f)
                            crosshairY = (change.position.y / size.height).coerceIn(0.12f, 0.88f)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Pitch Ladder / Horizon
                drawLine(
                    color = Color(0x30FFFFFF),
                    start = Offset(w * 0.15f, h * 0.5f),
                    end = Offset(w * 0.85f, h * 0.5f),
                    strokeWidth = 2f
                )

                // Grid guidelines
                for (i in 1..4) {
                    val y = h * (i * 0.2f)
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Target Waypoint Reticle (Amber or Green)
                val tx = targetX * w
                val ty = targetY * h
                val targetRadius = 38.dp.toPx()
                drawCircle(
                    color = if (isLockedOnTarget) Color(0xFF00E676) else LogoGold,
                    radius = targetRadius,
                    center = Offset(tx, ty),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = if (isLockedOnTarget) Color(0x6000E676) else Color(0x30D4AF37),
                    radius = 8.dp.toPx(),
                    center = Offset(tx, ty)
                )

                // User Crosshair (Controlled by touch)
                val cx = crosshairX * w
                val cy = crosshairY * h
                val crosshairSize = 30.dp.toPx()

                // Center reticle
                drawCircle(
                    color = Color(0xFF4FC3F7),
                    radius = 14.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Reticle Cross Lines
                drawLine(
                    color = Color(0xFF4FC3F7),
                    start = Offset(cx - crosshairSize, cy),
                    end = Offset(cx + crosshairSize, cy),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF4FC3F7),
                    start = Offset(cx, cy - crosshairSize),
                    end = Offset(cx, cy + crosshairSize),
                    strokeWidth = 2.5.dp.toPx()
                )
            }

            // HUD Status overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isLockedOnTarget) "قفل روی هدف: تمرکز عالی!" else "نشانگر را روی دایره زرد هدایت کنید",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLockedOnTarget) Color(0xFF00E676) else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mission Outcome or Controls
        if (missionFinished) {
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Mint,
                shapeRadius = 18.dp,
                extrusionDepth = 3.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "گزارش پرواز و تمرکز ذهنی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "امتیاز کسب‌شده: $score XP | دقت تمرکز: ${(lockDurationSeconds / 25f * 100f).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = "نکته علمی: حفظ کانون توجه روی هدف متحرک، کورتکس پیش‌پیشانی و هماهنگی چشم-دست را به اوج می‌رساند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (!isPlaying) {
            Mind3DButton(
                text = if (missionFinished) "پرواز دوباره" else "شروع مأموریت پرواز",
                onClick = { isPlaying = true },
                variant = Mind3DButtonVariant.Gold,
                icon = Icons.Default.AirplanemodeActive,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Mind3DButton(
                text = "پایان مأموریت",
                onClick = {
                    isPlaying = false
                    missionFinished = true
                },
                variant = Mind3DButtonVariant.Neutral,
                modifier = Modifier.fillMaxWidth(),
                height = 46.dp
            )
        }
    }
}

// -------------------------------------------------------------
// Game 6: Sender & Receiver Zener (فرستنده و دریافت‌کننده)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZenerTelepathyGame(viewModel: MainViewModel) {
    val zenerSymbols = listOf(
        "○ دایره",
        "✚ بعلاوه",
        "≈ امواج",
        "□ مربع",
        "★ ستاره"
    )

    var currentSenderSymbol by remember { mutableStateOf(zenerSymbols.random()) }
    var selectedReceiverGuess by remember { mutableStateOf<String?>(null) }
    var isRevealed by remember { mutableStateOf(false) }
    var trialsCount by remember { mutableIntStateOf(0) }
    var hitsCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueSoft.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stats Card
        Mind3DCard(
            modifier = Modifier.fillMaxWidth(),
            tone = Mind3DCardTone.IceCobalt,
            shapeRadius = 18.dp,
            extrusionDepth = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تعداد آزمایش", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Text("$trialsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("پاسخ‌های صحیح", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Text("$hitsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("دقت فعلی", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    val percent = if (trialsCount > 0) (hitsCount * 100 / trialsCount) else 0
                    Text("$percent%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("شانس تصادفی", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Text("۲۰٪", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                }
            }
        }

        // Center Zener Telepathy Card
        Mind3DCard(
            modifier = Modifier.size(width = 230.dp, height = 184.dp),
            tone = if (isRevealed) Mind3DCardTone.Gold else Mind3DCardTone.Sky,
            shapeRadius = 24.dp,
            extrusionDepth = 5.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRevealed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentSenderSymbol,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val isCorrect = selectedReceiverGuess == currentSenderSymbol
                        Text(
                            text = if (isCorrect) "همخوانی کامل! (+۲۵ XP)" else "تفاوت حدس با نماد فرستنده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF15803D) else TextPrimaryLight,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "فرستنده نماد را انتخاب کرد",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اولین حس شهودی خود را لمس کنید",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Symbol Choices (Zener 5 symbols)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "حدس شهودی دریافت‌کننده:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zenerSymbols.forEach { sym ->
                    val isSelected = selectedReceiverGuess == sym
                    Mind3DCard(
                        modifier = Modifier
                            .widthIn(min = 104.dp)
                            .defaultMinSize(minHeight = 52.dp),
                        tone = if (isSelected) Mind3DCardTone.Sky else Mind3DCardTone.WarmIvory,
                        shapeRadius = 14.dp,
                        extrusionDepth = if (isSelected) 2.dp else 4.dp,
                        borderWidth = if (isSelected) 2.dp else 1.2.dp,
                        onClick = if (!isRevealed) {
                            { selectedReceiverGuess = sym }
                        } else null
                    ) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 52.dp)
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SkyBluePrimary else TextPrimaryLight,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isRevealed) {
                Mind3DButton(
                    text = "آشکارسازی نماد فرستنده",
                    onClick = {
                        if (selectedReceiverGuess != null) {
                            isRevealed = true
                            trialsCount++
                            if (selectedReceiverGuess == currentSenderSymbol) {
                                hitsCount++
                                viewModel.recordGameScore("game_zener", "فرستنده و دریافت‌کننده", 25, 100f, 0, "مبتدی")
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Gold,
                    enabled = selectedReceiverGuess != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Mind3DButton(
                    text = "دور بعدی آزمایش",
                    onClick = {
                        currentSenderSymbol = zenerSymbols.random()
                        selectedReceiverGuess = null
                        isRevealed = false
                    },
                    variant = Mind3DButtonVariant.Sky,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Game 11: Reaction Speed Challenge (چالش سرعت واکنش)
// -------------------------------------------------------------
@Composable
private fun ReactionSpeedGame(viewModel: MainViewModel) {
    var gameState by remember { mutableStateOf("WAIT") } // "WAIT", "READY", "TRIGGER", "DONE"
    var startTime by remember { mutableLongStateOf(0L) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(gameState) {
        if (gameState == "READY") {
            val randomDelay = (2000..5000).random().toLong()
            delay(randomDelay)
            startTime = System.currentTimeMillis()
            gameState = "TRIGGER"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (targetTone, stateText, textColor) = when (gameState) {
            "WAIT" -> Triple(Mind3DCardTone.WarmIvory, "برای شروع چالش لمس کنید", TextPrimaryLight)
            "READY" -> Triple(Mind3DCardTone.Peach, "منتظر رنگ سبز باشید... هنوز نزنید!", Color(0xFF9A3412))
            "TRIGGER" -> Triple(Mind3DCardTone.Mint, "الان سریع لمس کنید!", Color(0xFF14532D))
            else -> Triple(Mind3DCardTone.Sky, "نتیجه: $reactionTimeMs میلی‌ثانیه", SkyBluePrimary)
        }

        Mind3DCard(
            modifier = Modifier.size(250.dp),
            tone = targetTone,
            shapeRadius = 125.dp,
            extrusionDepth = 7.dp,
            borderWidth = 2.5.dp,
            onClick = {
                if (gameState == "TRIGGER") {
                    reactionTimeMs = System.currentTimeMillis() - startTime
                    gameState = "DONE"
                    viewModel.recordGameScore(
                        gameId = "game_reaction",
                        gameTitle = "چالش سرعت واکنش",
                        score = (1000 - reactionTimeMs.coerceAtMost(1000)).toInt(),
                        accuracy = 100f,
                        reactionTime = reactionTimeMs,
                        difficulty = "سرعتی"
                    )
                } else if (gameState == "READY") {
                    gameState = "WAIT"
                } else if (gameState == "WAIT" || gameState == "DONE") {
                    gameState = "READY"
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (gameState == "DONE") {
            val eval = when {
                reactionTimeMs < 250 -> "واکنش فوق‌العاده سریع (نخبه شناختی)"
                reactionTimeMs < 350 -> "سرعت عالی و توجه متمرکز"
                else -> "طبیعی؛ با تمرین تنفس و آلفا سرعت بهبود می‌یابد"
            }
            Text(
                text = eval,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SkyBluePrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Mind3DButton(
                text = "تلاش مجدد",
                onClick = { gameState = "READY" },
                variant = Mind3DButtonVariant.Orange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// Game 8: Guess or Observation? (حدس یا مشاهده؟)
// -------------------------------------------------------------
@Composable
private fun GuessOrObservationGame(viewModel: MainViewModel) {
    val scenarios = listOf(
        Pair("دوست شما امروز دیر به قرار رسید و گوشی‌اش خاموش بود.", "مشاهده عینی"),
        Pair("حتماً به من بی‌احترامی کرده و دوستی ما برایش ارزش ندارد.", "برداشت ذهنی"),
        Pair("احتمالاً در مسیر اتومبیلش خراب شده یا مشکلی پیش آمده است.", "حدس تصادفی")
    )

    var scenarioIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    val current = scenarios[scenarioIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Mind3DCard(
            modifier = Modifier.fillMaxWidth(),
            tone = Mind3DCardTone.Sky,
            shapeRadius = 20.dp,
            extrusionDepth = 3.5.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "عبارت زیر را تحلیل و نوع آن را مشخص کنید:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "« ${current.first} »",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    lineHeight = 24.sp
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val options = listOf("مشاهده عینی", "برداشت ذهنی", "حدس تصادفی")
            options.forEach { opt ->
                val isSelected = selectedOption == opt
                val isCorrect = opt == current.second

                val optionTone = when {
                    isSubmitted && isCorrect -> Mind3DCardTone.Mint
                    isSubmitted && isSelected && !isCorrect -> Mind3DCardTone.Peach
                    isSelected -> Mind3DCardTone.Sky
                    else -> Mind3DCardTone.WarmIvory
                }

                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = optionTone,
                    shapeRadius = 16.dp,
                    extrusionDepth = if (isSelected) 2.dp else 4.dp,
                    borderWidth = if (isSelected) 2.dp else 1.2.dp,
                    onClick = if (!isSubmitted) {
                        { selectedOption = opt }
                    } else null
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubmitted && isCorrect) Color(0xFF15803D) else TextPrimaryLight,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (!isSubmitted) {
            Mind3DButton(
                text = "ثبت ارزیابی",
                onClick = {
                    if (selectedOption != null) {
                        isSubmitted = true
                        if (selectedOption == current.second) {
                            viewModel.recordGameScore("game_guess_obs", "حدس یا مشاهده؟", 30, 100f, 0, "مبتدی")
                        }
                    }
                },
                variant = Mind3DButtonVariant.Sky,
                enabled = selectedOption != null,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Mind3DButton(
                text = "عبارت بعدی",
                onClick = {
                    scenarioIndex = (scenarioIndex + 1) % scenarios.size
                    selectedOption = null
                    isSubmitted = false
                },
                variant = Mind3DButtonVariant.Orange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// Fallback Standard Cognitive Game Template
// -------------------------------------------------------------
@Composable
private fun StandardCognitiveGame(
    gameId: String,
    title: String,
    viewModel: MainViewModel
) {
    var score by remember { mutableIntStateOf(100) }
    var step by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Mind3DCard(
            modifier = Modifier.fillMaxWidth(),
            tone = Mind3DCardTone.LemonChiffon,
            shapeRadius = 20.dp,
            extrusionDepth = 3.5.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "مرحله $step: تمرکز کامل بر نشانگر و پردازش سریع اطلاعات.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )
            }
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(OrangeLightSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = OrangeLightPrimary,
                modifier = Modifier.size(64.dp)
            )
        }

        Mind3DButton(
            text = "تکمیل مرحله و ثبت امتیاز (+50 XP)",
            onClick = {
                step++
                score += 50
                viewModel.recordGameScore(gameId, title, score, 92f, 290, "متوسط")
            },
            variant = Mind3DButtonVariant.Emerald,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
