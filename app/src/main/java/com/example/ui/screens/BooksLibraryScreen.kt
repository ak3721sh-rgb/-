package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.MentorBookEntity
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.ButterYellowPrimary
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LogoGold
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.SpiritualVioletSoft
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.testTag
import com.example.data.local.InitialDataSeed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooksLibraryScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val books by viewModel.mentorBooks.collectAsState()
    val allBookPages by viewModel.allBookPages.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val metadata by viewModel.contentMetadata.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()

    var showBundleDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("ALL") } // "ALL", "BOOKMARKS"
    var selectedCategory by remember { mutableStateOf("همه موضوعات") }

    if (showBundleDialog) {
        StudentContentSyncDialog(
            viewModel = viewModel,
            initialUrl = metadata?.remoteFeedUrl.orEmpty(),
            onDismiss = { showBundleDialog = false }
        )
    }

    val categories = remember(books) {
        val dynamic = books.map { it.subjectCategory }.filter { it.isNotBlank() }
        (InitialDataSeed.COURSE_SUBJECT_CATEGORIES + dynamic).distinct()
    }

    val filteredBooks = remember(books, searchQuery, selectedCategory) {
        books.filter { book ->
            val matchesCat = selectedCategory == "همه موضوعات" || book.subjectCategory == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.subtitle.contains(searchQuery, ignoreCase = true) ||
                book.description.contains(searchQuery, ignoreCase = true) ||
                book.subjectCategory.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val matchingPages = remember(allBookPages, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allBookPages.filter {
            it.sectionTitle.contains(searchQuery, ignoreCase = true) ||
                it.chapterTitle.contains(searchQuery, ignoreCase = true) ||
                it.bodyText.contains(searchQuery, ignoreCase = true)
        }.take(25)
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "کتابخانه «کتاب‌های مدرس»",
                subtitle = "تألیف مدرس: اکبر شیرزادخانی",
                backButtonVariant = Mind3DIconVariant.Sky,
                showSettingsAction = true,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    Mind3DIconButton(
                        icon = Icons.Default.CloudDownload,
                        onClick = { showBundleDialog = true },
                        variant = Mind3DIconVariant.Mint,
                        buttonSize = 48.dp,
                        contentDescription = "دریافت بسته بروزرسانی محتوا"
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SkyBlueSoft,
                            PeachSoft.copy(alpha = 0.25f),
                            SpiritualVioletSoft.copy(alpha = 0.2f),
                            ButterYellowSoft.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            val gridColumns = when {
                maxWidth >= 700.dp -> 3
                maxWidth >= 340.dp -> 2
                else -> 1
            }

            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Summary + Offline Version Status
                item {
                    Mind3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = Mind3DCardTone.Mint,
                        shapeRadius = 22.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.OfflinePin,
                                        contentDescription = null,
                                        tint = MintPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "نسخه محتوا: ${metadata?.contentVersion ?: "1.0.0"} • ذخیره آفلاین",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary
                                    )
                                }

                                Mind3DChip(
                                    text = "همه کتاب‌ها آزاد",
                                    onClick = {},
                                    isSelected = true,
                                    icon = Icons.Default.CheckCircle,
                                    tone = Mind3DChipTone.Mint
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "مجموعه هشت‌جلدی آموزش‌های تخصصی ذهن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "تمام کتاب‌ها و بخش‌های آموزشی به صورت آفلاین در دستگاه شما در دسترس هستند و می‌توانید بدون نیاز به اینترنت مطالعه کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryLight,
                                lineHeight = 20.sp
                            )

                            if (readingProgress != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Mind3DButton(
                                    text = "ادامه مطالعه: کتاب ${readingProgress!!.lastReadBookId} (صفحه ${readingProgress!!.lastReadPageNumber})",
                                    onClick = {
                                        viewModel.navigateTo(
                                            ScreenDestination.BookReader(
                                                bookId = readingProgress!!.lastReadBookId,
                                                pageNumber = readingProgress!!.lastReadPageNumber
                                            )
                                        )
                                    },
                                    variant = Mind3DButtonVariant.Sky,
                                    icon = Icons.Default.Book,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 48.dp
                                )
                            }
                        }
                    }
                }

                // Search & Filter Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در عنوان و موضوع کتاب‌ها...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SkyBluePrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Mind3DChip(
                                text = "همه کتاب‌ها (${filteredBooks.size})",
                                onClick = { selectedTab = "ALL" },
                                isSelected = selectedTab == "ALL",
                                icon = Icons.Default.Book,
                                tone = Mind3DChipTone.Sky
                            )
                            Mind3DChip(
                                text = "نشانک‌های من (${bookmarks.size})",
                                onClick = { selectedTab = "BOOKMARKS" },
                                isSelected = selectedTab == "BOOKMARKS",
                                icon = Icons.Default.Bookmark,
                                tone = Mind3DChipTone.Violet
                            )
                            Mind3DChip(
                                text = "بروزرسانی بسته محتوا",
                                onClick = { showBundleDialog = true },
                                isSelected = false,
                                icon = Icons.Default.CloudDownload,
                                tone = Mind3DChipTone.Mint
                            )
                        }

                        if (selectedTab == "ALL") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                itemsIndexed(categories) { idx, cat ->
                                    Mind3DChip(
                                        text = cat,
                                        isSelected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        tone = Mind3DChipTone.Turquoise,
                                        testTag = "library_category_chip_$idx"
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedTab == "BOOKMARKS") {
                    if (bookmarks.isEmpty()) {
                        item {
                            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                                Text(
                                    text = "هنوز صفحه‌ای را نشانک‌گذاری نکرده‌اید. هنگام مطالعه کتاب می‌توانید از بالای صفحه دکمه نشانک را بزنید.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(bookmarks, key = { "${it.bookId}_${it.pageNumber}" }) { bm ->
                            Mind3DCard(
                                modifier = Modifier.fillMaxWidth(),
                                tone = Mind3DCardTone.Violet,
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.BookReader(bm.bookId, bm.pageNumber))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = SpiritualVioletPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "کتاب ${bm.bookId} • صفحه ${bm.pageNumber}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SpiritualVioletPrimary
                                        )
                                        Text(
                                            text = bm.sectionTitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimaryLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val bookRows = filteredBooks.chunked(gridColumns)
                    if (bookRows.isEmpty()) {
                        item {
                            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                                Text(
                                    text = "کتابی در این دسته‌بندی یا عبارت جستجو یافت نشد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(bookRows, key = { row -> "book_row_${row.joinToString("_") { it.id.toString() }}" }) { rowBooks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowBooks.forEach { book ->
                                    BookCoverCard(
                                        book = book,
                                        modifier = Modifier.weight(1f),
                                        onToggleOffline = {
                                            viewModel.toggleBookOfflineSaved(book.id, !book.isOfflineSaved)
                                        },
                                        onClick = {
                                            viewModel.navigateTo(ScreenDestination.BookReader(bookId = book.id, pageNumber = 1))
                                        }
                                    )
                                }
                                repeat(gridColumns - rowBooks.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (searchQuery.isNotBlank() && matchingPages.isNotEmpty()) {
                        item {
                            Text(
                                text = "بخش‌های یافت‌شده در متن کتاب‌ها (${matchingPages.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(matchingPages, key = { it.id }) { page ->
                            Mind3DCard(
                                modifier = Modifier.fillMaxWidth(),
                                tone = Mind3DCardTone.WarmIvory,
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.BookReader(page.bookId, page.pageNumber))
                                },
                                shapeRadius = 16.dp,
                                extrusionDepth = 2.5.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "کتاب ${page.bookId} • صفحه ${page.pageNumber} — ${page.sectionTitle}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = page.bodyText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCoverCard(
    book: MentorBookEntity,
    modifier: Modifier = Modifier,
    onToggleOffline: () -> Unit,
    onClick: () -> Unit
) {
    val (coverBgGradient, accentColor, iconVec) = when (book.coverAccentColor) {
        "Sky" -> Triple(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), SkyBluePrimary, Icons.Default.Psychology)
        "Mint" -> Triple(listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)), MintPrimary, Icons.Default.SelfImprovement)
        "Peach" -> Triple(
            listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)),
            PeachPrimary,
            if (book.id == 3) Icons.Default.SwapHoriz else Icons.Default.Favorite
        )
        "Violet" -> Triple(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)), SpiritualVioletPrimary, Icons.Default.AutoAwesome)
        "Yellow" -> Triple(listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)), ButterYellowPrimary, Icons.Default.WbSunny)
        "Gold" -> Triple(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)), LogoGold, Icons.Default.MilitaryTech)
        else -> Triple(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)), SkyBluePrimary, Icons.Default.Book)
    }

    val cardTone = when (book.coverAccentColor) {
        "Sky" -> Mind3DCardTone.Sky
        "Mint" -> Mind3DCardTone.Mint
        "Peach" -> Mind3DCardTone.Peach
        "Violet" -> Mind3DCardTone.Violet
        "Yellow" -> Mind3DCardTone.Lemon
        "Gold" -> Mind3DCardTone.Gold
        else -> Mind3DCardTone.WarmIvory
    }

    val statusText = when {
        book.isContentComplete -> "متن کامل آماده"
        book.id == 1 -> "۱۶ سرفصل آماده مطالعه"
        else -> "محتوا در حال تکمیل است"
    }

    Mind3DCard(
        modifier = modifier.defaultMinSize(minHeight = 250.dp),
        tone = cardTone,
        onClick = onClick,
        shapeRadius = 22.dp,
        extrusionDepth = 3.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (book.id == 1) {
                Box(
                    modifier = Modifier
                        .size(66.dp, 88.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, SkyBluePrimary, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_book1_cover),
                        contentDescription = "جلد کتاب اول: ${book.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(66.dp, 88.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(coverBgGradient))
                        .border(1.5.dp, accentColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = iconVec,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "کتاب ${book.id}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = book.subjectCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimaryLight,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (book.isContentComplete || book.id == 1) MintPrimary else TextPrimaryLight,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${book.totalPages} بخش",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (book.isOfflineSaved) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .clickable { onToggleOffline() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("book_offline_toggle_${book.id}")
                    ) {
                        Text(
                            text = if (book.isOfflineSaved) "آفلاین ✓" else "ذخیره آفلاین",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (book.isOfflineSaved) MintPrimary else TextSecondaryLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentContentSyncDialog(
    viewModel: MainViewModel,
    initialUrl: String,
    onDismiss: () -> Unit
) {
    var syncUrl by remember { mutableStateOf(initialUrl) }
    var pastedJson by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.Mint, shapeRadius = 22.dp) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "به‌روزرسانی و ذخیره آفلاین محتوا",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight
                )
                Text(
                    text = "می‌توانید بسته JSON منتشرشده توسط مدرس (اکبر شیرزادخانی) را مستقیماً وارد کنید یا نشانی HTTPS همگام‌سازی را وارد نمایید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )

                OutlinedTextField(
                    value = syncUrl,
                    onValueChange = { syncUrl = it },
                    label = { Text("نشانی HTTPS بسته محتوا (اختیاری)") },
                    placeholder = { Text("https://example.com/mind-bundle.json") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Mind3DButton(
                    text = "همگام‌سازی آنلاین و ذخیره آفلاین",
                    onClick = {
                        viewModel.syncContentFromUrl(syncUrl) { _, msg ->
                            feedback = msg
                        }
                    },
                    variant = Mind3DButtonVariant.Sky,
                    modifier = Modifier.fillMaxWidth(),
                    height = 46.dp
                )

                OutlinedTextField(
                    value = pastedJson,
                    onValueChange = { pastedJson = it },
                    label = { Text("یا متن JSON بسته مدرس را اینجا جای‌گذاری کنید") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                if (feedback != null) {
                    Text(
                        text = feedback!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Mind3DButton(
                        text = "وارد کردن JSON",
                        onClick = {
                            viewModel.importContentBundle(pastedJson) { _, msg ->
                                feedback = msg
                            }
                        },
                        variant = Mind3DButtonVariant.Mint,
                        enabled = pastedJson.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                    Mind3DButton(
                        text = "بستن",
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
