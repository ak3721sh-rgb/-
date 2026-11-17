package com.example.data.sync

import com.example.data.local.AppDatabase
import com.example.data.model.BookPageEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ContentMetadataEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.LessonEntity
import com.example.data.model.MentorBookEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class BundleSyncResult {
    data class Success(val version: String, val updatedCount: Int) : BundleSyncResult()
    data class Error(val message: String) : BundleSyncResult()
}

/**
 * Dynamic content bundle export/import & optional HTTPS sync manager.
 * Enables Instructor (اکبر شیرزادخانی) to publish updated chapters, books, pages, lessons, quizzes,
 * and flashcards without rebuilding or reinstalling the APK, while strictly preserving student
 * bookmarks, notes, reading progress, lesson completions, and Leitner spaced-repetition progress.
 */
object ContentBundleManager {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun exportBundleJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val meta = database.contentMetadataDao().getMetadataDirect()
        val chapters = database.chapterDao().getAllChaptersDirect()
        val books = database.mentorBookDao().getAllBooksDirect()
        val pages = database.bookPageDao().getAllPagesDirect()
        val lessons = database.lessonDao().getAllLessonsDirect()
        val flashcards = database.flashcardDao().getAllFlashcardsDirect()

        val root = JSONObject().apply {
            put("contentVersion", meta?.contentVersion ?: "1.0.0")
            put("author", "اکبر شیرزادخانی")
            put("exportedAt", System.currentTimeMillis())

            put("chapters", JSONArray().apply {
                chapters.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("title", c.title)
                        put("subtitle", c.subtitle)
                        put("introduction", c.introduction)
                        put("learningObjectives", c.learningObjectives)
                        put("iconName", c.iconName)
                    })
                }
            })

            put("books", JSONArray().apply {
                books.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("title", b.title)
                        put("subtitle", b.subtitle)
                        put("description", b.description)
                        put("coverAccentColor", b.coverAccentColor)
                        put("iconName", b.iconName)
                        put("totalPages", b.totalPages)
                        put("contentVersion", b.contentVersion)
                        put("isContentComplete", b.isContentComplete)
                    })
                }
            })

            put("pages", JSONArray().apply {
                pages.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("bookId", p.bookId)
                        put("chapterNumber", p.chapterNumber)
                        put("pageNumber", p.pageNumber)
                        put("chapterTitle", p.chapterTitle)
                        put("sectionTitle", p.sectionTitle)
                        put("bodyText", p.bodyText)
                        put("techniqueBox", p.techniqueBox ?: "")
                        put("reflectionExercise", p.reflectionExercise ?: "")
                        put("isSourcePlaceholder", p.isSourcePlaceholder)
                    })
                }
            })

            put("lessons", JSONArray().apply {
                lessons.forEach { l ->
                    put(JSONObject().apply {
                        put("id", l.id)
                        put("chapterId", l.chapterId)
                        put("title", l.title)
                        put("contentType", l.contentType)
                        put("content", l.content)
                        put("example", l.example)
                        put("stepTechnique", l.stepTechnique)
                        put("soloExercise", l.soloExercise)
                        put("pairExercise", l.pairExercise)
                        put("quizQuestion", l.quizQuestion)
                        put("quizOption1", l.quizOption1)
                        put("quizOption2", l.quizOption2)
                        put("quizOption3", l.quizOption3)
                        put("quizOption4", l.quizOption4)
                        put("quizCorrectIndex", l.quizCorrectIndex)
                        put("quizExplanation", l.quizExplanation)
                        put("summary", l.summary)
                    })
                }
            })

            put("flashcards", JSONArray().apply {
                flashcards.forEach { f ->
                    put(JSONObject().apply {
                        put("id", f.id)
                        put("chapterId", f.chapterId)
                        put("category", f.category)
                        put("frontTitle", f.frontTitle)
                        put("frontContent", f.frontContent)
                        put("backAnswer", f.backAnswer)
                    })
                }
            })
        }

        root.toString(2)
    }

    suspend fun importBundleJson(database: AppDatabase, jsonText: String): BundleSyncResult =
        withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(jsonText.trim())
                val newVersion = root.optString("contentVersion", "1.0.1").ifBlank { "1.0.1" }
                val profile = database.userProfileDao().getProfileDirect()
                val courseUnlocked = profile?.isCourseUnlocked == true
                var count = 0

                val chaptersArray = root.optJSONArray("chapters")
                if (chaptersArray != null && chaptersArray.length() > 0) {
                    val existingChapters = database.chapterDao().getAllChaptersDirect().associateBy { it.id }
                    val importedChapters = mutableListOf<ChapterEntity>()
                    for (i in 0 until chaptersArray.length()) {
                        val o = chaptersArray.getJSONObject(i)
                        val cId = o.getInt("id")
                        val prev = existingChapters[cId]
                        importedChapters.add(
                            ChapterEntity(
                                id = cId,
                                title = o.getString("title"),
                                subtitle = o.optString("subtitle", prev?.subtitle ?: ""),
                                introduction = o.optString("introduction", prev?.introduction ?: ""),
                                learningObjectives = o.optString("learningObjectives", prev?.learningObjectives ?: ""),
                                isUnlocked = prev?.isUnlocked ?: true || courseUnlocked,
                                iconName = o.optString("iconName", prev?.iconName ?: "menu_book")
                            )
                        )
                    }
                    database.chapterDao().insertChapters(importedChapters)
                    count += importedChapters.size
                }

                val booksArray = root.optJSONArray("books")
                if (booksArray != null && booksArray.length() > 0) {
                    val existingBooks = database.mentorBookDao().getAllBooksDirect().associateBy { it.id }
                    val importedBooks = mutableListOf<MentorBookEntity>()
                    for (i in 0 until booksArray.length()) {
                        val o = booksArray.getJSONObject(i)
                        val bId = o.getInt("id")
                        val prev = existingBooks[bId]
                        importedBooks.add(
                            MentorBookEntity(
                                id = bId,
                                title = o.getString("title"),
                                subtitle = o.optString("subtitle", prev?.subtitle ?: ""),
                                description = o.optString("description", prev?.description ?: ""),
                                coverAccentColor = o.optString("coverAccentColor", prev?.coverAccentColor ?: "Sky"),
                                iconName = o.optString("iconName", prev?.iconName ?: "menu_book"),
                                totalPages = o.optInt("totalPages", prev?.totalPages ?: 1),
                                isUnlocked = prev?.isUnlocked ?: true || courseUnlocked,
                                contentVersion = o.optString("contentVersion", newVersion),
                                isOfflineSaved = true,
                                isContentComplete = o.optBoolean("isContentComplete", false),
                                lastUpdatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    database.mentorBookDao().insertBooks(importedBooks)
                    count += importedBooks.size
                }

                val pagesArray = root.optJSONArray("pages")
                if (pagesArray != null && pagesArray.length() > 0) {
                    val groupedByBook = mutableMapOf<Int, MutableList<BookPageEntity>>()
                    for (i in 0 until pagesArray.length()) {
                        val o = pagesArray.getJSONObject(i)
                        val bookId = o.getInt("bookId")
                        val page = BookPageEntity(
                            id = o.optInt("id", 0),
                            bookId = bookId,
                            chapterNumber = o.optInt("chapterNumber", 1),
                            pageNumber = o.getInt("pageNumber"),
                            chapterTitle = o.optString("chapterTitle", ""),
                            sectionTitle = o.optString("sectionTitle", ""),
                            bodyText = o.getString("bodyText"),
                            techniqueBox = o.optString("techniqueBox", "").ifBlank { null },
                            reflectionExercise = o.optString("reflectionExercise", "").ifBlank { null },
                            isSourcePlaceholder = o.optBoolean("isSourcePlaceholder", false)
                        )
                        groupedByBook.getOrPut(bookId) { mutableListOf() }.add(page)
                    }
                    groupedByBook.forEach { (bId, list) ->
                        val sorted = list.sortedBy { it.pageNumber }
                        database.bookPageDao().deletePagesByBook(bId)
                        database.bookPageDao().insertPages(sorted.map { it.copy(id = 0) })
                        database.mentorBookDao().getBookById(bId)?.let { existingBook ->
                            database.mentorBookDao().upsertBook(
                                existingBook.copy(
                                    totalPages = sorted.size,
                                    isContentComplete = existingBook.isContentComplete && sorted.none { it.isSourcePlaceholder },
                                    lastUpdatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                        count += sorted.size
                    }
                }

                val lessonsArray = root.optJSONArray("lessons")
                if (lessonsArray != null && lessonsArray.length() > 0) {
                    val existingLessons = database.lessonDao().getAllLessonsDirect().associateBy { it.id }
                    val importedLessons = mutableListOf<LessonEntity>()
                    for (i in 0 until lessonsArray.length()) {
                        val o = lessonsArray.getJSONObject(i)
                        val lessonId = o.getInt("id")
                        val existing = existingLessons[lessonId]
                        importedLessons.add(
                            LessonEntity(
                                id = lessonId,
                                chapterId = o.getInt("chapterId"),
                                title = o.getString("title"),
                                contentType = o.optString("contentType", "CONCEPT"),
                                content = o.getString("content"),
                                example = o.optString("example", ""),
                                stepTechnique = o.optString("stepTechnique", ""),
                                soloExercise = o.optString("soloExercise", ""),
                                pairExercise = o.optString("pairExercise", ""),
                                quizQuestion = o.optString("quizQuestion", ""),
                                quizOption1 = o.optString("quizOption1", ""),
                                quizOption2 = o.optString("quizOption2", ""),
                                quizOption3 = o.optString("quizOption3", ""),
                                quizOption4 = o.optString("quizOption4", ""),
                                quizCorrectIndex = o.optInt("quizCorrectIndex", 0),
                                quizExplanation = o.optString("quizExplanation", ""),
                                summary = o.optString("summary", ""),
                                isCompleted = existing?.isCompleted ?: false
                            )
                        )
                    }
                    database.lessonDao().insertLessons(importedLessons)
                    count += importedLessons.size
                }

                val flashcardsArray = root.optJSONArray("flashcards")
                if (flashcardsArray != null && flashcardsArray.length() > 0) {
                    val existingCards = database.flashcardDao().getAllFlashcardsDirect().associateBy { it.id }
                    val now = System.currentTimeMillis()
                    val importedCards = mutableListOf<FlashcardEntity>()
                    for (i in 0 until flashcardsArray.length()) {
                        val o = flashcardsArray.getJSONObject(i)
                        val cardId = o.getInt("id")
                        val existing = existingCards[cardId]
                        importedCards.add(
                            FlashcardEntity(
                                id = cardId,
                                chapterId = o.getInt("chapterId"),
                                category = o.optString("category", "CONCEPT"),
                                frontTitle = o.getString("frontTitle"),
                                frontContent = o.getString("frontContent"),
                                backAnswer = o.getString("backAnswer"),
                                level = existing?.level ?: 1,
                                nextReviewDate = existing?.nextReviewDate ?: now,
                                errorCount = existing?.errorCount ?: 0,
                                isMastered = existing?.isMastered ?: false,
                                isMarkedDifficult = existing?.isMarkedDifficult ?: false
                            )
                        )
                    }
                    database.flashcardDao().insertFlashcards(importedCards)
                    count += importedCards.size
                }

                val currentMeta = database.contentMetadataDao().getMetadataDirect()
                database.contentMetadataDao().saveMetadata(
                    (currentMeta ?: ContentMetadataEntity()).copy(
                        contentVersion = newVersion,
                        lastUpdatedAt = System.currentTimeMillis(),
                        notes = "آخرین بروزرسانی محتوا ($newVersion) با $count مورد ذخیره آفلاین شد."
                    )
                )

                BundleSyncResult.Success(newVersion, count)
            } catch (e: Exception) {
                BundleSyncResult.Error("ساختار JSON بسته محتوا معتبر نیست: ${e.localizedMessage ?: "خطای ناشناخته"}")
            }
        }

    suspend fun syncFromRemoteUrl(database: AppDatabase, url: String): BundleSyncResult =
        withContext(Dispatchers.IO) {
            val cleanUrl = url.trim()
            if (!cleanUrl.startsWith("https://")) {
                return@withContext BundleSyncResult.Error("آدرس سرور همگام‌سازی باید با https:// آغاز شود.")
            }
            try {
                val request = Request.Builder().url(cleanUrl).get().build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext BundleSyncResult.Error("سرور پاسخ خطای HTTP ${response.code} بازگرداند.")
                    }
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) {
                        return@withContext BundleSyncResult.Error("فایل محتوای دریافتی از سرور خالی است.")
                    }
                    importBundleJson(database, body)
                }
            } catch (e: Exception) {
                BundleSyncResult.Error("خطا در اتصال به سرور همگام‌سازی: ${e.localizedMessage ?: "دسترسی اینترنت بررسی شود"}")
            }
        }
}
