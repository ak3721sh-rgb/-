package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccessRequestEntity
import com.example.data.model.BookBookmarkEntity
import com.example.data.model.BookPageEntity
import com.example.data.model.BookPageNoteEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ContentMetadataEntity
import com.example.data.model.ExperienceLogEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.GameResultEntity
import com.example.data.model.LessonEntity
import com.example.data.model.LicenseEntity
import com.example.data.model.MentorBookEntity
import com.example.data.model.PredictionEntryEntity
import com.example.data.model.ReadingProgressEntity
import com.example.data.model.UserProfileEntity
import com.example.data.sync.AppUpdateManager
import com.example.data.sync.BundleSyncResult
import com.example.data.sync.ContentBundleManager
import com.example.data.sync.UpdateCheckResult
import com.example.security.ActivationResult
import com.example.security.LicenseSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScreenDestination {
    data object Splash : ScreenDestination()
    data object Portal : ScreenDestination()
    data object InstructorProfile : ScreenDestination()
    data object BooksLibrary : ScreenDestination()
    data class BookReader(val bookId: Int, val pageNumber: Int = 1) : ScreenDestination()
    data object Chapters : ScreenDestination()
    data class ChapterDetail(val chapterId: Int) : ScreenDestination()
    data class LessonDetail(val lessonId: Int) : ScreenDestination()
    data object SpiritualSection : ScreenDestination()
    data object PracticesHub : ScreenDestination()
    data object TechniquesHub : ScreenDestination()
    data object ProgressStats : ScreenDestination()
    data object GamesHub : ScreenDestination()
    data class GamePlay(val gameId: String) : ScreenDestination()
    data object Flashcards : ScreenDestination()
    data object Journal : ScreenDestination()
    data object InstructorRoom : ScreenDestination()
    data object ContactInstructor : ScreenDestination()
    data object DownloadHub : ScreenDestination()
}

sealed class UpdateCheckUiState {
    data object Idle : UpdateCheckUiState()
    data object Checking : UpdateCheckUiState()
    data class Result(val result: UpdateCheckResult) : UpdateCheckUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val securityManager = LicenseSecurityManager(application, database)

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf<ScreenDestination>()
    private var lastSavedPageKey: Pair<Int, Int>? = null

    val userProfile: StateFlow<UserProfileEntity?> = database.userProfileDao()
        .getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val mentorBooks: StateFlow<List<MentorBookEntity>> = database.mentorBookDao()
        .getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookPages: StateFlow<List<BookPageEntity>> = database.bookPageDao()
        .getAllPages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingProgress: StateFlow<ReadingProgressEntity?> = database.readingProgressDao()
        .getProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chapters: StateFlow<List<ChapterEntity>> = database.chapterDao()
        .getAllChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessons: StateFlow<List<LessonEntity>> = database.lessonDao()
        .getAllLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLicenses: StateFlow<List<LicenseEntity>> = database.licenseDao()
        .getAllLicenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = database.flashcardDao()
        .getAllFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val experienceLogs: StateFlow<List<ExperienceLogEntity>> = database.experienceLogDao()
        .getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameResults: StateFlow<List<GameResultEntity>> = database.gameResultDao()
        .getRecentResults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookBookmarkEntity>> = database.bookBookmarkDao()
        .getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pageNotes: StateFlow<List<BookPageNoteEntity>> = database.bookPageNoteDao()
        .getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val predictionEntries: StateFlow<List<PredictionEntryEntity>> = database.predictionEntryDao()
        .getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accessRequests: StateFlow<List<AccessRequestEntity>> = database.accessRequestDao()
        .getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentMetadata: StateFlow<ContentMetadataEntity?> = database.contentMetadataDao()
        .getMetadata()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _activationState = MutableStateFlow<String?>(null)
    val activationState: StateFlow<String?> = _activationState.asStateFlow()

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private val _updateCheckState = MutableStateFlow<UpdateCheckUiState>(UpdateCheckUiState.Idle)
    val updateCheckState: StateFlow<UpdateCheckUiState> = _updateCheckState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.ensureSeeded(database)
        }
    }

    fun checkForAppUpdate() {
        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckUiState.Checking
            val configuredUrl = withContext(Dispatchers.IO) {
                database.contentMetadataDao().getMetadataDirect()?.apkDownloadUrl.orEmpty()
            }
            val result = AppUpdateManager.checkForUpdate(
                isNetworkConnected = isNetworkConnected(),
                configuredApkOrManifestUrl = configuredUrl
            )
            _updateCheckState.value = UpdateCheckUiState.Result(result)
        }
    }

    fun resetUpdateCheckState() {
        _updateCheckState.value = UpdateCheckUiState.Idle
    }

    fun isNetworkConnected(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value == destination) return
        _screenStack.add(_currentScreen.value)
        _currentScreen.value = destination
    }

    fun navigateBack(): Boolean {
        if (_screenStack.isNotEmpty()) {
            _currentScreen.value = _screenStack.removeAt(_screenStack.size - 1)
            return true
        }
        if (_currentScreen.value != ScreenDestination.Splash) {
            _currentScreen.value = ScreenDestination.Splash
            return true
        }
        return false
    }

    fun activateLicenseKey(key: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isActivating.value = true
            val result = withContext(Dispatchers.IO) {
                val res = securityManager.activateKey(key)
                if (res is ActivationResult.Success) {
                    database.userProfileDao().addXp(150)
                    database.mentorBookDao().unlockAllBooks()
                    database.chapterDao().unlockAllChapters()
                }
                res
            }
            when (result) {
                is ActivationResult.Success -> {
                    _isActivating.value = false
                    _activationState.value = "دوره با موفقیت برای این دستگاه فعال شد!"
                    onComplete(true, "دوره با موفقیت برای این دستگاه فعال شد.")
                }
                is ActivationResult.Error -> {
                    _isActivating.value = false
                    _activationState.value = result.message
                    onComplete(false, result.message)
                }
            }
        }
    }

    fun saveProgress(bookId: Int, chapterNum: Int, pageNum: Int, title: String) {
        val pageKey = bookId to pageNum
        if (lastSavedPageKey == pageKey) return
        lastSavedPageKey = pageKey
        viewModelScope.launch(Dispatchers.IO) {
            val current = database.readingProgressDao().getProgressDirect()
            val updated = (current ?: ReadingProgressEntity()).copy(
                lastReadBookId = bookId,
                lastReadChapterNumber = chapterNum,
                lastReadPageNumber = pageNum,
                lastActivityTitle = title,
                completedPagesCount = maxOf((current?.completedPagesCount ?: 0), pageNum),
                totalReadingMinutes = (current?.totalReadingMinutes ?: 0) + 1
            )
            database.readingProgressDao().saveProgress(updated)
            database.userProfileDao().addXp(10)
        }
    }

    fun getPagesForBook(bookId: Int): Flow<List<BookPageEntity>> =
        database.bookPageDao().getPagesByBook(bookId)

    fun getLessonsForChapter(chapterId: Int): Flow<List<LessonEntity>> =
        database.lessonDao().getLessonsByChapter(chapterId)

    fun toggleBookmark(bookId: Int, pageNumber: Int, sectionTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = database.bookBookmarkDao().getBookmark(bookId, pageNumber)
            if (existing != null) {
                database.bookBookmarkDao().deleteBookmark(existing)
            } else {
                database.bookBookmarkDao().insertBookmark(
                    BookBookmarkEntity(
                        bookId = bookId,
                        pageNumber = pageNumber,
                        sectionTitle = sectionTitle
                    )
                )
                database.userProfileDao().addXp(10)
            }
        }
    }

    fun savePageNote(bookId: Int, pageNumber: Int, noteText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = database.bookPageNoteDao().getNoteForPage(bookId, pageNumber)
            if (noteText.isBlank()) {
                if (existing != null) database.bookPageNoteDao().deleteNote(existing)
            } else {
                database.bookPageNoteDao().upsertNote(
                    BookPageNoteEntity(
                        id = existing?.id ?: 0,
                        bookId = bookId,
                        pageNumber = pageNumber,
                        noteText = noteText.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                database.userProfileDao().addXp(15)
            }
        }
    }

    /**
     * Save or update a 7-day prediction entry.
     * Once outcome != PENDING (or isPredictionLocked == true), predictionText is strictly preserved
     * from DB/previous locked state so pre-occurrence predictions cannot be altered retroactively.
     */
    fun savePredictionEntry(entry: PredictionEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = if (entry.id != 0) database.predictionEntryDao().getEntryById(entry.id) else null
            val shouldLock = entry.outcomeStatus != "PENDING" || entry.isPredictionLocked || (existing?.isPredictionLocked == true)
            val safePredictionText = if (existing?.isPredictionLocked == true) {
                existing.predictionText
            } else {
                entry.predictionText.trim()
            }
            val finalEntry = entry.copy(
                predictionText = safePredictionText,
                isPredictionLocked = shouldLock,
                lockedAt = if (shouldLock) (existing?.lockedAt ?: entry.lockedAt ?: System.currentTimeMillis()) else null
            )
            database.predictionEntryDao().upsertEntry(finalEntry)
            database.userProfileDao().addXp(25)
        }
    }

    fun deletePredictionEntry(entry: PredictionEntryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.predictionEntryDao().deleteEntry(entry)
        }
    }

    fun markLessonCompleted(lessonId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val lesson = database.lessonDao().getLessonById(lessonId) ?: return@launch
            if (!lesson.isCompleted) {
                database.lessonDao().updateLesson(lesson.copy(isCompleted = true))
                database.userProfileDao().addXp(35)
            }
        }
    }

    fun updateBookPageContent(
        bookId: Int,
        pageNumber: Int,
        chapterTitle: String,
        sectionTitle: String,
        bodyText: String,
        techniqueBox: String?,
        reflectionExercise: String?,
        isSourcePlaceholder: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = database.bookPageDao().getPage(bookId, pageNumber)
            val updated = (existing ?: BookPageEntity(
                bookId = bookId,
                chapterNumber = 1,
                pageNumber = pageNumber,
                chapterTitle = chapterTitle,
                sectionTitle = sectionTitle,
                bodyText = bodyText
            )).copy(
                chapterTitle = chapterTitle.trim(),
                sectionTitle = sectionTitle.trim(),
                bodyText = bodyText.trim(),
                techniqueBox = techniqueBox?.trim()?.ifBlank { null },
                reflectionExercise = reflectionExercise?.trim()?.ifBlank { null },
                isSourcePlaceholder = isSourcePlaceholder
            )
            database.bookPageDao().upsertPage(updated)
            val book = database.mentorBookDao().getBookById(bookId)
            if (book != null) {
                val allPages = database.bookPageDao().getAllPagesDirect().filter { it.bookId == bookId }
                val allComplete = allPages.isNotEmpty() && allPages.none { it.isSourcePlaceholder }
                database.mentorBookDao().upsertBook(
                    book.copy(
                        isContentComplete = allComplete,
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun upsertLessonContent(lesson: LessonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.lessonDao().upsertLesson(lesson)
        }
    }

    fun exportContentBundle(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) { ContentBundleManager.exportBundleJson(database) }
            _syncStatusMessage.value = "بسته کامل محتوا با فرمت JSON آماده شد."
            onReady(json)
        }
    }

    fun importContentBundle(jsonText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = withContext(Dispatchers.IO) { ContentBundleManager.importBundleJson(database, jsonText) }) {
                is BundleSyncResult.Success -> {
                    val msg = "به‌روزرسانی آفلاین نسخه ${res.version} با ${res.updatedCount} مورد با موفقیت انجام شد."
                    _syncStatusMessage.value = msg
                    onResult(true, msg)
                }
                is BundleSyncResult.Error -> {
                    _syncStatusMessage.value = res.message
                    onResult(false, res.message)
                }
            }
        }
    }

    fun syncContentFromUrl(url: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = withContext(Dispatchers.IO) { ContentBundleManager.syncFromRemoteUrl(database, url) }) {
                is BundleSyncResult.Success -> {
                    val msg = "همگام‌سازی نسخه ${res.version} با ${res.updatedCount} مورد با موفقیت انجام و ذخیره آفلاین شد."
                    _syncStatusMessage.value = msg
                    onResult(true, msg)
                }
                is BundleSyncResult.Error -> {
                    _syncStatusMessage.value = res.message
                    onResult(false, res.message)
                }
            }
        }
    }

    fun updateRemoteUrls(remoteFeedUrl: String, apkDownloadUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = database.contentMetadataDao().getMetadataDirect() ?: ContentMetadataEntity()
            database.contentMetadataDao().saveMetadata(
                current.copy(
                    remoteFeedUrl = remoteFeedUrl.trim(),
                    apkDownloadUrl = apkDownloadUrl.trim(),
                    lastUpdatedAt = System.currentTimeMillis()
                )
            )
            _syncStatusMessage.value = "نشانی‌های انتشار و همگام‌سازی ذخیره شد."
        }
    }

    fun generateNewKeyForInstructor(
        studentName: String,
        studentContact: String = "",
        validityDays: Int? = 90,
        maxDevices: Int = 1,
        onGenerated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val key = withContext(Dispatchers.IO) {
                securityManager.generateNewLicenseKey(
                    studentName = studentName,
                    studentContact = studentContact,
                    validityDays = validityDays,
                    maxDevices = maxDevices
                )
            }
            onGenerated(key)
        }
    }

    fun extendLicense(key: String, extraDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.extendLicenseDays(key, extraDays)
        }
    }

    fun revokeKeyByInstructor(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.revokeLicense(key)
        }
    }

    fun requestKeySms(studentName: String, studentContact: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.recordAccessRequest(studentName, studentContact, "SMS")
        }
        securityManager.requestKeyViaSms(studentName)
    }

    fun requestKeyEmail(studentName: String, studentContact: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.recordAccessRequest(studentName, studentContact, "EMAIL")
        }
        securityManager.requestKeyViaEmail(studentName)
    }

    fun logAccessRequest(studentName: String, studentContact: String, channel: String = "IN_APP") {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.recordAccessRequest(studentName, studentContact, channel)
        }
    }

    fun markAccessRequestIssued(request: AccessRequestEntity, issuedKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.accessRequestDao().updateRequest(
                request.copy(status = "ISSUED", issuedKey = issuedKey)
            )
        }
    }

    fun dismissAccessRequest(request: AccessRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.accessRequestDao().updateRequest(
                request.copy(status = "DISMISSED")
            )
        }
    }

    fun addExperienceLog(
        title: String,
        conditions: String,
        prediction: String,
        actual: String,
        confidence: Int,
        feeling: String,
        interpretation: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = ExperienceLogEntity(
                dateString = "امروز",
                exerciseName = title,
                executionConditions = conditions,
                prediction = prediction,
                actualOutcome = actual,
                confidencePercent = confidence,
                personalFeeling = feeling,
                interpretation = interpretation
            )
            database.experienceLogDao().insertLog(log)
            database.userProfileDao().addXp(40)
        }
    }

    fun deleteExperienceLog(log: ExperienceLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.experienceLogDao().deleteLog(log)
        }
    }

    fun updateFlashcardMastery(card: FlashcardEntity, isCorrect: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = if (isCorrect) {
                val nextLevel = (card.level + 1).coerceAtMost(5)
                card.copy(
                    level = nextLevel,
                    nextReviewDate = System.currentTimeMillis() + (nextLevel * 86400000L),
                    isMastered = true,
                    isMarkedDifficult = false
                )
            } else {
                card.copy(
                    level = 1,
                    errorCount = card.errorCount + 1,
                    nextReviewDate = System.currentTimeMillis() + 3600000L,
                    isMastered = false,
                    isMarkedDifficult = true
                )
            }
            database.flashcardDao().updateFlashcard(updated)
            database.userProfileDao().addXp(20)
        }
    }

    fun recordGameScore(
        gameId: String,
        gameTitle: String,
        score: Int,
        accuracy: Float,
        reactionTime: Long,
        difficulty: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = GameResultEntity(
                gameId = gameId,
                gameTitle = gameTitle,
                score = score,
                accuracyPercent = accuracy,
                reactionTimeMs = reactionTime,
                difficultyLevel = difficulty
            )
            database.gameResultDao().insertResult(result)
            database.userProfileDao().addXp((score / 4).coerceAtLeast(10))
        }
    }
}
