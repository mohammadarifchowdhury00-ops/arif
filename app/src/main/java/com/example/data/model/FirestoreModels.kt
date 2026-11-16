package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: String = "student",
    val totalXp: Long = 0L,
    val dailyXp: Long = 0L,
    val weeklyXp: Long = 0L,
    val monthlyXp: Long = 0L,
    val level: Long = 1L,
    val streakDays: Long = 1L,
    val lastStudyDate: String = "",
    val dailyGoalMinutes: Long = 15L,
    val todayStudyMinutes: Long = 0L,
    val totalStudyMinutes: Long = 0L,
    val completedLessonsCount: Long = 0L,
    val badges: List<String> = emptyList(),
    val isSuspended: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LessonProgressRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val lessonNumber: Long = 1L,
    val completedSections: List<String> = emptyList(),
    val vocabMastered: Long = 0L,
    val grammarScore: Long = 0L,
    val gameHighScore: Long = 0L,
    val challengePassed: Boolean = false,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ScriptMasteryRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val scriptType: String = "hiragana",
    val character: String = "",
    val correctCount: Long = 0L,
    val attemptCount: Long = 0L,
    val mastered: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class XpTransactionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val activityKey: String = "",
    val sourceType: String = "",
    val xpAmount: Long = 0L,
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ExamAttemptRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val examType: String = "jft_full",
    val title: String = "",
    val score: Long = 0L,
    val totalQuestions: Long = 60L,
    val durationSeconds: Long = 0L,
    val categoryBreakdown: Map<String, Long> = emptyMap(),
    val passed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class NotificationRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "reminder",
    val title: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val totalXp: Long = 0L,
    val dailyXp: Long = 0L,
    val weeklyXp: Long = 0L,
    val monthlyXp: Long = 0L,
    val level: Long = 1L,
    val streakDays: Long = 1L,
    val badgeCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AnnouncementRecord(
    @DocumentId val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "সাধারণ",
    val published: Boolean = true,
    val authorEmail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LearningVideoRecord(
    @DocumentId val id: String = "",
    val title: String = "",
    val descriptionBn: String = "",
    val videoUrl: String = "",
    val bookId: String = "starter",
    val lessonNumber: Long = 1L,
    val published: Boolean = true,
    val featuredOnHome: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomLessonRecord(
    @DocumentId val id: String = "",
    val bookId: String = "starter",
    val lessonNumber: Long = 1L,
    val titleJp: String = "",
    val titleBn: String = "",
    val canDoBn: String = "",
    val vocabSummary: String = "",
    val grammarSummary: String = "",
    val cultureNoteBn: String = "",
    val published: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomQuestionRecord(
    @DocumentId val id: String = "",
    val examCategory: String = "script_vocab",
    val promptJp: String = "",
    val promptBn: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctIndex: Long = 0L,
    val explanationBn: String = "",
    val difficulty: String = "normal",
    val published: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminActivityLogRecord(
    @DocumentId val id: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
