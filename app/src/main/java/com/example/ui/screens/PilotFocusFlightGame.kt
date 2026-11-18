package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GameGuideCard
import com.example.ui.components.GameResultReportCard
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.theme.LogoDarkForest
import com.example.ui.theme.LogoGold
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private enum class FlightDifficulty(
    val label: String,
    val lockRadius: Float,
    val targetSpeedFactor: Float,
    val hazardRadius: Float
) {
    EASY("آسان", 0.15f, 0.7f, 0.08f),
    MEDIUM("متوسط", 0.12f, 1.0f, 0.10f),
    HARD("پیشرفته", 0.09f, 1.4f, 0.12f)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PilotFocusFlightGame(viewModel: MainViewModel) {
    val gameResults by viewModel.gameResults.collectAsState()
    val bestRecordedScore = remember(gameResults) {
        gameResults.filter { it.gameId == "game_flight" }.maxOfOrNull { it.score } ?: 0
    }

    var difficulty by remember { mutableStateOf(FlightDifficulty.MEDIUM) }
    var selectedDurationSec by remember { mutableIntStateOf(25) }

    var isPlaying by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var missionFinished by remember { mutableStateOf(false) }

    var timeLeftSec by remember { mutableIntStateOf(25) }
    var elapsedSecondsTotal by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var lockDurationSeconds by remember { mutableFloatStateOf(0f) }
    var hazardHits by remember { mutableIntStateOf(0) }

    // Crosshair position (0f..1f normalized)
    var crosshairX by remember { mutableFloatStateOf(0.5f) }
    var crosshairY by remember { mutableFloatStateOf(0.5f) }

    // Target waypoint (smoothed motion)
    var targetX by remember { mutableFloatStateOf(0.5f) }
    var targetY by remember { mutableFloatStateOf(0.38f) }
    var goalX by remember { mutableFloatStateOf(0.5f) }
    var goalY by remember { mutableFloatStateOf(0.38f) }

    // Moving turbulence hazard zone
    var hazardX by remember { mutableFloatStateOf(0.25f) }
    var hazardY by remember { mutableFloatStateOf(0.62f) }

    var isLockedOnTarget by remember { mutableStateOf(false) }
    var isInHazard by remember { mutableStateOf(false) }
    var lockStartMs by remember { mutableLongStateOf(0L) }
    var lockAcquireSamples by remember { mutableStateOf(emptyList<Long>()) }

    val currentCrosshairX by rememberUpdatedState(crosshairX)
    val currentCrosshairY by rememberUpdatedState(crosshairY)
    val currentIsPaused by rememberUpdatedState(isPaused)
    val currentDifficulty by rememberUpdatedState(difficulty)
    val currentDuration by rememberUpdatedState(selectedDurationSec)

    fun startMission() {
        score = 0
        lockDurationSeconds = 0f
        elapsedSecondsTotal = 0f
        hazardHits = 0
        lockAcquireSamples = emptyList()
        timeLeftSec = selectedDurationSec
        crosshairX = 0.5f
        crosshairY = 0.5f
        targetX = 0.5f
        targetY = 0.38f
        goalX = 0.25f + Random.nextFloat() * 0.5f
        goalY = 0.25f + Random.nextFloat() * 0.45f
        hazardX = 0.22f
        hazardY = 0.65f
        lockStartMs = System.currentTimeMillis()
        isPaused = false
        missionFinished = false
        isPlaying = true
    }

    fun finishMission(recordScore: Boolean) {
        isPlaying = false
        isPaused = false
        missionFinished = true
        val effectiveElapsed = elapsedSecondsTotal.coerceAtLeast(1f)
        val accuracy = (lockDurationSeconds / effectiveElapsed * 100f).coerceIn(0f, 100f)
        val avgReaction = if (lockAcquireSamples.isNotEmpty()) lockAcquireSamples.average().toLong() else 360L
        if (recordScore && elapsedSecondsTotal >= 2f) {
            viewModel.recordGameScore(
                gameId = "game_flight",
                gameTitle = "پرواز تمرکز خلبانی",
                score = score.coerceAtLeast(accuracy.toInt() * 5),
                accuracy = accuracy,
                reactionTime = avgReaction,
                difficulty = difficulty.label
            )
        }
    }

    // Fixed continuous 50ms simulation tick loop using rememberUpdatedState
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var tickCounter = 0
        while (isPlaying && timeLeftSec > 0) {
            delay(50L)
            if (currentIsPaused) continue

            tickCounter++
            elapsedSecondsTotal += 0.05f

            // Pick a fresh destination every ~2.5s scaled by difficulty
            val intervalTicks = (50f / currentDifficulty.targetSpeedFactor).toInt().coerceAtLeast(22)
            if (tickCounter % intervalTicks == 0) {
                goalX = 0.16f + Random.nextFloat() * 0.68f
                goalY = 0.20f + Random.nextFloat() * 0.55f
                lockStartMs = System.currentTimeMillis()
            }

            // Smooth interpolation toward goal
            val lerpRate = 0.035f * currentDifficulty.targetSpeedFactor
            targetX += (goalX - targetX) * lerpRate
            targetY += (goalY - targetY) * lerpRate

            // Gentle oscillating turbulence zone
            hazardX = 0.5f + 0.30f * sin(elapsedSecondsTotal * 1.15f * currentDifficulty.targetSpeedFactor)
            hazardY = 0.55f + 0.22f * sin(elapsedSecondsTotal * 0.85f)

            val distTarget = hypot(currentCrosshairX - targetX, currentCrosshairY - targetY)
            val distHazard = hypot(currentCrosshairX - hazardX, currentCrosshairY - hazardY)

            val wasLocked = isLockedOnTarget
            isLockedOnTarget = distTarget < currentDifficulty.lockRadius
            isInHazard = distHazard < currentDifficulty.hazardRadius

            if (isLockedOnTarget) {
                if (!wasLocked && lockStartMs > 0L) {
                    val delta = (System.currentTimeMillis() - lockStartMs).coerceIn(120L, 4000L)
                    lockAcquireSamples = (lockAcquireSamples + delta).takeLast(12)
                }
                lockDurationSeconds += 0.05f
                score += when (currentDifficulty) {
                    FlightDifficulty.EASY -> 2
                    FlightDifficulty.MEDIUM -> 3
                    FlightDifficulty.HARD -> 5
                }
            }
            if (isInHazard) {
                if (tickCounter % 10 == 0) hazardHits++
                score = (score - 2).coerceAtLeast(0)
            }

            if (tickCounter % 20 == 0) {
                timeLeftSec = (currentDuration - (tickCounter / 20)).coerceAtLeast(0)
            }
        }
        if (isPlaying && timeLeftSec <= 0) {
            finishMission(recordScore = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoDarkForest)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Cockpit Header HUD Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "کابین پرواز تمرکز خلبانی HUD",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LogoGold
                )
                Text(
                    text = if (bestRecordedScore > 0) "بهترین رکورد: $bestRecordedScore XP • امتیاز فعلی: $score"
                    else "امتیاز فعلی: $score XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBAE6FD)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (timeLeftSec < 5 && isPlaying) Color(0xFFF87171) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$timeLeftSec ثانیه",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (!isPlaying && !missionFinished) {
            GameGuideCard(
                title = "کابین خلبانی",
                goal = "نگهداری نشانگر پرواز روی هدف طلایی متحرک و دوری از منطقه قرمز تلاطم جوی.",
                controls = "سطح سختی و مدت مأموریت را انتخاب کنید و پس از شروع، انگشت خود را روی نمایشگر HUD بکشید.",
                initiallyExpanded = true
            )

            // Difficulty & Duration Selectors
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FlightDifficulty.entries.forEach { diff ->
                    Mind3DChip(
                        text = diff.label,
                        isSelected = difficulty == diff,
                        onClick = { difficulty = diff },
                        tone = Mind3DChipTone.Gold,
                        height = 42.dp
                    )
                }
                listOf(15, 25, 40).forEach { sec ->
                    Mind3DChip(
                        text = "$sec ثانیه",
                        isSelected = selectedDurationSec == sec,
                        onClick = {
                            selectedDurationSec = sec
                            timeLeftSec = sec
                        },
                        tone = Mind3DChipTone.Sky,
                        height = 42.dp
                    )
                }
            }
        }

        // HUD Cockpit Viewport (Rich Canvas with Horizon, Altitude/Speed Scales & Hazard Zone)
        val borderColor = when {
            isInHazard -> Color(0xFFEF4444)
            isLockedOnTarget -> Color(0xFF22C55E)
            else -> LogoGold
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.5.dp, borderColor, RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF061B2E), Color(0xFF051811), Color(0xFF0A2418))
                    )
                )
                .pointerInput(isPlaying, isPaused) {
                    if (isPlaying && !isPaused) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            crosshairX = (change.position.x / size.width).coerceIn(0.08f, 0.92f)
                            crosshairY = (change.position.y / size.height).coerceIn(0.10f, 0.90f)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Artificial Horizon & Pitch Ladder
                val pitchShift = (crosshairY - 0.5f) * h * 0.25f
                drawLine(
                    color = Color(0x5538BDF8),
                    start = Offset(w * 0.14f, h * 0.5f + pitchShift),
                    end = Offset(w * 0.86f, h * 0.5f + pitchShift),
                    strokeWidth = 2.5f
                )
                for (step in -2..2) {
                    if (step == 0) continue
                    val y = (h * 0.5f + pitchShift) + step * (h * 0.11f)
                    val span = if (step % 2 == 0) 0.18f else 0.11f
                    drawLine(
                        color = Color(0x30FFFFFF),
                        start = Offset(w * (0.5f - span), y),
                        end = Offset(w * (0.5f + span), y),
                        strokeWidth = 1.6f
                    )
                }

                // Side Altitude (Right) & Airspeed (Left) Tick Rails
                for (i in 0..8) {
                    val ty = h * (0.14f + i * 0.09f)
                    drawLine(Color(0x45A7F3D0), Offset(10.dp.toPx(), ty), Offset(22.dp.toPx(), ty), 2f)
                    drawLine(Color(0x45A7F3D0), Offset(w - 22.dp.toPx(), ty), Offset(w - 10.dp.toPx(), ty), 2f)
                }

                // Turbulence Hazard Zone (Red warning ring)
                val hx = hazardX * w
                val hy = hazardY * h
                val hazardPx = difficulty.hazardRadius * minOf(w, h)
                drawCircle(
                    color = Color(0x38EF4444),
                    radius = hazardPx,
                    center = Offset(hx, hy)
                )
                drawCircle(
                    color = Color(0xCCEF4444),
                    radius = hazardPx,
                    center = Offset(hx, hy),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Target Waypoint Ring (Gold / Locked Emerald)
                val tx = targetX * w
                val ty = targetY * h
                val targetRadiusPx = difficulty.lockRadius * minOf(w, h)
                drawCircle(
                    color = if (isLockedOnTarget) Color(0xFF22C55E) else LogoGold,
                    radius = targetRadiusPx,
                    center = Offset(tx, ty),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = if (isLockedOnTarget) Color(0x6622C55E) else Color(0x33F59E0B),
                    radius = targetRadiusPx * 0.35f,
                    center = Offset(tx, ty)
                )

                // User Crosshair Reticle (Sky-Cyan HUD sight)
                val cx = crosshairX * w
                val cy = crosshairY * h
                val crossArm = 26.dp.toPx()
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = 13.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawLine(Color(0xFF38BDF8), Offset(cx - crossArm, cy), Offset(cx + crossArm, cy), 2.5.dp.toPx())
                drawLine(Color(0xFF38BDF8), Offset(cx, cy - crossArm), Offset(cx, cy + crossArm), 2.5.dp.toPx())
                drawRect(
                    color = Color(0x2838BDF8),
                    topLeft = Offset(w * 0.32f, 6.dp.toPx()),
                    size = Size(w * 0.36f, 20.dp.toPx())
                )
            }

            // Live Cockpit Status Banner inside HUD
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                val statusText = when {
                    isPaused -> "مأموریت متوقف شد (Pause)"
                    isInHazard -> "هشدار تلاطم جوی! از دایره قرمز خارج شوید"
                    isLockedOnTarget -> "قفل روی هدف: تمرکز عالی! (+امتیاز پیوسته)"
                    isPlaying -> "نشانگر را روی حلقه طلایی نگه دارید"
                    else -> "کابین آماده پرواز — دکمه شروع را لمس کنید"
                }
                val statusColor = when {
                    isInHazard -> Color(0xFFF87171)
                    isLockedOnTarget -> Color(0xFF4ADE80)
                    else -> Color.White
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Compass / Telemetry Strip Bottom HUD
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val altitudeM = (1200 + (0.5f - crosshairY) * 600).toInt()
                val speedKt = if (isPlaying && !isPaused) 240 + (lockDurationSeconds * 4).toInt() else 0
                Text("ALT ${altitudeM}m", style = MaterialTheme.typography.labelSmall, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                Text("LOCK ${lockDurationSeconds.toInt()}s • ERR $hazardHits", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFDE047), fontWeight = FontWeight.Bold)
                Text("SPD ${speedKt}kt", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7DD3FC), fontWeight = FontWeight.Bold)
            }
        }

        if (missionFinished) {
            val accPercent = ((lockDurationSeconds / elapsedSecondsTotal.coerceAtLeast(1f)) * 100f).toInt().coerceIn(0, 100)
            val avgMs = if (lockAcquireSamples.isNotEmpty()) lockAcquireSamples.average().toLong() else 0L
            GameResultReportCard(
                gameTitle = "پرواز تمرکز خلبانی (${difficulty.label})",
                score = score,
                bestScore = bestRecordedScore,
                accuracyPercent = accPercent,
                reactionTimeMs = avgMs.takeIf { it > 0L },
                encouragement = when {
                    accPercent >= 65 -> "ثبات توجه شما در کابین عالی بود؛ قفل پیوسته روی هدف متحرک بسیار دقیق است."
                    accPercent >= 35 -> "عملکرد قابل قبول؛ با حرکت آرام‌تر انگشت، ثبات بیشتری در قفل هدف می‌گیرید."
                    else -> "برای شروع خوب است؛ در تمرین بعدی چند ثانیه با تنفس آرام چشم را روی هدف تثبیت کنید."
                },
                onPlayAgain = { startMission() }
            )
        } else if (!isPlaying) {
            Mind3DButton(
                text = "شروع مأموریت پرواز",
                onClick = { startMission() },
                variant = Mind3DButtonVariant.Gold,
                icon = Icons.Default.AirplanemodeActive,
                modifier = Modifier.fillMaxWidth().testTag("pilot_start_btn"),
                height = 52.dp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mind3DButton(
                    text = if (isPaused) "ادامه پرواز" else "توقف موقت",
                    onClick = { isPaused = !isPaused },
                    variant = if (isPaused) Mind3DButtonVariant.Mint else Mind3DButtonVariant.Lemon,
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    singleLine = true,
                    height = 48.dp,
                    modifier = Modifier.weight(1f).testTag("pilot_pause_btn")
                )
                Mind3DButton(
                    text = "مأموریت جدید",
                    onClick = { startMission() },
                    variant = Mind3DButtonVariant.Sky,
                    icon = Icons.Default.Refresh,
                    singleLine = true,
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )
                Mind3DButton(
                    text = "پایان",
                    onClick = { finishMission(recordScore = true) },
                    variant = Mind3DButtonVariant.Orange,
                    icon = Icons.Default.Stop,
                    singleLine = true,
                    height = 48.dp,
                    modifier = Modifier.weight(0.85f)
                )
            }
        }
    }
}
