package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.curriculum.KanjiRepository
import com.example.data.model.AnnouncementRecord
import com.example.data.model.ExamAttemptRecord
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LessonProgressRecord
import com.example.data.model.NotificationRecord
import com.example.data.model.ScriptMasteryRecord
import com.example.data.model.UserProfile
import com.example.data.model.XpTransactionRecord
import com.example.data.repository.JournalRepository
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.StreakCoral
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentDashboardScreen(
    profile: UserProfile?,
    lessonProgress: List<LessonProgressRecord>,
    scriptMastery: List<ScriptMasteryRecord>,
    examAttempts: List<ExamAttemptRecord>,
    xpTransactions: List<XpTransactionRecord>,
    onUpdateDailyGoal: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val completedLessons = lessonProgress.count { it.completed }
    val completedBooks = listOf("starter", "elementary_1", "elementary_2").count { bookId ->
        lessonProgress.count { it.bookId == bookId && it.completed } >= 18
    }
    val vocabMastered = lessonProgress.sumOf { it.vocabMastered }
    val grammarMastered = lessonProgress.count { it.grammarScore > 0L }
    val kanjiMastered = scriptMastery.count { it.scriptType == "kanji" && it.mastered }
    val jftResults = examAttempts.filter { it.examType.startsWith("jft") }
    val jlptResults = examAttempts.filter { it.examType.startsWith("jlpt") }
    val totalXp = profile?.totalXp ?: 0L
    val level = profile?.level ?: JournalRepository.computeLevel(totalXp)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরুন")
                }
                Text("শিক্ষার্থী ড্যাশবোর্ড", style = MaterialTheme.typography.headlineMedium, color = ToriiCrimson)
            }
        }

        // Student Profile Card
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!profile?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile?.photoUrl,
                            contentDescription = "প্রোফাইল ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                        )
                    } else {
                        Surface(shape = CircleShape, color = IndigoScholar.copy(alpha = 0.14f), modifier = Modifier.size(64.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = IndigoScholar, modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(profile?.displayName?.ifBlank { "শিক্ষার্থী" } ?: "শিক্ষার্থী", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("লেভেল $level • ${JournalRepository.levelTitleBn(level)}", style = MaterialTheme.typography.bodyMedium, color = IndigoScholar)
                        Text("ইউজার আইডি: ${profile?.userId?.take(10) ?: "..."}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Core Metrics
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard("মোট XP", "$totalXp", XpGold, Modifier.weight(1f))
                DashboardStatCard("স্ট্রিক", "${profile?.streakDays ?: 1} দিন", StreakCoral, Modifier.weight(1f))
                DashboardStatCard("অধ্যয়ন সময়", "${profile?.totalStudyMinutes ?: 0} মি.", MatchaJade, Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard("সম্পন্ন পাঠ", "$completedLessons / ৫৪", ToriiCrimson, Modifier.weight(1f))
                DashboardStatCard("সম্পন্ন বই", "$completedBooks / ৩", IndigoScholar, Modifier.weight(1f))
                DashboardStatCard("কাঞ্জি আয়ত্ত", "$kanjiMastered / ${KanjiRepository.allKanji.size}", EmeraldSuccess, Modifier.weight(1f))
            }
        }

        // Vocab, Grammar, Kanji Progress Bars
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("বিষয়ভিত্তিক দক্ষতা", style = MaterialTheme.typography.titleMedium)
                    ProgressLine("শব্দভাণ্ডার আয়ত্ত", "$vocabMastered শব্দ", (vocabMastered / 300f).coerceIn(0f, 1f), ToriiCrimson)
                    ProgressLine("ব্যাকরণ অনুশীলন", "$grammarMastered পাঠ", (grammarMastered / 54f).coerceIn(0f, 1f), IndigoScholar)
                    ProgressLine("কাঞ্জি দক্ষতা", "$kanjiMastered কাঞ্জি", (kanjiMastered.toFloat() / KanjiRepository.allKanji.size.coerceAtLeast(1)).coerceIn(0f, 1f), MatchaJade)
                }
            }
        }

        // Daily Goal Setting
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("দৈনিক লক্ষ্য নির্ধারণ (${profile?.dailyGoalMinutes ?: 15} মিনিট)", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10L, 15L, 30L, 45L).forEach { mins ->
                            FilterChip(
                                selected = (profile?.dailyGoalMinutes ?: 15L) == mins,
                                onClick = { onUpdateDailyGoal(mins) },
                                label = { Text("$mins মি.") }
                            )
                        }
                    }
                }
            }
        }

        // Learning Calendar / Study Days Strip
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MatchaJade)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("শেখার ক্যালেন্ডার ও স্ট্রিক", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "সর্বশেষ অধ্যয়নের তারিখ: ${profile?.lastStudyDate?.ifBlank { "আজ" } ?: "আজ"} • টানা ${profile?.streakDays ?: 1} দিনের স্ট্রিক!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "JFT মক পরীক্ষা দিয়েছেন: ${jftResults.size} বার • JLPT মক পরীক্ষা: ${jlptResults.size} বার",
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoScholar
                    )
                }
            }
        }

        // Badges
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("অর্জিত ব্যাজসমূহ (${profile?.badges?.size ?: 0})", style = MaterialTheme.typography.titleMedium)
                    val badges = profile?.badges.orEmpty()
                    if (badges.isEmpty()) {
                        Text("পাঠ বা কানা কুইজ সম্পন্ন করলে ব্যাজ আনলক হবে!", style = MaterialTheme.typography.bodySmall)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            badges.forEach { badge ->
                                Surface(shape = RoundedCornerShape(12.dp), color = XpGold.copy(alpha = 0.14f)) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = XpGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(badge, style = MaterialTheme.typography.labelMedium, color = XpGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent XP Activities
        item {
            Text("সাম্প্রতিক XP ও কার্যক্রম", style = MaterialTheme.typography.titleMedium)
        }
        items(xpTransactions.take(10), key = { it.id }) { tx ->
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = XpGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tx.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("+${tx.xpAmount} XP", style = MaterialTheme.typography.labelLarge, color = EmeraldSuccess)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    currentUserId: String
) {
    var period by remember { mutableStateOf("all") } // daily, weekly, monthly, all

    val sortedEntries = remember(leaderboard, period) {
        when (period) {
            "daily" -> leaderboard.sortedByDescending { it.dailyXp }
            "weekly" -> leaderboard.sortedByDescending { it.weeklyXp }
            "monthly" -> leaderboard.sortedByDescending { it.monthlyXp }
            else -> leaderboard.sortedByDescending { it.totalXp }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("লিডারবোর্ড ও র‍্যাঙ্কিং", style = MaterialTheme.typography.headlineMedium, color = XpGold)
            Text("বিশ্বজুড়ে বাংলাভাষী জাপানি শিক্ষার্থীদের সেরা তালিকা", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("daily" to "দৈনিক", "weekly" to "সাপ্তাহিক", "monthly" to "মাসিক", "all" to "সর্বকালের").forEach { (key, label) ->
                    FilterChip(
                        selected = period == key,
                        onClick = { period = key },
                        label = { Text(label) },
                        modifier = Modifier.testTag("lb_tab_$key")
                    )
                }
            }
        }

        if (sortedEntries.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Text(
                        text = "এখনো র‍্যাঙ্কিং তালিকা তৈরি হচ্ছে। পাঠ সম্পন্ন করে XP অর্জন করুন!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            itemsIndexed(sortedEntries, key = { _, e -> e.id }) { idx, entry ->
                val xpVal = when (period) {
                    "daily" -> entry.dailyXp
                    "weekly" -> entry.weeklyXp
                    "monthly" -> entry.monthlyXp
                    else -> entry.totalXp
                }
                val isMe = entry.userId == currentUserId
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) ToriiCrimson.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = when (idx) {
                                    0 -> XpGold
                                    1 -> IndigoScholar
                                    2 -> MatchaJade
                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        color = if (idx < 3) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${entry.displayName.ifBlank { "শিক্ষার্থী" }}${if (isMe) " (আপনি)" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = StreakCoral, modifier = Modifier.size(14.dp))
                                    Text(" ${entry.streakDays} দিন • লেভেল ${entry.level}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Text("$xpVal XP", style = MaterialTheme.typography.titleSmall, color = XpGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationRecord>,
    announcements: List<AnnouncementRecord>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "ফিরুন")
                }
                Text("বিজ্ঞপ্তি ও ঘোষণা", style = MaterialTheme.typography.headlineMedium, color = IndigoScholar)
            }
        }

        if (announcements.isNotEmpty()) {
            item { Text("প্রাতিষ্ঠানিক ঘোষণা", style = MaterialTheme.typography.titleMedium) }
            items(announcements, key = { it.id }) { ann ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IndigoScholar.copy(alpha = 0.09f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Campaign, contentDescription = null, tint = IndigoScholar)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(ann.title, style = MaterialTheme.typography.titleSmall, color = IndigoScholar)
                            Text(ann.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item { Text("ব্যক্তিগত বিজ্ঞপ্তি", style = MaterialTheme.typography.titleMedium) }
        if (notifications.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Text("নতুন কোনো বিজ্ঞপ্তি নেই।", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(notifications, key = { it.id }) { notif ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notif.isRead) MaterialTheme.colorScheme.surfaceContainerLowest else ToriiCrimson.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (notif.isRead) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = if (notif.isRead) EmeraldSuccess else ToriiCrimson
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall)
                                Text(notif.body, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (!notif.isRead) {
                            TextButton(onClick = { onMarkRead(notif.id) }) {
                                Text("পঠিত")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProgressLine(label: String, countText: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(countText, style = MaterialTheme.typography.labelLarge, color = color)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color
        )
    }
}
