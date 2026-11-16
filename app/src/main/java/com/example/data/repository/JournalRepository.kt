package com.example.data.repository

import android.content.Context
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class JournalRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    companion object {
        const val ADMIN_EMAIL = "Mohammadarifchowdhury00@gmail.com"
        fun isAdminEmail(email: String?): Boolean =
            email != null && email.trim().equals(ADMIN_EMAIL, ignoreCase = true)

        fun computeLevel(totalXp: Long): Long = (totalXp / 150L) + 1L

        fun levelTitleBn(level: Long): String = when {
            level >= 25 -> "জাপানি মহাগুরু (N4+)"
            level >= 15 -> "কাঞ্জি সাধক"
            level >= 8 -> "ইরোদোরি অভিযাত্রী"
            level >= 4 -> "কানা যোদ্ধা"
            else -> "নবীন শিক্ষার্থী"
        }
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Google Sign-In required before Firestore access.")

    private fun todayDateStr(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun sanitizeDocId(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_\\-.]"), "_").take(120).ifBlank { "item" }

    private fun <T> Flow<T>.wrapFirestoreFlowErrors(
        operationType: OperationType,
        path: String,
        uid: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        email: String,
        photoUrl: String?
    ): UserProfile {
        val userRef = db.collection("users").document(userId)
        val lbRef = db.collection("leaderboard").document(userId)
        val today = todayDateStr()
        return try {
            val snap = userRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserProfile(userId = userId, displayName = displayName, email = email)
                // Ensure leaderboard projection exists too
                val lbSnap = lbRef.get().await()
                if (!lbSnap.exists()) {
                    val lbPayload = buildMap<String, Any> {
                        put("userId", userId)
                        put("displayName", existing.displayName.ifBlank { displayName }.take(100))
                        if (!existing.photoUrl.isNullOrBlank()) put("photoUrl", existing.photoUrl.take(600))
                        put("totalXp", existing.totalXp)
                        put("dailyXp", existing.dailyXp)
                        put("weeklyXp", existing.weeklyXp)
                        put("monthlyXp", existing.monthlyXp)
                        put("level", existing.level)
                        put("streakDays", existing.streakDays)
                        put("badgeCount", existing.badges.size.toLong())
                        put("createdAt", FieldValue.serverTimestamp())
                        put("updatedAt", FieldValue.serverTimestamp())
                    }
                    lbRef.set(lbPayload).await()
                }
                existing
            } else {
                val safeName = displayName.ifBlank { "শিক্ষার্থী" }.take(100)
                val safeEmail = email.ifBlank { "learner@arifjournal.app" }.take(200)
                val profilePayload = buildMap<String, Any> {
                    put("userId", userId)
                    put("displayName", safeName)
                    put("email", safeEmail)
                    if (!photoUrl.isNullOrBlank()) put("photoUrl", photoUrl.take(600))
                    put("role", "student")
                    put("totalXp", 0L)
                    put("dailyXp", 0L)
                    put("weeklyXp", 0L)
                    put("monthlyXp", 0L)
                    put("level", 1L)
                    put("streakDays", 1L)
                    put("lastStudyDate", today)
                    put("dailyGoalMinutes", 15L)
                    put("todayStudyMinutes", 0L)
                    put("totalStudyMinutes", 0L)
                    put("completedLessonsCount", 0L)
                    put("badges", emptyList<String>())
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                userRef.set(profilePayload).await()

                val lbPayload = buildMap<String, Any> {
                    put("userId", userId)
                    put("displayName", safeName)
                    if (!photoUrl.isNullOrBlank()) put("photoUrl", photoUrl.take(600))
                    put("totalXp", 0L)
                    put("dailyXp", 0L)
                    put("weeklyXp", 0L)
                    put("monthlyXp", 0L)
                    put("level", 1L)
                    put("streakDays", 1L)
                    put("badgeCount", 0L)
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                lbRef.set(lbPayload).await()

                val welcomeNotif = mapOf(
                    "userId" to userId,
                    "category" to "welcome",
                    "title" to "THE ARIF JOURNAL-এ স্বাগতম!",
                    "body" to "শিখছি, অনুশীলন করছি, এগিয়ে যাচ্ছি — আজই হিরাগানা বা ইরোদোরি শুরু করুন।",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.collection("notifications").document("welcome_1").set(welcomeNotif).await()

                UserProfile(
                    id = userId,
                    userId = userId,
                    displayName = safeName,
                    email = safeEmail,
                    photoUrl = photoUrl,
                    lastStudyDate = today
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userRef.path)
            throw e
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .wrapFirestoreFlowErrors(OperationType.GET, path, userId)
        )
    }

    fun observeLessonProgress(userId: String): Flow<List<LessonProgressRecord>> = flow {
        val path = "users/$userId/lesson_progress"
        emitAll(
            db.collection("users").document(userId).collection("lesson_progress")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        LessonProgressRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observeScriptMastery(userId: String): Flow<List<ScriptMasteryRecord>> = flow {
        val path = "users/$userId/script_mastery"
        emitAll(
            db.collection("users").document(userId).collection("script_mastery")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ScriptMasteryRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observeExamAttempts(userId: String): Flow<List<ExamAttemptRecord>> = flow {
        val path = "users/$userId/exam_attempts"
        emitAll(
            db.collection("users").document(userId).collection("exam_attempts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ExamAttemptRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observeXpTransactions(userId: String): Flow<List<XpTransactionRecord>> = flow {
        val path = "users/$userId/xp_transactions"
        emitAll(
            db.collection("users").document(userId).collection("xp_transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        XpTransactionRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observeNotifications(userId: String): Flow<List<NotificationRecord>> = flow {
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        NotificationRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observeLeaderboard(userId: String): Flow<List<LeaderboardEntry>> = flow {
        val path = "leaderboard"
        emitAll(
            db.collection("leaderboard")
                .orderBy("totalXp", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        LeaderboardEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observePublishedAnnouncements(userId: String, isAdmin: Boolean): Flow<List<AnnouncementRecord>> = flow {
        val path = "announcements"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("published", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(
                        AnnouncementRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observePublishedVideos(userId: String, isAdmin: Boolean): Flow<List<LearningVideoRecord>> = flow {
        val path = "learning_videos"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("published", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(
                        LearningVideoRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observePublishedCustomLessons(userId: String, isAdmin: Boolean): Flow<List<CustomLessonRecord>> = flow {
        val path = "custom_lessons"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("published", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomLessonRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    fun observePublishedCustomQuestions(userId: String, isAdmin: Boolean): Flow<List<CustomQuestionRecord>> = flow {
        val path = "custom_questions"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("published", true)
        }
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomQuestionRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .wrapFirestoreFlowErrors(OperationType.LIST, path, userId)
        )
    }

    suspend fun awardXpOnce(
        activityKey: String,
        sourceType: String,
        xpAmount: Long,
        description: String,
        studyMinutesDelta: Long = 2L,
        badgeUnlock: String? = null
    ): Boolean {
        val uid = requireUserId()
        val safeKey = sanitizeDocId(activityKey)
        val clampedXp = xpAmount.coerceIn(1L, 500L)
        val txRef = db.collection("users").document(uid).collection("xp_transactions").document(safeKey)
        val userRef = db.collection("users").document(uid)
        val lbRef = db.collection("leaderboard").document(uid)

        return try {
            val existingTx = txRef.get().await()
            if (existingTx.exists()) {
                return false
            }

            txRef.set(
                mapOf(
                    "userId" to uid,
                    "activityKey" to safeKey,
                    "sourceType" to sourceType.take(64),
                    "xpAmount" to clampedXp,
                    "description" to description.take(250),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val userSnap = userRef.get().await()
            val profile = userSnap.toObject(UserProfile::class.java)
            if (profile != null) {
                val today = todayDateStr()
                val sameDay = profile.lastStudyDate == today
                val newTotalXp = profile.totalXp + clampedXp
                val newDailyXp = if (sameDay) profile.dailyXp + clampedXp else clampedXp
                val newWeeklyXp = profile.weeklyXp + clampedXp
                val newMonthlyXp = profile.monthlyXp + clampedXp
                val newLevel = computeLevel(newTotalXp)
                val newStreak = if (sameDay) profile.streakDays.coerceAtLeast(1L) else profile.streakDays + 1L
                val newTodayMinutes = if (sameDay) profile.todayStudyMinutes + studyMinutesDelta else studyMinutesDelta
                val newTotalMinutes = profile.totalStudyMinutes + studyMinutesDelta
                val updatedBadges = profile.badges.toMutableList()
                if (!badgeUnlock.isNullOrBlank() && !updatedBadges.contains(badgeUnlock) && updatedBadges.size < 50) {
                    updatedBadges.add(badgeUnlock)
                }
                if (newTotalXp >= 500L && !updatedBadges.contains("xp_500")) updatedBadges.add("xp_500")
                if (newLevel > profile.level) {
                    userRef.collection("notifications").document("lvl_$newLevel").set(
                        mapOf(
                            "userId" to uid,
                            "category" to "level_up",
                            "title" to "অভিনন্দন! লেভেল $newLevel অর্জিত!",
                            "body" to "আপনি এখন ${levelTitleBn(newLevel)} স্তরে পৌঁছেছেন।",
                            "isRead" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                userRef.update(
                    mapOf(
                        "totalXp" to newTotalXp,
                        "dailyXp" to newDailyXp,
                        "weeklyXp" to newWeeklyXp,
                        "monthlyXp" to newMonthlyXp,
                        "level" to newLevel,
                        "streakDays" to newStreak,
                        "lastStudyDate" to today,
                        "todayStudyMinutes" to newTodayMinutes,
                        "totalStudyMinutes" to newTotalMinutes,
                        "badges" to updatedBadges.take(50),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                lbRef.update(
                    mapOf(
                        "totalXp" to newTotalXp,
                        "dailyXp" to newDailyXp,
                        "weeklyXp" to newWeeklyXp,
                        "monthlyXp" to newMonthlyXp,
                        "level" to newLevel,
                        "streakDays" to newStreak,
                        "badgeCount" to updatedBadges.size.toLong(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            throw e
        }
    }

    suspend fun saveLessonSectionProgress(
        bookId: String,
        lessonNumber: Int,
        sectionKey: String,
        vocabMastered: Int = 0,
        grammarScore: Int = 0,
        gameHighScore: Int = 0,
        challengePassed: Boolean = false
    ) {
        val uid = requireUserId()
        val key = sanitizeDocId("${bookId}_${lessonNumber}")
        val docRef = db.collection("users").document(uid).collection("lesson_progress").document(key)
        try {
            val snap = docRef.get().await()
            val existing = snap.toObject(LessonProgressRecord::class.java)
            val sections = (existing?.completedSections ?: emptyList()).toMutableSet()
            sections.add(sectionKey)
            val newVocab = maxOf(existing?.vocabMastered ?: 0L, vocabMastered.toLong())
            val newGrammar = maxOf(existing?.grammarScore ?: 0L, grammarScore.toLong())
            val newGame = maxOf(existing?.gameHighScore ?: 0L, gameHighScore.toLong())
            val newChallenge = (existing?.challengePassed == true) || challengePassed
            val isCompleted = newChallenge || sections.size >= 10
            val wasAlreadyCompleted = existing?.completed == true

            if (existing == null) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "bookId" to bookId,
                        "lessonNumber" to lessonNumber.toLong(),
                        "completedSections" to sections.toList().take(25),
                        "vocabMastered" to newVocab,
                        "grammarScore" to newGrammar,
                        "gameHighScore" to newGame,
                        "challengePassed" to newChallenge,
                        "completed" to isCompleted,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "completedSections" to sections.toList().take(25),
                        "vocabMastered" to newVocab,
                        "grammarScore" to newGrammar,
                        "gameHighScore" to newGame,
                        "challengePassed" to newChallenge,
                        "completed" to isCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (isCompleted && !wasAlreadyCompleted) {
                db.collection("users").document(uid).update(
                    mapOf(
                        "completedLessonsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                awardXpOnce(
                    activityKey = "lesson_${key}_complete",
                    sourceType = "lesson",
                    xpAmount = 80L,
                    description = "ইরোদোরি লেসন সম্পন্ন: $bookId #$lessonNumber",
                    studyMinutesDelta = 8L,
                    badgeUnlock = "first_lesson"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun recordScriptAttempt(
        scriptType: String,
        character: String,
        isCorrect: Boolean
    ) {
        val uid = requireUserId()
        val charId = sanitizeDocId("${scriptType}_${character.hashCode()}")
        val docRef = db.collection("users").document(uid).collection("script_mastery").document(charId)
        try {
            val snap = docRef.get().await()
            val existing = snap.toObject(ScriptMasteryRecord::class.java)
            val newCorrect = (existing?.correctCount ?: 0L) + if (isCorrect) 1L else 0L
            val newAttempts = (existing?.attemptCount ?: 0L) + 1L
            val mastered = newCorrect >= 2L
            if (existing == null) {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "scriptType" to scriptType,
                        "character" to character,
                        "correctCount" to newCorrect,
                        "attemptCount" to newAttempts,
                        "mastered" to mastered,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "correctCount" to newCorrect,
                        "attemptCount" to newAttempts,
                        "mastered" to mastered,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun saveExamAttempt(
        examType: String,
        title: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        categoryBreakdown: Map<String, Long>,
        passed: Boolean
    ) {
        val uid = requireUserId()
        val attemptRef = db.collection("users").document(uid).collection("exam_attempts").document()
        try {
            attemptRef.set(
                mapOf(
                    "userId" to uid,
                    "examType" to examType,
                    "title" to title.take(150),
                    "score" to score.toLong(),
                    "totalQuestions" to totalQuestions.toLong(),
                    "durationSeconds" to durationSeconds.toLong(),
                    "categoryBreakdown" to categoryBreakdown,
                    "passed" to passed,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val notifRef = db.collection("users").document(uid).collection("notifications").document()
            notifRef.set(
                mapOf(
                    "userId" to uid,
                    "category" to "exam_result",
                    "title" to "$title ফলাফল প্রকাশিত",
                    "body" to "স্কোর: $score / $totalQuestions (${if (passed) "উত্তীর্ণ" else "আরও অনুশীলন প্রয়োজন"})",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val xp = (score * 3L).coerceIn(10L, 250L)
            awardXpOnce(
                activityKey = "exam_${attemptRef.id}",
                sourceType = "exam",
                xpAmount = xp,
                description = "$title সম্পন্ন",
                studyMinutesDelta = (durationSeconds / 60L).coerceAtLeast(2L),
                badgeUnlock = if (passed) "exam_pass" else null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, attemptRef.path)
            throw e
        }
    }

    suspend fun markNotificationRead(notifId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notifications").document(notifId)
        try {
            docRef.update(
                mapOf(
                    "isRead" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateDailyGoal(minutes: Long) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "dailyGoalMinutes" to minutes.coerceIn(5L, 240L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- ADMIN ACTIONS (Protected by Firestore rules: Mohammadarifchowdhury00@gmail.com) ---

    suspend fun fetchAllStudentsForAdmin(): List<UserProfile> {
        val path = "users"
        return try {
            db.collection(path).get().await().toObjects(
                UserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun setStudentSuspended(targetUid: String, suspended: Boolean) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("users").document(targetUid)
        try {
            ref.update(
                mapOf(
                    "isSuspended" to suspended,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(adminEmail, if (suspended) "Suspended student" else "Activated student", "user", targetUid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            throw e
        }
    }

    suspend fun createAnnouncement(title: String, body: String, category: String) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("announcements").document()
        try {
            ref.set(
                mapOf(
                    "title" to title.take(150),
                    "body" to body.take(2000),
                    "category" to category.take(64),
                    "published" to true,
                    "authorEmail" to adminEmail,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(adminEmail, "Published announcement: $title", "announcement", ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun deleteAnnouncement(id: String) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("announcements").document(id)
        try {
            ref.delete().await()
            logAdminAction(adminEmail, "Deleted announcement", "announcement", id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun createLearningVideo(
        title: String,
        descriptionBn: String,
        videoUrl: String,
        bookId: String,
        lessonNumber: Long,
        featuredOnHome: Boolean
    ) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("learning_videos").document()
        try {
            ref.set(
                mapOf(
                    "title" to title.take(150),
                    "descriptionBn" to descriptionBn.take(1000),
                    "videoUrl" to videoUrl.take(500),
                    "bookId" to bookId.take(64),
                    "lessonNumber" to lessonNumber.coerceIn(0L, 54L),
                    "published" to true,
                    "featuredOnHome" to featuredOnHome,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(adminEmail, "Added video: $title", "learning_video", ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun deleteLearningVideo(id: String) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("learning_videos").document(id)
        try {
            ref.delete().await()
            logAdminAction(adminEmail, "Deleted video", "learning_video", id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun createCustomLesson(
        bookId: String,
        lessonNumber: Long,
        titleJp: String,
        titleBn: String,
        canDoBn: String,
        vocabSummary: String,
        grammarSummary: String,
        cultureNoteBn: String
    ) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("custom_lessons").document()
        try {
            ref.set(
                mapOf(
                    "bookId" to bookId.take(64),
                    "lessonNumber" to lessonNumber.coerceIn(1L, 54L),
                    "titleJp" to titleJp.take(200),
                    "titleBn" to titleBn.take(200),
                    "canDoBn" to canDoBn.take(1000),
                    "vocabSummary" to vocabSummary.take(2000),
                    "grammarSummary" to grammarSummary.take(2000),
                    "cultureNoteBn" to cultureNoteBn.take(2000),
                    "published" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(adminEmail, "Added lesson material: $titleBn", "custom_lesson", ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun deleteCustomLesson(id: String) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("custom_lessons").document(id)
        try {
            ref.delete().await()
            logAdminAction(adminEmail, "Deleted lesson material", "custom_lesson", id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun createCustomQuestion(
        examCategory: String,
        promptJp: String,
        promptBn: String,
        options: List<String>,
        correctIndex: Int,
        explanationBn: String,
        difficulty: String
    ) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("custom_questions").document()
        try {
            val safeOptions = (options + listOf("", "", "", "")).take(4).map { it.ifBlank { "বিকল্প" } }
            ref.set(
                mapOf(
                    "examCategory" to examCategory.take(64),
                    "promptJp" to promptJp.take(1000),
                    "promptBn" to promptBn.take(1000),
                    "options" to safeOptions,
                    "correctIndex" to correctIndex.coerceIn(0, 3).toLong(),
                    "explanationBn" to explanationBn.take(1000),
                    "difficulty" to difficulty.take(32),
                    "published" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction(adminEmail, "Added exam question", "custom_question", ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun deleteCustomQuestion(id: String) {
        val adminEmail = auth.currentUser?.email ?: ADMIN_EMAIL
        val ref = db.collection("custom_questions").document(id)
        try {
            ref.delete().await()
            logAdminAction(adminEmail, "Deleted question", "custom_question", id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    suspend fun fetchAdminLogs(): List<AdminActivityLogRecord> {
        val path = "admin_activity_logs"
        return try {
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()
                .toObjects(
                    AdminActivityLogRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    private suspend fun logAdminAction(
        adminEmail: String,
        action: String,
        targetType: String,
        targetId: String
    ) {
        val ref = db.collection("admin_activity_logs").document()
        ref.set(
            mapOf(
                "adminEmail" to adminEmail.take(200),
                "action" to action.take(200),
                "targetType" to targetType.take(64),
                "targetId" to targetId.take(128),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
