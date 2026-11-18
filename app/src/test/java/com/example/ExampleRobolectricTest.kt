package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.InitialDataSeed
import com.example.data.model.BookBookmarkEntity
import com.example.data.model.BookPageNoteEntity
import com.example.data.model.PredictionEntryEntity
import com.example.data.model.ReadingProgressEntity
import com.example.data.sync.AppUpdateManager
import com.example.data.sync.BundleSyncResult
import com.example.data.sync.ContentBundleManager
import com.example.data.sync.UpdateCheckResult
import com.example.security.ActivationResult
import com.example.security.LicenseSecurityManager
import com.example.security.OtpIssueResult
import com.example.security.OtpVerifyResult
import com.example.security.PinVerifyStatus
import com.example.ui.components.SectionSquareThemes
import com.example.ui.theme.InteractionPreferencesStore
import com.example.ui.theme.InteractionSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var securityManager: LicenseSecurityManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("mind_sec_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        securityManager = LicenseSecurityManager(context, db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context matches independent app title`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("هنر ارتباط ذهن‌ها", appName)
    }

    @Test
    fun `seed content contains 16 Book 1 pages 14 Book 2 draft pages 25 flashcards 8 chapters and zero doctor title`() {
        val allPages = InitialDataSeed.getBookPages()
        val book1Pages = allPages.filter { it.bookId == 1 }
        val book2Pages = allPages.filter { it.bookId == 2 }
        val book5Pages = allPages.filter { it.bookId == 5 }

        assertEquals(16, book1Pages.size)
        assertEquals(14, book2Pages.size)
        assertEquals(4, book5Pages.size)
        assertTrue(book2Pages.all { it.isSourcePlaceholder && it.bodyText.contains("محتوا در حال تکمیل است") && it.contentOrigin == "SCIENTIFIC_SUPPLEMENT" })
        assertTrue(InitialDataSeed.getMentorBooks().all { it.subjectCategory in InitialDataSeed.COURSE_SUBJECT_CATEGORIES })
        assertEquals(25, InitialDataSeed.getFlashcards().size)
        assertEquals(8, InitialDataSeed.getChapters().size)
        assertEquals(12, InitialDataSeed.getLessons().size)
        assertTrue(InitialDataSeed.getInitialLicenses().isEmpty())

        val allText = buildString {
            InitialDataSeed.getMentorBooks().forEach { append(it.title).append(' ').append(it.subtitle).append(' ').append(it.description) }
            InitialDataSeed.getChapters().forEach { append(it.title).append(' ').append(it.subtitle).append(' ').append(it.introduction) }
            InitialDataSeed.getLessons().forEach { append(it.title).append(' ').append(it.content).append(' ').append(it.quizExplanation) }
            InitialDataSeed.getFlashcards().forEach { append(it.frontTitle).append(' ').append(it.frontContent).append(' ').append(it.backAnswer) }
            allPages.forEach { append(it.sectionTitle).append(' ').append(it.bodyText) }
        }
        assertFalse("نباید عنوان دکتر در محتوای آموزشی وجود داشته باشد", allText.contains("دکتر"))
        assertTrue(allText.contains("اکبر شیرزادخانی"))
    }

    @Test
    fun `verify eight distinct square button themes exist and differ`() {
        val themes = listOf(
            SectionSquareThemes.SkyBlue,
            SectionSquareThemes.Peach,
            SectionSquareThemes.LemonGold,
            SectionSquareThemes.MintSage,
            SectionSquareThemes.Turquoise,
            SectionSquareThemes.SoftLilac,
            SectionSquareThemes.IceCobalt,
            SectionSquareThemes.SoftRose
        )

        assertEquals(8, themes.map { it.borderColor }.toSet().size)
        assertEquals(8, themes.map { it.extrusionShadowColor }.toSet().size)
        assertEquals(8, themes.map { it.particleColor }.toSet().size)
    }

    @Test
    fun `instructor PIN uses salted SHA-256 without hardcoded defaults`() {
        assertFalse(securityManager.hasInstructorPinSet())
        assertFalse(securityManager.verifyInstructorPin("1388"))
        assertFalse(securityManager.verifyInstructorPin("96822"))
        assertFalse(securityManager.setupInitialInstructorPin("12345"))

        assertTrue(securityManager.setupInitialInstructorPin("Akbar@7410"))
        assertTrue(securityManager.hasInstructorPinSet())
        assertTrue(securityManager.verifyInstructorPin("Akbar@7410"))
        assertFalse(securityManager.verifyInstructorPin("1388"))

        val rawPrefs = context.getSharedPreferences("mind_sec_store", Context.MODE_PRIVATE).all
        assertFalse(rawPrefs.values.any { it.toString().contains("Akbar@7410") })

        assertFalse(securityManager.changeInstructorPin("wrongPin", "NewPin#99"))
        assertTrue(securityManager.changeInstructorPin("Akbar@7410", "NewPin#99"))
        assertFalse(securityManager.verifyInstructorPin("Akbar@7410"))
        assertTrue(securityManager.verifyInstructorPin("NewPin#99"))
    }

    @Test
    fun `signed license activation unlocks course rejects tampered or revoked or expired keys`() = runBlocking {
        AppDatabase.ensureSeeded(db)
        assertFalse(db.userProfileDao().getProfileDirect()!!.isCourseUnlocked)

        val key = securityManager.generateNewLicenseKey(
            studentName = "علی رضایی",
            studentContact = "09120000000",
            validityDays = 30,
            maxDevices = 1
        )
        assertTrue(securityManager.isValidSignedKeyStructure(key))

        val tampered = key.dropLast(1) + if (key.last() == 'A') 'B' else 'A'
        assertFalse(securityManager.isValidSignedKeyStructure(tampered))
        assertTrue(securityManager.activateKey(tampered) is ActivationResult.Error)

        val ok = securityManager.activateKey(key)
        assertTrue(ok is ActivationResult.Success)
        val profile = db.userProfileDao().getProfileDirect()!!
        assertTrue(profile.isCourseUnlocked)
        assertEquals(key, profile.activeLicenseKey)
        assertTrue(db.chapterDao().getAllChaptersDirect().all { it.isUnlocked })
        assertTrue(db.mentorBookDao().getAllBooksDirect().all { it.isUnlocked })

        context.getSharedPreferences("mind_sec_store", Context.MODE_PRIVATE)
            .edit()
            .putString("device_fingerprint_id", "DEV-OTHERDEVICE")
            .commit()
        val overCapacity = securityManager.activateKey(key)
        assertTrue(overCapacity is ActivationResult.Error)

        securityManager.revokeLicense(key)
        assertTrue(securityManager.activateKey(key) is ActivationResult.Error)

        val expiredKey = securityManager.generateNewLicenseKey(validityDays = 7, maxDevices = 2)
        val stored = db.licenseDao().getLicenseByKey(expiredKey)!!
        db.licenseDao().updateLicense(stored.copy(expiresAt = System.currentTimeMillis() - 10_000L))
        assertTrue(securityManager.activateKey(expiredKey) is ActivationResult.Error)
        assertEquals("EXPIRED", db.licenseDao().getLicenseByKey(expiredKey)?.status)
    }

    @Test
    fun `database seeding bookmarks notes and 7-day predictions persist properly`() = runBlocking {
        AppDatabase.ensureSeeded(db)
        assertEquals(8, db.chapterDao().getAllChaptersDirect().size)
        assertEquals(16, db.bookPageDao().getAllPagesDirect().count { it.bookId == 1 })
        assertEquals(14, db.bookPageDao().getAllPagesDirect().count { it.bookId == 2 })
        assertEquals(25, db.flashcardDao().getAllFlashcardsDirect().size)
        assertEquals(0, db.readingProgressDao().getProgressDirect()!!.completedExercisesCount)

        db.bookBookmarkDao().insertBookmark(
            BookBookmarkEntity(bookId = 1, pageNumber = 4, sectionTitle = "نگاه علمی به شواهد")
        )
        assertNotNull(db.bookBookmarkDao().getBookmark(1, 4))

        db.bookPageNoteDao().upsertNote(
            BookPageNoteEntity(bookId = 1, pageNumber = 4, noteText = "یادداشت تمرینی فصل اول")
        )
        assertEquals("یادداشت تمرینی فصل اول", db.bookPageNoteDao().getNoteForPage(1, 4)?.noteText)

        db.predictionEntryDao().upsertEntry(
            PredictionEntryEntity(
                dayNumber = 1,
                dateTimeText = "۱۴۰۵/۰۲/۲۰ - ۰۹:۰۰",
                predictionText = "دریافت تماس غیرمنتظره قبل از ظهر",
                isPredictionLocked = true,
                outcomeStatus = "CORRECT",
                confidencePercent = 80,
                lockedAt = System.currentTimeMillis()
            )
        )
        val predictions = db.predictionEntryDao().getAllEntries().first()
        assertEquals(1, predictions.size)
        assertTrue(predictions.first().isPredictionLocked)
        assertEquals("CORRECT", predictions.first().outcomeStatus)
    }

    @Test
    fun `content bundle import preserves bookmarks notes reading progress lesson completions and Leitner state`() = runBlocking {
        AppDatabase.ensureSeeded(db)

        // Read real canonical JSON bundle from app assets
        val bundledJson = context.assets.open("mind_content_bundle.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        assertTrue(bundledJson.contains("اکبر شیرزادخانی"))
        assertFalse(bundledJson.contains("دکتر"))

        // Simulate student activity before bundle update
        db.bookBookmarkDao().insertBookmark(BookBookmarkEntity(bookId = 1, pageNumber = 6, sectionTitle = "تفاوت حدس و شهود"))
        db.bookPageNoteDao().upsertNote(BookPageNoteEntity(bookId = 1, pageNumber = 6, noteText = "نکته مهم تمرینی"))
        db.readingProgressDao().saveProgress(ReadingProgressEntity(id = 1, lastReadBookId = 1, lastReadChapterNumber = 2, lastReadPageNumber = 6))
        val lesson101 = db.lessonDao().getLessonById(101)!!
        db.lessonDao().updateLesson(lesson101.copy(isCompleted = true))
        val card1 = db.flashcardDao().getFlashcardById(1)!!
        db.flashcardDao().updateFlashcard(card1.copy(level = 4, isMastered = true))

        // Import bundled JSON + verify round-trip export
        val res = ContentBundleManager.importBundleJson(db, bundledJson)
        assertTrue(res is BundleSyncResult.Success)
        val roundTrip = ContentBundleManager.exportBundleJson(db)
        assertTrue(ContentBundleManager.importBundleJson(db, roundTrip) is BundleSyncResult.Success)

        // Verify zero loss of student data
        assertNotNull(db.bookBookmarkDao().getBookmark(1, 6))
        assertEquals("نکته مهم تمرینی", db.bookPageNoteDao().getNoteForPage(1, 6)?.noteText)
        assertEquals(6, db.readingProgressDao().getProgressDirect()?.lastReadPageNumber)
        assertTrue(db.lessonDao().getLessonById(101)?.isCompleted == true)
        val preservedCard = db.flashcardDao().getFlashcardById(1)!!
        assertEquals(4, preservedCard.level)
        assertTrue(preservedCard.isMastered)
        assertEquals(8, db.chapterDao().getAllChaptersDirect().size)
        assertEquals(25, db.flashcardDao().getAllFlashcardsDirect().size)
        assertNotEquals(true, db.mentorBookDao().getBookById(1)?.isContentComplete)
    }

    @Test
    fun `instructor PIN brute-force protection locks out after 5 failed attempts`() {
        assertTrue(securityManager.setupInitialInstructorPin("Akbar@2026"))

        for (expectedRemaining in listOf(4, 3, 2, 1)) {
            val status = securityManager.verifyInstructorPinDetailed("WrongPass")
            assertTrue(status is PinVerifyStatus.Invalid)
            assertEquals(expectedRemaining, (status as PinVerifyStatus.Invalid).attemptsRemaining)
        }

        val locked = securityManager.verifyInstructorPinDetailed("WrongPass")
        assertTrue(locked is PinVerifyStatus.LockedOut)
        assertTrue((locked as PinVerifyStatus.LockedOut).remainingSeconds > 0L)

        // Even the real PIN is blocked while lockout is active
        val blockedEvenCorrect = securityManager.verifyInstructorPinDetailed("Akbar@2026")
        assertTrue(blockedEvenCorrect is PinVerifyStatus.LockedOut)
    }

    @Test
    fun `local CSPRNG confirmation OTP is single-use expiring and rate-limited`() {
        val issued1 = securityManager.issueLocalConfirmationOtp()
        assertTrue(issued1 is OtpIssueResult.Issued)
        val token1 = (issued1 as OtpIssueResult.Issued).code
        assertEquals(6, token1.length)
        assertTrue(token1.all { it.isDigit() })

        // Verify once -> succeeds and immediately invalidates token
        val firstUse = securityManager.verifyAndConsumeConfirmationOtp(token1)
        assertEquals(OtpVerifyResult.VerifiedAndConsumed, firstUse)

        // Replay must fail immediately
        val replay = securityManager.verifyAndConsumeConfirmationOtp(token1)
        assertEquals(OtpVerifyResult.NotIssuedOrUsed, replay)

        // Expiration test
        val issued2 = securityManager.issueLocalConfirmationOtp() as OtpIssueResult.Issued
        val expiredResult = securityManager.verifyAndConsumeConfirmationOtp(
            inputCode = issued2.code,
            nowMs = System.currentTimeMillis() + 180_000L
        )
        assertEquals(OtpVerifyResult.Expired, expiredResult)

        // Brute-force lockout on wrong guesses
        securityManager.issueLocalConfirmationOtp()
        repeat(4) {
            val invalid = securityManager.verifyAndConsumeConfirmationOtp("000000")
            assertTrue(invalid is OtpVerifyResult.Invalid)
        }
        val locked = securityManager.verifyAndConsumeConfirmationOtp("000000")
        assertTrue(locked is OtpVerifyResult.LockedOut)
    }

    @Test
    fun `AppUpdateManager validates HTTPS URLs rejects AI Studio URLs and compares versionCode accurately`() = runBlocking {
        assertEquals("com.aistudio.mindconnection.akbar", BuildConfig.APPLICATION_ID)
        assertEquals(5, BuildConfig.VERSION_CODE)
        assertEquals("1.4", BuildConfig.VERSION_NAME)

        assertFalse(AppUpdateManager.isValidPublicHttpsUrl(""))
        assertFalse(AppUpdateManager.isValidPublicHttpsUrl("http://mind.ir/app.apk"))
        assertFalse(AppUpdateManager.isValidPublicHttpsUrl("https://example.com/app.apk"))
        assertFalse(AppUpdateManager.isValidPublicHttpsUrl("https://localhost/app.apk"))
        assertFalse(AppUpdateManager.isValidPublicHttpsUrl("https://ais-dev-demo.us-east1.run.app/app.apk"))
        assertFalse(AppUpdateManager.isValidPublicHttpsUrl("https://ais-pre-demo.us-east1.run.app/app.apk"))
        assertTrue(AppUpdateManager.isValidPublicHttpsUrl("https://github.com/ak3721sh-rgb/-/releases/download/v1.4/MindConnection-Akbar-v1.4.apk"))

        val updateJson = """
            {
              "versionCode": 6,
              "versionName": "1.5",
              "releaseDate": "2026-12-01",
              "changelog": "محتوای تکمیلی و بازی‌های جدید",
              "apkUrl": "https://mindconnection.ir/releases/mind-v1.5.apk",
              "sha256": "abc123"
            }
        """.trimIndent()
        val available = AppUpdateManager.parseManifestJson(updateJson, installedVersionCode = 5, installedVersionName = "1.4")
        assertTrue(available is UpdateCheckResult.UpdateAvailable)
        assertEquals(6, (available as UpdateCheckResult.UpdateAvailable).manifest.versionCode)
        assertEquals("1.5", available.manifest.versionName)
        assertEquals("2026-12-01", available.manifest.releaseDate)

        val sameJson = """
            {
              "versionCode": 5,
              "versionName": "1.4",
              "changelog": "نسخه فعلی",
              "apkUrl": "https://mindconnection.ir/releases/mind-v1.4.apk"
            }
        """.trimIndent()
        val upToDate = AppUpdateManager.parseManifestJson(sameJson, installedVersionCode = 5, installedVersionName = "1.4")
        assertTrue(upToDate is UpdateCheckResult.UpToDate)

        val badJson = "{not-valid"
        val invalid = AppUpdateManager.parseManifestJson(badJson, installedVersionCode = 5, installedVersionName = "1.4")
        assertTrue(invalid is UpdateCheckResult.Unavailable)

        val offlineResult = AppUpdateManager.checkForUpdate(isNetworkConnected = false, configuredApkOrManifestUrl = "")
        assertTrue(offlineResult is UpdateCheckResult.Unavailable)
        assertTrue((offlineResult as UpdateCheckResult.Unavailable).reason.contains("منتشر نشده است"))

        val repoPair = AppUpdateManager.extractGitHubRepo("https://github.com/ak3721sh-rgb/-/releases/download/v1.4/MindConnection-Akbar-v1.4.apk")
        assertEquals("ak3721sh-rgb" to "-", repoPair)
        assertEquals(
            "https://github.com/ak3721sh-rgb/-/releases",
            AppUpdateManager.githubReleasesPageUrl("https://github.com/ak3721sh-rgb/-/releases/download/v1.4/MindConnection-Akbar-v1.4.apk")
        )
        assertTrue(AppUpdateManager.isReleaseTagNewer("v1.5", "1.4"))
        assertTrue(AppUpdateManager.isReleaseTagNewer("1.4.1", "1.4"))
        assertFalse(AppUpdateManager.isReleaseTagNewer("v1.4", "1.4"))
        assertFalse(AppUpdateManager.isReleaseTagNewer("v1.3", "1.4"))
    }

    @Test
    fun `InteractionPreferencesStore persists sound haptics reduced motion and reader font size`() {
        val updated = InteractionSettings(
            soundEnabled = false,
            hapticsEnabled = false,
            reducedMotionEnabled = true,
            readerFontSizeSp = 21f
        )
        InteractionPreferencesStore.save(context, updated)
        val reloaded = InteractionPreferencesStore.load(context)
        assertEquals(false, reloaded.soundEnabled)
        assertEquals(false, reloaded.hapticsEnabled)
        assertEquals(true, reloaded.reducedMotionEnabled)
        assertEquals(21f, reloaded.readerFontSizeSp, 0.01f)
    }

    @Test
    fun `MIGRATION_3_4 adds new columns and diagnostic_logs table without dropping existing rows`() {
        val dbFile = context.getDatabasePath("test_mig_v3_to_v4.db")
        dbFile.delete()
        val rawDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        rawDb.execSQL("CREATE TABLE mentor_books (id INTEGER PRIMARY KEY NOT NULL, title TEXT NOT NULL)")
        rawDb.execSQL("INSERT INTO mentor_books (id, title) VALUES (1, 'کتاب آزمایشی')")
        rawDb.execSQL("CREATE TABLE book_pages (id INTEGER PRIMARY KEY NOT NULL, bookId INTEGER NOT NULL, bodyText TEXT NOT NULL)")
        rawDb.execSQL("INSERT INTO book_pages (id, bookId, bodyText) VALUES (10, 1, 'متن یادداشت دانشجو')")

        val supportDb = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbFile.absolutePath)
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {}
                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldV: Int, newV: Int) {}
                    })
                    .build()
            )
            .writableDatabase
        rawDb.close()

        AppDatabase.MIGRATION_3_4.migrate(supportDb)

        supportDb.query("SELECT subjectCategory, title FROM mentor_books WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("ارتباط ذهن و ذهن", c.getString(0))
            assertEquals("کتاب آزمایشی", c.getString(1))
        }
        supportDb.query("SELECT contentOrigin, bodyText FROM book_pages WHERE id = 10").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("SCIENTIFIC_SUPPLEMENT", c.getString(0))
            assertEquals("متن یادداشت دانشجو", c.getString(1))
        }
        supportDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='diagnostic_logs'").use { c ->
            assertTrue(c.moveToFirst())
        }
        supportDb.close()
        dbFile.delete()
    }

    @Test
    fun `flashcard session preferences persist category index and shuffle mode across restarts`() {
        val prefs = context.getSharedPreferences("mind_flashcard_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val vm1 = com.example.ui.viewmodel.MainViewModel(ApplicationProvider.getApplicationContext())
        vm1.saveFlashcardIndex("TECHNIQUE", 4)
        vm1.setFlashcardShuffleMode(true)
        vm1.markFlashcardSeen(7)

        val vm2 = com.example.ui.viewmodel.MainViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(4, vm2.getFlashcardIndex("TECHNIQUE"))
        assertTrue(vm2.isFlashcardShuffleMode())
        assertTrue(7 in vm2.getSeenFlashcardIds())
    }
}
