package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.LogoGold
import com.example.ui.theme.SkyBlueLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Mind3DAvatarButton(
    @DrawableRes drawableRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    isCircular: Boolean = false,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = LogoGold,
    extrusionColor: Color = Color(0xFF9A7B1C),
    glowColor: Color = SkyBlueLight,
    backgroundColor: Color = Color(0xFF0B2117),
    testTag: String = "mind_3d_avatar_button"
) {
    val interaction = LocalInteractionController.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val effectProgress = remember { Animatable(0f) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val extrusion = 4.dp
    val shape: Shape = if (isCircular) CircleShape else RoundedCornerShape(cornerRadius)

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !interaction.effectiveReducedMotion) 0.96f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "avatar_scale"
    )
    val depthOffset by animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "avatar_depth"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "avatar_shadow"
    )

    val handleSafeClick: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 320L) {
            lastClickTime = now
            interaction.triggerTapFeedback()
            if (!interaction.effectiveReducedMotion) {
                scope.launch {
                    effectProgress.snapTo(0f)
                    effectProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    )
                    effectProgress.snapTo(0f)
                }
            }
            onClick()
        }
    }

    Box(
        modifier = modifier
            .size(width = size, height = size + extrusion)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(bottom = extrusion)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // 3D base extrusion
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = extrusion)
                .clip(shape)
                .background(extrusionColor)
        )

        // Interactive face
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = depthOffset)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = borderColor.copy(alpha = 0.35f),
                    spotColor = borderColor.copy(alpha = 0.45f)
                )
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = if (isPressed) 2.8.dp else 2.2.dp,
                    color = if (isPressed) glowColor else borderColor,
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = handleSafeClick
                )
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtle aura + delicate micro-particles on tap
        val progress = effectProgress.value
        if (!interaction.effectiveReducedMotion && progress > 0f && progress < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val auraAlpha = ((1f - progress) * 0.48f).coerceIn(0f, 1f)
                val expand = progress * 8.dp.toPx()
                if (isCircular) {
                    drawCircle(
                        color = glowColor.copy(alpha = auraAlpha),
                        radius = (this.size.minDimension / 2f) + expand,
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
                    )
                } else {
                    val r = cornerRadius.toPx() + (expand * 0.5f)
                    drawRoundRect(
                        color = glowColor.copy(alpha = auraAlpha),
                        topLeft = Offset(-expand / 2f, -expand / 2f),
                        size = androidx.compose.ui.geometry.Size(this.size.width + expand, this.size.height + expand),
                        cornerRadius = CornerRadius(r, r),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
                    )
                }

                val particleCount = 6
                val outerRadius = (this.size.minDimension / 2f) + (progress * 15.dp.toPx())
                val pAlpha = ((1f - progress) * 0.85f).coerceIn(0f, 1f)
                for (i in 0 until particleCount) {
                    val angle = (i * (2.0 * Math.PI / particleCount)).toFloat()
                    val px = (this.size.width / 2f) + (outerRadius * cos(angle))
                    val py = (this.size.height / 2f) + (outerRadius * sin(angle))
                    drawCircle(
                        color = if (i % 2 == 0) borderColor.copy(alpha = pAlpha) else Color.White.copy(alpha = pAlpha),
                        radius = (2.2.dp.toPx() * (1f - progress * 0.45f)).coerceAtLeast(1f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
