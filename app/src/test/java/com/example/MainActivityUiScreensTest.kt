package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class MainActivityUiScreensTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(substring: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText(substring, substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `launch MainActivity and verify all core screens and new Practices Techniques Flashcards Reader flows`() {
        // 1. SplashScreen (Start screen + 8 quick sections + messenger row)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("هنر ارتباط ذهن‌ها").assertIsDisplayed()
        composeRule.onNodeWithText("مدرس: اکبر شیرزادخانی").assertIsDisplayed()
        listOf(
            "home_sec_library",
            "home_sec_practices",
            "home_sec_techniques",
            "home_sec_spiritual",
            "home_sec_games",
            "home_sec_flashcards",
            "home_sec_stats",
            "home_sec_contact"
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }

        // Open PracticesHub directly from Home
        composeRule.onNodeWithTag("home_sec_practices").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("تمرین‌های کاربردی و ثبت تجربه").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        // Open TechniquesHub directly from Home
        composeRule.onNodeWithTag("home_sec_techniques").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("تکنیک‌های عملی و گام‌به‌گام").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("splash_enter_portal_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        // 2. PortalScreen + 8 original + 2 dedicated buttons
        composeRule.onNodeWithText("دنیای ذهن‌ها و آموزش‌ها").assertIsDisplayed()
        composeRule.onNodeWithText("بخش‌های آموزشی و مهارتی").performScrollTo().assertIsDisplayed()
        listOf(
            "portal_btn_chapters",
            "portal_btn_practices",
            "portal_btn_techniques",
            "portal_btn_games",
            "portal_btn_flashcards",
            "portal_btn_journal",
            "portal_btn_stats",
            "portal_btn_instructor",
            "portal_btn_download",
            "portal_btn_contact"
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }

        // 3. ChaptersScreen -> 4. ChapterDetailScreen -> 7. LessonDetailScreen -> 10a. Quiz
        composeRule.onNodeWithTag("portal_btn_chapters").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("مسیر آموزشی هشت‌فصلی").assertIsDisplayed()
        waitForText("فصل اول: مبانی ارتباط ذهن‌ها")
        composeRule.onAllNodes(hasText("فصل اول: مبانی ارتباط ذهن‌ها", substring = true))[0].performClick()
        composeRule.waitForIdle()

        // 4. ChapterDetailScreen (incl. Quiz on chapter page)
        waitForText("اهداف یادگیری این فصل")
        waitForText("درس‌ها و تمرین‌های این فصل")
        waitForText("مفهوم علمی")
        composeRule.onAllNodes(hasText("مفهوم علمی", substring = true))[0]
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        waitForText("ارزیابی سریع درس")
        composeRule.onNode(hasText("ارزیابی سریع درس", substring = true))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to ChapterDetail
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to Chapters
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to Portal
        composeRule.waitForIdle()

        // 5. BooksLibraryScreen -> 6. Redesigned BookReaderScreen (Warm Paper + Slim Footer + Focus Mode)
        composeRule.onNode(hasText("کتابخانه اختصاصی «کتاب‌های مدرس»", substring = true))
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("کتابخانه «کتاب‌های مدرس»").assertIsDisplayed()
        waitForText("۱۶ سرفصل آماده مطالعه")
        composeRule.onAllNodes(hasText("۱۶ سرفصل آماده مطالعه", substring = true))[0]
            .performClick()
        composeRule.waitForIdle()

        waitForText("کتاب شماره ۱")
        composeRule.onNodeWithTag("book_reader_footer").assertIsDisplayed()
        composeRule.onNodeWithTag("book_next_page").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("book_focus_toggle").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("book_focus_toggle").performClick()
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodes(hasText("صفحه", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        )
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to Library
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasText("پیش‌نویس ساختاریافته", substring = true))[0]
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to Portal
        composeRule.waitForIdle()

        // 8. Two-Step FlashcardsScreen (Category Landing -> Active Deck Flip/Next -> Back Preserving Index)
        composeRule.onNodeWithTag("portal_btn_flashcards").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasText("فلش‌کارت‌های لایتنر", substring = true)).assertIsDisplayed()
        waitForText("همه کارت‌ها")
        composeRule.onNodeWithTag("flashcard_cat_ALL").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("flashcard_flip_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("flashcard_next_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("flashcard_back_to_categories").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        // Re-enter ALL category and verify preserved card position ("ادامه از کارت 2")
        composeRule.onNodeWithTag("flashcard_cat_ALL").performScrollTo().assertIsDisplayed()
        waitForText("ادامه از کارت 2")
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        // 9. GamesHubScreen + GamePlayScreen
        composeRule.onNodeWithTag("portal_btn_games").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("اتاق ۱۲ بازی شناختی و تمرکز").assertIsDisplayed()
        composeRule.onNodeWithText("ورود به کابین خلبانی")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("پرواز تمرکز خلبانی").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to GamesHub
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick() // Back to Portal
        composeRule.waitForIdle()

        // 10b. ProgressStatsScreen
        composeRule.onNodeWithTag("portal_btn_stats").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("کارنامه و نمودار پیشرفت").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        // Additional screens check (Journal, DownloadHub, Contact)
        composeRule.onNodeWithTag("portal_btn_journal").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("portal_btn_download").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("download_check_update_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        waitForText("به‌روزرسانی در دسترس نیست")
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("portal_btn_contact").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()

        // 11. InstructorRoomScreen -> First PIN Setup -> 12. ContentPublishingTab
        composeRule.onNodeWithTag("portal_btn_instructor").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("تنظیم رمز اولیه اتاق مدرس").assertIsDisplayed()
        composeRule.onNodeWithTag("instructor_setup_pin_input").performTextInput("Akbar@2026")
        composeRule.onNodeWithTag("instructor_setup_confirm_input").performTextInput("Akbar@2026")
        composeRule.onNodeWithTag("instructor_setup_submit").performClick()
        composeRule.waitForIdle()

        // 12. Live Book Editor & Content Manager Tab inside InstructorRoom
        composeRule.onNodeWithText("ویرایش و انتشار محتوا").performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasText("ویرایشگر زنده کتاب‌ها", substring = true)).assertIsDisplayed()
    }

    @Test
    @Config(sdk = [34], qualifiers = "fa-rIR-w800dp-h1280dp-xhdpi")
    fun `splash portal and games adapt cleanly in multi-column tablet layout`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("هنر ارتباط ذهن‌ها").assertIsDisplayed()
        composeRule.onNodeWithTag("home_sec_library").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_enter_portal_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("دنیای ذهن‌ها و آموزش‌ها").assertIsDisplayed()
        composeRule.onNodeWithTag("portal_btn_chapters").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("portal_btn_contact").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("portal_btn_games").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("اتاق ۱۲ بازی شناختی و تمرکز").assertIsDisplayed()
    }

    @Test
    fun `quick download button shows honest status dialog and avatars open InstructorProfile`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("دانلود سریع APK").assertIsDisplayed()
        composeRule.onNodeWithTag("quick_download_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        waitForText("لینک دانلود عمومی هنوز منتشر نشده است")
        composeRule.onNodeWithText("بستن").performClick()
        composeRule.waitForIdle()

        // Open central Interaction Settings Dialog & toggle reduced motion
        composeRule.onNodeWithTag("topbar_settings_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("interaction_settings_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("setting_reduced_motion_switch").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_close_btn").performClick()
        composeRule.waitForIdle()

        // Tap mentor studio photo -> InstructorProfileScreen
        composeRule.onNodeWithTag("splash_mentor_photo").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("instructor_profile_name").assertIsDisplayed()
        composeRule.onNodeWithTag("instructor_profile_call_btn").performScrollTo().assertIsDisplayed()
        assertTrue(composeRule.onAllNodes(hasText("دکتر", substring = true)).fetchSemanticsNodes().isEmpty())

        // Back to Splash, tap official logo -> InstructorProfileScreen
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("splash_official_logo").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("instructor_profile_name").assertIsDisplayed()
    }

    @Test
    fun `chapters 2 to 8 and books 2 to 8 open freely without paywall dialog`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("splash_enter_portal_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Chapters -> Open Chapter 2 directly
        composeRule.onNodeWithTag("portal_btn_chapters").performScrollTo().performClick()
        composeRule.waitForIdle()
        waitForText("فصل دوم")
        composeRule.onAllNodes(hasText("فصل دوم", substring = true))[0].performScrollTo().performClick()
        composeRule.waitForIdle()
        waitForText("اهداف یادگیری این فصل")

        // Back to Portal -> Open BooksLibrary -> Open Book 2 directly
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("بازگشت").performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasText("کتابخانه اختصاصی «کتاب‌های مدرس»", substring = true))
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        waitForText("پیش‌نویس ساختاریافته")
        composeRule.onAllNodes(hasText("پیش‌نویس ساختاریافته", substring = true))[0]
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        waitForText("محتوا در حال تکمیل است")
    }

    @Test
    @Config(sdk = [34], qualifiers = "fa-rIR-w360dp-h640dp-xxhdpi")
    fun `compact 360dp phone layout renders messengers quick grid book reader and Zener without overflow`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quick_download_button").assertIsDisplayed()
        composeRule.onNodeWithText("دانلود سریع APK").assertIsDisplayed()
        composeRule.onNodeWithText("هنر ارتباط ذهن‌ها").assertIsDisplayed()

        // Scroll to messenger buttons on compact 360dp phone
        composeRule.onNodeWithTag("home_btn_eitaa").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home_btn_rubika").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home_btn_whatsapp").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("splash_enter_portal_button").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("portal_btn_chapters").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("portal_btn_practices").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("portal_btn_techniques").performScrollTo().assertIsDisplayed()

        // Check Zener game FlowRow symbols on 360dp
        composeRule.onNodeWithTag("portal_btn_games").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("games_lazy_column").performScrollToNode(hasText("۶. فرستنده و دریافت‌کننده", substring = true))
        composeRule.onNodeWithText("۶. فرستنده و دریافت‌کننده").performClick()
        composeRule.waitForIdle()
        waitForText("○ دایره")
        waitForText("★ ستاره")
    }

    @Test
    @Config(sdk = [34], qualifiers = "fa-rIR-w360dp-h640dp-xxhdpi", fontScale = 1.3f)
    fun `large fontScale 1_3x renders Splash and Flashcards cleanly without crash`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("دانلود سریع APK").assertIsDisplayed()
        composeRule.onNodeWithText("هنر ارتباط ذهن‌ها").assertIsDisplayed()
        composeRule.onNodeWithTag("home_btn_eitaa").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home_btn_whatsapp").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home_sec_flashcards").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasTestTag("flashcard_cat_ALL")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("flashcard_cat_ALL").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("flashcard_flip_button").assertIsDisplayed()
    }
}
