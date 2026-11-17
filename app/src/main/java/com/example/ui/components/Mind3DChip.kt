package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class MindChipTone {
    Sky,
    Peach,
    Mint,
    Lemon,
    Violet,
    Rose,
    Turquoise,
    Gold,
    Danger
}

typealias Mind3DChipTone = MindChipTone

private data class MindChipPalette(
    val activeTop: Color,
    val activeBottom: Color,
    val activeShadow: Color,
    val activeText: Color,
    val particleColor: Color
)

private fun resolveChipPalette(tone: MindChipTone): MindChipPalette = when (tone) {
    MindChipTone.Sky -> MindChipPalette(Color(0xFF38BDF8), Color(0xFF0288D1), Color(0xFF0369A1), Color.White, Color(0xFF38BDF8))
    MindChipTone.Peach -> MindChipPalette(Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFFC2410C), Color.White, Color(0xFFFB923C))
    MindChipTone.Mint -> MindChipPalette(Color(0xFF4ADE80), Color(0xFF16A34A), Color(0xFF15803D), Color.White, Color(0xFF4ADE80))
    MindChipTone.Lemon -> MindChipPalette(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFA16207), Color(0xFF451A03), Color(0xFFEAB308))
    MindChipTone.Violet -> MindChipPalette(Color(0xFFA78BFA), Color(0xFF7C3AED), Color(0xFF6D28D9), Color.White, Color(0xFFA78BFA))
    MindChipTone.Rose -> MindChipPalette(Color(0xFFFB7185), Color(0xFFF43F5E), Color(0xFFBE123C), Color.White, Color(0xFFFDA4AF))
    MindChipTone.Turquoise -> MindChipPalette(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E), Color.White, Color(0xFF2DD4BF))
    MindChipTone.Gold -> MindChipPalette(Color(0xFFFFD54F), Color(0xFFD4AF37), Color(0xFF854D0E), Color(0xFF2E1901), Color(0xFFFFD54F))
    MindChipTone.Danger -> MindChipPalette(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B), Color.White, Color(0xFFF87171))
}

@Composable
fun Mind3DChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null,
    tone: MindChipTone = MindChipTone.Sky,
    height: Dp = 44.dp,
    shapeRadius: Dp = 22.dp,
    testTag: String = "mind_3d_chip"
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val particleProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val palette = resolveChipPalette(tone)
    val shape = RoundedCornerShape(shapeRadius)
    val extrusion = 3.dp

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !interaction.effectiveReducedMotion) 0.96f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "chip_scale"
    )

    val depthOffset by animateDpAsState(
        targetValue = if (isPressed || isSelected) 2.5.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "chip_depth"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 1.dp else 3.5.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "chip_shadow"
    )

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 300L) {
            lastClickTime = now
            interaction.triggerTapFeedback()
            if (!interaction.effectiveReducedMotion) {
                scope.launch {
                    particleProgress.snapTo(0f)
                    particleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    )
                    particleProgress.snapTo(0f)
                }
            }
            onClick()
        }
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(bottom = extrusion),
        contentAlignment = Alignment.Center
    ) {
        // Lower Extrusion Block
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = extrusion)
                .clip(shape)
                .background(if (isSelected) palette.activeShadow else Color(0xFFCBD5E1))
        )

        // Upper Chip Face
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = height)
                .offset(y = depthOffset)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = if (isSelected) palette.activeShadow.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
                )
                .clip(shape)
                .background(
                    brush = if (isSelected) {
                        Brush.verticalGradient(listOf(palette.activeTop, palette.activeBottom))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)))
                    }
                )
                .border(
                    width = 1.2.dp,
                    color = if (isSelected || isPressed) palette.activeTop else Color(0xFFCBD5E1),
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = handleSafeClick
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) palette.activeText else TextSecondaryLight,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = if (isSelected) palette.activeText else TextPrimaryLight,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) palette.activeText else TextPrimaryLight
                        )
                    }
                }
            }
        }

        // Glow Ring & Particle Burst
        val progress = particleProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val expand = progress * 7.dp.toPx()
                val ringAlpha = ((1f - progress) * 0.45f).coerceIn(0f, 1f)
                drawRoundRect(
                    color = palette.particleColor.copy(alpha = ringAlpha),
                    topLeft = Offset(-expand / 2f, -expand / 2f),
                    size = Size(size.width + expand, size.height + expand),
                    cornerRadius = CornerRadius(shapeRadius.toPx() + expand / 2f),
                    style = Stroke(width = 1.6.dp.toPx())
                )

                val count = 6
                val radius = (16.dp.toPx() * progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                for (i in 0 until count) {
                    val angle = (i * (2 * Math.PI / count)).toFloat()
                    val px = (size.width / 2f) + (radius * cos(angle))
                    val py = (size.height / 2f) + (radius * sin(angle))
                    drawCircle(
                        color = if (i % 2 == 0) Color.White.copy(alpha = alpha) else palette.particleColor.copy(alpha = alpha),
                        radius = (2.dp.toPx() * (1f - progress * 0.5f)),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
