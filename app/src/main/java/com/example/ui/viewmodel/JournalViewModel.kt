package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.IrodoriRepository
import com.example.data.curriculum.PracticeQuestion
import com.example.data.model.AdminActivityLogRecord
import com.example.data.model.AnnouncementRecord
import com.example.data.model.CustomLessonRecord
import com.example.data.model.CustomQuestionRecord
import com.example.data.model.ExamAttemptRecord
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LearningVideoRecord
import com.example.data.model.LessonProgressRecord
import com.example.data.model.NotificationRecord
import com.example.data.model.ScriptMasteryRecord
import com.example.data.model.UserProfile
import com.example.data.model.XpTransactionRecord
import com.example.data.repository.JournalRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class JournalViewModel(
    private val repository: JournalRepository,
    val currentUserId: String,
    val userEmail: String,
    val userDisplayName: String,
    val userPhotoUrl: String?
) : ViewModel() {

    val isAdmin: Boolean = JournalRepository.isAdminEmail(userEmail)

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _adminStudents = MutableStateFlow<List<UserProfile>>(emptyList())
    val adminStudents: StateFlow<List<UserProfile>> = _adminStudents.asStateFlow()

    private val _adminLogs = MutableStateFlow<List<AdminActivityLogRecord>>(emptyList())
    val adminLogs: StateFlow<List<AdminActivityLogRecord>> = _adminLogs.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.ensureUserProfile(
                    userId = currentUserId,
                    displayName = userDisplayName,
                    email = userEmail,
                    photoUrl = userPhotoUrl
                )
            } catch (e: Exception) {
                Log.w(TAG, "Profile bootstrap failed: ${e.message}")
            }
        }
    }

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "তথ্য লোড করতে সমস্যা হয়েছে ($label)"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val userProfile: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId).toUiState("প্রোফাইল")

    val lessonProgress: StateFlow<UiState<List<LessonProgressRecord>>> =
        repository.observeLessonProgress(currentUserId).toUiState("পাঠ অগ্রগতি")

    val scriptMastery: StateFlow<UiState<List<ScriptMasteryRecord>>> =
        repository.observeScriptMastery(currentUserId).toUiState("অক্ষর দক্ষতা")

    val examAttempts: StateFlow<UiState<List<ExamAttemptRecord>>> =
        repository.observeExamAttempts(currentUserId).toUiState("পরীক্ষার ফলাফল")

    val xpTransactions: StateFlow<UiState<List<XpTransactionRecord>>> =
        repository.observeXpTransactions(currentUserId).toUiState("XP ইতিহাস")

    val notifications: StateFlow<UiState<List<NotificationRecord>>> =
        repository.observeNotifications(currentUserId).toUiState("বিজ্ঞপ্তি")

    val leaderboard: StateFlow<UiState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard(currentUserId).toUiState("লিডারবোর্ড")

    val announcements: StateFlow<UiState<List<AnnouncementRecord>>> =
        repository.observePublishedAnnouncements(currentUserId, isAdmin).toUiState("ঘোষণা")

    val learningVideos: StateFlow<UiState<List<LearningVideoRecord>>> =
        repository.observePublishedVideos(currentUserId, isAdmin).toUiState("ভিডিও")

    val customLessons: StateFlow<UiState<List<CustomLessonRecord>>> =
        repository.observePublishedCustomLessons(currentUserId, isAdmin).toUiState("কাস্টম পাঠ")

    val customQuestions: StateFlow<UiState<List<CustomQuestionRecord>>> =
        repository.observePublishedCustomQuestions(currentUserId, isAdmin).toUiState("কাস্টম প্রশ্ন")

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun showMessage(msg: String) {
        _actionMessage.value = msg
    }

    fun awardActivityXp(
        activityKey: String,
        sourceType: String,
        xpAmount: Long,
        description: String,
        studyMinutesDelta: Long = 2L,
        badgeUnlock: String? = null
    ) {
        viewModelScope.launch {
            try {
                val awarded = repository.awardXpOnce(
                    activityKey = activityKey,
                    sourceType = sourceType,
                    xpAmount = xpAmount,
                    description = description,
                    studyMinutesDelta = studyMinutesDelta,
                    badgeUnlock = badgeUnlock
                )
                if (awarded) {
                    _actionMessage.value = "+$xpAmount XP অর্জিত! ($description)"
                }
            } catch (e: Exception) {
                Log.w(TAG, "awardXp error: ${e.message}")
            }
        }
    }

    fun saveLessonSection(
        bookId: String,
        lessonNumber: Int,
        sectionKey: String,
        vocabMastered: Int = 0,
        grammarScore: Int = 0,
        gameHighScore: Int = 0,
        challengePassed: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                repository.saveLessonSectionProgress(
                    bookId = bookId,
                    lessonNumber = lessonNumber,
                    sectionKey = sectionKey,
                    vocabMastered = vocabMastered,
                    grammarScore = grammarScore,
                    gameHighScore = gameHighScore,
                    challengePassed = challengePassed
                )
                repository.awardXpOnce(
                    activityKey = "sec_${bookId}_${lessonNumber}_${sectionKey}",
                    sourceType = "section",
                    xpAmount = 15L,
                    description = "পাঠ $lessonNumber: $sectionKey সম্পন্ন",
                    studyMinutesDelta = 2L
                )
            } catch (e: Exception) {
                Log.w(TAG, "saveLessonSection error: ${e.message}")
            }
        }
    }

    fun recordScriptPractice(scriptType: String, character: String, correct: Boolean) {
        viewModelScope.launch {
            try {
                repository.recordScriptAttempt(scriptType, character, correct)
                if (correct) {
                    repository.awardXpOnce(
                        activityKey = "kana_${scriptType}_${character.hashCode()}",
                        sourceType = "script",
                        xpAmount = 10L,
                        description = "$scriptType অক্ষর আয়ত্ত: $character",
                        studyMinutesDelta = 1L,
                        badgeUnlock = "${scriptType}_starter"
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "recordScriptPractice error: ${e.message}")
            }
        }
    }

    fun submitExamResult(
        examType: String,
        title: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        categoryBreakdown: Map<String, Long>,
        passed: Boolean
    ) {
        viewModelScope.launch {
            try {
                repository.saveExamAttempt(
                    examType = examType,
                    title = title,
                    score = score,
                    totalQuestions = totalQuestions,
                    durationSeconds = durationSeconds,
                    categoryBreakdown = categoryBreakdown,
                    passed = passed
                )
                _actionMessage.value = "পরীক্ষা জমা হয়েছে! স্কোর: $score / $totalQuestions"
            } catch (e: Exception) {
                _actionMessage.value = "ফলাফল সংরক্ষণে সমস্যা হয়েছে: ${e.localizedMessage}"
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            try {
                repository.markNotificationRead(notifId)
            } catch (e: Exception) {
                Log.w(TAG, "markNotificationRead error: ${e.message}")
            }
        }
    }

    fun updateDailyGoal(minutes: Long) {
        viewModelScope.launch {
            try {
                repository.updateDailyGoal(minutes)
                _actionMessage.value = "দৈনিক লক্ষ্য $minutes মিনিট সেট করা হয়েছে"
            } catch (e: Exception) {
                Log.w(TAG, "updateDailyGoal error: ${e.message}")
            }
        }
    }

    fun buildJftQuestionsWithCustom(): List<PracticeQuestion> {
        val customList = (customQuestions.value as? UiState.Success)?.data.orEmpty().map { cq ->
            PracticeQuestion(
                id = "custom_${cq.id}",
                category = when (cq.examCategory) {
                    "conversation" -> "Conversation & Expression"
                    "listening" -> "Listening Comprehension"
                    "reading" -> "Reading Comprehension"
                    else -> "Script & Vocabulary"
                },
                promptJp = cq.promptJp,
                promptBn = cq.promptBn,
                audioTextJp = cq.promptJp,
                options = cq.options,
                correctIndex = cq.correctIndex.toInt(),
                explanationBn = cq.explanationBn,
                difficulty = cq.difficulty
            )
        }
        return IrodoriRepository.buildJftFullMockQuestions(customList)
    }

    // --- ADMIN ACTIONS ---
    fun loadAdminDashboardData() {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                _adminStudents.value = repository.fetchAllStudentsForAdmin()
                _adminLogs.value = repository.fetchAdminLogs()
            } catch (e: Exception) {
                _actionMessage.value = "অ্যাডমিন তথ্য লোড হয়নি: ${e.localizedMessage}"
            }
        }
    }

    fun toggleStudentSuspension(targetUid: String, currentSuspended: Boolean) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.setStudentSuspended(targetUid, !currentSuspended)
                loadAdminDashboardData()
                _actionMessage.value = if (!currentSuspended) "শিক্ষার্থী স্থগিত করা হয়েছে" else "শিক্ষার্থী সক্রিয় করা হয়েছে"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminPublishAnnouncement(title: String, body: String, category: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.createAnnouncement(title, body, category)
                loadAdminDashboardData()
                _actionMessage.value = "ঘোষণা প্রকাশিত হয়েছে!"
            } catch (e: Exception) {
                _actionMessage.value = "ঘোষণা প্রকাশ ব্যর্থ: ${e.localizedMessage}"
            }
        }
    }

    fun adminDeleteAnnouncement(id: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.deleteAnnouncement(id)
                _actionMessage.value = "ঘোষণা মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminAddVideo(
        title: String,
        descriptionBn: String,
        videoUrl: String,
        bookId: String,
        lessonNumber: Long,
        featuredOnHome: Boolean
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.createLearningVideo(title, descriptionBn, videoUrl, bookId, lessonNumber, featuredOnHome)
                loadAdminDashboardData()
                _actionMessage.value = "ভিডিও যুক্ত ও প্রকাশিত হয়েছে!"
            } catch (e: Exception) {
                _actionMessage.value = "ভিডিও যুক্ত ব্যর্থ: ${e.localizedMessage}"
            }
        }
    }

    fun adminDeleteVideo(id: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.deleteLearningVideo(id)
                _actionMessage.value = "ভিডিও মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminAddCustomLesson(
        bookId: String,
        lessonNumber: Long,
        titleJp: String,
        titleBn: String,
        canDoBn: String,
        vocabSummary: String,
        grammarSummary: String,
        cultureNoteBn: String
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.createCustomLesson(
                    bookId, lessonNumber, titleJp, titleBn, canDoBn, vocabSummary, grammarSummary, cultureNoteBn
                )
                loadAdminDashboardData()
                _actionMessage.value = "নতুন পাঠ সংযুক্ত হয়েছে!"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminDeleteCustomLesson(id: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.deleteCustomLesson(id)
                _actionMessage.value = "পাঠ মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminAddQuestion(
        examCategory: String,
        promptJp: String,
        promptBn: String,
        options: List<String>,
        correctIndex: Int,
        explanationBn: String,
        difficulty: String
    ) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.createCustomQuestion(
                    examCategory, promptJp, promptBn, options, correctIndex, explanationBn, difficulty
                )
                loadAdminDashboardData()
                _actionMessage.value = "প্রশ্নব্যাংকে নতুন প্রশ্ন যুক্ত হয়েছে!"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun adminDeleteQuestion(id: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            try {
                repository.deleteCustomQuestion(id)
                _actionMessage.value = "প্রশ্ন মুছে ফেলা হয়েছে"
            } catch (e: Exception) {
                _actionMessage.value = "ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    private companion object {
        const val TAG = "JournalVM"
    }
}
