package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BookReaderScreen
import com.example.ui.screens.BooksLibraryScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.ChaptersScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.DownloadHubScreen
import com.example.ui.screens.FlashcardsScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.GamesHubScreen
import com.example.ui.screens.InstructorProfileScreen
import com.example.ui.screens.InstructorRoomScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.LessonDetailScreen
import com.example.ui.screens.PortalScreen
import com.example.ui.screens.PracticesScreen
import com.example.ui.screens.ProgressStatsScreen
import com.example.ui.screens.SpiritualSectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TechniquesScreen
import com.example.ui.theme.MindConnectionTheme
import com.example.ui.theme.ProvideInteractionController
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MindConnectionTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ProvideInteractionController {
                        val viewModel: MainViewModel = viewModel()
                        val currentScreen by viewModel.currentScreen.collectAsState()

                        BackHandler(enabled = currentScreen != ScreenDestination.Splash) {
                            viewModel.navigateBack()
                        }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (val screen = currentScreen) {
                                is ScreenDestination.Splash -> SplashScreen(viewModel)
                                is ScreenDestination.Portal -> PortalScreen(viewModel)
                                is ScreenDestination.InstructorProfile -> InstructorProfileScreen(viewModel)
                                is ScreenDestination.BooksLibrary -> BooksLibraryScreen(viewModel)
                                is ScreenDestination.BookReader -> BookReaderScreen(screen.bookId, screen.pageNumber, viewModel)
                                is ScreenDestination.Chapters -> ChaptersScreen(viewModel)
                                is ScreenDestination.ChapterDetail -> ChapterDetailScreen(screen.chapterId, viewModel)
                                is ScreenDestination.LessonDetail -> LessonDetailScreen(screen.lessonId, viewModel)
                                is ScreenDestination.SpiritualSection -> SpiritualSectionScreen(viewModel)
                                is ScreenDestination.PracticesHub -> PracticesScreen(viewModel)
                                is ScreenDestination.TechniquesHub -> TechniquesScreen(viewModel)
                                is ScreenDestination.ProgressStats -> ProgressStatsScreen(viewModel)
                                is ScreenDestination.GamesHub -> GamesHubScreen(viewModel)
                                is ScreenDestination.GamePlay -> GamePlayScreen(screen.gameId, viewModel)
                                is ScreenDestination.Flashcards -> FlashcardsScreen(viewModel)
                                is ScreenDestination.Journal -> JournalScreen(viewModel)
                                is ScreenDestination.InstructorRoom -> InstructorRoomScreen(viewModel)
                                is ScreenDestination.ContactInstructor -> ContactScreen(viewModel)
                                is ScreenDestination.DownloadHub -> DownloadHubScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
