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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButterYellowGlow
import com.example.ui.theme.ButterYellowLight
import com.example.ui.theme.ButterYellowPrimary
import com.example.ui.theme.Button3DShadowGold
import com.example.ui.theme.Button3DShadowMint
import com.example.ui.theme.Button3DShadowNeutral
import com.example.ui.theme.Button3DShadowPeach
import com.example.ui.theme.Button3DShadowSky
import com.example.ui.theme.Button3DShadowViolet
import com.example.ui.theme.Button3DShadowYellow
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

enum class Mind3DButtonVariant {
    Sky,
    Peach,
    Mint,
    Violet,
    Lemon,
    Gold,
    Neutral,
    Orange,
    Emerald,
    Rose,
    Turquoise,
    Danger
}

private data class MindButtonPalette(
    val topColor: Color,
    val bottomColor: Color,
    val shadowColor: Color,
    val textColor: Color,
    val glowColor: Color
)

private fun resolveButtonPalette(variant: Mind3DButtonVariant): MindButtonPalette = when (variant) {
    Mind3DButtonVariant.Sky -> MindButtonPalette(SkyBlueLight, SkyBluePrimary, Button3DShadowSky, Color.White, Color(0xFF38BDF8))
    Mind3DButtonVariant.Peach, Mind3DButtonVariant.Orange -> MindButtonPalette(PeachLight, PeachPrimary, Button3DShadowPeach, Color.White, Color(0xFFFB923C))
    Mind3DButtonVariant.Mint, Mind3DButtonVariant.Emerald -> MindButtonPalette(MintLight, MintPrimary, Button3DShadowMint, Color.White, Color(0xFF4ADE80))
    Mind3DButtonVariant.Violet -> MindButtonPalette(SpiritualVioletLight, SpiritualVioletPrimary, Button3DShadowViolet, Color.White, Color(0xFFA78BFA))
    Mind3DButtonVariant.Lemon -> MindButtonPalette(ButterYellowLight, ButterYellowPrimary, Button3DShadowYellow, Color(0xFF451A03), Color(0xFFFACC15))
    Mind3DButtonVariant.Gold -> MindButtonPalette(LogoGoldLight, LogoGold, Button3DShadowGold, Color(0xFF2E1901), Color(0xFFFFD54F))
    Mind3DButtonVariant.Rose -> MindButtonPalette(Color(0xFFFB7185), Color(0xFFF43F5E), Color(0xFFBE123C), Color.White, Color(0xFFFDA4AF))
    Mind3DButtonVariant.Turquoise -> MindButtonPalette(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E), Color.White, Color(0xFF5EEAD4))
    Mind3DButtonVariant.Danger -> MindButtonPalette(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B), Color.White, Color(0xFFF87171))
    Mind3DButtonVariant.Neutral -> MindButtonPalette(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Button3DShadowNeutral, Color(0xFF0F172A), Color(0xFF94A3B8))
}

@Composable
fun Mind3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: Mind3DButtonVariant = Mind3DButtonVariant.Sky,
    icon: ImageVector? = null,
    subText: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
    singleLine: Boolean = false,
    testTag: String = "mind_3d_button",
    height: Dp = 54.dp,
    shapeRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val particleProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val palette = resolveButtonPalette(variant)
    val extrusion = 5.dp

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading && !interaction.effectiveReducedMotion) 0.97f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "btn_scale"
    )

    val depthOffset by animateDpAsState(
        targetValue = if (isPressed && enabled && !isLoading) 3.8.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "btn_depth"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed || !enabled) 1.5.dp else 5.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "btn_shadow"
    )

    val shape = RoundedCornerShape(shapeRadius)
    val surfaceGradient = remember(enabled, palette) {
        if (enabled) {
            Brush.verticalGradient(listOf(palette.topColor, palette.bottomColor))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
        }
    }

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 320L && enabled && !isLoading) {
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

    val buttonModifier = if (enabled && !isLoading) {
        modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = handleSafeClick
        )
    } else {
        modifier
    }

    Box(
        modifier = buttonModifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .defaultMinSize(minHeight = height + extrusion)
            .drawBehind {
                val cornerPx = shapeRadius.toPx()
                val extrusionPx = extrusion.toPx()
                if (enabled) {
                    drawRoundRect(
                        color = palette.shadowColor,
                        topLeft = Offset(0f, extrusionPx * 0.55f),
                        size = Size(size.width, size.height - extrusionPx * 0.55f),
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                }
            }
            .padding(bottom = extrusion)
    ) {
        // Main Button Face
        Box(
            modifier = Modifier
                .offset(y = depthOffset)
                .fillMaxWidth()
                .defaultMinSize(minHeight = height)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = palette.shadowColor.copy(alpha = 0.28f),
                    spotColor = palette.shadowColor.copy(alpha = 0.36f),
                    clip = false
                )
                .clip(shape)
                .background(brush = surfaceGradient)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.4.dp,
                            color = ButterYellowGlow,
                            shape = shape
                        )
                    } else {
                        Modifier.border(
                            width = 1.1.dp,
                            color = if (isPressed) palette.glowColor else Color.White.copy(alpha = 0.55f),
                            shape = shape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular Top Gloss Highlight
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val highlightHeight = size.height * 0.42f
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(ButtonSpecularHighlight.copy(alpha = 0.65f), Color.Transparent)
                            ),
                            size = Size(size.width, highlightHeight),
                            cornerRadius = CornerRadius(shapeRadius.toPx(), shapeRadius.toPx())
                        )
                    }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = palette.textColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) palette.textColor else Color(0xFF334155),
                            modifier = Modifier.size(if (height < 48.dp) 18.dp else 20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = text,
                            color = if (enabled) palette.textColor else Color(0xFF475569),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = when {
                                    subText != null -> 15.sp
                                    height <= 46.dp -> 14.sp
                                    singleLine -> 15.sp
                                    else -> 16.sp
                                },
                                lineHeight = if (singleLine) 18.sp else 21.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = if (singleLine) 1 else 2,
                            softWrap = !singleLine,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subText != null) {
                            Text(
                                text = subText,
                                color = if (enabled) palette.textColor.copy(alpha = 0.94f) else Color(0xFF475569),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Bounded Micro-Particle Burst on Tap
        val progress = particleProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val expand = progress * 8.dp.toPx()
                val auraAlpha = ((1f - progress) * 0.45f).coerceIn(0f, 1f)
                drawRoundRect(
                    color = palette.glowColor.copy(alpha = auraAlpha),
                    topLeft = Offset(-expand / 2f, -expand / 2f),
                    size = Size(size.width + expand, size.height + expand),
                    cornerRadius = CornerRadius(shapeRadius.toPx() + expand / 2f),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                val particleCount = 6
                val radius = (26.dp.toPx() * progress)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                for (i in 0 until particleCount) {
                    val angle = (i * (2 * Math.PI / particleCount)).toFloat()
                    val px = (size.width / 2f) + (radius * cos(angle))
                    val py = (size.height / 2f) + (radius * sin(angle))
                    val sparkColor = if (i % 2 == 0) Color.White.copy(alpha = alpha) else palette.glowColor.copy(alpha = alpha)
                    drawCircle(
                        color = sparkColor,
                        radius = (2.4.dp.toPx() * (1f - (progress * 0.45f))),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
