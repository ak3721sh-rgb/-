package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.LogoGold
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

/**
 * Unified Inner-Screen Header displaying:
 * - Tactile 3D Back button (48dp touch target)
 * - Compact Official App Emblem Logo + Instructor Miniature Avatar
 * - Readable Persian Title & Subtitle (wrapping safely up to 2 lines on compact widths)
 * - Optional quick action buttons & interaction settings
 */
@Composable
fun Mind3DTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    backButtonVariant: Mind3DIconVariant = Mind3DIconVariant.Sky,
    showSettingsAction: Boolean = false,
    onInstructorBadgeClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val interaction = LocalInteractionController.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF5FAFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Mind3DIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBackClick,
                    variant = backButtonVariant,
                    buttonSize = 48.dp,
                    contentDescription = "بازگشت",
                    testTag = "topbar_back_button"
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Compact Official Logo + Instructor Miniature Photo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .then(
                            if (onInstructorBadgeClick != null) {
                                Modifier.clickable { onInstructorBadgeClick() }
                            } else {
                                Modifier
                            }
                        )
                        .testTag("topbar_brand_badges")
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFEFF))
                            .border(1.2.dp, LogoGold.copy(alpha = 0.8f), CircleShape)
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.official_logo),
                            contentDescription = "نشان رسمی هنر ارتباط ذهن‌ها",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .testTag("topbar_official_logo"),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFBEB))
                            .border(1.2.dp, LogoGold, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mentor_studio),
                            contentDescription = "تصویر استاد اکبر شیرزادخانی",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .testTag("topbar_instructor_photo"),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        lineHeight = 20.sp,
                        color = TextPrimaryLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle ?: "مدرس: اکبر شیرزادخانی",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.5.sp,
                        color = TextSecondaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (actions != null || showSettingsAction) {
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (actions != null) {
                        actions()
                    }
                    if (showSettingsAction) {
                        Mind3DIconButton(
                            icon = Icons.Default.Tune,
                            onClick = { interaction.openSettingsDialog() },
                            variant = Mind3DIconVariant.Neutral,
                            buttonSize = 48.dp,
                            contentDescription = "تنظیمات تعامل و مطالعه",
                            testTag = "topbar_settings_button"
                        )
                    }
                }
            }
        }
    }
}
