package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashcardEntity
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.theme.ButterYellowSoft
import com.example.ui.theme.LocalInteractionController
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel

private data class FlashcardCategoryMeta(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tone: Mind3DCardTone,
    val accentColor: Color,
    val cards: List<FlashcardEntity>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardsScreen(viewModel: MainViewModel) {
    val allCards by viewModel.flashcards.collectAsState()
    val revision by viewModel.flashcardRevision.collectAsState()
    val interaction = LocalInteractionController.current

    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    var isShuffleMode by remember { mutableStateOf(viewModel.isFlashcardShuffleMode()) }
    var shuffleSeed by remember { mutableIntStateOf(1) }
    var activeCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val seenIds = remember(revision) { viewModel.getSeenFlashcardIds() }

    BackHandler {
        if (selectedCategoryKey != null) {
            selectedCategoryKey = null
            isFlipped = false
        } else {
            viewModel.navigateBack()
        }
    }

    val categories = remember(allCards) {
        val raw = listOf(
            FlashcardCategoryMeta(
                key = "ALL",
                title = "همه کارت‌ها",
                subtitle = "مرور جامع کل دوره",
                icon = Icons.Default.Style,
                tone = Mind3DCardTone.Sky,
                accentColor = SkyBluePrimary,
                cards = allCards
            ),
            FlashcardCategoryMeta(
                key = "TECHNIQUE",
                title = "تکنیک‌ها",
                subtitle = "گام‌های عملی و تمرکز",
                icon = Icons.Default.Lightbulb,
                tone = Mind3DCardTone.Peach,
                accentColor = PeachPrimary,
                cards = allCards.filter { it.category == "TECHNIQUE" }
            ),
            FlashcardCategoryMeta(
                key = "EXERCISE",
                title = "تمرین‌ها",
                subtitle = "پروتکل‌های فردی و دونفره",
                icon = Icons.Default.FitnessCenter,
                tone = Mind3DCardTone.Mint,
                accentColor = MintPrimary,
                cards = allCards.filter { it.category == "EXERCISE" }
            ),
            FlashcardCategoryMeta(
                key = "CONCEPT",
                title = "مفاهیم پایه",
                subtitle = "اصول شناختی و علمی",
                icon = Icons.Default.Psychology,
                tone = Mind3DCardTone.Violet,
                accentColor = SpiritualVioletPrimary,
                cards = allCards.filter { it.category == "CONCEPT" }
            ),
            FlashcardCategoryMeta(
                key = "REVIEW",
                title = "نیازمند مرور",
                subtitle = "کارت‌های نیازمند تکرار",
                icon = Icons.Default.Refresh,
                tone = Mind3DCardTone.Lemon,
                accentColor = Color(0xFFB45309),
                cards = allCards.filter { !it.isMastered || it.isMarkedDifficult || it.errorCount > 0 }
            )
        )
        raw.filter { it.cards.isNotEmpty() }
    }

    // Smart priority card suggestion: due/difficult first, then unseen, else first non-mastered
    val smartSuggestedCard = remember(allCards, seenIds) {
        val now = System.currentTimeMillis()
        allCards.filter { it.isMarkedDifficult || it.errorCount > 0 }
            .maxByOrNull { it.errorCount }
            ?: allCards.filter { !it.isMastered && it.nextReviewDate <= now }.firstOrNull()
            ?: allCards.firstOrNull { it.id !in seenIds }
            ?: allCards.firstOrNull { !it.isMastered }
            ?: allCards.firstOrNull()
    }

    val activeCategory = categories.find { it.key == selectedCategoryKey }
    val filteredCards = remember(activeCategory, isShuffleMode, shuffleSeed) {
        val base = activeCategory?.cards.orEmpty()
        if (isShuffleMode && base.size > 1) {
            base.shuffled(kotlin.random.Random(shuffleSeed))
        } else {
            base
        }
    }

    val safeCardIndex = if (filteredCards.isNotEmpty()) activeCardIndex.coerceIn(0, filteredCards.size - 1) else 0
    val currentCard = filteredCards.getOrNull(safeCardIndex)

    LaunchedEffect(currentCard?.id, selectedCategoryKey) {
        if (currentCard != null && selectedCategoryKey != null) {
            viewModel.markFlashcardSeen(currentCard.id)
        }
    }

    fun selectCategory(catKey: String, jumpToCardId: Int? = null) {
        selectedCategoryKey = catKey
        val targetList = categories.find { it.key == catKey }?.cards.orEmpty()
        val startIdx = if (jumpToCardId != null) {
            targetList.indexOfFirst { it.id == jumpToCardId }.takeIf { it >= 0 } ?: 0
        } else {
            viewModel.getFlashcardIndex(catKey).coerceIn(0, (targetList.size - 1).coerceAtLeast(0))
        }
        activeCardIndex = startIdx
        viewModel.saveFlashcardIndex(catKey, startIdx)
        isFlipped = false
    }

    fun moveToIndex(catKey: String, nextIdx: Int) {
        val clamped = nextIdx.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0))
        activeCardIndex = clamped
        viewModel.saveFlashcardIndex(catKey, clamped)
        isFlipped = false
    }

    val masteredCount = allCards.count { it.isMastered }
    val reviewCount = allCards.count { !it.isMastered || it.isMarkedDifficult }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = activeCategory?.let { "مرور دسته: ${it.title}" } ?: "فلش‌کارت‌های لایتنر",
                subtitle = if (activeCategory == null) {
                    "انتخاب دسته‌بندی یا ادامه مرور زمان‌بندی‌شده"
                } else {
                    "کارت ${safeCardIndex + 1} از ${filteredCards.size}"
                },
                backButtonVariant = Mind3DIconVariant.Sky,
                showSettingsAction = true,
                onBackClick = {
                    if (selectedCategoryKey != null) {
                        selectedCategoryKey = null
                        isFlipped = false
                    } else {
                        viewModel.navigateBack()
                    }
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
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.22f), ButterYellowSoft.copy(alpha = 0.45f))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 780.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                val isNarrowPhone = maxWidth < 340.dp

                if (selectedCategoryKey == null || activeCategory == null) {
                    // STEP 1: CATEGORY LANDING SCREEN
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mastery & Review Summary Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Mind3DCard(
                                modifier = Modifier.weight(1f),
                                tone = Mind3DCardTone.Mint,
                                shapeRadius = 16.dp,
                                extrusionDepth = 2.5.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تسلط کامل: $masteredCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Mind3DCard(
                                modifier = Modifier.weight(1f),
                                tone = Mind3DCardTone.Peach,
                                shapeRadius = 16.dp,
                                extrusionDepth = 2.5.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = PeachPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "نیاز به مرور: $reviewCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                        }

                        // Smart Suggestion Card for Today + Persistent Shuffle Option
                        if (smartSuggestedCard != null) {
                            Mind3DCard(
                                modifier = Modifier.fillMaxWidth(),
                                tone = Mind3DCardTone.Gold,
                                shapeRadius = 20.dp,
                                extrusionDepth = 3.5.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA16207), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "پیشنهاد هوشمند کارت برای تمرین امروز",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryLight
                                        )
                                    }
                                    Text(
                                        text = "«${smartSuggestedCard.frontTitle}» — اولویت‌دار بر اساس مرور لایتنر و کارت‌های دیده‌نشده.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryLight,
                                        lineHeight = 20.sp
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Mind3DButton(
                                            text = "مرور کارت پیشنهادی",
                                            onClick = {
                                                if (isShuffleMode) {
                                                    isShuffleMode = false
                                                    viewModel.setFlashcardShuffleMode(false)
                                                }
                                                selectCategory("ALL", jumpToCardId = smartSuggestedCard.id)
                                            },
                                            variant = Mind3DButtonVariant.Gold,
                                            icon = Icons.Default.AutoAwesome,
                                            height = 48.dp,
                                            testTag = "flashcard_smart_today_btn"
                                        )
                                        Mind3DChip(
                                            text = if (isShuffleMode) "مرور تصادفی: روشن" else "مرور تصادفی",
                                            isSelected = isShuffleMode,
                                            icon = Icons.Default.Shuffle,
                                            onClick = {
                                                val next = !isShuffleMode
                                                isShuffleMode = next
                                                viewModel.setFlashcardShuffleMode(next)
                                                if (next) shuffleSeed++
                                            },
                                            tone = Mind3DChipTone.Violet,
                                            height = 48.dp,
                                            testTag = "flashcard_shuffle_chip"
                                        )
                                    }
                                }
                            }
                        }

                        Mind3DCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = Mind3DCardTone.WarmIvory,
                            shapeRadius = 20.dp,
                            extrusionDepth = 3.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "یک دسته را برای شروع مرور انتخاب کنید",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryLight
                                )
                                Text(
                                    text = "جایگاه مطالعه و کارت‌های دیده‌شده در هر دسته به‌صورت دائمی روی دستگاه شما ذخیره می‌شوند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryLight,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        if (categories.isEmpty()) {
                            Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                                Text(
                                    text = "هنوز کارتی در بانک فلش‌کارت‌ها ثبت نشده است.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else if (isNarrowPhone) {
                            categories.forEach { cat ->
                                FlashcardCategoryCard(
                                    meta = cat,
                                    savedIndex = viewModel.getFlashcardIndex(cat.key),
                                    seenCount = cat.cards.count { it.id in seenIds },
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { selectCategory(cat.key) }
                                )
                            }
                        } else {
                            categories.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { cat ->
                                        FlashcardCategoryCard(
                                            meta = cat,
                                            savedIndex = viewModel.getFlashcardIndex(cat.key),
                                            seenCount = cat.cards.count { it.id in seenIds },
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectCategory(cat.key) }
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    // STEP 2: FOCUSED ACTIVE DECK STUDY VIEW
                    val rotation = if (interaction.effectiveReducedMotion) {
                        if (isFlipped) 180f else 0f
                    } else {
                        val animVal by animateFloatAsState(
                            targetValue = if (isFlipped) 180f else 0f,
                            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                            label = "card_flip"
                        )
                        animVal
                    }

                    val seenInCategory = filteredCards.count { it.id in seenIds }
                    val remainingInCategory = (filteredCards.size - seenInCategory).coerceAtLeast(0)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Deck Status Bar + Shuffle Chip
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Mind3DChip(
                                    text = "بازگشت به دسته‌ها",
                                    isSelected = false,
                                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                                    onClick = {
                                        selectedCategoryKey = null
                                        isFlipped = false
                                    },
                                    tone = Mind3DChipTone.Sky,
                                    height = 40.dp,
                                    testTag = "flashcard_back_to_categories"
                                )
                                Mind3DChip(
                                    text = if (isShuffleMode) "تصادفی" else "ترتیبی",
                                    isSelected = isShuffleMode,
                                    icon = Icons.Default.Shuffle,
                                    onClick = {
                                        val next = !isShuffleMode
                                        isShuffleMode = next
                                        viewModel.setFlashcardShuffleMode(next)
                                        if (next) shuffleSeed++
                                        moveToIndex(activeCategory.key, 0)
                                    },
                                    tone = Mind3DChipTone.Violet,
                                    height = 40.dp,
                                    testTag = "flashcard_deck_shuffle_chip"
                                )
                            }

                            Text(
                                text = "دیده‌شده: $seenInCategory • باقیمانده: $remainingInCategory",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryLight,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentCard == null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "هیچ کارتی در این دسته وجود ندارد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryLight
                                )
                            }
                        } else {
                            // Centered Scroll-Safe Flipping Card
                            Mind3DCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        rotationY = rotation
                                        cameraDistance = 14f * density
                                    },
                                tone = if (rotation <= 90f) Mind3DCardTone.Lemon else Mind3DCardTone.Mint,
                                shapeRadius = 24.dp,
                                extrusionDepth = 4.5.dp,
                                testTag = "flashcard_active_card",
                                onClick = { isFlipped = !isFlipped }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rotation <= 90f) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White.copy(alpha = 0.85f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = when (currentCard.category) {
                                                        "TECHNIQUE" -> "تکنیک مرحله‌ای"
                                                        "EXERCISE" -> "تمرین کاربردی"
                                                        else -> "مفهوم پایه"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeCategory.accentColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = currentCard.frontTitle,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = SkyBluePrimary,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            val fontSp = interaction.settings.readerFontSizeSp
                                            Text(
                                                text = currentCard.frontContent,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = fontSp.sp,
                                                    lineHeight = (fontSp * 1.58f).sp
                                                ),
                                                color = TextPrimaryLight,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Flip,
                                                    contentDescription = null,
                                                    tint = OrangeLightPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "لمس کارت یا دکمه زیر برای مشاهده پاسخ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = OrangeLightPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer { rotationY = 180f }
                                                .verticalScroll(rememberScrollState()),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "پاسخ و تحلیل علمی:",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            val fontSp = interaction.settings.readerFontSizeSp
                                            Text(
                                                text = currentCard.backAnswer,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = fontSp.sp,
                                                    lineHeight = (fontSp * 1.58f).sp
                                                ),
                                                color = TextPrimaryLight,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "وضعیت تسلط خود را در زیر ثبت کنید:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondaryLight,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Bar 1: Flip Card
                            Mind3DButton(
                                text = if (isFlipped) "مشاهده پرسش" else "نمایش پاسخ",
                                onClick = { isFlipped = !isFlipped },
                                variant = Mind3DButtonVariant.Gold,
                                icon = Icons.Default.Flip,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("flashcard_flip_button"),
                                height = 46.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Bar 2: Review / Skip / Mastered
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Mind3DButton(
                                    text = "نیاز به تمرین",
                                    onClick = {
                                        viewModel.updateFlashcardMastery(currentCard, false)
                                        if (safeCardIndex < filteredCards.size - 1) {
                                            moveToIndex(activeCategory.key, safeCardIndex + 1)
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Orange,
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flashcard_review_btn"),
                                    height = 46.dp
                                )

                                Mind3DButton(
                                    text = "رد کردن",
                                    onClick = {
                                        if (safeCardIndex < filteredCards.size - 1) {
                                            moveToIndex(activeCategory.key, safeCardIndex + 1)
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Neutral,
                                    icon = Icons.Default.SkipNext,
                                    enabled = safeCardIndex < filteredCards.size - 1,
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .testTag("flashcard_skip_btn"),
                                    height = 46.dp
                                )

                                Mind3DButton(
                                    text = "بلدم (+۲۰)",
                                    onClick = {
                                        viewModel.updateFlashcardMastery(currentCard, true)
                                        if (safeCardIndex < filteredCards.size - 1) {
                                            moveToIndex(activeCategory.key, safeCardIndex + 1)
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Emerald,
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flashcard_master_btn"),
                                    height = 46.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Bar 3: Prev / Next
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Mind3DButton(
                                    text = "کارت قبلی",
                                    onClick = {
                                        if (safeCardIndex > 0) {
                                            moveToIndex(activeCategory.key, safeCardIndex - 1)
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Neutral,
                                    enabled = safeCardIndex > 0,
                                    singleLine = true,
                                    height = 46.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flashcard_prev_btn")
                                )

                                Mind3DButton(
                                    text = "کارت بعدی",
                                    onClick = {
                                        if (safeCardIndex < filteredCards.size - 1) {
                                            moveToIndex(activeCategory.key, safeCardIndex + 1)
                                        }
                                    },
                                    variant = Mind3DButtonVariant.Sky,
                                    enabled = safeCardIndex < filteredCards.size - 1,
                                    singleLine = true,
                                    height = 46.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("flashcard_next_btn")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardCategoryCard(
    meta: FlashcardCategoryMeta,
    savedIndex: Int,
    seenCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val total = meta.cards.size
    val resumePage = (savedIndex + 1).coerceIn(1, total)

    Mind3DCard(
        modifier = modifier.defaultMinSize(minHeight = 148.dp),
        tone = meta.tone,
        shapeRadius = 20.dp,
        extrusionDepth = 3.5.dp,
        testTag = "flashcard_cat_${meta.key}",
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 148.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(meta.accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = null,
                        tint = meta.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$total کارت",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = meta.accentColor
                    )
                }
            }

            Column {
                Text(
                    text = meta.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = meta.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = if (seenCount > 0) "ادامه از کارت $resumePage • دیده‌شده: $seenCount" else "شروع مرور دسته",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = meta.accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
