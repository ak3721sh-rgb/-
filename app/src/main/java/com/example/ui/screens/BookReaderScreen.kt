package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.components.CalloutType
import com.example.ui.components.Mind3DAvatarButton
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.MindCalloutBox
import com.example.ui.components.PredictionTrackerPanel
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.WarmReadingPaper
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookReaderScreen(
    bookId: Int,
    initialPage: Int = 1,
    viewModel: MainViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val books by viewModel.mentorBooks.collectAsState()
    val book = books.find { it.id == bookId }
    val pagesListFlow = remember(bookId) { viewModel.getPagesForBook(bookId) }
    val pages by pagesListFlow.collectAsState(initial = emptyList())
    val bookmarks by viewModel.bookmarks.collectAsState()
    val pageNotes by viewModel.pageNotes.collectAsState()
    val interaction = LocalInteractionController.current

    var currentPageNumber by remember { mutableIntStateOf(initialPage.coerceAtLeast(1)) }
    val scrollState = rememberScrollState()

    var showTocSheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showPredictionModal by remember { mutableStateOf(false) }
    var isFocusMode by remember { mutableStateOf(false) }

    val totalPages = if (pages.isNotEmpty()) pages.size else (book?.totalPages ?: 1)
    val safePageNumber = currentPageNumber.coerceIn(1, totalPages.coerceAtLeast(1))
    val currentPage = pages.find { it.pageNumber == safePageNumber } ?: pages.firstOrNull()
    val isBookmarked = bookmarks.any { it.bookId == bookId && it.pageNumber == safePageNumber }
    val currentNote = pageNotes.find { it.bookId == bookId && it.pageNumber == safePageNumber }

    val distinctChapters = remember(pages) { pages.map { it.chapterNumber }.distinct().sorted() }
    val currentChapIdx = distinctChapters.indexOf(currentPage?.chapterNumber ?: 1)

    LaunchedEffect(bookId, currentPageNumber, currentPage) {
        if (currentPage != null) {
            viewModel.saveProgress(
                bookId = bookId,
                chapterNum = currentPage.chapterNumber,
                pageNum = currentPageNumber,
                title = "${book?.title ?: "کتاب"} — ${currentPage.sectionTitle}"
            )
            scrollState.scrollTo(0)
        }
    }

    // Real active reading dwell-time tracker (counts seconds while reader is open)
    LaunchedEffect(bookId, currentPageNumber) {
        while (true) {
            kotlinx.coroutines.delay(10_000L)
            viewModel.recordReadingDwellSeconds(10)
        }
    }

    if (showTocSheet && pages.isNotEmpty()) {
        var tocQuery by remember { mutableStateOf("") }
        val filteredToc = remember(pages, tocQuery) {
            if (tocQuery.isBlank()) pages
            else pages.filter {
                it.sectionTitle.contains(tocQuery, ignoreCase = true) ||
                    it.chapterTitle.contains(tocQuery, ignoreCase = true) ||
                    it.bodyText.contains(tocQuery, ignoreCase = true)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = WarmReadingPaper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "فهرست مطالب و ابزار مطالعه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Font Size + Chapter Jump + Experience Log inside Sheet (keeps main footer minimal)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Mind3DChip(
                        text = "قلم کوچک‌تر",
                        isSelected = false,
                        onClick = {
                            val nextSize = (interaction.settings.readerFontSizeSp - 1f).coerceIn(15f, 24f)
                            interaction.updateSettings(interaction.settings.copy(readerFontSizeSp = nextSize))
                        },
                        tone = Mind3DChipTone.Sky
                    )
                    Mind3DChip(
                        text = "قلم ${interaction.settings.readerFontSizeSp.toInt()}sp",
                        isSelected = true,
                        onClick = { interaction.openSettingsDialog() },
                        tone = Mind3DChipTone.Violet
                    )
                    Mind3DChip(
                        text = "قلم بزرگ‌تر",
                        isSelected = false,
                        onClick = {
                            val nextSize = (interaction.settings.readerFontSizeSp + 1f).coerceIn(15f, 24f)
                            interaction.updateSettings(interaction.settings.copy(readerFontSizeSp = nextSize))
                        },
                        tone = Mind3DChipTone.Sky
                    )
                    Mind3DChip(
                        text = "ثبت تجربه",
                        isSelected = false,
                        onClick = {
                            showTocSheet = false
                            viewModel.navigateTo(ScreenDestination.Journal)
                        },
                        tone = Mind3DChipTone.Peach
                    )
                }

                if (distinctChapters.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DButton(
                            text = "فصل قبلی",
                            onClick = {
                                if (currentChapIdx > 0) {
                                    val targetChap = distinctChapters[currentChapIdx - 1]
                                    pages.firstOrNull { it.chapterNumber == targetChap }?.let {
                                        currentPageNumber = it.pageNumber
                                    }
                                    showTocSheet = false
                                }
                            },
                            variant = Mind3DButtonVariant.Neutral,
                            enabled = currentChapIdx > 0,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                        Mind3DButton(
                            text = "فصل بعدی",
                            onClick = {
                                if (currentChapIdx >= 0 && currentChapIdx < distinctChapters.size - 1) {
                                    val targetChap = distinctChapters[currentChapIdx + 1]
                                    pages.firstOrNull { it.chapterNumber == targetChap }?.let {
                                        currentPageNumber = it.pageNumber
                                    }
                                    showTocSheet = false
                                }
                            },
                            variant = Mind3DButtonVariant.Sky,
                            enabled = currentChapIdx >= 0 && currentChapIdx < distinctChapters.size - 1,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            height = 44.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tocQuery,
                    onValueChange = { tocQuery = it },
                    placeholder = { Text("جستجو در سرفصل‌ها و متن این کتاب...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyBluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredToc, key = { it.id }) { p ->
                        val isCurrent = p.pageNumber == currentPageNumber
                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = if (isCurrent) Mind3DCardTone.Sky else Mind3DCardTone.WarmIvory,
                            onClick = {
                                currentPageNumber = p.pageNumber
                                showTocSheet = false
                            },
                            shapeRadius = 14.dp,
                            extrusionDepth = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${p.pageNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.sectionTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = TextPrimaryLight
                                    )
                                    Text(
                                        text = p.chapterTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showNoteDialog && currentPage != null) {
        var noteInput by remember(currentNote) { mutableStateOf(currentNote?.noteText.orEmpty()) }
        Dialog(onDismissRequest = { showNoteDialog = false }) {
            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Lemon, shapeRadius = 22.dp) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "یادداشت شخصی برای صفحه $currentPageNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Text(
                        text = currentPage.sectionTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = { Text("برداشت یا پرسش خود از این بخش را بنویسید...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Mind3DButton(
                            text = "ذخیره یادداشت",
                            onClick = {
                                viewModel.savePageNote(bookId, currentPageNumber, noteInput)
                                showNoteDialog = false
                            },
                            variant = Mind3DButtonVariant.Mint,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            height = 46.dp
                        )
                        Mind3DButton(
                            text = "بستن",
                            onClick = { showNoteDialog = false },
                            variant = Mind3DButtonVariant.Neutral,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            height = 46.dp
                        )
                    }
                }
            }
        }
    }

    if (showPredictionModal) {
        Dialog(onDismissRequest = { showPredictionModal = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PredictionTrackerPanel(viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
                Mind3DButton(
                    text = "بستن فرم پیش‌بینی",
                    onClick = { showPredictionModal = false },
                    variant = Mind3DButtonVariant.Neutral,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = book?.title ?: "کتابخوان دوره",
                subtitle = "مؤلف: اکبر شیرزادخانی",
                backButtonVariant = Mind3DIconVariant.Sky,
                showSettingsAction = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    Mind3DIconButton(
                        icon = Icons.Default.List,
                        onClick = { showTocSheet = true },
                        variant = Mind3DIconVariant.Sky,
                        buttonSize = 48.dp,
                        contentDescription = "فهرست مطالب کتاب"
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            WarmReadingPaper,
                            PeachSoft.copy(alpha = 0.2f),
                            ButterYellowSoft.copy(alpha = 0.16f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Compact Reading Progress, Bookmark, Note & Focus Toggle Strip
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "صفحه $currentPageNumber از $totalPages",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                        val progressPercent = ((currentPageNumber.toFloat() / totalPages.coerceAtLeast(1)) * 100).toInt()
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Mind3DIconButton(
                            icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            onClick = {
                                currentPage?.let {
                                    viewModel.toggleBookmark(bookId, currentPageNumber, it.sectionTitle)
                                }
                            },
                            variant = if (isBookmarked) Mind3DIconVariant.Gold else Mind3DIconVariant.Neutral,
                            buttonSize = 40.dp,
                            contentDescription = if (isBookmarked) "حذف نشانک صفحه" else "نشانک‌گذاری صفحه",
                            testTag = "book_bookmark_btn"
                        )
                        Mind3DIconButton(
                            icon = Icons.Default.EditNote,
                            onClick = { showNoteDialog = true },
                            variant = if (currentNote != null) Mind3DIconVariant.Violet else Mind3DIconVariant.Neutral,
                            buttonSize = 40.dp,
                            contentDescription = "یادداشت صفحه",
                            testTag = "book_note_btn"
                        )
                        Mind3DChip(
                            text = if (isFocusMode) "خروج از تمرکز" else "حالت تمرکز",
                            isSelected = isFocusMode,
                            onClick = { isFocusMode = !isFocusMode },
                            icon = Icons.Default.CenterFocusStrong,
                            tone = Mind3DChipTone.Mint,
                            height = 40.dp,
                            testTag = "book_focus_toggle"
                        )
                    }
                }

                if (!isFocusMode) {
                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { (currentPageNumber.toFloat() / totalPages.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SkyBluePrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Warm Paper Study Canvas (Takes maximum vertical space)
                Mind3DCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    tone = Mind3DCardTone.WarmIvory,
                    shapeRadius = 22.dp,
                    extrusionDepth = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WarmReadingPaper)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        // Compact Book 1 Cover Header on Page 1 so body text begins visible above fold
                        if (!isFocusMode && bookId == 1 && currentPageNumber == 1) {
                            Mind3DCard(
                                modifier = Modifier.fillMaxWidth(),
                                tone = Mind3DCardTone.Sky,
                                shapeRadius = 18.dp,
                                extrusionDepth = 2.5.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_book1_cover),
                                        contentDescription = "جلد کتاب اول",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp, 88.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.5.dp, LogoGold, RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Mind3DAvatarButton(
                                                drawableRes = R.drawable.official_logo,
                                                contentDescription = "لوگوی دوره — معرفی مدرس",
                                                size = 28.dp,
                                                cornerRadius = 8.dp,
                                                onClick = { viewModel.navigateTo(ScreenDestination.InstructorProfile) }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "کتاب شماره ۱",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SkyBluePrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "مبانی ارتباط ذهن‌ها؛ از تله‌پاتی تا بررسی علمی",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryLight,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "مؤلف و مدرس: اکبر شیرزادخانی",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (currentPage != null) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SkyBlueSoft)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = currentPage.chapterTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary
                                    )
                                }

                                val (originLabel, originBg, originText) = when (currentPage.contentOrigin) {
                                    "MENTOR_AUTHORED" -> Triple("متن تألیفی مدرس", Color(0xFFDCFCE7), Color(0xFF065F46))
                                    "SCIENTIFIC_SUPPLEMENT" -> Triple("محتوای علمی تکمیلی", Color(0xFFE0F2FE), Color(0xFF075985))
                                    else -> Triple("محتوا در حال تکمیل است", Color(0xFFFEF3C7), Color(0xFF92400E))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(originBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = originLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = originText
                                    )
                                }

                                if (currentNote != null) {
                                    Mind3DChip(
                                        text = "دارای یادداشت",
                                        onClick = { showNoteDialog = true },
                                        isSelected = true,
                                        icon = Icons.Default.EditNote,
                                        tone = Mind3DChipTone.Violet
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentPage.sectionTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            if (currentPage.isSourcePlaceholder) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "محتوا در حال تکمیل است — چارچوب آموزشی این بخش آماده مطالعه است و متن تفصیلی استاد اکبر شیرزادخانی در بروزرسانی بعدی افزوده می‌شود.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val bodyFontSize = interaction.settings.readerFontSizeSp
                            Text(
                                text = currentPage.bodyText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = bodyFontSize.sp,
                                    lineHeight = (bodyFontSize * 1.68f).sp
                                ),
                                color = TextPrimaryLight
                            )

                            if (bookId == 1 && (currentPageNumber == 10 || currentPageNumber == 11)) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Mind3DButton(
                                    text = "باز کردن فرم ثبت پیش‌بینی ۷ روزه",
                                    onClick = { showPredictionModal = true },
                                    variant = Mind3DButtonVariant.Sky,
                                    icon = Icons.Default.TrackChanges,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!currentPage.techniqueBox.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                MindCalloutBox(
                                    title = "نکته و چارچوب آموزشی",
                                    content = currentPage.techniqueBox!!,
                                    type = CalloutType.Technique
                                )
                            }

                            if (!currentPage.reflectionExercise.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                MindCalloutBox(
                                    title = "تمرین عملی و ثبت تجربه",
                                    content = currentPage.reflectionExercise!!,
                                    type = CalloutType.Practice
                                )
                            }

                            if (currentNote != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                MindCalloutBox(
                                    title = "یادداشت شخصی شما در این صفحه",
                                    content = currentNote.noteText,
                                    type = CalloutType.Reflection
                                )
                            }
                        } else {
                            Text(
                                text = "در حال بارگذاری متن کتاب...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimaryLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Single Slim Footer Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_reader_footer"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Mind3DButton(
                        text = "صفحه قبلی",
                        onClick = { if (currentPageNumber > 1) currentPageNumber-- },
                        variant = Mind3DButtonVariant.Neutral,
                        enabled = currentPageNumber > 1,
                        singleLine = true,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.95f).testTag("book_prev_page"),
                        height = 48.dp
                    )

                    Mind3DButton(
                        text = "فهرست $currentPageNumber/$totalPages",
                        onClick = { showTocSheet = true },
                        variant = Mind3DButtonVariant.Gold,
                        icon = Icons.Default.MenuBook,
                        singleLine = true,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1.15f).testTag("book_toc_button"),
                        height = 48.dp
                    )

                    Mind3DButton(
                        text = if (currentPageNumber < totalPages) "صفحه بعدی" else "پایان کتاب",
                        onClick = {
                            if (currentPageNumber < totalPages) currentPageNumber++
                            else viewModel.navigateTo(ScreenDestination.BooksLibrary)
                        },
                        variant = if (currentPageNumber < totalPages) Mind3DButtonVariant.Sky else Mind3DButtonVariant.Mint,
                        singleLine = true,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.95f).testTag("book_next_page"),
                        height = 48.dp
                    )
                }
            }
        }
    }
}
