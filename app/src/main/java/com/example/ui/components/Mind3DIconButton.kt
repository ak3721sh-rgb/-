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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ButtonSpecularHighlight
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.LogoGold
import com.example.ui.theme.LogoGoldLight
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.PeachLight
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SpiritualVioletLight
import com.example.ui.theme.SpiritualVioletPrimary
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class Mind3DIconVariant {
    Sky,
    Peach,
    Mint,
    Violet,
    Lemon,
    Gold,
    Rose,
    Turquoise,
    Danger,
    Neutral
}

private data class MindIconPalette(
    val topColor: Color,
    val bottomColor: Color,
    val shadowColor: Color,
    val tintColor: Color,
    val particleColor: Color
)

private fun resolveIconPalette(variant: Mind3DIconVariant): MindIconPalette = when (variant) {
    Mind3DIconVariant.Sky -> MindIconPalette(SkyBlueLight, SkyBluePrimary, Color(0xFF0369A1), Color.White, Color(0xFF38BDF8))
    Mind3DIconVariant.Peach -> MindIconPalette(PeachLight, PeachPrimary, Color(0xFFC2410C), Color.White, Color(0xFFFB923C))
    Mind3DIconVariant.Mint -> MindIconPalette(MintLight, MintPrimary, Color(0xFF15803D), Color.White, Color(0xFF4ADE80))
    Mind3DIconVariant.Violet -> MindIconPalette(SpiritualVioletLight, SpiritualVioletPrimary, Color(0xFF6D28D9), Color.White, Color(0xFFA78BFA))
    Mind3DIconVariant.Lemon -> MindIconPalette(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFA16207), Color(0xFF451A03), Color(0xFFFACC15))
    Mind3DIconVariant.Gold -> MindIconPalette(LogoGoldLight, LogoGold, Color(0xFF854D0E), Color(0xFF2E1901), Color(0xFFFFD54F))
    Mind3DIconVariant.Rose -> MindIconPalette(Color(0xFFFB7185), Color(0xFFF43F5E), Color(0xFFBE123C), Color.White, Color(0xFFFDA4AF))
    Mind3DIconVariant.Turquoise -> MindIconPalette(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E), Color.White, Color(0xFF5EEAD4))
    Mind3DIconVariant.Danger -> MindIconPalette(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B), Color.White, Color(0xFFF87171))
    Mind3DIconVariant.Neutral -> MindIconPalette(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF0F172A), Color(0xFF94A3B8))
}

@Composable
fun Mind3DIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    variant: Mind3DIconVariant = Mind3DIconVariant.Sky,
    buttonSize: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    testTag: String = "mind_3d_icon_button",
    enabled: Boolean = true
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val particleProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val palette = resolveIconPalette(variant)
    val surfaceGradient = remember(enabled, palette) {
        if (enabled) {
            Brush.verticalGradient(listOf(palette.topColor, palette.bottomColor))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !interaction.effectiveReducedMotion) 0.94f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "icon_btn_scale"
    )

    val depthOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.2.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "icon_btn_depth"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed || !enabled) 1.dp else 4.5.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "icon_btn_shadow"
    )

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 300L && enabled) {
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
            .size(buttonSize)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Lower 3D extrusion block
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 3.2.dp)
                .clip(shape)
                .background(if (enabled) palette.shadowColor else Color(0xFF94A3B8))
        )

        // Upper Button Face
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = depthOffset)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = palette.shadowColor.copy(alpha = 0.3f),
                    spotColor = palette.shadowColor.copy(alpha = 0.4f)
                )
                .clip(shape)
                .background(brush = surfaceGradient)
                .border(
                    width = 1.dp,
                    color = if (isPressed) palette.particleColor else Color.White.copy(alpha = 0.55f),
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = handleSafeClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular sheen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                ButtonSpecularHighlight.copy(alpha = 0.42f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) palette.tintColor else Color(0xFF475569),
                modifier = Modifier.size(buttonSize * 0.50f)
            )
        }

        // Subtle Glow Ring & Bounded Micro-Particles
        val progress = particleProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val expand = progress * 7.dp.toPx()
                val ringAlpha = ((1f - progress) * 0.45f).coerceIn(0f, 1f)
                if (shape == CircleShape) {
                    drawCircle(
                        color = palette.particleColor.copy(alpha = ringAlpha),
                        radius = (size.minDimension / 2f) + expand * 0.55f,
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                } else {
                    val cornerPx = 16.dp.toPx() + (expand * 0.35f)
                    drawRoundRect(
                        color = palette.particleColor.copy(alpha = ringAlpha),
                        topLeft = Offset(-expand / 2f, -expand / 2f),
                        size = Size(size.width + expand, size.height + expand),
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }

                val count = 6
                val radius = (18.dp.toPx() * progress) + (size.minDimension * 0.25f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                for (i in 0 until count) {
                    val angle = (i * (2 * Math.PI / count)).toFloat()
                    val px = (size.width / 2f) + (radius * cos(angle))
                    val py = (size.height / 2f) + (radius * sin(angle))
                    drawCircle(
                        color = if (i % 2 == 0) Color.White.copy(alpha = alpha) else palette.particleColor.copy(alpha = alpha),
                        radius = (2.1.dp.toPx() * (1f - progress * 0.45f)).coerceAtLeast(1f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
