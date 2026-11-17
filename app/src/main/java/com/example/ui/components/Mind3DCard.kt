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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class Mind3DCardTone {
    Sky,
    Peach,
    Mint,
    Lemon,
    Violet,
    Turquoise,
    Rose,
    Gold,
    WarmIvory,
    LemonChiffon,
    Apricot,
    IceCobalt,
    Lavender
}

private data class MindCardPalette(
    val topColor: Color,
    val bottomColor: Color,
    val borderColor: Color,
    val extrusionColor: Color,
    val particleColor: Color
)

private fun resolveCardPalette(tone: Mind3DCardTone): MindCardPalette = when (tone) {
    Mind3DCardTone.Sky, Mind3DCardTone.IceCobalt -> MindCardPalette(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF0284C7), Color(0xFF38BDF8))
    Mind3DCardTone.Peach, Mind3DCardTone.Apricot -> MindCardPalette(Color(0xFFFFF7ED), Color(0xFFFFEDD5), Color(0xFFFED7AA), Color(0xFFEA580C), Color(0xFFFB923C))
    Mind3DCardTone.Mint -> MindCardPalette(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFFBBF7D0), Color(0xFF16A34A), Color(0xFF4ADE80))
    Mind3DCardTone.Lemon, Mind3DCardTone.LemonChiffon -> MindCardPalette(Color(0xFFFEFCE8), Color(0xFFFEF9C3), Color(0xFFFEF08A), Color(0xFFCA8A04), Color(0xFFEAB308))
    Mind3DCardTone.Violet, Mind3DCardTone.Lavender -> MindCardPalette(Color(0xFFFAF5FF), Color(0xFFEDE9FE), Color(0xFFDDD6FE), Color(0xFF7C3AED), Color(0xFFA78BFA))
    Mind3DCardTone.Turquoise -> MindCardPalette(Color(0xFFF0FDFA), Color(0xFFCCFBF1), Color(0xFF99F6E4), Color(0xFF0D9488), Color(0xFF2DD4BF))
    Mind3DCardTone.Rose -> MindCardPalette(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFECDD3), Color(0xFFE11D48), Color(0xFFFB7185))
    Mind3DCardTone.Gold -> MindCardPalette(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFFF59E0B))
    Mind3DCardTone.WarmIvory -> MindCardPalette(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8))
}

@Composable
fun Mind3DCard(
    modifier: Modifier = Modifier,
    tone: Mind3DCardTone = Mind3DCardTone.WarmIvory,
    shapeRadius: Dp = 20.dp,
    extrusionDepth: Dp = 3.5.dp,
    borderWidth: Dp = 1.2.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "mind_3d_card",
    content: @Composable () -> Unit
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val particleProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val palette = resolveCardPalette(tone)
    val surfaceGradient = remember(palette) {
        Brush.verticalGradient(listOf(palette.topColor, palette.bottomColor))
    }

    val isClickable = onClick != null
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && isClickable && !interaction.effectiveReducedMotion) 0.985f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "card_scale"
    )
    val depthOffset by animateDpAsState(
        targetValue = if (isPressed && isClickable) 3.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "card_depth"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed && isClickable) 1.5.dp else 4.5.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "card_shadow"
    )

    val shape = RoundedCornerShape(shapeRadius)

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 320L && onClick != null) {
            lastClickTime = now
            interaction.triggerTapFeedback()
            if (!interaction.effectiveReducedMotion) {
                scope.launch {
                    particleProgress.snapTo(0f)
                    particleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
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
            .padding(bottom = extrusionDepth),
        contentAlignment = Alignment.TopCenter
    ) {
        // Lower 3D Extrusion
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = extrusionDepth)
                .clip(shape)
                .background(palette.extrusionColor.copy(alpha = 0.85f))
        )

        // Upper Card Surface
        Box(
            modifier = Modifier
                .offset(y = depthOffset)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = palette.extrusionColor.copy(alpha = 0.22f),
                    spotColor = palette.extrusionColor.copy(alpha = 0.30f)
                )
                .clip(shape)
                .background(surfaceGradient)
                .border(
                    width = if (isPressed && isClickable) (borderWidth + 0.8.dp) else borderWidth,
                    color = if (isPressed && isClickable) palette.particleColor else palette.borderColor.copy(alpha = 0.88f),
                    shape = shape
                )
                .then(
                    if (isClickable) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = handleSafeClick
                        )
                    } else Modifier
                )
        ) {
            // Specular sheen at top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.42f), Color.Transparent)
                        )
                    )
            )

            content()
        }

        // Glow Ring & Bounded Particle Burst on click
        val progress = particleProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val expand = progress * 8.dp.toPx()
                val auraAlpha = ((1f - progress) * 0.45f).coerceIn(0f, 1f)
                drawRoundRect(
                    color = palette.particleColor.copy(alpha = auraAlpha),
                    topLeft = Offset(-expand / 2f, -expand / 2f),
                    size = Size(size.width + expand, size.height + expand),
                    cornerRadius = CornerRadius(shapeRadius.toPx() + expand / 2f),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                val count = 6
                val radius = (24.dp.toPx() * progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                for (i in 0 until count) {
                    val angle = (i * (2 * Math.PI / count)).toFloat()
                    val px = (size.width / 2f) + (radius * cos(angle))
                    val py = (size.height / 2f) + (radius * sin(angle))
                    drawCircle(
                        color = if (i % 2 == 0) Color.White.copy(alpha = alpha) else palette.particleColor.copy(alpha = alpha),
                        radius = (2.4.dp.toPx() * (1f - progress * 0.45f)),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

enum class CalloutType {
    Technique,
    Practice,
    Reflection,
    Warning
}

typealias MindCalloutType = CalloutType

@Composable
fun MindCalloutBox(
    title: String = "",
    content: String = "",
    type: MindCalloutType = MindCalloutType.Technique,
    modifier: Modifier = Modifier
) {
    val (tone, icon, iconColor) = when (type) {
        CalloutType.Technique -> Triple(
            Mind3DCardTone.Lemon,
            Icons.Default.Lightbulb,
            Color(0xFFA16207)
        )
        CalloutType.Practice -> Triple(
            Mind3DCardTone.Mint,
            Icons.Default.AutoAwesome,
            Color(0xFF15803D)
        )
        CalloutType.Reflection -> Triple(
            Mind3DCardTone.Violet,
            Icons.Default.Psychology,
            Color(0xFF6D28D9)
        )
        CalloutType.Warning -> Triple(
            Mind3DCardTone.Peach,
            Icons.Default.Info,
            Color(0xFFC2410C)
        )
    }

    Mind3DCard(
        modifier = modifier.fillMaxWidth(),
        tone = tone,
        shapeRadius = 18.dp,
        extrusionDepth = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimaryLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.5.sp,
                color = TextSecondaryLight,
                lineHeight = 25.sp
            )
        }
    }
}
