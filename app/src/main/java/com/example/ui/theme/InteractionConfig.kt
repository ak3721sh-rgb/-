package com.example.ui.theme

import android.content.Context
import android.provider.Settings
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone

@Immutable
data class InteractionSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val readerFontSizeSp: Float = 18f
)

object InteractionPreferencesStore {
    private const val PREFS_NAME = "mind_ui_interaction_prefs"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_HAPTICS = "haptics_enabled"
    private const val KEY_REDUCED_MOTION = "reduced_motion_enabled"
    private const val KEY_READER_FONT_SIZE = "reader_font_size_sp"

    fun load(context: Context): InteractionSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return InteractionSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            reducedMotionEnabled = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            readerFontSizeSp = prefs.getFloat(KEY_READER_FONT_SIZE, 18f).coerceIn(15f, 24f)
        )
    }

    fun save(context: Context, settings: InteractionSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SOUND, settings.soundEnabled)
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putBoolean(KEY_REDUCED_MOTION, settings.reducedMotionEnabled)
            .putFloat(KEY_READER_FONT_SIZE, settings.readerFontSizeSp.coerceIn(15f, 24f))
            .apply()
    }

    fun isSystemReducedMotion(context: Context): Boolean {
        return try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}

class InteractionController(
    val settings: InteractionSettings,
    val effectiveReducedMotion: Boolean,
    private val view: View?,
    private val haptic: HapticFeedback?,
    private val onUpdate: (InteractionSettings) -> Unit,
    private val onOpenSettings: () -> Unit
) {
    fun triggerTapFeedback() {
        if (settings.soundEnabled) {
            try {
                view?.playSoundEffect(SoundEffectConstants.CLICK)
            } catch (_: Exception) {
            }
        }
        if (settings.hapticsEnabled) {
            try {
                haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
            }
        }
    }

    fun updateSettings(newSettings: InteractionSettings) {
        onUpdate(newSettings)
    }

    fun openSettingsDialog() {
        onOpenSettings()
    }
}

val LocalInteractionController = staticCompositionLocalOf<InteractionController> {
    InteractionController(
        settings = InteractionSettings(),
        effectiveReducedMotion = false,
        view = null,
        haptic = null,
        onUpdate = {},
        onOpenSettings = {}
    )
}

@Composable
fun ProvideInteractionController(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    var settings by remember { mutableStateOf(InteractionPreferencesStore.load(context)) }
    val systemReduced = remember(context) { InteractionPreferencesStore.isSystemReducedMotion(context) }
    var showDialog by remember { mutableStateOf(false) }

    val controller = remember(settings, systemReduced, view, haptic) {
        InteractionController(
            settings = settings,
            effectiveReducedMotion = settings.reducedMotionEnabled || systemReduced,
            view = view,
            haptic = haptic,
            onUpdate = { updated ->
                val clamped = updated.copy(readerFontSizeSp = updated.readerFontSizeSp.coerceIn(15f, 24f))
                settings = clamped
                InteractionPreferencesStore.save(context, clamped)
            },
            onOpenSettings = { showDialog = true }
        )
    }

    CompositionLocalProvider(LocalInteractionController provides controller) {
        content()
        if (showDialog) {
            InteractionSettingsDialog(
                settings = settings,
                systemReducedMotion = systemReduced,
                onUpdate = { controller.updateSettings(it) },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun InteractionSettingsDialog(
    settings: InteractionSettings,
    systemReducedMotion: Boolean,
    onUpdate: (InteractionSettings) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Mind3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("interaction_settings_dialog"),
            tone = Mind3DCardTone.WarmIvory,
            shapeRadius = 24.dp,
            extrusionDepth = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkyBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = SkyBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تنظیمات تعامل و مطالعه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "شخصی‌سازی صدا، لرزش، انیمیشن و قلم کتاب‌خوان",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                    }
                }

                SettingToggleRow(
                    title = "صدای ملایم لمس دکمه‌ها",
                    subtitle = "پخش کلیک سیستمی هنگام فشردن دکمه‌ها",
                    icon = Icons.Default.VolumeUp,
                    checked = settings.soundEnabled,
                    testTag = "setting_sound_switch",
                    onCheckedChange = { onUpdate(settings.copy(soundEnabled = it)) }
                )

                SettingToggleRow(
                    title = "لرزش ظریف لمسی (Haptic)",
                    subtitle = "بازخورد لمسی کوتاه در انتخاب گزینه‌ها",
                    icon = Icons.Default.Vibration,
                    checked = settings.hapticsEnabled,
                    testTag = "setting_haptics_switch",
                    onCheckedChange = { onUpdate(settings.copy(hapticsEnabled = it)) }
                )

                SettingToggleRow(
                    title = "کاهش انیمیشن و ذرات",
                    subtitle = if (systemReducedMotion) {
                        "تنظیم سیستم دستگاه نیز در حالت کاهش حرکت است"
                    } else {
                        "غیرفعال‌سازی ذرات و حرکت‌های اضافی برای سرعت بیشتر"
                    },
                    icon = Icons.Default.Animation,
                    checked = settings.reducedMotionEnabled,
                    testTag = "setting_reduced_motion_switch",
                    onCheckedChange = { onUpdate(settings.copy(reducedMotionEnabled = it)) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اندازه متن کتاب‌خوان",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )
                        }
                        Text(
                            text = "${settings.readerFontSizeSp.toInt()} sp",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }

                    Slider(
                        value = settings.readerFontSizeSp,
                        onValueChange = { onUpdate(settings.copy(readerFontSizeSp = it)) },
                        valueRange = 15f..24f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SkyBluePrimary,
                            activeTrackColor = SkyBluePrimary
                        )
                    )

                    Text(
                        text = "نمونه متن مطالعه: آرامش ذهن، آغاز درک عمیق و همدلی است.",
                        fontSize = settings.readerFontSizeSp.sp,
                        lineHeight = (settings.readerFontSizeSp * 1.55f).sp,
                        color = TextPrimaryLight
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Mind3DButton(
                    text = "ذخیره و بستن",
                    onClick = onDismiss,
                    variant = Mind3DButtonVariant.Sky,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_close_btn"),
                    height = 46.dp
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MintPrimary
            )
        )
    }
}
