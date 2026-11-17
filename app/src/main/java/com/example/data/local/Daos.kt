package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {
    @Query("SELECT * FROM licenses ORDER BY issuedAt DESC")
    fun getAllLicenses(): Flow<List<LicenseEntity>>

    @Query("SELECT * FROM licenses ORDER BY issuedAt DESC")
    suspend fun getAllLicensesDirect(): List<LicenseEntity>

    @Query("SELECT * FROM licenses WHERE licenseKey = :key LIMIT 1")
    suspend fun getLicenseByKey(key: String): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicense(license: LicenseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLicenses(licenses: List<LicenseEntity>)

    @Update
    suspend fun updateLicense(license: LicenseEntity)

    @Query("UPDATE licenses SET status = :status WHERE licenseKey = :key")
    suspend fun updateStatus(key: String, status: String)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    suspend fun getAllChaptersDirect(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET isUnlocked = 1")
    suspend fun unlockAllChapters()
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE chapterId = :chapterId ORDER BY id ASC")
    fun getLessonsByChapter(chapterId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Int): LessonEntity?

    @Query("SELECT * FROM lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons ORDER BY id ASC")
    suspend fun getAllLessonsDirect(): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("SELECT COUNT(*) FROM lessons WHERE isCompleted = 1")
    fun getCompletedLessonCount(): Flow<Int>
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    suspend fun getAllFlashcardsDirect(): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getFlashcardById(id: Int): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE category = :category ORDER BY id ASC")
    fun getFlashcardsByCategory(category: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :now ORDER BY nextReviewDate ASC")
    fun getDueFlashcards(now: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE errorCount > 0 OR isMarkedDifficult = 1 ORDER BY errorCount DESC")
    fun getErrorFlashcards(): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)
}

@Dao
interface ExperienceLogDao {
    @Query("SELECT * FROM experience_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ExperienceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExperienceLogEntity)

    @Update
    suspend fun updateLog(log: ExperienceLogEntity)

    @Delete
    suspend fun deleteLog(log: ExperienceLogEntity)
}

@Dao
interface GameResultDao {
    @Query("SELECT * FROM game_results ORDER BY timestamp DESC LIMIT 50")
    fun getRecentResults(): Flow<List<GameResultEntity>>

    @Query("SELECT * FROM game_results WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun getResultsByGame(gameId: String): Flow<List<GameResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: GameResultEntity)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET isCourseUnlocked = 1, activeLicenseKey = :key, boundDeviceId = :deviceId WHERE id = 1")
    suspend fun setCourseUnlocked(key: String, deviceId: String)

    @Query("UPDATE user_profile SET xp = xp + :points WHERE id = 1")
    suspend fun addXp(points: Int)
}

@Dao
interface MentorBookDao {
    @Query("SELECT * FROM mentor_books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<MentorBookEntity>>

    @Query("SELECT * FROM mentor_books ORDER BY id ASC")
    suspend fun getAllBooksDirect(): List<MentorBookEntity>

    @Query("SELECT * FROM mentor_books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Int): MentorBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<MentorBookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: MentorBookEntity)

    @Query("UPDATE mentor_books SET isUnlocked = 1")
    suspend fun unlockAllBooks()
}

@Dao
interface BookPageDao {
    @Query("SELECT * FROM book_pages WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getPagesByBook(bookId: Int): Flow<List<BookPageEntity>>

    @Query("SELECT * FROM book_pages ORDER BY bookId ASC, pageNumber ASC")
    fun getAllPages(): Flow<List<BookPageEntity>>

    @Query("SELECT * FROM book_pages ORDER BY bookId ASC, pageNumber ASC")
    suspend fun getAllPagesDirect(): List<BookPageEntity>

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getPage(bookId: Int, pageNumber: Int): BookPageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<BookPageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: BookPageEntity)

    @Query("DELETE FROM book_pages WHERE bookId = :bookId")
    suspend fun deletePagesByBook(bookId: Int)
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getProgress(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgressDirect(): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ReadingProgressEntity)
}

@Dao
interface BookBookmarkDao {
    @Query("SELECT * FROM book_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookBookmarkEntity>>

    @Query("SELECT * FROM book_bookmarks WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getBookmark(bookId: Int, pageNumber: Int): BookBookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookBookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookBookmarkEntity)
}

@Dao
interface BookPageNoteDao {
    @Query("SELECT * FROM book_page_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<BookPageNoteEntity>>

    @Query("SELECT * FROM book_page_notes WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getNoteForPage(bookId: Int, pageNumber: Int): BookPageNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: BookPageNoteEntity)

    @Delete
    suspend fun deleteNote(note: BookPageNoteEntity)
}

@Dao
interface PredictionEntryDao {
    @Query("SELECT * FROM prediction_entries ORDER BY dayNumber ASC, createdAt DESC")
    fun getAllEntries(): Flow<List<PredictionEntryEntity>>

    @Query("SELECT * FROM prediction_entries WHERE dayNumber = :dayNumber ORDER BY createdAt DESC")
    fun getEntriesForDay(dayNumber: Int): Flow<List<PredictionEntryEntity>>

    @Query("SELECT * FROM prediction_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Int): PredictionEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: PredictionEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: PredictionEntryEntity)
}

@Dao
interface AccessRequestDao {
    @Query("SELECT * FROM access_requests ORDER BY requestedAt DESC")
    fun getAllRequests(): Flow<List<AccessRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: AccessRequestEntity)

    @Update
    suspend fun updateRequest(request: AccessRequestEntity)
}

@Dao
interface ContentMetadataDao {
    @Query("SELECT * FROM content_metadata WHERE id = 1 LIMIT 1")
    fun getMetadata(): Flow<ContentMetadataEntity?>

    @Query("SELECT * FROM content_metadata WHERE id = 1 LIMIT 1")
    suspend fun getMetadataDirect(): ContentMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMetadata(metadata: ContentMetadataEntity)
}
