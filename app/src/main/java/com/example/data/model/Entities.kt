package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "licenses")
data class LicenseEntity(
    @PrimaryKey val licenseKey: String,
    val status: String, // "ACTIVE", "USED", "REVOKED", "EXPIRED"
    val boundDeviceId: String? = null,
    val boundDeviceIds: String = "",
    val maxDevices: Int = 1,
    val usedCount: Int = 0,
    val issuedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // null = unlimited
    val activatedAt: Long? = null,
    val studentName: String? = null,
    val studentContact: String? = null,
    val notes: String = ""
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subtitle: String,
    val introduction: String,
    val learningObjectives: String,
    val isUnlocked: Boolean = true,
    val iconName: String = "menu_book"
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val chapterId: Int,
    val title: String,
    val contentType: String = "CONCEPT", // "TECHNIQUE", "EXERCISE", "CONCEPT"
    val content: String,
    val example: String,
    val stepTechnique: String,
    val soloExercise: String,
    val pairExercise: String,
    val quizQuestion: String,
    val quizOption1: String,
    val quizOption2: String,
    val quizOption3: String,
    val quizOption4: String,
    val quizCorrectIndex: Int,
    val quizExplanation: String,
    val summary: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chapterId: Int,
    val category: String, // "TECHNIQUE", "EXERCISE", "CONCEPT"
    val frontTitle: String,
    val frontContent: String,
    val backAnswer: String,
    val level: Int = 1, // 1 to 5 (Leitner system)
    val nextReviewDate: Long = System.currentTimeMillis(),
    val errorCount: Int = 0,
    val isMastered: Boolean = false,
    val isMarkedDifficult: Boolean = false
)

@Entity(tableName = "experience_logs")
data class ExperienceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val exerciseName: String,
    val executionConditions: String,
    val prediction: String,
    val actualOutcome: String,
    val confidencePercent: Int, // 1 to 100
    val personalFeeling: String,
    val interpretation: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameId: String,
    val gameTitle: String,
    val score: Int,
    val accuracyPercent: Float,
    val reactionTimeMs: Long,
    val difficultyLevel: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "هنرجوی گرامی",
    val xp: Int = 240,
    val level: Int = 1,
    val streakDays: Int = 3,
    val lastActiveDate: String = "",
    val isCourseUnlocked: Boolean = false,
    val activeLicenseKey: String? = null,
    val boundDeviceId: String = ""
)

@Entity(tableName = "mentor_books")
data class MentorBookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val coverAccentColor: String,
    val iconName: String,
    val totalPages: Int,
    val isUnlocked: Boolean = true,
    val contentVersion: String = "1.0",
    val isOfflineSaved: Boolean = true,
    val isContentComplete: Boolean = false,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "book_pages")
data class BookPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val chapterNumber: Int,
    val pageNumber: Int,
    val chapterTitle: String,
    val sectionTitle: String,
    val bodyText: String,
    val techniqueBox: String? = null,
    val reflectionExercise: String? = null,
    val isSourcePlaceholder: Boolean = false
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: Int = 1,
    val lastReadBookId: Int = 1,
    val lastReadChapterNumber: Int = 1,
    val lastReadPageNumber: Int = 1,
    val lastActivityTitle: String = "کتاب اول: مبانی ارتباط ذهن‌ها؛ تله‌پاتی چیست؟",
    val completedExercisesCount: Int = 0,
    val completedPagesCount: Int = 1,
    val totalReadingMinutes: Int = 15
)

@Entity(tableName = "book_bookmarks")
data class BookBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val pageNumber: Int,
    val sectionTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "book_page_notes")
data class BookPageNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val pageNumber: Int,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 7-Day Prediction Tracker (فرم ثبت پیش‌بینی پیش از وقوع با قابلیت قفل متن پیش‌بینی پس از رخداد)
 */
@Entity(tableName = "prediction_entries")
data class PredictionEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayNumber: Int, // 1..7
    val dateTimeText: String,
    val predictionText: String,
    val isPredictionLocked: Boolean = false,
    val confidencePercent: Int = 70,
    val outcomeStatus: String = "PENDING", // "PENDING", "CORRECT", "INCORRECT", "UNKNOWN"
    val actualOutcomeText: String = "",
    val reflectionNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lockedAt: Long? = null
)

@Entity(tableName = "access_requests")
data class AccessRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val studentContact: String = "",
    val deviceId: String,
    val channel: String, // "SMS", "EMAIL", "IN_APP"
    val status: String = "PENDING", // "PENDING", "ISSUED", "DISMISSED"
    val issuedKey: String? = null,
    val requestedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_metadata")
data class ContentMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val contentVersion: String = "1.0.0",
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val remoteFeedUrl: String = "",
    val apkDownloadUrl: String = "",
    val notes: String = "نسخه پایه آفلاین — آماده همگام‌سازی و دریافت متن رسمی جزوه‌ها"
)
