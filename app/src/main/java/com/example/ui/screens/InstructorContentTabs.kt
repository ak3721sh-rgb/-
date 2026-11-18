package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.OtpIssueResult
import com.example.security.OtpVerifyResult
import com.example.security.PinVerifyStatus
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ContentPublishingTab(viewModel: MainViewModel) {
    val books by viewModel.mentorBooks.collectAsState()
    val metadata by viewModel.contentMetadata.collectAsState()
    var selectedBookId by remember { mutableIntStateOf(1) }
    val pagesFlow = remember(selectedBookId) { viewModel.getPagesForBook(selectedBookId) }
    val pages by pagesFlow.collectAsState(initial = emptyList())
    var selectedPageNumber by remember(selectedBookId) { mutableIntStateOf(1) }

    val selectedPage = pages.find { it.pageNumber == selectedPageNumber } ?: pages.firstOrNull()

    var chapterTitle by remember { mutableStateOf("") }
    var sectionTitle by remember { mutableStateOf("") }
    var bodyText by remember { mutableStateOf("") }
    var techniqueBox by remember { mutableStateOf("") }
    var reflectionExercise by remember { mutableStateOf("") }
    var editorStatus by remember { mutableStateOf<String?>(null) }

    var jsonText by remember { mutableStateOf("") }
    var syncStatus by remember { mutableStateOf<String?>(null) }
    var syncUrl by remember(metadata) { mutableStateOf(metadata?.remoteFeedUrl.orEmpty()) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val loaded = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (loaded.isNotBlank()) {
                    jsonText = loaded
                    viewModel.importContentBundle(loaded) { _, msg ->
                        syncStatus = msg
                    }
                } else {
                    syncStatus = "فایل انتخاب‌شده خالی است."
                }
            } catch (e: Exception) {
                syncStatus = "خطا در خواندن فایل JSON: ${e.localizedMessage ?: "نامشخص"}"
            }
        }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && jsonText.isNotBlank()) {
            try {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                    it.write(jsonText)
                }
                syncStatus = "فایل بسته محتوای JSON با موفقیت روی دستگاه ذخیره شد."
            } catch (e: Exception) {
                syncStatus = "خطا در ذخیره فایل JSON: ${e.localizedMessage ?: "نامشخص"}"
            }
        }
    }

    LaunchedEffect(selectedPage) {
        if (selectedPage != null) {
            chapterTitle = selectedPage.chapterTitle
            sectionTitle = selectedPage.sectionTitle
            bodyText = selectedPage.bodyText
            techniqueBox = selectedPage.techniqueBox.orEmpty()
            reflectionExercise = selectedPage.reflectionExercise.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Page & Chapter Content Editor
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ویرایشگر زنده کتاب‌ها و جای‌گذاری متن رسمی فصل اول",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                Text(
                    text = "متن رسمی کتاب اول یا کتاب‌های بعدی را می‌توانید مستقیماً اینجا وارد و منتشر کنید تا بدون نیاز به ساخت APK جدید ذخیره شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(books, key = { it.id }) { book ->
                        Mind3DChip(
                            text = "کتاب ${book.id}",
                            onClick = {
                                selectedBookId = book.id
                                selectedPageNumber = 1
                                editorStatus = null
                            },
                            isSelected = selectedBookId == book.id,
                            tone = Mind3DChipTone.Sky
                        )
                    }
                }

                if (pages.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(pages, key = { it.id }) { page ->
                            Mind3DChip(
                                text = "صفحه ${page.pageNumber}",
                                onClick = {
                                    selectedPageNumber = page.pageNumber
                                    editorStatus = null
                                },
                                isSelected = selectedPageNumber == page.pageNumber,
                                tone = if (page.isSourcePlaceholder) Mind3DChipTone.Peach else Mind3DChipTone.Mint
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("عنوان فصل") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = sectionTitle,
                    onValueChange = { sectionTitle = it },
                    label = { Text("تیتر بخش") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("متن اصلی صفحه") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = techniqueBox,
                    onValueChange = { techniqueBox = it },
                    label = { Text("کادر تکنیک / چارچوب علمی (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = reflectionExercise,
                    onValueChange = { reflectionExercise = it },
                    label = { Text("کادر تمرین عملی (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                if (editorStatus != null) {
                    Text(editorStatus!!, color = MintPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Mind3DButton(
                    text = "ذخیره متن صفحه $selectedPageNumber از کتاب $selectedBookId",
                    onClick = {
                        viewModel.updateBookPageContent(
                            bookId = selectedBookId,
                            pageNumber = selectedPageNumber,
                            chapterTitle = chapterTitle,
                            sectionTitle = sectionTitle,
                            bodyText = bodyText,
                            techniqueBox = techniqueBox,
                            reflectionExercise = reflectionExercise,
                            isSourcePlaceholder = false
                        )
                        editorStatus = "متن صفحه $selectedPageNumber کتاب $selectedBookId ذخیره شد و وضعیت تکمیل آن به‌روزرسانی گردید."
                    },
                    variant = Mind3DButtonVariant.Emerald,
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. JSON Export / Import & HTTPS Content Sync
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "انتشار پویای بسته محتوا (JSON & همگام‌سازی HTTPS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MintPrimary
                )
                Text(
                    text = "نسخه فعلی محتوای آفلاین: ${metadata?.contentVersion ?: "1.0.0"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimaryLight,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Mind3DButton(
                        text = "خروجی JSON دوره",
                        onClick = {
                            viewModel.exportContentBundle { exported ->
                                jsonText = exported
                                syncStatus = "خروجی JSON ساخته شد؛ می‌توانید کپی یا برای هنرجویان ارسال کنید."
                            }
                        },
                        variant = Mind3DButtonVariant.Sky,
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                    Mind3DButton(
                        text = "کپی JSON",
                        onClick = {
                            if (jsonText.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(jsonText))
                                syncStatus = "متن JSON در حافظه کپی شد."
                            }
                        },
                        variant = Mind3DButtonVariant.Neutral,
                        icon = Icons.Default.ContentCopy,
                        enabled = jsonText.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Mind3DButton(
                        text = "انتخاب فایل JSON از گوشی",
                        onClick = {
                            openJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        variant = Mind3DButtonVariant.Orange,
                        icon = Icons.Default.FileOpen,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                    Mind3DButton(
                        text = "بارگذاری قالب رسمی برنامه",
                        onClick = {
                            try {
                                val bundled = context.assets.open("mind_content_bundle.json")
                                    .bufferedReader(Charsets.UTF_8)
                                    .use { it.readText() }
                                jsonText = bundled
                                syncStatus = "فایل قالب آماده از assets برنامه بارگذاری شد؛ می‌توانید آن را ویرایش یا اعمال کنید."
                            } catch (e: Exception) {
                                syncStatus = "خطا در خواندن فایل قالب: ${e.localizedMessage ?: "یافت نشد"}"
                            }
                        },
                        variant = Mind3DButtonVariant.Violet,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                }

                if (jsonText.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Mind3DButton(
                            text = "ذخیره فایل JSON در گوشی",
                            onClick = {
                                saveJsonLauncher.launch("mind_content_bundle.json")
                            },
                            variant = Mind3DButtonVariant.Mint,
                            icon = Icons.Default.Save,
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                        Mind3DButton(
                            text = "اشتراک‌گذاری با هنرجو",
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, jsonText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "ارسال بسته محتوای دوره"))
                            },
                            variant = Mind3DButtonVariant.Gold,
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                    }
                }

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    label = { Text("متن JSON بسته محتوا (برای وارد کردن یا ویرایش)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp)
                )

                Mind3DButton(
                    text = "وارد کردن و اعمال بسته JSON",
                    onClick = {
                        viewModel.importContentBundle(jsonText) { _, msg ->
                            syncStatus = msg
                        }
                    },
                    variant = Mind3DButtonVariant.Mint,
                    enabled = jsonText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )

                OutlinedTextField(
                    value = syncUrl,
                    onValueChange = { syncUrl = it },
                    label = { Text("آدرس HTTPS فایل JSON روی سرور (اختیاری)") },
                    placeholder = { Text("https://example.com/mind_content.json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Mind3DButton(
                    text = "دریافت از سرور و ذخیره آفلاین",
                    onClick = {
                        viewModel.syncContentFromUrl(syncUrl) { _, msg ->
                            syncStatus = msg
                        }
                    },
                    variant = Mind3DButtonVariant.Orange,
                    icon = Icons.Default.CloudDownload,
                    enabled = syncUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )

                if (syncStatus != null) {
                    Text(syncStatus!!, style = MaterialTheme.typography.bodySmall, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReleaseAndSecurityTab(viewModel: MainViewModel) {
    val metadata by viewModel.contentMetadata.collectAsState()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsState()
    var apkUrl by remember(metadata) { mutableStateOf(metadata?.apkDownloadUrl.orEmpty()) }
    var feedUrl by remember(metadata) { mutableStateOf(metadata?.remoteFeedUrl.orEmpty()) }
    var urlMessage by remember { mutableStateOf<String?>(null) }

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }

    var issuedOtpCode by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var otpStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Apricot) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "تنظیم آدرس واقعی انتشار فایل APK و همگام‌سازی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeLightPrimary
                )
                Text(
                    text = "برای جلوگیری از نمایش لینک نادرست به هنرجویان، لینک دانلود APK فقط در صورتی در صفحه اول و «دانلود و نصب APK» فعال می‌شود که آدرس واقعی فایل .apk یا صفحه انتشار HTTPS را اینجا ثبت کنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    lineHeight = 20.sp
                )

                OutlinedTextField(
                    value = apkUrl,
                    onValueChange = { apkUrl = it },
                    label = { Text("نشانی مستقیم دانلود فایل APK (HTTPS)") },
                    placeholder = { Text("https://your-host.com/mind-connection-v1.apk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = feedUrl,
                    onValueChange = { feedUrl = it },
                    label = { Text("نشانی فید JSON بروزرسانی محتوا (HTTPS)") },
                    placeholder = { Text("https://your-host.com/mind-content.json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (urlMessage != null) {
                    Text(urlMessage!!, style = MaterialTheme.typography.bodySmall, color = MintPrimary, fontWeight = FontWeight.Bold)
                }

                Mind3DButton(
                    text = "ذخیره نشانی‌های رسمی انتشار",
                    onClick = {
                        viewModel.updateRemoteUrls(feedUrl, apkUrl)
                        urlMessage = "نشانی‌های انتشار در پایگاه داده ذخیره شدند."
                    },
                    variant = Mind3DButtonVariant.Orange,
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Violet) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "تغییر رمز امنیتی اتاق مدرس (SHA-256 + قفل ضدحدس)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )

                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { currentPin = it; pinMessage = null },
                    label = { Text("رمز فعلی") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it; pinMessage = null },
                    label = { Text("رمز جدید (حداقل ۶ رقم/کاراکتر)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it; pinMessage = null },
                    label = { Text("تکرار رمز جدید") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (pinMessage != null) {
                    Text(
                        text = pinMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pinMessage!!.contains("موفقیت")) MintPrimary else Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }

                Mind3DButton(
                    text = "به‌روزرسانی رمز امنیتی مدرس",
                    onClick = {
                        when {
                            newPin.trim().length < 6 -> pinMessage = "رمز جدید باید حداقل ۶ کاراکتر باشد."
                            newPin != confirmPin -> pinMessage = "تکرار رمز جدید با رمز واردشده یکسان نیست."
                            else -> when (val res = viewModel.securityManager.changeInstructorPinDetailed(currentPin, newPin)) {
                                is PinVerifyStatus.Success -> {
                                    pinMessage = "رمز عبور اتاق مدرس با موفقیت تغییر یافت."
                                    currentPin = ""
                                    newPin = ""
                                    confirmPin = ""
                                }
                                is PinVerifyStatus.Invalid -> {
                                    pinMessage = "رمز فعلی نادرست است (${res.attemptsRemaining} تلاش باقیمانده)."
                                }
                                is PinVerifyStatus.LockedOut -> {
                                    pinMessage = "به دلیل تلاش‌های ناموفق، تغییر رمز تا ${res.remainingSeconds} ثانیه قفل شد."
                                }
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Violet,
                    icon = Icons.Default.Key,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Genuine Local CSPRNG Expiring Single-Use OTP Verification + Honest Disclosure
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Turquoise) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "کد یک‌بارمصرف محلی (CSPRNG + انقضای ۱۲۰ ثانیه)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F766E)
                )
                Text(
                    text = "این بخش با SecureRandom یک کد ۶ رقمی غیرقابل‌پیش‌بینی، زمان‌دار (۱۲۰ ثانیه) و یک‌بارمصرف با هش نمک‌دار تولید می‌کند. توجه صادقانه: ارسال پیامک OTP واقعی از راه دور به زیرساخت سرور و پنل پیامکی نیاز دارد و در برنامه آفلاین شبیه‌سازی نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    lineHeight = 20.sp
                )

                Mind3DButton(
                    text = "صدور کد تأیید یک‌بارمصرف محلی",
                    onClick = {
                        when (val issued = viewModel.securityManager.issueLocalConfirmationOtp()) {
                            is OtpIssueResult.Issued -> {
                                issuedOtpCode = issued.code
                                otpStatusMessage = "کد محلی جدید برای مدت ${issued.ttlSeconds} ثانیه صادر شد (فقط ۱ بار معتبر است)."
                            }
                            is OtpIssueResult.LockedOut -> {
                                issuedOtpCode = null
                                otpStatusMessage = "صدور کد به دلیل خطاهای پیاپی به مدت ${issued.remainingSeconds} ثانیه قفل شده است."
                            }
                        }
                    },
                    variant = Mind3DButtonVariant.Turquoise,
                    icon = Icons.Default.Security,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "issue_local_otp_btn"
                )

                if (issuedOtpCode != null) {
                    Text(
                        text = "کد محلی فعال (برای تأیید تک‌بار روی این دستگاه): $issuedOtpCode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    )
                }

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { otpInput = it },
                    label = { Text("ورود کد ۶ رقمی یک‌بارمصرف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("local_otp_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Mind3DButton(
                    text = "بررسی و مصرف کد یک‌بارمصرف",
                    onClick = {
                        otpStatusMessage = when (val res = viewModel.securityManager.verifyAndConsumeConfirmationOtp(otpInput)) {
                            is OtpVerifyResult.VerifiedAndConsumed -> {
                                issuedOtpCode = null
                                otpInput = ""
                                "کد تأیید شد و بلافاصله برای جلوگیری از استفاده مجدد باطل گردید."
                            }
                            is OtpVerifyResult.Expired -> "مهلت ۱۲۰ ثانیه‌ای این کد به پایان رسیده است."
                            is OtpVerifyResult.NotIssuedOrUsed -> "هیچ کد فعالی وجود ندارد یا قبلاً مصرف شده است."
                            is OtpVerifyResult.Invalid -> "کد اشتباه است (${res.attemptsRemaining} تلاش باقیمانده)."
                            is OtpVerifyResult.LockedOut -> "تلاش بیش از حد؛ قفل موقت به مدت ${res.remainingSeconds} ثانیه."
                        }
                    },
                    variant = Mind3DButtonVariant.Mint,
                    enabled = otpInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "verify_local_otp_btn"
                )

                if (otpStatusMessage != null) {
                    Text(
                        text = otpStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (otpStatusMessage!!.contains("تأیید شد")) MintPrimary else TextPrimaryLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Offline vs Server-Dependent Transparency Card
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Sky) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "شفافیت قابلیت‌های آفلاین و نیازمند سرور",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                Text(
                    text = "• تمام بخش‌های آفلاین (بدون نیاز به اینترنت): مطالعه کامل ۸ کتاب و فصل‌ها، ذخیره یادداشت و نشانک، فلش‌کارت‌های لایتنر، ۱۲ بازی شناختی، نمودار پیشرفت، تولید کلید فعال‌سازی امضاشده (HMAC-SHA256) و قفل رمز مدرس.\n• نیازمند زیرساخت سرور / HTTPS: میزبانی فایل APK برای دانلود آنلاین، دریافت خودکار بسته JSON از نشانی راه دور، و ارسال پیامک انبوه از راه دور (این بخش‌ها با آدرس رسمی GitHub یا میزبان شما متصل می‌شوند).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryLight,
                    lineHeight = 21.sp
                )
            }
        }

        // Local Diagnostic Log Viewer
        Mind3DCard(
            modifier = Modifier.fillMaxWidth(),
            tone = Mind3DCardTone.WarmIvory,
            testTag = "instructor_diagnostics_card"
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "گزارش رخدادها و خطاهای محلی (${diagnosticLogs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                }
                if (diagnosticLogs.isEmpty()) {
                    Text(
                        text = "تا این لحظه هیچ خطا یا رخداد فنی ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryLight
                    )
                } else {
                    diagnosticLogs.take(10).forEach { log ->
                        Text(
                            text = "[${log.severity}] ${log.tag}: ${log.message}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (log.severity == "ERROR" || log.severity == "WARN") Color(0xFFC2410C) else TextSecondaryLight
                        )
                    }
                    Mind3DButton(
                        text = "پاک کردن گزارش‌های محلی",
                        onClick = { viewModel.clearDiagnosticLogs() },
                        variant = Mind3DButtonVariant.Neutral,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp
                    )
                }
            }
        }
    }
}

