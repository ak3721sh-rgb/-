package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExperienceLogEntity
import com.example.ui.components.Mind3DButton
import com.example.ui.components.Mind3DButtonVariant
import com.example.ui.components.Mind3DCard
import com.example.ui.components.Mind3DCardTone
import com.example.ui.components.Mind3DChip
import com.example.ui.components.Mind3DChipTone
import com.example.ui.components.Mind3DIconButton
import com.example.ui.components.Mind3DIconVariant
import com.example.ui.components.Mind3DTopAppBar
import com.example.ui.components.PredictionTrackerPanel
import com.example.ui.theme.LemonYellowGlow
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SpiritualVioletPrimary
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val logs by viewModel.experienceLogs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val pageNotes by viewModel.pageNotes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: experiences, 1: 7-day prediction, 2: notes & bookmarks
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, searchQuery) {
        if (searchQuery.isBlank()) logs
        else logs.filter {
            it.exerciseName.contains(searchQuery, ignoreCase = true) ||
                it.prediction.contains(searchQuery, ignoreCase = true) ||
                it.interpretation.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showAddDialog) {
        AddExperienceDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Mind3DTopAppBar(
                title = "دفتر تجربه و پیش‌بینی ۷ روزه",
                subtitle = "ثبت محرمانه و آفلاین روی دستگاه شما",
                backButtonVariant = Mind3DIconVariant.Sky,
                onBackClick = { viewModel.navigateBack() },
                showSettingsAction = true,
                actions = {
                    Mind3DIconButton(
                        icon = Icons.Default.Add,
                        onClick = { showAddDialog = true },
                        variant = Mind3DIconVariant.Mint,
                        buttonSize = 48.dp,
                        contentDescription = "ثبت تجربه جدید"
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
                        listOf(SkyBlueSoft, OrangeLightSoft.copy(alpha = 0.2f), LemonYellowGlow)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Mind3DChip(
                        text = "تجربه‌های آزاد (${logs.size})",
                        onClick = { selectedTab = 0 },
                        isSelected = selectedTab == 0,
                        icon = Icons.Default.EditNote,
                        tone = Mind3DChipTone.Sky
                    )
                    Mind3DChip(
                        text = "پیش‌بینی ۷ روزه",
                        onClick = { selectedTab = 1 },
                        isSelected = selectedTab == 1,
                        icon = Icons.Default.TrackChanges,
                        tone = Mind3DChipTone.Mint
                    )
                    Mind3DChip(
                        text = "یادداشت‌ها و نشانک‌ها (${bookmarks.size + pageNotes.size})",
                        onClick = { selectedTab = 2 },
                        isSelected = selectedTab == 2,
                        icon = Icons.Default.Bookmark,
                        tone = Mind3DChipTone.Violet
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در یادداشت‌ها و تمرین‌ها...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryLight) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SkyBluePrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اطلاعات این دفترچه کاملاً آفلاین و خصوصی در حافظه دستگاه شما نگهداری می‌شود.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredLogs.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(54.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("هنوز تجربه‌ای ثبت نکرده‌اید.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("برای ثبت اولین آزمایش یا تمرین شهودی، دکمه زیر را لمس کنید.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Mind3DButton(
                                        text = "ثبت اولین تجربه شخصی",
                                        onClick = { showAddDialog = true },
                                        variant = Mind3DButtonVariant.Sky
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredLogs, key = { it.id }) { log ->
                                    ExperienceCard(
                                        log = log,
                                        onDelete = { viewModel.deleteExperienceLog(log) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            PredictionTrackerPanel(viewModel = viewModel)
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(bookmarks, key = { "bm_${it.bookId}_${it.pageNumber}" }) { bm ->
                                Mind3DCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    tone = Mind3DCardTone.Violet,
                                    onClick = { viewModel.navigateTo(ScreenDestination.BookReader(bm.bookId, bm.pageNumber)) }
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = SpiritualVioletPrimary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("نشانک کتاب ${bm.bookId} • صفحه ${bm.pageNumber}", fontWeight = FontWeight.Bold, color = SpiritualVioletPrimary)
                                            Text(bm.sectionTitle, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                                        }
                                    }
                                }
                            }
                            items(pageNotes, key = { "note_${it.bookId}_${it.pageNumber}" }) { note ->
                                Mind3DCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    tone = Mind3DCardTone.Lemon,
                                    onClick = { viewModel.navigateTo(ScreenDestination.BookReader(note.bookId, note.pageNumber)) }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("یادداشت در کتاب ${note.bookId} • صفحه ${note.pageNumber}", fontWeight = FontWeight.Bold, color = Color(0xFFA16207))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(note.noteText, style = MaterialTheme.typography.bodySmall, color = TextPrimaryLight)
                                    }
                                }
                            }
                            if (bookmarks.isEmpty() && pageNotes.isEmpty()) {
                                item {
                                    Mind3DCard(modifier = Modifier.fillMaxWidth(), tone = Mind3DCardTone.WarmIvory) {
                                        Text(
                                            text = "هنوز نشانک یا یادداشت صفحه‌ای در کتابخوان ثبت نکرده‌اید.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondaryLight,
                                            modifier = Modifier.padding(18.dp)
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
}

@Composable
private fun ExperienceCard(
    log: ExperienceLogEntity,
    onDelete: () -> Unit
) {
    Mind3DCard(
        modifier = Modifier.fillMaxWidth(),
        tone = Mind3DCardTone.Sky,
        shapeRadius = 20.dp,
        extrusionDepth = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    )
                    Text(
                        text = "شرایط: ${log.executionConditions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryLight
                    )
                }

                Mind3DIconButton(
                    icon = Icons.Default.Delete,
                    onClick = onDelete,
                    variant = Mind3DIconVariant.Danger,
                    buttonSize = 48.dp,
                    contentDescription = "حذف تجربه"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("پیش‌بینی:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(log.prediction, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("نتیجه واقعی:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(log.actualOutcome, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "اطمینان ذهنی: ${log.confidencePercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OrangeLightPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "حس: ${log.personalFeeling}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                )
            }

            if (log.interpretation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SkyBlueSoft.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "تحلیل و یادداشت: ${log.interpretation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF01579B)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddExperienceDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var confidence by remember { mutableFloatStateOf(70f) }
    var feeling by remember { mutableStateOf("آرامش و تمرکز") }
    var interpretation by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Mind3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            tone = Mind3DCardTone.WarmIvory,
            shapeRadius = 22.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ثبت تجربه جدید در دفترچه",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام تمرین یا آزمایش") },
                    placeholder = { Text("مثال: آزمایش کارت زنر با دوستم") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = conditions,
                    onValueChange = { conditions = it },
                    label = { Text("شرایط محیط و زمان") },
                    placeholder = { Text("مثال: ساعت ۱۰ شب، سکوت کامل، بعد از تنفس ۴-۷-۸") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = prediction,
                    onValueChange = { prediction = it },
                    label = { Text("پیش‌بینی یا حدس شهودی") },
                    placeholder = { Text("مثال: حس کردم نماد امواج یا ستاره است") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = actual,
                    onValueChange = { actual = it },
                    label = { Text("نتیجه واقعی") },
                    placeholder = { Text("مثال: نماد ستاره بود") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "میزان اطمینان قبل از آشکارسازی: ${confidence.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangeLightPrimary
                    )
                    Slider(
                        value = confidence,
                        onValueChange = { confidence = it },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = OrangeLightPrimary,
                            activeTrackColor = OrangeLightPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = feeling,
                    onValueChange = { feeling = it },
                    label = { Text("احساس شخصی حین تمرین") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = interpretation,
                    onValueChange = { interpretation = it },
                    label = { Text("برداشت و تحلیل خودتان") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Mind3DButton(
                    text = "ذخیره در دفترچه (+۴۰ XP)",
                    onClick = {
                        if (title.isNotBlank() && prediction.isNotBlank()) {
                            viewModel.addExperienceLog(
                                title = title,
                                conditions = conditions,
                                prediction = prediction,
                                actual = actual,
                                confidence = confidence.toInt(),
                                feeling = feeling,
                                interpretation = interpretation
                            )
                            onDismiss()
                        }
                    },
                    variant = Mind3DButtonVariant.Emerald,
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Mind3DButton(
                    text = "انصراف",
                    onClick = onDismiss,
                    variant = Mind3DButtonVariant.Neutral,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )
            }
        }
    }
}
