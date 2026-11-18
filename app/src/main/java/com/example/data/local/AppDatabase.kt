package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccessRequestEntity
import com.example.data.model.BookBookmarkEntity
import com.example.data.model.BookPageEntity
import com.example.data.model.BookPageNoteEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ContentMetadataEntity
import com.example.data.model.DiagnosticLogEntity
import com.example.data.model.ExperienceLogEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.GameResultEntity
import com.example.data.model.LessonEntity
import com.example.data.model.LicenseEntity
import com.example.data.model.MentorBookEntity
import com.example.data.model.PredictionEntryEntity
import com.example.data.model.ReadingProgressEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [
        LicenseEntity::class,
        ChapterEntity::class,
        LessonEntity::class,
        FlashcardEntity::class,
        ExperienceLogEntity::class,
        GameResultEntity::class,
        UserProfileEntity::class,
        MentorBookEntity::class,
        BookPageEntity::class,
        ReadingProgressEntity::class,
        BookBookmarkEntity::class,
        BookPageNoteEntity::class,
        PredictionEntryEntity::class,
        AccessRequestEntity::class,
        ContentMetadataEntity::class,
        DiagnosticLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun licenseDao(): LicenseDao
    abstract fun chapterDao(): ChapterDao
    abstract fun lessonDao(): LessonDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun experienceLogDao(): ExperienceLogDao
    abstract fun gameResultDao(): GameResultDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun mentorBookDao(): MentorBookDao
    abstract fun bookPageDao(): BookPageDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun bookBookmarkDao(): BookBookmarkDao
    abstract fun bookPageNoteDao(): BookPageNoteDao
    abstract fun predictionEntryDao(): PredictionEntryDao
    abstract fun accessRequestDao(): AccessRequestDao
    abstract fun contentMetadataDao(): ContentMetadataDao
    abstract fun diagnosticLogDao(): DiagnosticLogDao
    abstract fun progressMaintenanceDao(): ProgressMaintenanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val seedMutex = Mutex()

        /**
         * Preserves 100% of existing student notes, bookmarks, flashcards, licenses,
         * reading progress, game history, and logs when upgrading from DB v3 to v4.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE mentor_books ADD COLUMN subjectCategory TEXT NOT NULL DEFAULT 'ارتباط ذهن و ذهن'"
                )
                db.execSQL(
                    "ALTER TABLE book_pages ADD COLUMN contentOrigin TEXT NOT NULL DEFAULT 'SCIENTIFIC_SUPPLEMENT'"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `diagnostic_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `tag` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `severity` TEXT NOT NULL DEFAULT 'INFO',
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mind_connection_db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureSeeded(database: AppDatabase) = seedMutex.withLock {
            if (database.userProfileDao().getProfileDirect() == null) {
                database.userProfileDao().insertOrUpdate(InitialDataSeed.getInitialProfile())
            }
            val existingMeta = database.contentMetadataDao().getMetadataDirect()
            if (existingMeta == null) {
                database.contentMetadataDao().saveMetadata(InitialDataSeed.getInitialContentMetadata())
            } else if (existingMeta.apkDownloadUrl.isBlank() ||
                existingMeta.apkDownloadUrl.trim() == InitialDataSeed.LEGACY_V13_APK_URL
            ) {
                database.contentMetadataDao().saveMetadata(
                    existingMeta.copy(apkDownloadUrl = InitialDataSeed.DEFAULT_PUBLIC_APK_URL)
                )
            }
            if (database.chapterDao().getAllChaptersDirect().isEmpty()) {
                database.chapterDao().insertChapters(InitialDataSeed.getChapters())
            }
            if (database.lessonDao().getAllLessonsDirect().isEmpty()) {
                database.lessonDao().insertLessons(InitialDataSeed.getLessons())
            }
            if (database.flashcardDao().getAllFlashcardsDirect().isEmpty()) {
                database.flashcardDao().insertFlashcards(InitialDataSeed.getFlashcards())
            }

            val seedBooks = InitialDataSeed.getMentorBooks()
            val existingBooks = database.mentorBookDao().getAllBooksDirect()
            if (existingBooks.isEmpty()) {
                database.mentorBookDao().insertBooks(seedBooks)
            } else {
                // Non-destructive enrichment for v1.4 subjectCategory & clean subtitles
                val seedBookMap = seedBooks.associateBy { it.id }
                existingBooks.forEach { existing ->
                    val seed = seedBookMap[existing.id] ?: return@forEach
                    val cleanSub = if (existing.subtitle.contains("پیشنهادی ـ نیازمند تأیید مدرس")) {
                        seed.subtitle
                    } else {
                        existing.subtitle
                    }
                    val needsCategoryUpdate = existing.subjectCategory == "ارتباط ذهن و ذهن" && seed.subjectCategory != "ارتباط ذهن و ذهن"
                    if (needsCategoryUpdate || cleanSub != existing.subtitle) {
                        database.mentorBookDao().upsertBook(
                            existing.copy(
                                subjectCategory = seed.subjectCategory,
                                subtitle = cleanSub
                            )
                        )
                    }
                }
            }

            val seedPages = InitialDataSeed.getBookPages()
            val existingPages = database.bookPageDao().getAllPagesDirect()
            if (existingPages.isEmpty()) {
                database.bookPageDao().insertPages(seedPages)
            } else {
                // Upgrade unedited legacy developer placeholder strings to clean learner-facing status & contentOrigin
                val seedByKey = seedPages.associateBy { it.bookId to it.pageNumber }
                existingPages.forEach { page ->
                    val seed = seedByKey[page.bookId to page.pageNumber] ?: return@forEach
                    val hasLegacyNotice = page.bodyText.contains("فصل_اول_هنر_ارتباط_ذهنها.docx") ||
                        page.bodyText.contains("فایل Word و PDF اصلی فصل دوم در پروژه بارگذاری نشده") ||
                        page.bodyText.contains("[پیشنهادی ـ نیازمند تأیید مدرس]")
                    val cleanTitle = page.chapterTitle.replace(" (پیشنهادی ـ نیازمند تأیید مدرس)", "")
                    if (hasLegacyNotice || page.contentOrigin != seed.contentOrigin || cleanTitle != page.chapterTitle) {
                        database.bookPageDao().upsertPage(
                            page.copy(
                                chapterTitle = cleanTitle,
                                bodyText = if (hasLegacyNotice) seed.bodyText else page.bodyText,
                                techniqueBox = if (hasLegacyNotice) seed.techniqueBox else page.techniqueBox,
                                reflectionExercise = if (hasLegacyNotice) seed.reflectionExercise else page.reflectionExercise,
                                contentOrigin = if (page.isSourcePlaceholder) seed.contentOrigin else page.contentOrigin
                            )
                        )
                    }
                }
            }

            if (database.readingProgressDao().getProgressDirect() == null) {
                database.readingProgressDao().saveProgress(InitialDataSeed.getInitialReadingProgress())
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureSeeded(database)
                    }
                }
            }
        }
    }
}
