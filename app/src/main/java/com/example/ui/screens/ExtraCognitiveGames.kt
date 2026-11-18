package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameControlBar
import com.example.ui.components.GameGuideCard
import com.example.ui.components.GameResultReportCard
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Game 2: Symbol Hunt (شکار نماد)
 */
@Composable
fun SymbolHuntGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_symbol" }.maxOfOrNull { it.score } ?: 0
    }

    val symbols = remember { listOf("◆", "●", "▲", "★", "✦", "❖", "✚", "◈", "◎") }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var targetSymbol by remember { mutableStateOf(symbols.random()) }
    var gridSymbols by remember { mutableStateOf(symbols.shuffled()) }
    var phase by remember { mutableStateOf("SHOW") } // "SHOW", "PICK", "DONE"
    var isPaused by remember { mutableStateOf(false) }
    var roundStartTime by remember { mutableLongStateOf(0L) }
    var totalElapsedMs by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf("") }

    fun restart() {
        round = 1
        score = 0
        hits = 0
        totalElapsedMs = 0L
        feedback = ""
        isPaused = false
        phase = "SHOW"
    }

    LaunchedEffect(round, phase, isPaused) {
        if (phase == "SHOW" && !isPaused) {
            targetSymbol = symbols.random()
            gridSymbols = symbols.shuffled()
            delay(1500)
            roundStartTime = System.currentTimeMillis()
            phase = "PICK"
        }
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
            title = "شکار نماد سریع",
            goal = "بازشناسی فوری نماد هدف در شبکه ۳×۳ در ۵ دور پیاپی.",
            controls = "ابتدا نماد هدف نمایش داده می‌شود؛ پس از ناپدید شدن، خانه درست را لمس کنید."
        )

        GameControlBar(
            isRunning = phase != "DONE",
            isPaused = isPaused,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد شکار نماد: $bestScore XP" else null,
            onTogglePause = { isPaused = !isPaused },
            onRestart = { restart() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Peach) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("دور $round از ۵", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = OrangeLightPrimary)
            }
        }

        if (isPaused) {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Lemon) {
                Text(
                    text = "بازی در حالت توقف موقت است. برای ادامه «ادامه تمرین» را بزنید.",
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }
        } else {
            when (phase) {
                "SHOW" -> {
                    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Lemon) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("نماد هدف را به خاطر بسپارید:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(targetSymbol, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                        }
                    }
                }
                "PICK" -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("نماد هدف را سریع انتخاب کنید:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            val cellSize = ((maxWidth - 24.dp) / 3).coerceIn(72.dp, 108.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (r in 0 until 3) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        for (c in 0 until 3) {
                                            val sym = gridSymbols[r * 3 + c]
                                            Mind3DCard(
                                                modifier = Modifier.size(cellSize),
                                                tone = Mind3DCardTone.Sky,
                                                onClick = {
                                                    val elapsed = (System.currentTimeMillis() - roundStartTime).coerceAtLeast(1L)
                                                    totalElapsedMs += elapsed
                                                    if (sym == targetSymbol) {
                                                        hits++
                                                        score += (120 - (elapsed / 80).toInt()).coerceAtLeast(30)
                                                        feedback = "آفرین! انتخاب دقیق در ${elapsed}ms"
                                                    } else {
                                                        feedback = "اشتباه شد؛ نماد هدف $targetSymbol بود."
                                                    }
                                                    if (round < 5) {
                                                        round++
                                                        phase = "SHOW"
                                                    } else {
                                                        phase = "DONE"
                                                        val acc = (hits * 100f / 5f).coerceIn(0f, 100f)
                                                        viewModel.recordGameScore("game_symbol", "شکار نماد", score, acc, totalElapsedMs / 5L, "متوسط")
                                                    }
                                                }
                                            ) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text(sym, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (feedback.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(feedback, style = MaterialTheme.typography.bodySmall, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "DONE" -> {
                    val accPercent = (hits * 100 / 5).coerceIn(0, 100)
                    GameResultReportCard(
                        gameTitle = "شکار نماد",
                        score = score,
                        bestScore = bestScore,
                        accuracyPercent = accPercent,
                        reactionTimeMs = (totalElapsedMs / 5L).coerceAtLeast(1L),
                        encouragement = if (hits >= 4) "تیزبینی و سرعت ادراک دیداری شما بسیار خوب است!" else "تکرار روزانه به افزایش سرعت جست‌وجوی دیداری کمک می‌کند.",
                        onPlayAgain = { restart() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Game 3: Visual Memory Matrix (حافظه تصویری)
 */
@Composable
fun VisualMemoryGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_memory" }.maxOfOrNull { it.score } ?: 0
    }

    var level by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf("MEMORIZE") } // MEMORIZE, RECALL, DONE
    var isPaused by remember { mutableStateOf(false) }
    var pattern by remember { mutableStateOf((0..8).shuffled().take(3).toSet()) }
    val picked = remember { mutableStateListOf<Int>() }
    var message by remember { mutableStateOf("الگوی خانه‌های روشن را به خاطر بسپارید") }
    var didMistake by remember { mutableStateOf(false) }

    fun restart() {
        level = 1
        score = 0
        didMistake = false
        isPaused = false
        phase = "MEMORIZE"
    }

    LaunchedEffect(level, phase, isPaused) {
        if (phase == "MEMORIZE" && !isPaused) {
            picked.clear()
            val count = (2 + level).coerceAtMost(6)
            pattern = (0..8).shuffled().take(count).toSet()
            message = "سطح $level: به الگو دقت کنید..."
            delay(1750)
            message = "اکنون خانه‌های روشن را لمس کنید"
            phase = "RECALL"
        }
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
            title = "ماتریس حافظه تصویری",
            goal = "به خاطر سپردن محل خانه‌های روشن در ۴ سطح با افزایش تدریجی تعداد الگوها.",
            controls = "پس از خاموش شدن خانه‌ها، دقیقاً همان خانه‌ها را به نوبت لمس کنید."
        )

        GameControlBar(
            isRunning = phase != "DONE",
            isPaused = isPaused,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد حافظه تصویری: $bestScore XP" else null,
            onTogglePause = { isPaused = !isPaused },
            onRestart = { restart() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Lemon) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سطح $level از ۴", fontWeight = FontWeight.Bold)
                    Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryLight)
            }
        }

        if (phase == "DONE") {
            val accPercent = if (didMistake) ((level - 1) * 25).coerceIn(25, 90) else 100
            GameResultReportCard(
                gameTitle = "حافظه تصویری",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (!didMistake) "عالی! تمام سطوح ماتریس را بدون خطا بازسازی کردید." else "با تصویرسازی کلیِ شکل به جای تک‌تک خانه‌ها، حافظه کاری سریع‌تر می‌شود.",
                onPlayAgain = { restart() }
            )
        } else if (isPaused) {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                Text(
                    text = "تمرین موقتاً متوقف شده است.",
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val cellSize = ((maxWidth - 24.dp) / 3).coerceIn(72.dp, 108.dp)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (r in 0 until 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (c in 0 until 3) {
                                val idx = r * 3 + c
                                val isLit = (phase == "MEMORIZE" && idx in pattern) || (phase == "RECALL" && idx in picked && idx in pattern)
                                val isWrong = phase == "RECALL" && idx in picked && idx !in pattern
                                Mind3DCard(
                                    modifier = Modifier.size(cellSize),
                                    tone = when {
                                        isLit -> Mind3DCardTone.Mint
                                        isWrong -> Mind3DCardTone.Rose
                                        else -> Mind3DCardTone.WarmIvory
                                    },
                                    onClick = {
                                        if (phase == "RECALL" && idx !in picked) {
                                            picked.add(idx)
                                            if (idx in pattern) {
                                                score += 25
                                                if (pattern.all { it in picked }) {
                                                    if (level < 4) {
                                                        level++
                                                        phase = "MEMORIZE"
                                                    } else {
                                                        didMistake = false
                                                        phase = "DONE"
                                                        viewModel.recordGameScore("game_memory", "حافظه تصویری", score, 100f, 380, "۴ سطح کامل")
                                                    }
                                                }
                                            } else {
                                                didMistake = true
                                                phase = "DONE"
                                                val acc = ((level - 1) * 25f).coerceIn(25f, 90f)
                                                viewModel.recordGameScore("game_memory", "حافظه تصویری", score, acc, 420, "سطح $level")
                                            }
                                        }
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (isLit) Text("✦", fontSize = 26.sp, color = MintPrimary)
                                        else if (isWrong) Text("✕", fontSize = 22.sp, color = Color(0xFFE11D48))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Game 4: Focus Tunnel (تونل تمرکز)
 */
@Composable
fun FocusTunnelGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_tunnel" }.maxOfOrNull { it.score } ?: 0
    }

    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var playerX by remember { mutableFloatStateOf(0.5f) }
    var tick by remember { mutableFloatStateOf(0f) }
    var insideRatioSum by remember { mutableFloatStateOf(0f) }
    var ticksCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val currentPlayerX by rememberUpdatedState(playerX)
    val currentPaused by rememberUpdatedState(isPaused)

    val centerAtPlayerY = remember(tick) { 0.5f + 0.25f * sin(tick * 2.2f) }
    val isInsideTunnel = abs(playerX - centerAtPlayerY) <= 0.16f

    fun startTunnel() {
        timeLeft = 15
        insideRatioSum = 0f
        ticksCount = 0
        playerX = 0.5f
        tick = 0f
        isPaused = false
        finished = false
        isRunning = true
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            var subTicks = 0
            while (timeLeft > 0 && isRunning) {
                delay(100)
                if (currentPaused) continue
                subTicks++
                tick += 0.12f
                ticksCount++
                if (abs(currentPlayerX - (0.5f + 0.25f * sin(tick * 2.2f))) <= 0.16f) {
                    insideRatioSum += 1f
                }
                if (subTicks % 10 == 0) {
                    timeLeft--
                }
            }
            if (isRunning) {
                isRunning = false
                isPaused = false
                finished = true
                val accuracy = if (ticksCount > 0) (insideRatioSum / ticksCount * 100f) else 0f
                viewModel.recordGameScore("game_tunnel", "تونل تمرکز", (accuracy * 3).toInt() + 20, accuracy, 250, "متوسط")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "هدایت در تونل تمرکز",
            goal = "حفظ گوی سبز درون مسیر موج‌دار آبی به مدت ۱۵ ثانیه.",
            controls = "هنگام حرکت تونل، انگشت خود را به چپ و راست بکشید تا به دیواره‌ها برخورد نکنید."
        )

        GameControlBar(
            isRunning = isRunning,
            isPaused = isPaused,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد تونل تمرکز: $bestScore XP" else null,
            onTogglePause = { isPaused = !isPaused },
            onRestart = { startTunnel() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("زمان باقی‌مانده: $timeLeft ثانیه", fontWeight = FontWeight.Bold)
                Text(
                    text = if (isPaused) "متوقف شد" else if (isInsideTunnel) "درون مسیر ✓" else "خروج از مسیر!",
                    fontWeight = FontWeight.Bold,
                    color = if (isInsideTunnel) MintPrimary else OrangeLightPrimary
                )
            }
        }

        if (finished) {
            val accPercent = if (ticksCount > 0) (insideRatioSum / ticksCount * 100f).toInt().coerceIn(0, 100) else 0
            val finalScore = (accPercent * 3) + 20
            GameResultReportCard(
                gameTitle = "تونل تمرکز",
                score = finalScore,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (accPercent >= 75) "ثبات تمرکز حرکتی بسیار عالی بود!" else "با پیش‌بینی قوس بعدی مسیر، زمان ماندگاری درون تونل بیشتر می‌شود.",
                onPlayAgain = { startTunnel() }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, if (isInsideTunnel) MintPrimary else OrangeLightPrimary, RoundedCornerShape(22.dp))
                    .pointerInput(isRunning, isPaused) {
                        if (isRunning && !isPaused) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                playerX = (change.position.x / size.width).coerceIn(0.08f, 0.92f)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val corridorHalf = w * 0.16f
                    val leftPath = Path()
                    val rightPath = Path()
                    for (step in 0..20) {
                        val yFrac = step / 20f
                        val wave = 0.5f + 0.25f * sin((tick + (1f - yFrac) * 1.5f) * 2.2f)
                        val cx = w * wave
                        val y = h * yFrac
                        if (step == 0) {
                            leftPath.moveTo(cx - corridorHalf, y)
                            rightPath.moveTo(cx + corridorHalf, y)
                        } else {
                            leftPath.lineTo(cx - corridorHalf, y)
                            rightPath.lineTo(cx + corridorHalf, y)
                        }
                    }
                    drawPath(leftPath, color = Color(0xFF38BDF8), style = Stroke(width = 4.dp.toPx()))
                    drawPath(rightPath, color = Color(0xFF38BDF8), style = Stroke(width = 4.dp.toPx()))

                    drawCircle(
                        color = if (isInsideTunnel) Color(0xFF4ADE80) else Color(0xFFFB7185),
                        radius = 18.dp.toPx(),
                        center = Offset(playerX * w, h * 0.78f)
                    )
                }
            }

            if (!isRunning) {
                Mind3DButton(
                    text = "شروع مسیر تونل",
                    onClick = { startTunnel() },
                    variant = Mind3DButtonVariant.Mint,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Game 5: Attentive Ear (گوش شنوا)
 */
@Composable
fun AttentiveEarGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_ear" }.maxOfOrNull { it.score } ?: 0
    }

    val scope = rememberCoroutineScope()
    var targetPulses by remember { mutableIntStateOf(3) }
    var isPlayingTone by remember { mutableStateOf(false) }
    var activePulseIndex by remember { mutableIntStateOf(-1) }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var awaitingAnswer by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("دکمه «پخش پالس صوتی» را بزنید و تعداد پالس را بشمارید.") }

    val toneGen = remember {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 80) } catch (_: Exception) { null }
    }
    DisposableEffect(Unit) {
        onDispose { toneGen?.release() }
    }

    fun restart() {
        round = 1
        score = 0
        correctCount = 0
        finished = false
        awaitingAnswer = false
        feedback = "دکمه «پخش پالس صوتی» را بزنید و تعداد پالس را بشمارید."
    }

    fun playPulses() {
        if (isPlayingTone) return
        scope.launch {
            isPlayingTone = true
            awaitingAnswer = false
            targetPulses = Random.nextInt(2, 7)
            feedback = "با دقت بشمارید (پالس صوتی و نوری)..."
            for (i in 1..targetPulses) {
                activePulseIndex = i
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 160) } catch (_: Exception) {}
                delay(280)
                activePulseIndex = -1
                delay(260)
            }
            isPlayingTone = false
            awaitingAnswer = true
            feedback = "چند پالس شنیدید و دیدید؟ عدد درست را لمس کنید."
        }
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
            title = "گوش شنوا و شمارش پالس",
            goal = "تقویت دقت شنیداری و شمارش متمرکز پالس‌ها در ۵ دور متوالی.",
            controls = "دکمه پخش پالس را بزنید (هم‌زمان نشانگر دایره‌ای هم چشمک می‌زند) و سپس تعداد را انتخاب کنید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد گوش شنوا: $bestScore XP" else null,
            onTogglePause = {},
            onRestart = { restart() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("دور $round از ۵", fontWeight = FontWeight.Bold)
                    Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(feedback, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (finished) {
            val accPercent = (correctCount * 100 / 5).coerceIn(0, 100)
            GameResultReportCard(
                gameTitle = "گوش شنوا",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (correctCount >= 4) "شنوایی فعال و تمرکز متوالی شما در سطح عالی قرار دارد." else "بستن چشم هنگام شمارش ریتم به تفکیک دقیق پالس‌ها کمک می‌کند.",
                onPlayAgain = { restart() }
            )
        } else {
            Box(
                modifier = Modifier
                    .size(145.dp)
                    .clip(CircleShape)
                    .background(if (activePulseIndex != -1) SkyBluePrimary else SkyBlueSoft)
                    .border(3.dp, SkyBluePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (activePulseIndex != -1) Icons.Default.VolumeUp else Icons.Default.Hearing,
                    contentDescription = "سیگنال صوتی",
                    tint = if (activePulseIndex != -1) Color.White else SkyBluePrimary,
                    modifier = Modifier.size(58.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Mind3DButton(
                    text = if (isPlayingTone) "در حال پخش..." else "پخش پالس صوتی جدید",
                    onClick = { playPulses() },
                    variant = Mind3DButtonVariant.Sky,
                    icon = Icons.Default.PlayArrow,
                    enabled = !isPlayingTone,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..6).forEach { countOption ->
                        Mind3DButton(
                            text = "$countOption",
                            onClick = {
                                if (!isPlayingTone && awaitingAnswer) {
                                    awaitingAnswer = false
                                    if (countOption == targetPulses) {
                                        score += 40
                                        correctCount++
                                        feedback = "دقیق بود! ($targetPulses پالس)"
                                    } else {
                                        feedback = "تعداد واقعی $targetPulses پالس بود."
                                    }
                                    if (round < 5) {
                                        round++
                                    } else {
                                        val acc = (correctCount * 100f / 5f).coerceIn(0f, 100f)
                                        viewModel.recordGameScore("game_ear", "گوش شنوا", score, acc, 310, "۵ دور")
                                        finished = true
                                    }
                                }
                            },
                            variant = Mind3DButtonVariant.Mint,
                            enabled = !isPlayingTone && awaitingAnswer,
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Game 7: Emotion Mirror (آینه احساس)
 */
@Composable
fun EmotionMirrorGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_emotion" }.maxOfOrNull { it.score } ?: 0
    }

    val questions = remember {
        listOf(
            Triple("فردی شانه‌های خود را بالا برده، مکث طولانی کرده و با صدای آرام می‌گوید «مطمئن نیستم از پسش بربیایم».", listOf("تردید و نیاز به دلگرمی", "خشم شدید", "بی‌تفاوتی کامل", "شادی مفرط"), 0),
            Triple("دوست شما بعد از شنیدن خبر شما لبخند واقعی می‌زند و با تماس چشمی فعال جزئیات بیشتری می‌پرسد.", listOf("همدلی و همراهی مشتاقانه", "خستگی و ملال", "تمسخر پنهان", "اضطراب شدید"), 0),
            Triple("شخصی وسط گفت‌وگو دست به سینه می‌نشیند و نگاهش را به ساعت می‌دوزد.", listOf("نگرانی زمانی یا بسته شدن کانال ارتباط", "اشتیاق زیاد به ادامه بحث", "آرامش عمیق", "هیچ علامتی ندارد"), 0),
            Triple("همکاری که اشتباهش را پذیرفته و سرش را پایین انداخته، به آرامی عذرخواهی می‌کند.", listOf("احساس مسئولیت و پشیمانی", "لجاجت و انکار", "هیجان پیروزی", "بی‌اعتنایی"), 0),
            Triple("یکی از اعضای خانواده با وجود خستگی، صبورانه حرف‌های شما را تا انتها گوش می‌دهد و یادداشت برمی‌دارد.", listOf("احترام عمیق و توجه حمایتی", "بی‌قراری و کلافگی", "رقابت پنهان", "بی‌حوصلگی"), 0)
        )
    }

    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var revealed by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    fun restart() {
        index = 0
        score = 0
        correctCount = 0
        selected = -1
        revealed = false
        finished = false
    }

    val q = questions[index]

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "آینه احساس و همدلی",
            goal = "خواندن دقیق نشانه‌های غیرکلامی و لحن، بدون برچسب‌زنی و قضاوت شتابزده.",
            controls = "موقعیت را بررسی کنید و دقیق‌ترین تفسیر همدلانه را برگزینید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد آینه احساس: $bestScore XP" else null,
            onTogglePause = {},
            onRestart = { restart() }
        )

        if (finished) {
            val accPercent = (correctCount * 100 / questions.size).coerceIn(0, 100)
            GameResultReportCard(
                gameTitle = "آینه احساس",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (accPercent >= 80) "درک همدلانه شما از زبان بدن و لحن دیگران بسیار دقیق است!" else "توجه هم‌زمان به زبان بدن و جمله گوینده، خطای تفسیر را به حداقل می‌رساند.",
                onPlayAgain = { restart() }
            )
        } else {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Rose) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("سناریوی ${index + 1} از ${questions.size} • امتیاز: $score", fontWeight = FontWeight.Bold, color = TextSecondaryLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(q.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryLight, lineHeight = 26.sp)
                }
            }

            q.second.forEachIndexed { idx, opt ->
                val isCorrect = idx == q.third
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = when {
                        revealed && isCorrect -> Mind3DCardTone.Mint
                        revealed && selected == idx && !isCorrect -> Mind3DCardTone.Peach
                        selected == idx -> Mind3DCardTone.Sky
                        else -> Mind3DCardTone.WarmIvory
                    },
                    onClick = { if (!revealed) selected = idx }
                ) {
                    Text(opt, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                }
            }

            if (!revealed) {
                Mind3DButton(
                    text = "ثبت تشخیص همدلانه",
                    onClick = {
                        if (selected != -1) {
                            revealed = true
                            if (selected == q.third) {
                                score += 50
                                correctCount++
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    enabled = selected != -1,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Mind3DButton(
                    text = if (index < questions.size - 1) "سناریوی بعدی" else "مشاهده کارنامه نهایی",
                    onClick = {
                        if (index < questions.size - 1) {
                            index++
                            selected = -1
                            revealed = false
                        } else {
                            val acc = (correctCount * 100f / questions.size).coerceIn(0f, 100f)
                            viewModel.recordGameScore("game_emotion", "آینه احساس", score, acc, 0, "همدلی")
                            finished = true
                        }
                    },
                    variant = Mind3DButtonVariant.Emerald,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Game 9: Distraction Hunter (شکار حواس‌پرتی - بازداری پاسخ)
 */
@Composable
fun DistractionHunterGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_distraction" }.maxOfOrNull { it.score } ?: 0
    }

    val targetLabel = "★ تمرکز اصلی"
    val decoys = remember { listOf("⚡ پیام فوری", "✖ خبر پراکنده", "☁ فکر مزاحم", "⚠ زنگ حواس‌پرتی") }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var targetSlot by remember { mutableIntStateOf(Random.nextInt(4)) }
    var slots by remember { mutableStateOf(List(4) { i -> if (i == targetSlot) targetLabel else decoys.random() }) }

    fun nextRound() {
        targetSlot = Random.nextInt(4)
        slots = List(4) { i -> if (i == targetSlot) targetLabel else decoys.random() }
    }

    fun restart() {
        round = 1
        score = 0
        hits = 0
        finished = false
        nextRound()
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
            title = "شکار حواس‌پرتی و بازداری پاسخ",
            goal = "نادیده‌گرفتن محرک‌های مزاحم و انتخاب دقیق «$targetLabel» در ۶ دور متوالی.",
            controls = "سریع اما بدون عجله فقط دکمه حاوی «$targetLabel» را لمس کنید."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد شکار حواس‌پرتی: $bestScore XP" else null,
            onTogglePause = {},
            onRestart = { restart() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Peach) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("دور $round از ۶ • امتیاز: $score", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("محرک‌های انحرافی را نادیده بگیرید و فقط «$targetLabel» را لمس کنید!", textAlign = TextAlign.Center)
            }
        }

        if (finished) {
            val accPercent = (hits * 100 / 6).coerceIn(0, 100)
            GameResultReportCard(
                gameTitle = "شکار حواس‌پرتی",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (hits == 6) "فوق‌العاده! هیچ محرک انحرافی نتوانست تمرکز شما را منحرف کند." else "مکث نیم‌ثانیه‌ای پیش از لمس، جلوی پاسخ تکانشی را می‌گیرد.",
                onPlayAgain = { restart() }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                slots.forEachIndexed { idx, label ->
                    Mind3DButton(
                        text = label,
                        onClick = {
                            if (idx == targetSlot) {
                                score += 35
                                hits++
                            } else {
                                score = (score - 15).coerceAtLeast(0)
                            }
                            if (round < 6) {
                                round++
                                nextRound()
                            } else {
                                val acc = (hits * 100f / 6f).coerceIn(0f, 100f)
                                viewModel.recordGameScore("game_distraction", "شکار حواس‌پرتی", score, acc, 260, "۶ دور")
                                finished = true
                            }
                        },
                        variant = Mind3DButtonVariant.Sky,
                        modifier = Modifier.fillMaxWidth(),
                        height = 54.dp
                    )
                }
            }
        }
    }
}

/**
 * Game 10: Path Memory (حافظه مسیر)
 */
@Composable
fun PathMemoryGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_path" }.maxOfOrNull { it.score } ?: 0
    }

    var sequence by remember { mutableStateOf((0..8).shuffled().take(3)) }
    var showingStep by remember { mutableIntStateOf(-1) }
    var isInputPhase by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val userPath = remember { mutableStateListOf<Int>() }
    var score by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(1) }
    var finished by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("مسیر روشن‌شده را به ترتیب دنبال کنید") }

    fun restart() {
        round = 1
        score = 0
        finished = false
        isPaused = false
        sequence = (0..8).shuffled().take(3)
    }

    LaunchedEffect(sequence, isPaused) {
        if (!finished && !isPaused) {
            isInputPhase = false
            userPath.clear()
            status = "دور $round از ۳: به ترتیب روشن‌شدن مسیر دقت کنید..."
            delay(450)
            for (cell in sequence) {
                showingStep = cell
                delay(520)
            }
            showingStep = -1
            isInputPhase = true
            status = "حالا مسیر را به همان ترتیب لمس کنید"
        }
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
            title = "حافظه مسیر فضایی",
            goal = "به خاطرسپاری توالی حرکت و بازسازی دقیق آن از اول تا آخر در ۳ سطح متوالی.",
            controls = "به نقاط آبی/نارنجی دقت کنید و سپس خانه‌ها را به همان ترتیب اولیه لمس نمایید."
        )

        GameControlBar(
            isRunning = !finished,
            isPaused = isPaused,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد حافظه مسیر: $bestScore XP" else null,
            onTogglePause = { isPaused = !isPaused },
            onRestart = { restart() }
        )

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(status, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
            }
        }

        if (finished) {
            val accPercent = ((score / 3.6f).coerceIn(30f, 100f)).toInt()
            GameResultReportCard(
                gameTitle = "حافظه مسیر",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = "بازسازی توالی فضایی، ارتباط ذهن با الگوهای حرکتی و جهت‌یابی را تقویت می‌کند.",
                onPlayAgain = { restart() }
            )
        } else if (!isPaused) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val cellSize = ((maxWidth - 24.dp) / 3).coerceIn(72.dp, 108.dp)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (r in 0 until 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (c in 0 until 3) {
                                val idx = r * 3 + c
                                val stepOrder = userPath.indexOf(idx)
                                val highlighted = showingStep == idx || stepOrder >= 0
                                Mind3DCard(
                                    modifier = Modifier.size(cellSize),
                                    tone = if (highlighted) Mind3DCardTone.Sky else Mind3DCardTone.WarmIvory,
                                    onClick = {
                                        if (isInputPhase) {
                                            val expected = sequence[userPath.size]
                                            if (idx == expected) {
                                                userPath.add(idx)
                                                score += 30
                                                if (userPath.size == sequence.size) {
                                                    if (round < 3) {
                                                        round++
                                                        sequence = (0..8).shuffled().take((sequence.size + 1).coerceAtMost(5))
                                                    } else {
                                                        viewModel.recordGameScore("game_path", "حافظه مسیر", score, 100f, 320, "۳ مرحله")
                                                        finished = true
                                                    }
                                                }
                                            } else {
                                                viewModel.recordGameScore("game_path", "حافظه مسیر", score, 60f, 380, "دور $round")
                                                finished = true
                                            }
                                        }
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (stepOrder >= 0) {
                                            Text("${stepOrder + 1}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                                        } else if (showingStep == idx) {
                                            Text("●", fontSize = 30.sp, color = OrangeLightPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Game 12: Unfinished Story (داستان ناتمام - ۵ سناریو مهارت همدلی و حل تعارض)
 */
@Composable
fun UnfinishedStoryGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_story" }.maxOfOrNull { it.score } ?: 0
    }

    val stories = remember {
        listOf(
            Triple(
                "دوست نزدیکتان قرار مهم را فراموش کرده و حالا عصبانی و پشیمان پاسخ پیامتان را نمی‌دهد. بهترین ادامه داستان برای حفظ پیوند چیست؟",
                listOf(
                    "ابتدا آرام بمانم، برداشت سرزنش‌آمیز نسازم و پیامی همدلانه برای جویا شدن حالش بفرستم.",
                    "فوراً نتیجه‌گیری کنم که دیگر دوستم ندارد و پاسخ‌های تند بنویسم.",
                    "به همه بگویم که او فردی بی‌مسئولیت است."
                ),
                0
            ),
            Triple(
                "در جمع، یکی از اعضا به سرعت حرف دیگری را قطع می‌کند و فضای گفتگو متشنج می‌شود.",
                listOf(
                    "با احترام فضا را آرام کنم، از گوینده اول بخواهم حرفش تمام شود و سپس دیدگاه نفر دوم را بشنوم.",
                    "با صدای بلند از هر دو انتقاد کنم.",
                    "بدون شنیدن صحبت‌ها جلسه را ترک کنم."
                ),
                0
            ),
            Triple(
                "همکار شما بابت اشتباهی که خودتان در گزارش داشته‌اید، با لحن تند جلوی دیگران انتقاد می‌کند.",
                listOf(
                    "مسئولیت بخش خودم را با آرامش بپذیرم و پیشنهاد دهم جزئیات اصلاحی را دوتایی بررسی کنیم.",
                    "با صدای بلندتر اشتباهات گذشته او را یادآوری کنم.",
                    "قهر کنم و دیگر در جلسات نظر ندهم."
                ),
                0
            ),
            Triple(
                "در پیام‌رسان، متنی از دوستتان دریافت می‌کنید که کوتاه و سرد به نظر می‌رسد و احتمال سوءتفاهم دارید.",
                listOf(
                    "قبل از قضاوت درباره لحن متن، با تماس یا پرسش شفاف احساس و منظور واقعی‌اش را بپرسم.",
                    "بر اساس لحن فرضی یک پیام تلخ جواب بدهم.",
                    "بی‌دلیل او را مسدود کنم."
                ),
                0
            ),
            Triple(
                "شخصی تجربه‌ای متفاوت از باور شما دارد و هنگام تعریف کردن نگران است قضاوت شود.",
                listOf(
                    "بدون تمسخر یا تکذیب شتاب‌زده، با گوش‌سپاری فعال گوش کنم و صرفاً پرسش‌های روشنگر بپرسم.",
                    "بلافاصله ثابت کنم که اشتباه فکر می‌کند.",
                    "موضوع را عوض کنم و بی‌توجهی نشان دهم."
                ),
                0
            )
        )
    }

    var step by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var submitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    fun restart() {
        step = 0
        selected = -1
        submitted = false
        score = 0
        correctCount = 0
        finished = false
    }

    val story = stories[step]

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameGuideCard(
            title = "داستان ناتمام و حل تعارض",
            goal = "انتخاب سالم‌ترین و همدلانه‌ترین ادامه در ۵ موقعیت ارتباطی حساس.",
            controls = "هر سناریو را بخوانید و پاسخی را برگزینید که هم کرامت خودتان و هم پیوند انسانی را حفظ کند."
        )

        GameControlBar(
            isRunning = false,
            isPaused = false,
            bestScoreLabel = if (bestScore > 0) "بهترین رکورد داستان ناتمام: $bestScore XP" else null,
            onTogglePause = {},
            onRestart = { restart() }
        )

        if (finished) {
            val accPercent = (correctCount * 100 / stories.size).coerceIn(0, 100)
            GameResultReportCard(
                gameTitle = "داستان ناتمام",
                score = score,
                bestScore = bestScore,
                accuracyPercent = accPercent,
                encouragement = if (accPercent >= 80) "انتخاب‌های شما نشان‌دهنده بلوغ ارتباطی، خویشتن‌داری و گوش‌سپاری فعال است." else "در لحظه تنش، سه ثانیه مکث پیش از پاسخ می‌تواند کل مسیر ارتباط را ترمیم کند.",
                onPlayAgain = { restart() }
            )
        } else {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("داستان ناتمام ${step + 1} از ${stories.size}", fontWeight = FontWeight.Bold, color = MintPrimary)
                        Text("امتیاز: $score XP", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(story.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimaryLight, lineHeight = 26.sp)
                }
            }

            story.second.forEachIndexed { idx, opt ->
                val isCorrect = idx == story.third
                Mind3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = when {
                        submitted && isCorrect -> Mind3DCardTone.Mint
                        submitted && selected == idx && !isCorrect -> Mind3DCardTone.Peach
                        selected == idx -> Mind3DCardTone.Sky
                        else -> Mind3DCardTone.WarmIvory
                    },
                    onClick = { if (!submitted) selected = idx }
                ) {
                    Text(opt, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = TextPrimaryLight)
                }
            }

            if (!submitted) {
                Mind3DButton(
                    text = "انتخاب پایان داستان",
                    onClick = {
                        if (selected != -1) {
                            submitted = true
                            if (selected == story.third) {
                                score += 60
                                correctCount++
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Mint,
                    enabled = selected != -1,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Mind3DButton(
                    text = if (step < stories.size - 1) "داستان بعدی" else "مشاهده کارنامه نهایی",
                    onClick = {
                        if (step < stories.size - 1) {
                            step++
                            selected = -1
                            submitted = false
                        } else {
                            val acc = (correctCount * 100f / stories.size).coerceIn(0f, 100f)
                            viewModel.recordGameScore("game_story", "داستان ناتمام", score, acc, 0, "۵ داستان")
                            finished = true
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
