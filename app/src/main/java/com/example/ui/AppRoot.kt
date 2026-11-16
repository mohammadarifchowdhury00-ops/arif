package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audio.JapaneseSpeechManager
import com.example.data.preferences.AppPreferences
import com.example.data.repository.JournalRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ExamAcademyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IrodoriHubScreen
import com.example.ui.screens.IrodoriLessonScreen
import com.example.ui.screens.KanaAcademyScreen
import com.example.ui.screens.KanjiAcademyScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.theme.ArifJournalTheme
import com.example.ui.viewmodel.JournalViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_IRODORI = "irodori"
const val ROUTE_IRODORI_LESSON = "irodori_lesson"
const val ROUTE_KANA = "kana"
const val ROUTE_KANJI = "kanji"
const val ROUTE_EXAMS = "exams"
const val ROUTE_LEADERBOARD = "leaderboard"
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_NOTIFICATIONS = "notifications"
const val ROUTE_ADMIN = "admin"

private data class TopNavItem(
    val route: String,
    val labelBn: String,
    val icon: ImageVector
)

private val topNavItems = listOf(
    TopNavItem(ROUTE_HOME, "হোম", Icons.Rounded.Home),
    TopNavItem(ROUTE_IRODORI, "ইরোদোরি", Icons.Rounded.AutoStories),
    TopNavItem(ROUTE_KANA, "কানা/কাঞ্জি", Icons.Rounded.Translate),
    TopNavItem(ROUTE_EXAMS, "পরীক্ষা", Icons.Rounded.FactCheck),
    TopNavItem(ROUTE_LEADERBOARD, "র‍্যাঙ্ক", Icons.Rounded.EmojiEvents)
)

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val preferences = remember(context) { AppPreferences(context.applicationContext) }
    val isDarkMode by preferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val showRomaji by preferences.showRomaji.collectAsStateWithLifecycle(initialValue = true)
    val showFurigana by preferences.showFurigana.collectAsStateWithLifecycle(initialValue = true)

    val authUser by remember { Firebase.auth.authStateFlow() }.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    ArifJournalTheme(darkTheme = isDarkMode) {
        val currentUser = authUser
        if (currentUser == null) {
            AuthScreen()
        } else {
            key(currentUser.uid) {
                MainJournalShell(
                    userId = currentUser.uid,
                    userEmail = currentUser.email.orEmpty(),
                    userDisplayName = currentUser.displayName.orEmpty().ifBlank { "শিক্ষার্থী" },
                    userPhotoUrl = currentUser.photoUrl?.toString(),
                    preferences = preferences,
                    isDarkMode = isDarkMode,
                    showRomaji = showRomaji,
                    showFurigana = showFurigana
                )
            }
        }
    }
}

@Composable
private fun MainJournalShell(
    userId: String,
    userEmail: String,
    userDisplayName: String,
    userPhotoUrl: String?,
    preferences: AppPreferences,
    isDarkMode: Boolean,
    showRomaji: Boolean,
    showFurigana: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val speechManager = remember(context) { JapaneseSpeechManager(context.applicationContext) }
    DisposableEffect(speechManager) {
        onDispose { speechManager.release() }
    }

    val repository = remember(context) { JournalRepository(context.applicationContext) }
    val viewModel: JournalViewModel = viewModel(
        key = userId,
        factory = viewModelFactory {
            initializer {
                JournalViewModel(
                    repository = repository,
                    currentUserId = userId,
                    userEmail = userEmail,
                    userDisplayName = userDisplayName,
                    userPhotoUrl = userPhotoUrl
                )
            }
        }
    )

    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val lessonProgressState by viewModel.lessonProgress.collectAsStateWithLifecycle()
    val scriptMasteryState by viewModel.scriptMastery.collectAsStateWithLifecycle()
    val examAttemptsState by viewModel.examAttempts.collectAsStateWithLifecycle()
    val xpTxState by viewModel.xpTransactions.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notifications.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboard.collectAsStateWithLifecycle()
    val announcementsState by viewModel.announcements.collectAsStateWithLifecycle()
    val videosState by viewModel.learningVideos.collectAsStateWithLifecycle()
    val customLessonsState by viewModel.customLessons.collectAsStateWithLifecycle()
    val customQuestionsState by viewModel.customQuestions.collectAsStateWithLifecycle()
    val adminStudents by viewModel.adminStudents.collectAsStateWithLifecycle()
    val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val profile = (profileState as? UiState.Success)?.data
    val lessonProgress = (lessonProgressState as? UiState.Success)?.data.orEmpty()
    val scriptMastery = (scriptMasteryState as? UiState.Success)?.data.orEmpty()
    val examAttempts = (examAttemptsState as? UiState.Success)?.data.orEmpty()
    val xpTransactions = (xpTxState as? UiState.Success)?.data.orEmpty()
    val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
    val leaderboard = (leaderboardState as? UiState.Success)?.data.orEmpty()
    val announcements = (announcementsState as? UiState.Success)?.data.orEmpty()
    val videos = (videosState as? UiState.Success)?.data.orEmpty()
    val customLessons = (customLessonsState as? UiState.Success)?.data.orEmpty()
    val customQuestions = (customQuestionsState as? UiState.Success)?.data.orEmpty()

    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route.orEmpty()

    var activeIrodoriBookId by remember { mutableStateOf("starter") }
    var activeKanaOrKanjiTab by remember { mutableIntStateOf(0) } // 0=Hiragana, 1=Katakana, 2=Kanji
    var activeExamTab by remember { mutableStateOf("jft") }

    fun navigateTop(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(windowInsets = WindowInsets.navigationBars) {
                        topNavItems.forEach { item ->
                            val selected = currentRoute.startsWith(item.route) ||
                                (item.route == ROUTE_KANA && currentRoute == ROUTE_KANJI)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTop(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.labelBn) },
                                label = { Text(item.labelBn) },
                                modifier = Modifier.testTag("nav_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isWideScreen) {
                    NavigationRail {
                        topNavItems.forEach { item ->
                            val selected = currentRoute.startsWith(item.route) ||
                                (item.route == ROUTE_KANA && currentRoute == ROUTE_KANJI)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTop(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.labelBn) },
                                label = { Text(item.labelBn) },
                                modifier = Modifier.testTag("rail_${item.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(navController = navController, startDestination = ROUTE_HOME) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                profile = profile,
                                lessonProgress = lessonProgress,
                                leaderboard = leaderboard,
                                notifications = notifications,
                                announcements = announcements,
                                videos = videos,
                                isAdmin = viewModel.isAdmin,
                                isDarkMode = isDarkMode,
                                showRomaji = showRomaji,
                                showFurigana = showFurigana,
                                onToggleDarkMode = { enabled -> scope.launch { preferences.setDarkMode(enabled) } },
                                onToggleRomaji = { enabled -> scope.launch { preferences.setShowRomaji(enabled) } },
                                onToggleFurigana = { enabled -> scope.launch { preferences.setShowFurigana(enabled) } },
                                onOpenNotifications = { navController.navigate(ROUTE_NOTIFICATIONS) },
                                onOpenProfileDashboard = { navController.navigate(ROUTE_DASHBOARD) },
                                onOpenLeaderboard = { navigateTop(ROUTE_LEADERBOARD) },
                                onOpenAdminPanel = { if (viewModel.isAdmin) navController.navigate(ROUTE_ADMIN) },
                                onOpenIrodoriBook = { bookId ->
                                    activeIrodoriBookId = bookId
                                    navigateTop(ROUTE_IRODORI)
                                },
                                onContinueLesson = { bookId, lessonNumber ->
                                    activeIrodoriBookId = bookId
                                    navController.navigate("$ROUTE_IRODORI_LESSON/$bookId/$lessonNumber")
                                },
                                onOpenHiragana = {
                                    activeKanaOrKanjiTab = 0
                                    navigateTop(ROUTE_KANA)
                                },
                                onOpenKatakana = {
                                    activeKanaOrKanjiTab = 1
                                    navigateTop(ROUTE_KANA)
                                },
                                onOpenKanji = {
                                    activeKanaOrKanjiTab = 2
                                    navController.navigate(ROUTE_KANJI)
                                },
                                onOpenJlptN5 = {
                                    activeExamTab = "n5"
                                    navigateTop(ROUTE_EXAMS)
                                },
                                onOpenJlptN4 = {
                                    activeExamTab = "n4"
                                    navigateTop(ROUTE_EXAMS)
                                },
                                onOpenJftBasic = {
                                    activeExamTab = "jft"
                                    navigateTop(ROUTE_EXAMS)
                                },
                                onSignOut = {
                                    signOutUser(credentialManager, scope)
                                }
                            )
                        }

                        composable(ROUTE_IRODORI) {
                            IrodoriHubScreen(
                                selectedBookId = activeIrodoriBookId,
                                onSelectBook = { activeIrodoriBookId = it },
                                lessonProgress = lessonProgress,
                                customLessons = customLessons,
                                onOpenLesson = { bId, lNum ->
                                    navController.navigate("$ROUTE_IRODORI_LESSON/$bId/$lNum")
                                }
                            )
                        }

                        composable(
                            route = "$ROUTE_IRODORI_LESSON/{bookId}/{lessonNum}",
                            arguments = listOf(
                                navArgument("bookId") { type = NavType.StringType },
                                navArgument("lessonNum") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val bId = backStackEntry.arguments?.getString("bookId") ?: "starter"
                            val lNum = backStackEntry.arguments?.getInt("lessonNum") ?: 1
                            val rec = lessonProgress.find { it.bookId == bId && it.lessonNumber.toInt() == lNum }
                            val vidForLesson = videos.filter { it.bookId == bId && it.lessonNumber.toInt() == lNum }
                            IrodoriLessonScreen(
                                bookId = bId,
                                lessonNumber = lNum,
                                progressRecord = rec,
                                lessonVideos = vidForLesson,
                                showFurigana = showFurigana,
                                showRomaji = showRomaji,
                                speechManager = speechManager,
                                viewModel = viewModel,
                                onNavigateLesson = { nextB, nextL ->
                                    navController.navigate("$ROUTE_IRODORI_LESSON/$nextB/$nextL") {
                                        popUpTo("$ROUTE_IRODORI_LESSON/{bookId}/{lessonNum}") { inclusive = true }
                                    }
                                },
                                onBackToBook = { navController.popBackStack() }
                            )
                        }

                        composable(ROUTE_KANA) {
                            if (activeKanaOrKanjiTab == 2) {
                                KanjiAcademyScreen(
                                    scriptMastery = scriptMastery,
                                    speechManager = speechManager,
                                    viewModel = viewModel,
                                    onSwitchToKana = { script ->
                                        activeKanaOrKanjiTab = if (script == "katakana") 1 else 0
                                    }
                                )
                            } else {
                                val script = if (activeKanaOrKanjiTab == 1) "katakana" else "hiragana"
                                KanaAcademyScreen(
                                    initialScript = script,
                                    scriptMastery = scriptMastery,
                                    speechManager = speechManager,
                                    viewModel = viewModel,
                                    onSwitchToKanji = { activeKanaOrKanjiTab = 2 }
                                )
                            }
                        }

                        composable(ROUTE_KANJI) {
                            KanjiAcademyScreen(
                                scriptMastery = scriptMastery,
                                speechManager = speechManager,
                                viewModel = viewModel,
                                onSwitchToKana = { script ->
                                    activeKanaOrKanjiTab = if (script == "katakana") 1 else 0
                                    navigateTop(ROUTE_KANA)
                                }
                            )
                        }

                        composable(ROUTE_EXAMS) {
                            ExamAcademyScreen(
                                initialTab = activeExamTab,
                                examAttempts = examAttempts,
                                speechManager = speechManager,
                                viewModel = viewModel
                            )
                        }

                        composable(ROUTE_LEADERBOARD) {
                            LeaderboardScreen(
                                leaderboard = leaderboard,
                                currentUserId = userId
                            )
                        }

                        composable(ROUTE_DASHBOARD) {
                            StudentDashboardScreen(
                                profile = profile,
                                lessonProgress = lessonProgress,
                                scriptMastery = scriptMastery,
                                examAttempts = examAttempts,
                                xpTransactions = xpTransactions,
                                onUpdateDailyGoal = { viewModel.updateDailyGoal(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ROUTE_NOTIFICATIONS) {
                            NotificationsScreen(
                                notifications = notifications,
                                announcements = announcements,
                                onMarkRead = { viewModel.markNotificationRead(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(ROUTE_ADMIN) {
                            AdminDashboardScreen(
                                isAdmin = viewModel.isAdmin,
                                students = adminStudents,
                                announcements = announcements,
                                videos = videos,
                                customLessons = customLessons,
                                customQuestions = customQuestions,
                                adminLogs = adminLogs,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
