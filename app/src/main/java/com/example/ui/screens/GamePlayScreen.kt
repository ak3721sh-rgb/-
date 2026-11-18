package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GAME_EDUCATIONAL_DISCLAIMER
import com.example.ui.components.GameControlBar
import com.example.ui.components.GameGuideCard
import com.example.ui.components.GameResultReportCard
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

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
                    "game_flight" -> PilotFocusFlightGame(viewModel)
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
// Game 6: Sender & Receiver Zener (فرستنده و دریافت‌کننده)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZenerTelepathyGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestZener = remember(gameResults) {
        gameResults.filter { it.gameId == "game_zener" }.maxOfOrNull { it.score } ?: 0
    }

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
    var sessionEnded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlueSoft.copy(alpha = 0.4f))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "آزمایش ۵ نماد زنر",
            goal = "سنجش شهود خام در برابر احتمال تصادفی پایه ۲۰٪ (۱ از ۵).",
            controls = "ابتدا ذهن را آرام کنید، اولین نمادی را که حس می‌کنید لمس کنید و سپس نماد فرستنده را آشکار سازید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestZener > 0) "بهترین امتیاز ثبت‌شده زنر: $bestZener XP" else null,
            onTogglePause = {},
            onRestart = {
                trialsCount = 0
                hitsCount = 0
                currentSenderSymbol = zenerSymbols.random()
                selectedReceiverGuess = null
                isRevealed = false
                sessionEnded = false
            }
        )

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
                    Text("$hitsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("دقت فعلی", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    val percent = if (trialsCount > 0) (hitsCount * 100 / trialsCount) else 0
                    Text("$percent%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("شانس تصادفی", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
                    Text("۲۰٪", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                }
            }
        }

        if (sessionEnded && trialsCount > 0) {
            val accPercent = (hitsCount * 100 / trialsCount).coerceIn(0, 100)
            val totalSessionScore = hitsCount * 25 + trialsCount * 5
            GameResultReportCard(
                gameTitle = "فرستنده و دریافت‌کننده (زنر)",
                score = totalSessionScore,
                bestScore = bestZener,
                accuracyPercent = accPercent,
                encouragement = when {
                    accPercent > 25 -> "دقت جلسه شما ($accPercent٪) بالاتر از خط پایه تصادفی ۲۰٪ بود!"
                    accPercent == 20 -> "دقت جلسه دقیقاً برابر احتمال تصادفی پایه (۲۰٪) بود."
                    else -> "برای مقایسه علمی دقیق‌تر، جلسه را با حداقل ۱۰ آزمایش متوالی و آرامش ذهنی تکرار کنید."
                },
                onPlayAgain = {
                    trialsCount = 0
                    hitsCount = 0
                    currentSenderSymbol = zenerSymbols.random()
                    selectedReceiverGuess = null
                    isRevealed = false
                    sessionEnded = false
                }
            )
        } else {
            // Center Zener Telepathy Card
            Mind3DCard(
                modifier = Modifier.size(width = 230.dp, height = 180.dp),
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
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "حدس شهودی دریافت‌کننده:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
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

                Spacer(modifier = Modifier.height(6.dp))

                if (!isRevealed) {
                    Mind3DButton(
                        text = "آشکارسازی نماد فرستنده",
                        onClick = {
                            if (selectedReceiverGuess != null) {
                                isRevealed = true
                                trialsCount++
                                if (selectedReceiverGuess == currentSenderSymbol) {
                                    hitsCount++
                                    viewModel.recordGameScore("game_zener", "فرستنده و دریافت‌کننده", 25, 100f, 0, "آزمایش زنر")
                                }
                            }
                        },
                        variant = Mind3DButtonVariant.Gold,
                        enabled = selectedReceiverGuess != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DButton(
                            text = "دور بعدی آزمایش",
                            onClick = {
                                currentSenderSymbol = zenerSymbols.random()
                                selectedReceiverGuess = null
                                isRevealed = false
                            },
                            variant = Mind3DButtonVariant.Sky,
                            icon = Icons.Default.Refresh,
                            modifier = Modifier.weight(1f)
                        )
                        Mind3DButton(
                            text = "پایان جلسه",
                            onClick = {
                                val pct = if (trialsCount > 0) (hitsCount * 100f / trialsCount) else 0f
                                val sc = hitsCount * 25 + trialsCount * 5
                                viewModel.recordGameScore("game_zener", "فرستنده و دریافت‌کننده", sc, pct, 0, "جلسه $trialsCount تایی")
                                sessionEnded = true
                            },
                            variant = Mind3DButtonVariant.Mint,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Game 11: Reaction Speed Challenge (چالش سرعت واکنش)
// -------------------------------------------------------------
@Composable
private fun ReactionSpeedGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestFastMs = remember(gameResults) {
        gameResults.filter { it.gameId == "game_reaction" && it.reactionTimeMs > 0L }
            .minOfOrNull { it.reactionTimeMs }
    }
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_reaction" }.maxOfOrNull { it.score } ?: 0
    }

    var gameState by remember { mutableStateOf("WAIT") } // "WAIT", "READY", "TRIGGER", "DONE", "SUMMARY"
    var startTime by remember { mutableLongStateOf(0L) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var roundIndex by remember { mutableIntStateOf(1) }
    var samples by remember { mutableStateOf(emptyList<Long>()) }
    var earlyTapWarning by remember { mutableStateOf(false) }

    LaunchedEffect(gameState) {
        if (gameState == "READY") {
            val randomDelay = (1800..4200).random().toLong()
            delay(randomDelay)
            startTime = System.currentTimeMillis()
            gameState = "TRIGGER"
        }
    }

    fun resetChallenge() {
        gameState = "WAIT"
        roundIndex = 1
        samples = emptyList()
        reactionTimeMs = 0L
        earlyTapWarning = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "سنجش سرعت واکنش",
            goal = "اندازه‌گیری سرعت پاسخ عصبی در ۳ دور پیاپی و محاسبه میانگین (میلی‌ثانیه).",
            controls = "دایره مرکزی را لمس کنید؛ به محض سبز شدن صفحه، بدون مکث ضربه بزنید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = bestFastMs?.let { "بهترین واکنش ثبت‌شده: ${it}ms (امتیاز برتر: $bestScore)" },
            onTogglePause = {},
            onRestart = { resetChallenge() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky, shapeRadius = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("دور $roundIndex از ۳", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                val avgText = if (samples.isNotEmpty()) "${samples.average().toLong()} ms" else "—"
                Text("میانگین تا الان: $avgText", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
            }
        }

        if (gameState == "SUMMARY") {
            val avgMs = if (samples.isNotEmpty()) samples.average().toLong() else reactionTimeMs
            val finalScore = (1000 - avgMs.coerceAtMost(950)).toInt().coerceAtLeast(50)
            GameResultReportCard(
                gameTitle = "چالش سرعت واکنش (میانگین ۳ دور)",
                score = finalScore,
                bestScore = bestScore,
                accuracyPercent = 100,
                reactionTimeMs = avgMs,
                encouragement = when {
                    avgMs < 260L -> "واکنش فوق‌العاده سریع و هشیارانه! هماهنگی دیداری-حرکتی عالی است."
                    avgMs < 360L -> "سرعت پاسخ بسیار مطلوب و متمرکز."
                    else -> "سرعت طبیعی؛ تمرین تنفس آرام پیش از شروع، زمان واکنش را کاهش می‌دهد."
                },
                onPlayAgain = { resetChallenge() }
            )
        } else {
            val (targetTone, stateText, textColor) = when {
                earlyTapWarning -> Triple(Mind3DCardTone.Rose, "زود لمس کردید! منتظر سبز شدن بمانید — دوباره بزنید", Color(0xFF9F1239))
                gameState == "WAIT" -> Triple(Mind3DCardTone.WarmIvory, "برای شروع دور $roundIndex لمس کنید", TextPrimaryLight)
                gameState == "READY" -> Triple(Mind3DCardTone.Peach, "منتظر رنگ سبز باشید... هنوز نزنید!", Color(0xFF9A3412))
                gameState == "TRIGGER" -> Triple(Mind3DCardTone.Mint, "الان سریع لمس کنید!", Color(0xFF14532D))
                else -> Triple(Mind3DCardTone.Sky, "زمان این دور: $reactionTimeMs میلی‌ثانیه", SkyBluePrimary)
            }

            Mind3DCard(
                modifier = Modifier.size(230.dp),
                tone = targetTone,
                shapeRadius = 115.dp,
                extrusionDepth = 7.dp,
                borderWidth = 2.5.dp,
                onClick = {
                    when (gameState) {
                        "TRIGGER" -> {
                            reactionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                            earlyTapWarning = false
                            samples = samples + reactionTimeMs
                            if (roundIndex >= 3) {
                                val avgMs = samples.average().toLong()
                                val finalScore = (1000 - avgMs.coerceAtMost(950)).toInt().coerceAtLeast(50)
                                viewModel.recordGameScore(
                                    gameId = "game_reaction",
                                    gameTitle = "چالش سرعت واکنش",
                                    score = finalScore,
                                    accuracy = 100f,
                                    reactionTime = avgMs,
                                    difficulty = "سرعتی"
                                )
                                gameState = "SUMMARY"
                            } else {
                                gameState = "DONE"
                            }
                        }
                        "READY" -> {
                            earlyTapWarning = true
                            gameState = "WAIT"
                        }
                        "WAIT", "DONE" -> {
                            earlyTapWarning = false
                            if (gameState == "DONE") roundIndex++
                            gameState = "READY"
                        }
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

            if (gameState == "DONE") {
                Mind3DButton(
                    text = "شروع دور بعدی (${roundIndex + 1} از ۳)",
                    onClick = {
                        roundIndex++
                        gameState = "READY"
                    },
                    variant = Mind3DButtonVariant.Orange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = GAME_EDUCATIONAL_DISCLAIMER,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// Game 8: Guess or Observation? (حدس یا مشاهده؟ - ۷ سناریو)
// -------------------------------------------------------------
@Composable
private fun GuessOrObservationGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestObsScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_guess_obs" }.maxOfOrNull { it.score } ?: 0
    }

    val scenarios = remember {
        listOf(
            Triple("دوست شما امروز ۲۰ دقیقه دیر به قرار رسید و گوشی‌اش خاموش بود.", "مشاهده عینی", "بیان مستقیم رخداد بدون نتیجه‌گیری شخصی یا قضاوت."),
            Triple("حتماً به من بی‌احترامی کرده و دوستی ما دیگر برایش ارزشی ندارد.", "برداشت ذهنی", "نسبت‌دادن نیت و قضاوت ارزشی بدون شاهد عینی قطعی."),
            Triple("احتمالاً در مسیر ترافیک سنگینی بوده یا باتری گوشی‌اش تمام شده است.", "حدس تصادفی", "طرح فرضیه و حدس احتمالی، نه مشاهده قطعی و نه داوری سرزنشی."),
            Triple("هنگام ارائه گزارش، همکارمان سه بار جمله‌اش را تکرار کرد و به یادداشتش نگاه کرد.", "مشاهده عینی", "توصیف رفتار قابل مشاهده و قابل ثبت بدون حدس زدن نیت."),
            Triple("او از عمد نگاهش را دزدید چون می‌خواست واقعیت را از ما پنهان کند.", "برداشت ذهنی", "نتیجه‌گیری ذهنی درباره نیت فرد دیگر بدون داده کافی."),
            Triple("شاید هوای سرد امروز باعث شده برخی شرکت‌کنندگان دیرتر برسند.", "حدس تصادفی", "فرضیه محتمل درباره علت، همراه با کلمه «شاید»."),
            Triple("در پایان جلسه ۵ پرسش مطرح شد و ۲ مورد با ارائه مستندات پاسخ داده شد.", "مشاهده عینی", "گزارش دقیق، عددی و قابل راستی‌آزمایی.")
        )
    }

    var scenarioIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    fun resetGame() {
        scenarioIndex = 0
        selectedOption = null
        isSubmitted = false
        score = 0
        correctCount = 0
        finished = false
    }

    val current = scenarios[scenarioIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "تفکیک شناختی حدس، مشاهده و برداشت",
            goal = "تشخیص دقیق داده عینی از قضاوت ذهنی و فرضیه احتمالی در ۷ موقعیت روزمره.",
            controls = "عبارت را بخوانید و دقیق‌ترین طبقه‌بندی شناختی آن را انتخاب کنید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestObsScore > 0) "بهترین امتیاز ثبت‌شده: $bestObsScore XP" else null,
            onTogglePause = {},
            onRestart = { resetGame() }
        )

        if (finished) {
            val acc = (correctCount * 100 / scenarios.size).coerceIn(0, 100)
            GameResultReportCard(
                gameTitle = "حدس یا مشاهده؟",
                score = score,
                bestScore = bestObsScore,
                accuracyPercent = acc,
                encouragement = when {
                    acc >= 80 -> "تفکیک عینی‌نگری و پرهیز از قضاوت شتاب‌زده در سطح بسیار بالایی است!"
                    acc >= 55 -> "تشخیص خوب؛ تمرین تفکیک «شایدها» از «داده‌های دیده‌شده» ارتباط را شفاف‌تر می‌کند."
                    else -> "برای دقت بیشتر، هنگام خواندن جمله بررسی کنید آیا عددی یا رخدادی بدون قضاوت بیان شده است."
                },
                onPlayAgain = { resetGame() }
            )
        } else {
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Sky,
                shapeRadius = 20.dp,
                extrusionDepth = 3.5.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "عبارت ${scenarioIndex + 1} از ${scenarios.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        Text(
                            text = "امتیاز: $score XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "« ${current.first} »",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight,
                        lineHeight = 25.sp
                    )
                }
            }

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

            if (isSubmitted) {
                Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.LemonChiffon) {
                    Text(
                        text = "تحلیل: ${current.third}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (!isSubmitted) {
                Mind3DButton(
                    text = "ثبت ارزیابی",
                    onClick = {
                        if (selectedOption != null) {
                            isSubmitted = true
                            if (selectedOption == current.second) {
                                score += 35
                                correctCount++
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    enabled = selectedOption != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Mind3DButton(
                    text = if (scenarioIndex < scenarios.size - 1) "عبارت بعدی" else "مشاهده کارنامه نهایی",
                    onClick = {
                        if (scenarioIndex < scenarios.size - 1) {
                            scenarioIndex++
                            selectedOption = null
                            isSubmitted = false
                        } else {
                            val acc = (correctCount * 100f / scenarios.size).coerceIn(0f, 100f)
                            viewModel.recordGameScore("game_guess_obs", "حدس یا مشاهده؟", score, acc, 0, "۷ سناریو")
                            finished = true
                        }
                    },
                    variant = Mind3DButtonVariant.Orange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
