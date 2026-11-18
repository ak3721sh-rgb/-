package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

const val GAME_EDUCATIONAL_DISCLAIMER =
    "نکته آموزشی: این تمرین صرفاً برای پرورش توجه، حافظه کاری و مهارت ارتباطی طراحی شده است و ابزار تشخیص یا درمان پزشکی نیست."

@Composable
fun GameGuideCard(
    title: String,
    goal: String,
    controls: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Mind3DCard(
        modifier = modifier.fillMaxWidth(),
        tone = Mind3DCardTone.WarmIvory,
        shapeRadius = 18.dp,
        extrusionDepth = 2.5.dp,
        onClick = { expanded = !expanded },
        testTag = "game_guide_card"
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "راهنمای تمرین: $title",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "بستن راهنما" else "باز کردن راهنما",
                    tint = TextSecondaryLight
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• هدف: $goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryLight,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "• روش اجرا: $controls",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameControlBar(
    isRunning: Boolean,
    isPaused: Boolean,
    bestScoreLabel: String?,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
    pauseTestTag: String = "game_pause_btn",
    restartTestTag: String = "game_restart_btn"
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!bestScoreLabel.isNullOrBlank()) {
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Gold,
                shapeRadius = 14.dp,
                extrusionDepth = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bestScoreLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isRunning) {
                Mind3DButton(
                    text = if (isPaused) "ادامه تمرین" else "توقف موقت",
                    onClick = onTogglePause,
                    variant = if (isPaused) Mind3DButtonVariant.Mint else Mind3DButtonVariant.Lemon,
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    singleLine = true,
                    height = 48.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(pauseTestTag)
                )
            }
            Mind3DButton(
                text = "شروع مجدد",
                onClick = onRestart,
                variant = Mind3DButtonVariant.Neutral,
                icon = Icons.Default.Refresh,
                singleLine = true,
                height = 48.dp,
                modifier = Modifier
                    .weight(1f)
                    .testTag(restartTestTag)
            )
        }
    }
}

@Composable
fun GameResultReportCard(
    gameTitle: String,
    score: Int,
    bestScore: Int,
    accuracyPercent: Int,
    reactionTimeMs: Long? = null,
    encouragement: String,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNewRecord = score >= bestScore && score > 0
    Mind3DCard(
        modifier = modifier.fillMaxWidth(),
        tone = Mind3DCardTone.Mint,
        shapeRadius = 22.dp,
        extrusionDepth = 4.dp,
        testTag = "game_result_report_card"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LogoGold, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "گزارش عملکرد: $gameTitle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
            }

            if (isNewRecord) {
                Text(
                    text = "رکورد جدید شخصی ثبت شد!",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MintPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReportStatCol("امتیاز نهایی", "$score XP", SkyBluePrimary)
                ReportStatCol("دقت عملکرد", "$accuracyPercent%", Color(0xFF15803D))
                if (reactionTimeMs != null && reactionTimeMs > 0L) {
                    ReportStatCol("سرعت پاسخ", "${reactionTimeMs}ms", Color(0xFFB45309))
                } else {
                    ReportStatCol("بهترین رکورد", "${maxOf(score, bestScore)} XP", Color(0xFFB45309))
                }
            }

            Text(
                text = encouragement,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimaryLight,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = GAME_EDUCATIONAL_DISCLAIMER,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Mind3DButton(
                text = "اجرای دوباره تمرین",
                onClick = onPlayAgain,
                variant = Mind3DButtonVariant.Sky,
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp
            )
        }
    }
}

@Composable
private fun ReportStatCol(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
