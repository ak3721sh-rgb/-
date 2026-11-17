package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccessRequestEntity
import com.example.data.model.LicenseEntity
import com.example.security.PinVerifyStatus
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val InstructorRoomTabs = listOf(
    "صدور و مدیریت لایسنس",
    "درخواست‌ها و پیشرفت هنرجو",
    "ویرایش و انتشار محتوا",
    "انتشار APK و امنیت"
)

@Composable
fun InstructorRoomScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    var hasPinSet by remember { mutableStateOf(viewModel.securityManager.hasInstructorPinSet()) }
    var isAuthenticated by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "اتاق مدیریت مدرس",
                subtitle = "مدرس: اکبر شیرزادخانی",
                onBackClick = { viewModel.navigateBack() },
                showSettingsAction = true
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.2f), LemonYellowGlow)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(modifier = Modifier.widthIn(max = 820.dp).fillMaxSize().navigationBarsPadding()) {
                if (!hasPinSet) {
                    InstructorFirstPinSetupGate(
                        onSavePin = { pin ->
                            if (viewModel.securityManager.setupInitialInstructorPin(pin)) {
                                hasPinSet = true
                                isAuthenticated = true
                                true
                            } else false
                        }
                    )
                } else if (!isAuthenticated) {
                    InstructorVerifyPinGate(
                        onVerifyDetailed = { pin ->
                            val status = viewModel.securityManager.verifyInstructorPinDetailed(pin)
                            if (status is PinVerifyStatus.Success) isAuthenticated = true
                            status
                        }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(InstructorRoomTabs, key = { idx, _ -> idx }) { index, tabTitle ->
                                Mind3DChip(
                                    text = tabTitle,
                                    isSelected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    tone = when (index) {
                                        0 -> Mind3DChipTone.Sky
                                        1 -> Mind3DChipTone.Peach
                                        2 -> Mind3DChipTone.Mint
                                        else -> Mind3DChipTone.Violet
                                    },
                                    testTag = "instructor_tab_$index"
                                )
                            }
                        }

                        when (selectedTab) {
                            0 -> LicenseManagementTab(viewModel)
                            1 -> StudentRequestsAndStatsTab(viewModel)
                            2 -> ContentPublishingTab(viewModel)
                            3 -> ReleaseAndSecurityTab(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructorFirstPinSetupGate(
    onSavePin: (String) -> Boolean
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky, shapeRadius = 24.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(SkyBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(34.dp))
                }
                Text("تنظیم رمز اولیه اتاق مدرس", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "برای امنیت کامل پنل مدیریت استاد اکبر شیرزادخانی، هیچ رمز ثابتی در کد قرار نگرفته است. لطفاً یک رمز اختصاصی (حداقل ۶ رقم یا کاراکتر) تعیین نمایید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it; errorMsg = null },
                    label = { Text("رمز امنیتی جدید (حداقل ۶ کاراکتر)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("instructor_setup_pin_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it; errorMsg = null },
                    label = { Text("تکرار رمز امنیتی") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("instructor_setup_confirm_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFD32F2F), style = MaterialTheme.typography.bodySmall)
                }
                Mind3DButton(
                    text = "ثبت رمز هش‌شده و ورود",
                    onClick = {
                        when {
                            newPin.trim().length < 6 -> errorMsg = "رمز باید حداقل ۶ کاراکتر باشد."
                            newPin != confirmPin -> errorMsg = "تکرار رمز با رمز واردشده مطابقت ندارد."
                            !onSavePin(newPin) -> errorMsg = "خطا در ذخیره رمز امنیتی."
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    modifier = Modifier.fillMaxWidth().testTag("instructor_setup_submit")
                )
            }
        }
    }
}

@Composable
private fun InstructorVerifyPinGate(
    onVerifyDetailed: (String) -> PinVerifyStatus
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky, shapeRadius = 24.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(SkyBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(34.dp))
                }
                Text("احراز هویت اتاق مدرس", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "رمز اختصاصی مدرس (جناب اکبر شیرزادخانی) را وارد کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it; errorMessage = null },
                    label = { Text("رمز ورود مدرس") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("instructor_pin_input"),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    singleLine = true
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Mind3DButton(
                    text = "تأیید و ورود به اتاق مدیریت",
                    onClick = {
                        errorMessage = when (val status = onVerifyDetailed(enteredPin)) {
                            is PinVerifyStatus.Success -> null
                            is PinVerifyStatus.Invalid -> "رمز واردشده معتبر نیست (${status.attemptsRemaining} تلاش باقیمانده پیش از قفل موقت)."
                            is PinVerifyStatus.LockedOut -> "به دلیل تلاش‌های ناموفق متوالی، ورود به مدت ${status.remainingSeconds} ثانیه قفل شد."
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    modifier = Modifier.fillMaxWidth().testTag("instructor_pin_verify")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LicenseManagementTab(viewModel: MainViewModel) {
    val licenses by viewModel.allLicenses.collectAsState()
    var studentNameInput by remember { mutableStateOf("") }
    var studentContactInput by remember { mutableStateOf("") }
    var selectedValidityDays by remember { mutableStateOf<Int?>(90) }
    var selectedMaxDevices by remember { mutableIntStateOf(1) }
    var filterStatus by remember { mutableStateOf("ALL") }
    var newlyCreatedKey by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filteredLicenses = remember(licenses, filterStatus) {
        when (filterStatus) {
            "ACTIVE" -> licenses.filter { it.status == "ACTIVE" }
            "USED" -> licenses.filter { it.status == "USED" }
            "REVOKED" -> licenses.filter { it.status == "REVOKED" || it.status == "EXPIRED" }
            else -> licenses
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.LemonChiffon, shapeRadius = 22.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "صدور کلید امضاشده غیرقابل حدس (HMAC-SHA256)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    OutlinedTextField(
                        value = studentNameInput,
                        onValueChange = { studentNameInput = it },
                        label = { Text("نام هنرجو") },
                        placeholder = { Text("مثال: نرگس احمدی") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = studentContactInput,
                        onValueChange = { studentContactInput = it },
                        label = { Text("شماره تماس یا ایمیل (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text("مدت اعتبار کلید:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(7 to "۷ روز", 30 to "۱ ماه", 90 to "۳ ماه", 365 to "۱ سال", null to "نامحدود").forEach { (days, label) ->
                            Mind3DChip(
                                text = label,
                                onClick = { selectedValidityDays = days },
                                isSelected = selectedValidityDays == days,
                                tone = Mind3DChipTone.Sky
                            )
                        }
                    }

                    Text("حداکثر تعداد دستگاه مجاز:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 5).forEach { count ->
                            Mind3DChip(
                                text = "$count دستگاه",
                                onClick = { selectedMaxDevices = count },
                                isSelected = selectedMaxDevices == count,
                                tone = Mind3DChipTone.Peach
                            )
                        }
                    }

                    Mind3DButton(
                        text = "تولید کلید امنیتی یکبارمصرف",
                        onClick = {
                            viewModel.generateNewKeyForInstructor(
                                studentName = studentNameInput.ifBlank { "هنرجوی جدید" },
                                studentContact = studentContactInput,
                                validityDays = selectedValidityDays,
                                maxDevices = selectedMaxDevices
                            ) { key ->
                                newlyCreatedKey = key
                                studentNameInput = ""
                                studentContactInput = ""
                            }
                        },
                        variant = Mind3DButtonVariant.Orange,
                        icon = Icons.Default.Key,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (newlyCreatedKey != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(OrangeLightSoft)
                                .border(1.dp, OrangeLightPrimary, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("کلید صادرشده:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBF360C))
                                Text(
                                    text = newlyCreatedKey!!,
                                    style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Mind3DButton(
                                        text = "کپی",
                                        onClick = { clipboardManager.setText(AnnotatedString(newlyCreatedKey!!)) },
                                        variant = Mind3DButtonVariant.Neutral,
                                        icon = Icons.Default.ContentCopy,
                                        height = 46.dp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Mind3DButton(
                                        text = "ارسال به هنرجو",
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "کلید فعال‌سازی دوره «هنر ارتباط ذهن‌ها»:\n$newlyCreatedKey\nمدرس: اکبر شیرزادخانی")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "ارسال کلید لایسنس"))
                                        },
                                        variant = Mind3DButtonVariant.Sky,
                                        icon = Icons.Default.Share,
                                        height = 46.dp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL" to "همه (${licenses.size})", "ACTIVE" to "فعال", "USED" to "مصرف‌شده", "REVOKED" to "باطل/منقضی").forEach { (key, title) ->
                    Mind3DChip(
                        text = title,
                        onClick = { filterStatus = key },
                        isSelected = filterStatus == key,
                        tone = Mind3DChipTone.Sky
                    )
                }
            }
        }

        items(filteredLicenses, key = { it.licenseKey }) { license ->
            LicenseCard(
                license = license,
                onExtend30Days = { viewModel.extendLicense(license.licenseKey, 30) },
                onRevoke = { viewModel.revokeKeyByInstructor(license.licenseKey) }
            )
        }
    }
}

@Composable
private fun LicenseCard(
    license: LicenseEntity,
    onExtend30Days: () -> Unit,
    onRevoke: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.IceCobalt, shapeRadius = 18.dp) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = license.licenseKey,
                    style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 1.sp),
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                val (statusText, statusBg, statusColor) = when (license.status) {
                    "ACTIVE" -> Triple("فعال", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    "USED" -> Triple("مصرف‌شده", SkyBlueSoft, SkyBluePrimary)
                    "EXPIRED" -> Triple("منقضی", Color(0xFFFEF3C7), Color(0xFFB45309))
                    else -> Triple("باطل‌شده", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            Text(
                text = "هنرجو: ${license.studentName ?: "نامشخص"} ${if (!license.studentContact.isNullOrBlank()) "• ${license.studentContact}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimaryLight
            )
            Text(
                text = "ظرفیت دستگاه: ${license.usedCount} از ${license.maxDevices} • اعتبار تا: ${license.expiresAt?.let { dateFormat.format(Date(it)) } ?: "نامحدود"}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Mind3DIconButton(
                    icon = Icons.Default.ContentCopy,
                    onClick = { clipboardManager.setText(AnnotatedString(license.licenseKey)) },
                    variant = Mind3DIconVariant.Neutral,
                    buttonSize = 48.dp,
                    contentDescription = "کپی کلید"
                )
                if (license.status != "REVOKED") {
                    Mind3DButton(
                        text = "+۳۰ روز تمدید",
                        onClick = onExtend30Days,
                        variant = Mind3DButtonVariant.Mint,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                    Mind3DButton(
                        text = "ابطال کلید",
                        onClick = onRevoke,
                        variant = Mind3DButtonVariant.Neutral,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentRequestsAndStatsTab(viewModel: MainViewModel) {
    val requests by viewModel.accessRequests.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val predictions by viewModel.predictionEntries.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("وضعیت پیشرفت روی این دستگاه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("امتیاز XP هنرجو: ${userProfile?.xp ?: 0}", style = MaterialTheme.typography.bodySmall)
                    Text("درس‌های تکمیل‌شده: ${lessons.count { it.isCompleted }} از ${lessons.size}", style = MaterialTheme.typography.bodySmall)
                    Text("ثبت‌های فرم ۷ روزه پیش‌بینی: ${predictions.size} مورد", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Text("درخواست‌های کلید ثبت‌شده (${requests.size} مورد):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (requests.isEmpty()) {
            item {
                Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                    Text(
                        text = "هنوز درخواستی از بخش فعال‌سازی ثبت نشده است.",
                        modifier = Modifier.padding(16.dp),
                        color = TextSecondaryLight
                    )
                }
            }
        } else {
            items(requests, key = { it.id }) { req ->
                RequestItemCard(
                    request = req,
                    onIssue = {
                        viewModel.generateNewKeyForInstructor(req.studentName, req.studentContact, 90, 1) { newKey ->
                            viewModel.markAccessRequestIssued(req, newKey)
                        }
                    },
                    onDismiss = { viewModel.dismissAccessRequest(req) }
                )
            }
        }
    }
}

@Composable
private fun RequestItemCard(
    request: AccessRequestEntity,
    onIssue: () -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Peach) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MarkEmailUnread, contentDescription = null, tint = OrangeLightPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${request.studentName} (${request.channel})", fontWeight = FontWeight.Bold)
            }
            if (request.studentContact.isNotBlank()) {
                Text("راه ارتباطی: ${request.studentContact}", style = MaterialTheme.typography.bodySmall)
            }
            Text("شناسه دستگاه: ${request.deviceId}", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
            if (!request.issuedKey.isNullOrBlank()) {
                Text("کلید صادرشده: ${request.issuedKey}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MintPrimary)
                Mind3DButton(
                    text = "کپی کلید صادرشده",
                    onClick = { clipboard.setText(AnnotatedString(request.issuedKey)) },
                    variant = Mind3DButtonVariant.Neutral,
                    height = 44.dp
                )
            } else if (request.status == "PENDING") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Mind3DButton(
                        text = "صدور خودکار کلید",
                        onClick = onIssue,
                        variant = Mind3DButtonVariant.Mint,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                    Mind3DButton(
                        text = "بستن درخواست",
                        onClick = onDismiss,
                        variant = Mind3DButtonVariant.Neutral,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                }
            }
        }
    }
}
