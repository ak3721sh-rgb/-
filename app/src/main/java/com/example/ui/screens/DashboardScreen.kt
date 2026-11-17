package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.Mind3DAvatarButton
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.LemonYellowPrimary
import com.example.ui.theme.LogoGold
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val userProfile by viewModel.userProfile.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SkyBlueSoft,
                        OrangeLightSoft.copy(alpha = 0.25f),
                        LemonYellowGlow
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Mind3DAvatarButton(
                        drawableRes = R.drawable.official_logo,
                        contentDescription = "نشان دوره — معرفی مدرس",
                        size = 54.dp,
                        cornerRadius = 27.dp,
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "هنر ارتباط ذهن‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        Text(
                            text = "داشبورد تخصصی هنرجو",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Mind3DChip(
                    text = "معرفی مدرس",
                    onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) },
                    isSelected = true,
                    icon = Icons.Default.Person,
                    tone = Mind3DChipTone.Sky
                )
            }

            // Stats Card
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.LemonChiffon,
                shapeRadius = 22.dp,
                extrusionDepth = 3.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        title = "امتیاز XP",
                        value = "${userProfile?.xp ?: 180}",
                        color = LogoGold,
                        icon = Icons.Default.AutoAwesome
                    )
                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(Color(0xFFE2E8F0)))
                    StatItem(
                        title = "روز استمرار",
                        value = "${userProfile?.streakDays ?: 3} روز",
                        color = OrangeLightPrimary,
                        icon = Icons.Default.CheckCircle
                    )
                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(Color(0xFFE2E8F0)))
                    StatItem(
                        title = "سطح یادگیری",
                        value = "سطح ${userProfile?.level ?: 1}",
                        color = SkyBluePrimary,
                        icon = Icons.Default.Book
                    )
                }
            }

            // Mentor Card
            Mind3DCard(
                modifier = Modifier.fillMaxWidth(),
                tone = Mind3DCardTone.Sky,
                shapeRadius = 22.dp,
                extrusionDepth = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Mind3DAvatarButton(
                        drawableRes = R.drawable.mentor_portrait,
                        contentDescription = "اکبر شیرزادخانی - مدرس دوره",
                        size = 86.dp,
                        cornerRadius = 18.dp,
                        onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مدرس: اکبر شیرزادخانی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "مؤلف و مدرس دوره «هنر ارتباط ذهن‌ها»",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Mind3DButton(
                                text = "تماس با مدرس",
                                onClick = { viewModel.navigateTo(ScreenDestination.ContactInstructor) },
                                variant = Mind3DButtonVariant.Emerald,
                                modifier = Modifier.weight(1f),
                                height = 40.dp
                            )
                        }
                    }
                }
            }

            // Primary Features Grid
            Text(
                text = "بخش‌های اصلی دوره",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "۸ فصل آموزشی",
                    subtitle = "آموزش گام‌به‌گام و تمرین‌ها",
                    icon = Icons.Default.MenuBook,
                    accentColor = SkyBluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.Chapters) }
                )

                DashboardCard(
                    title = "۱۲ بازی شناختی",
                    subtitle = "پرواز خلبانی، زنر و تمرکز",
                    icon = Icons.Default.SportsEsports,
                    accentColor = OrangeLightPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.GamesHub) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "فلش‌کارت‌های لایتنر",
                    subtitle = "تکنیک‌ها، تمرین‌ها، مفاهیم",
                    icon = Icons.Default.Style,
                    accentColor = LemonYellowPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.Flashcards) }
                )

                DashboardCard(
                    title = "دفتر تجربه شخصی",
                    subtitle = "ثبت حدس‌ها، نتایج و احساس",
                    icon = Icons.Default.EditNote,
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.Journal) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "اتاق مدیریت مدرس",
                    subtitle = "صدور لایسنس و دانلود APK",
                    icon = Icons.Default.AdminPanelSettings,
                    accentColor = LogoGold,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.InstructorRoom) }
                )

                DashboardCard(
                    title = "ارتباط با مدرس",
                    subtitle = "تماس، پیامک، ایتا و روبیکا",
                    icon = Icons.Default.Phone,
                    accentColor = SkyBluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(ScreenDestination.ContactInstructor) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tone = when (accentColor) {
        SkyBluePrimary -> Mind3DCardTone.Sky
        OrangeLightPrimary -> Mind3DCardTone.Apricot
        LogoGold, LemonYellowPrimary -> Mind3DCardTone.LemonChiffon
        else -> Mind3DCardTone.IceCobalt
    }

    Mind3DCard(
        modifier = modifier.defaultMinSize(minHeight = 134.dp),
        tone = tone,
        shapeRadius = 20.dp,
        extrusionDepth = 3.5.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 134.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    maxLines = 1
                )
            }
        }
    }
}
