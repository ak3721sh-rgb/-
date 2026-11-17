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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class SectionSquareTheme(
    val gradientTop: Color,
    val gradientBottom: Color,
    val borderColor: Color,
    val extrusionShadowColor: Color,
    val iconBadgeColor: Color,
    val iconBadgeBorder: Color,
    val iconTint: Color,
    val particleColor: Color,
    val auraColor: Color
)

object SectionSquareThemes {
    val SkyBlue = SectionSquareTheme(
        gradientTop = Color(0xFFF0F9FF),
        gradientBottom = Color(0xFFBAE6FD),
        borderColor = Color(0xFF7DD3FC),
        extrusionShadowColor = Color(0xFF0284C7),
        iconBadgeColor = Color(0xFF0284C7).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFF38BDF8).copy(alpha = 0.5f),
        iconTint = Color(0xFF0369A1),
        particleColor = Color(0xFF38BDF8),
        auraColor = Color(0xFF38BDF8).copy(alpha = 0.40f)
    )

    val Peach = SectionSquareTheme(
        gradientTop = Color(0xFFFFF7ED),
        gradientBottom = Color(0xFFFED7AA),
        borderColor = Color(0xFFFDBA74),
        extrusionShadowColor = Color(0xFFEA580C),
        iconBadgeColor = Color(0xFFEA580C).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFFFB923C).copy(alpha = 0.5f),
        iconTint = Color(0xFFC2410C),
        particleColor = Color(0xFFFB923C),
        auraColor = Color(0xFFFB923C).copy(alpha = 0.40f)
    )

    val LemonGold = SectionSquareTheme(
        gradientTop = Color(0xFFFEFCE8),
        gradientBottom = Color(0xFFFEF08A),
        borderColor = Color(0xFFFDE047),
        extrusionShadowColor = Color(0xFFCA8A04),
        iconBadgeColor = Color(0xFFCA8A04).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFFFACC15).copy(alpha = 0.5f),
        iconTint = Color(0xFFA16207),
        particleColor = Color(0xFFEAB308),
        auraColor = Color(0xFFFDE047).copy(alpha = 0.45f)
    )

    val MintSage = SectionSquareTheme(
        gradientTop = Color(0xFFF0FDF4),
        gradientBottom = Color(0xFFBBF7D0),
        borderColor = Color(0xFF86EFAC),
        extrusionShadowColor = Color(0xFF16A34A),
        iconBadgeColor = Color(0xFF16A34A).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFF4ADE80).copy(alpha = 0.5f),
        iconTint = Color(0xFF15803D),
        particleColor = Color(0xFF22C55E),
        auraColor = Color(0xFF4ADE80).copy(alpha = 0.40f)
    )

    val Turquoise = SectionSquareTheme(
        gradientTop = Color(0xFFF0FDFA),
        gradientBottom = Color(0xFF99F6E4),
        borderColor = Color(0xFF5EEAD4),
        extrusionShadowColor = Color(0xFF0D9488),
        iconBadgeColor = Color(0xFF0D9488).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFF2DD4BF).copy(alpha = 0.5f),
        iconTint = Color(0xFF0F766E),
        particleColor = Color(0xFF14B8A6),
        auraColor = Color(0xFF2DD4BF).copy(alpha = 0.40f)
    )

    val SoftLilac = SectionSquareTheme(
        gradientTop = Color(0xFFFAF5FF),
        gradientBottom = Color(0xFFDDD6FE),
        borderColor = Color(0xFFC4B5FD),
        extrusionShadowColor = Color(0xFF7C3AED),
        iconBadgeColor = Color(0xFF7C3AED).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFFA78BFA).copy(alpha = 0.5f),
        iconTint = Color(0xFF6D28D9),
        particleColor = Color(0xFFA78BFA),
        auraColor = Color(0xFFA78BFA).copy(alpha = 0.40f)
    )

    val IceCobalt = SectionSquareTheme(
        gradientTop = Color(0xFFECFEFF),
        gradientBottom = Color(0xFFA5F3FC),
        borderColor = Color(0xFF67E8F9),
        extrusionShadowColor = Color(0xFF0891B2),
        iconBadgeColor = Color(0xFF0891B2).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFF22D3EE).copy(alpha = 0.5f),
        iconTint = Color(0xFF0E7490),
        particleColor = Color(0xFF06B6D4),
        auraColor = Color(0xFF22D3EE).copy(alpha = 0.40f)
    )

    val SoftRose = SectionSquareTheme(
        gradientTop = Color(0xFFFFF1F2),
        gradientBottom = Color(0xFFFECDD3),
        borderColor = Color(0xFFFDA4AF),
        extrusionShadowColor = Color(0xFFE11D48),
        iconBadgeColor = Color(0xFFE11D48).copy(alpha = 0.16f),
        iconBadgeBorder = Color(0xFFFB7185).copy(alpha = 0.5f),
        iconTint = Color(0xFFBE123C),
        particleColor = Color(0xFFF43F5E),
        auraColor = Color(0xFFFB7185).copy(alpha = 0.40f)
    )
}

@Composable
fun InteractiveSectionSquareButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    theme: SectionSquareTheme,
    testTag: String,
    modifier: Modifier = Modifier,
    height: Dp = 142.dp,
    shapeRadius: Dp = 20.dp,
    onClick: () -> Unit
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val particleProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val extrusion = 4.dp
    val shape = RoundedCornerShape(shapeRadius)
    val surfaceGradient = remember(theme) {
        Brush.verticalGradient(colors = listOf(theme.gradientTop, theme.gradientBottom))
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !interaction.effectiveReducedMotion) 0.968f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "sq_btn_scale"
    )
    val depthOffset by animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "sq_btn_depth"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 1.5.dp else 5.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "sq_btn_shadow"
    )

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 320L) {
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
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(bottom = extrusion)
            .testTag(testTag)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Lower 3D Extrusion Block matching adaptive height
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = extrusion)
                .clip(shape)
                .background(theme.extrusionShadowColor)
        )

        // 2. Upper Button Face with adaptive minHeight & spring offset
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = height)
                .offset(y = depthOffset)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = theme.extrusionShadowColor.copy(alpha = 0.30f),
                    spotColor = theme.extrusionShadowColor.copy(alpha = 0.38f)
                )
                .clip(shape)
                .background(brush = surfaceGradient)
                .border(
                    width = if (isPressed) 2.dp else 1.2.dp,
                    color = if (isPressed) theme.particleColor else theme.borderColor.copy(alpha = 0.90f),
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = handleSafeClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular Top Sheen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.58f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.iconBadgeColor)
                        .border(
                            width = 1.1.dp,
                            color = theme.iconBadgeBorder,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = theme.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 19.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. Canvas Layer: Controlled Glow Ring & Fine Sparkle Particles
        val progress = particleProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val baseRadiusX = (size.width / 2f) * 0.94f
                val baseRadiusY = (size.height / 2f) * 0.94f

                val ringExpansion = progress * 10.dp.toPx()
                val ringAlpha = ((1f - progress) * 0.55f).coerceIn(0f, 1f)
                drawRoundRect(
                    color = theme.auraColor.copy(alpha = ringAlpha),
                    topLeft = Offset(-ringExpansion / 2f, -ringExpansion / 2f),
                    size = Size(size.width + ringExpansion, size.height + ringExpansion),
                    cornerRadius = CornerRadius(shapeRadius.toPx() + ringExpansion / 2f),
                    style = Stroke(width = 2.dp.toPx() * (1f - progress * 0.4f))
                )

                val count = 8
                for (i in 0 until count) {
                    val angle = (i * (2.0 * Math.PI / count) + (Math.PI / 10.0)).toFloat()
                    val extraDist = progress * (15.dp.toPx() + (i % 3) * 3.dp.toPx())
                    val px = centerX + (baseRadiusX + extraDist) * cos(angle)
                    val py = centerY + (baseRadiusY + extraDist) * sin(angle)
                    val pAlpha = ((1f - progress) * 0.85f).coerceIn(0f, 1f)
                    val pRadius = (2.4.dp.toPx() * (1f - progress * 0.5f)).coerceAtLeast(1f)

                    drawCircle(
                        color = if (i % 2 == 0) Color.White.copy(alpha = pAlpha) else theme.particleColor.copy(alpha = pAlpha),
                        radius = pRadius,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
