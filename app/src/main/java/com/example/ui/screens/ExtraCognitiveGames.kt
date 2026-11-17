package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
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
    val symbols = listOf("◆", "●", "▲", "★", "✦", "❖", "✚", "◈", "◎")
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var targetSymbol by remember { mutableStateOf(symbols.random()) }
    var gridSymbols by remember { mutableStateOf(symbols.shuffled()) }
    var phase by remember { mutableStateOf("SHOW") } // "SHOW", "PICK", "DONE"
    var roundStartTime by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf("") }

    LaunchedEffect(round, phase) {
        if (phase == "SHOW") {
            targetSymbol = symbols.random()
            gridSymbols = symbols.shuffled()
            delay(1600)
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Peach) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("دور $round از ۵", fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = OrangeLightPrimary)
            }
        }

        when (phase) {
            "SHOW" -> {
                Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Lemon) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("نماد هدف را به خاطر بسپارید:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(targetSymbol, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                    }
                }
            }
            "PICK" -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("نماد هدف را سریع انتخاب کنید:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    for (r in 0 until 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 5.dp)) {
                            for (c in 0 until 3) {
                                val sym = gridSymbols[r * 3 + c]
                                Mind3DCard(
                                    modifier = Modifier.size(86.dp),
                                    tone = Mind3DCardTone.Sky,
                                    onClick = {
                                        val elapsed = System.currentTimeMillis() - roundStartTime
                                        if (sym == targetSymbol) {
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
                                            viewModel.recordGameScore("game_symbol", "شکار نماد", score, (score / 5f).coerceIn(40f, 100f), elapsed, "متوسط")
                                        }
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(sym, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextPrimaryLight)
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
                Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("پایان بازی شکار نماد!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("امتیاز نهایی: $score XP", style = MaterialTheme.typography.headlineSmall, color = MintPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Mind3DButton(
                            text = "بازی مجدد",
                            onClick = {
                                round = 1
                                score = 0
                                feedback = ""
                                phase = "SHOW"
                            },
                            variant = Mind3DButtonVariant.Sky,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
    var level by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf("MEMORIZE") } // MEMORIZE, RECALL, DONE
    var pattern by remember { mutableStateOf((0..8).shuffled().take(3).toSet()) }
    val picked = remember { mutableStateListOf<Int>() }
    var message by remember { mutableStateOf("الگوی خانه‌های روشن را به خاطر بسپارید") }

    LaunchedEffect(level, phase) {
        if (phase == "MEMORIZE") {
            picked.clear()
            val count = (2 + level).coerceAtMost(6)
            pattern = (0..8).shuffled().take(count).toSet()
            message = "سطح $level: به الگو دقت کنید..."
            delay(1800)
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (r in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in 0 until 3) {
                        val idx = r * 3 + c
                        val isLit = (phase == "MEMORIZE" && idx in pattern) || (phase == "RECALL" && idx in picked && idx in pattern)
                        val isWrong = phase == "RECALL" && idx in picked && idx !in pattern
                        Mind3DCard(
                            modifier = Modifier.size(90.dp),
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
                                                phase = "DONE"
                                                viewModel.recordGameScore("game_memory", "حافظه تصویری", score, 95f, 400, "متوسط")
                                            }
                                        }
                                    } else {
                                        phase = "DONE"
                                        viewModel.recordGameScore("game_memory", "حافظه تصویری", score, 70f, 450, "متوسط")
                                    }
                                }
                            }
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isLit) Text("✦", fontSize = 28.sp, color = MintPrimary)
                                else if (isWrong) Text("✕", fontSize = 24.sp, color = Color(0xFFE11D48))
                            }
                        }
                    }
                }
            }
        }

        if (phase == "DONE") {
            Mind3DButton(
                text = "شروع دوباره (امتیاز ثبت شد: $score)",
                onClick = {
                    level = 1
                    score = 0
                    phase = "MEMORIZE"
                },
                variant = Mind3DButtonVariant.Sky,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Game 4: Focus Tunnel (تونل تمرکز)
 */
@Composable
fun FocusTunnelGame(viewModel: MainViewModel) {
    var isRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var playerX by remember { mutableFloatStateOf(0.5f) }
    var tick by remember { mutableFloatStateOf(0f) }
    var insideRatioSum by remember { mutableFloatStateOf(0f) }
    var ticksCount by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val centerAtPlayerY = remember(tick) { 0.5f + 0.25f * sin(tick * 2.2f) }
    val isInsideTunnel = abs(playerX - centerAtPlayerY) <= 0.16f

    LaunchedEffect(isRunning) {
        if (isRunning) {
            timeLeft = 15
            insideRatioSum = 0f
            ticksCount = 0
            finished = false
            while (timeLeft > 0 && isRunning) {
                repeat(10) {
                    delay(100)
                    tick += 0.12f
                    ticksCount++
                    if (abs(playerX - (0.5f + 0.25f * sin(tick * 2.2f))) <= 0.16f) {
                        insideRatioSum += 1f
                    }
                }
                timeLeft--
            }
            isRunning = false
            finished = true
            val accuracy = if (ticksCount > 0) (insideRatioSum / ticksCount * 100f) else 0f
            viewModel.recordGameScore("game_tunnel", "تونل تمرکز", (accuracy * 3).toInt() + 20, accuracy, 250, "متوسط")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("زمان باقی‌مانده: $timeLeft ثانیه", fontWeight = FontWeight.Bold)
                Text(
                    text = if (isInsideTunnel) "درون مسیر ✓" else "خروج از مسیر!",
                    fontWeight = FontWeight.Bold,
                    color = if (isInsideTunnel) MintPrimary else OrangeLightPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, if (isInsideTunnel) MintPrimary else OrangeLightPrimary, RoundedCornerShape(22.dp))
                .pointerInput(isRunning) {
                    if (isRunning) {
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

        Mind3DButton(
            text = if (isRunning) "در حال حرکت... انگشت خود را بکشید" else if (finished) "تکرار تونل تمرکز" else "شروع مسیر تونل",
            onClick = { if (!isRunning) isRunning = true },
            variant = if (isRunning) Mind3DButtonVariant.Neutral else Mind3DButtonVariant.Mint,
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Game 5: Attentive Ear (گوش شنوا)
 */
@Composable
fun AttentiveEarGame(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    var targetPulses by remember { mutableIntStateOf(3) }
    var isPlayingTone by remember { mutableStateOf(false) }
    var activePulseIndex by remember { mutableIntStateOf(-1) }
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("دکمه «پخش پالس صوتی» را بزنید و تعداد پالس را بشمارید.") }

    val toneGen = remember {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 80) } catch (_: Exception) { null }
    }
    DisposableEffect(Unit) {
        onDispose { toneGen?.release() }
    }

    fun playPulses() {
        if (isPlayingTone) return
        scope.launch {
            isPlayingTone = true
            targetPulses = Random.nextInt(2, 7)
            feedback = "با دقت گوش کنید..."
            for (i in 1..targetPulses) {
                activePulseIndex = i
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 160) } catch (_: Exception) {}
                delay(280)
                activePulseIndex = -1
                delay(260)
            }
            isPlayingTone = false
            feedback = "چند پالس شنیدید و دیدید؟"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("دور $round از ۵", fontWeight = FontWeight.Bold)
                    Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(feedback, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(if (activePulseIndex != -1) SkyBluePrimary else SkyBlueSoft)
                .border(3.dp, SkyBluePrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (activePulseIndex != -1) Icons.Default.VolumeUp else Icons.Default.Hearing,
                contentDescription = "سیگنال صوتی",
                tint = if (activePulseIndex != -1) Color.White else SkyBluePrimary,
                modifier = Modifier.size(64.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Mind3DButton(
                text = "پخش پالس صوتی جدید",
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
                            if (!isPlayingTone) {
                                if (countOption == targetPulses) {
                                    score += 40
                                    feedback = "دقیق بود! ($targetPulses پالس)"
                                } else {
                                    feedback = "تعداد واقعی $targetPulses پالس بود."
                                }
                                if (round < 5) {
                                    round++
                                } else {
                                    viewModel.recordGameScore("game_ear", "گوش شنوا", score, (score / 2f).coerceIn(40f, 100f), 310, "متوسط")
                                    round = 1
                                    score = 0
                                }
                            }
                        },
                        variant = Mind3DButtonVariant.Mint,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
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
    val questions = listOf(
        Triple("فردی شانه‌های خود را بالا برده، مکث طولانی کرده و با صدای آرام می‌گوید «مطمئن نیستم از پسش بربیایم».", listOf("تردید و نیاز به دلگرمی", "خشم شدید", "بی‌تفاوتی کامل", "شادی مفرط"), 0),
        Triple("دوست شما بعد از شنیدن خبر شما لبخند واقعی می‌زند و با تماس چشمی فعال جزئیات بیشتری می‌پرسد.", listOf("همدلی و همراهی مشتاقانه", "خستگی و ملال", "تمسخر پنهان", "اضطراب شدید"), 0),
        Triple("شخصی وسط گفت‌وگو دست به سینه می‌نشیند و نگاهش را به ساعت می‌دوزد.", listOf("نگرانی زمانی یا بسته شدن کانال ارتباط", "اشتیاق زیاد به ادامه بحث", "آرامش عمیق", "هیچ علامتی ندارد"), 0),
        Triple("همکاری که اشتباهش را پذیرفته و سرش را پایین انداخته، به آرامی عذرخواهی می‌کند.", listOf("احساس مسئولیت و پشیمانی", "لجاجت و انکار", "هیجان پیروزی", "بی‌اعتنایی"), 0)
    )
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var revealed by remember { mutableStateOf(false) }

    val q = questions[index]

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        if (selected == q.third) score += 50
                    }
                },
                variant = Mind3DButtonVariant.Sky,
                enabled = selected != -1,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Mind3DButton(
                text = if (index < questions.size - 1) "سناریوی بعدی" else "ثبت پایان بازی (+امتیاز)",
                onClick = {
                    if (index < questions.size - 1) {
                        index++
                        selected = -1
                        revealed = false
                    } else {
                        viewModel.recordGameScore("game_emotion", "آینه احساس", score, (score / 2f).coerceIn(50f, 100f), 0, "همدلی")
                        index = 0
                        score = 0
                        selected = -1
                        revealed = false
                    }
                },
                variant = Mind3DButtonVariant.Emerald,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Game 9: Distraction Hunter (شکار حواس‌پرتی - بازداری پاسخ)
 */
@Composable
fun DistractionHunterGame(viewModel: MainViewModel) {
    val targetLabel = "★ تمرکز اصلی"
    val decoys = listOf("⚡ پیام فوری", "✖ خبر پراکنده", "☁ فکر مزاحم", "⚠ زنگ حواس‌پرتی")
    var round by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var targetSlot by remember { mutableIntStateOf(Random.nextInt(4)) }
    var slots by remember { mutableStateOf(List(4) { i -> if (i == targetSlot) targetLabel else decoys.random() }) }

    fun nextRound() {
        targetSlot = Random.nextInt(4)
        slots = List(4) { i -> if (i == targetSlot) targetLabel else decoys.random() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Peach) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("دور $round از ۶ • امتیاز: $score", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("محرک‌های انحرافی را نادیده بگیرید و فقط «$targetLabel» را لمس کنید!", textAlign = TextAlign.Center)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            slots.forEachIndexed { idx, label ->
                Mind3DButton(
                    text = label,
                    onClick = {
                        if (idx == targetSlot) score += 35 else score = (score - 15).coerceAtLeast(0)
                        if (round < 6) {
                            round++
                            nextRound()
                        } else {
                            viewModel.recordGameScore("game_distraction", "شکار حواس‌پرتی", score, (score / 2.1f).coerceIn(40f, 100f), 260, "سریع")
                            round = 1
                            score = 0
                            nextRound()
                        }
                    },
                    variant = if (idx == targetSlot) Mind3DButtonVariant.Gold else Mind3DButtonVariant.Orange,
                    modifier = Modifier.fillMaxWidth(),
                    height = 56.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * Game 10: Path Memory (حافظه مسیر)
 */
@Composable
fun PathMemoryGame(viewModel: MainViewModel) {
    var sequence by remember { mutableStateOf((0..8).shuffled().take(4)) }
    var showingStep by remember { mutableIntStateOf(-1) }
    var isInputPhase by remember { mutableStateOf(false) }
    val userPath = remember { mutableStateListOf<Int>() }
    var score by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("مسیر روشن‌شده را به ترتیب دنبال کنید") }

    LaunchedEffect(sequence) {
        isInputPhase = false
        userPath.clear()
        status = "به ترتیب روشن‌شدن مسیر دقت کنید..."
        delay(500)
        for (cell in sequence) {
            showingStep = cell
            delay(550)
        }
        showingStep = -1
        isInputPhase = true
        status = "حالا مسیر را به همان ترتیب لمس کنید"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(status, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("امتیاز: $score", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (r in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in 0 until 3) {
                        val idx = r * 3 + c
                        val stepOrder = userPath.indexOf(idx)
                        val highlighted = showingStep == idx || stepOrder >= 0
                        Mind3DCard(
                            modifier = Modifier.size(90.dp),
                            tone = if (highlighted) Mind3DCardTone.Sky else Mind3DCardTone.WarmIvory,
                            onClick = {
                                if (isInputPhase) {
                                    val expected = sequence[userPath.size]
                                    if (idx == expected) {
                                        userPath.add(idx)
                                        score += 30
                                        if (userPath.size == sequence.size) {
                                            viewModel.recordGameScore("game_path", "حافظه مسیر", score, 100f, 320, "فضایی")
                                            sequence = (0..8).shuffled().take((sequence.size + 1).coerceAtMost(6))
                                        }
                                    } else {
                                        status = "توالی اشتباه بود؛ مسیر جدید آماده شد"
                                        sequence = (0..8).shuffled().take(4)
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

        Mind3DButton(
            text = "مسیر جدید",
            onClick = { sequence = (0..8).shuffled().take(4) },
            variant = Mind3DButtonVariant.Neutral,
            icon = Icons.Default.Refresh,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Game 12: Unfinished Story (داستان ناتمام - مهارت همدلی و حل تعارض)
 */
@Composable
fun UnfinishedStoryGame(viewModel: MainViewModel) {
    val stories = listOf(
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
        )
    )
    var step by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var submitted by remember { mutableStateOf(false) }

    val story = stories[step]

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("داستان ناتمام ${step + 1} از ${stories.size}", fontWeight = FontWeight.Bold, color = MintPrimary)
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
                            viewModel.recordGameScore("game_story", "داستان ناتمام", 80, 100f, 0, "همدلی")
                        }
                    }
                },
                variant = Mind3DButtonVariant.Mint,
                enabled = selected != -1,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Mind3DButton(
                text = "داستان بعدی",
                onClick = {
                    step = (step + 1) % stories.size
                    selected = -1
                    submitted = false
                },
                variant = Mind3DButtonVariant.Sky,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
