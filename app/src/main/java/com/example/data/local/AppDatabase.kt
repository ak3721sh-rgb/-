package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
        ContentMetadataEntity::class
    ],
    version = 3,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val seedMutex = Mutex()

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mind_connection_db"
                )
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
            if (database.contentMetadataDao().getMetadataDirect() == null) {
                database.contentMetadataDao().saveMetadata(InitialDataSeed.getInitialContentMetadata())
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
            if (database.mentorBookDao().getAllBooksDirect().isEmpty()) {
                database.mentorBookDao().insertBooks(InitialDataSeed.getMentorBooks())
            }
            if (database.bookPageDao().getAllPagesDirect().isEmpty()) {
                database.bookPageDao().insertPages(InitialDataSeed.getBookPages())
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
