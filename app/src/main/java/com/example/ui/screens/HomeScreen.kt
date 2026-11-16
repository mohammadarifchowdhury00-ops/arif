package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.curriculum.IrodoriRepository
import com.example.data.model.AnnouncementRecord
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LearningVideoRecord
import com.example.data.model.LessonProgressRecord
import com.example.data.model.NotificationRecord
import com.example.data.model.UserProfile
import com.example.data.repository.JournalRepository
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.StreakCoral
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold

@Composable
fun HomeScreen(
    profile: UserProfile?,
    lessonProgress: List<LessonProgressRecord>,
    leaderboard: List<LeaderboardEntry>,
    notifications: List<NotificationRecord>,
    announcements: List<AnnouncementRecord>,
    videos: List<LearningVideoRecord>,
    isAdmin: Boolean,
    isDarkMode: Boolean,
    showRomaji: Boolean,
    showFurigana: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleRomaji: (Boolean) -> Unit,
    onToggleFurigana: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfileDashboard: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenIrodoriBook: (String) -> Unit,
    onContinueLesson: (bookId: String, lessonNumber: Int) -> Unit,
    onOpenHiragana: () -> Unit,
    onOpenKatakana: () -> Unit,
    onOpenKanji: () -> Unit,
    onOpenJlptN5: () -> Unit,
    onOpenJlptN4: () -> Unit,
    onOpenJftBasic: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val unreadNotifCount = notifications.count { !it.isRead }
    val completedLessons = lessonProgress.filter { it.completed }
    val completedKeySet = completedLessons.map { "${it.bookId}_${it.lessonNumber}" }.toSet()

    // Determine next recommended IRODORI lesson
    val nextLesson = remember(completedKeySet) {
        IrodoriRepository.allLessons.firstOrNull {
            !completedKeySet.contains("${it.bookId}_${it.lessonNumber}")
        } ?: IrodoriRepository.allLessons.first()
    }

    val overallProgressPct = ((completedLessons.size.toFloat() / 54f) * 100f).toInt().coerceIn(0, 100)
    val streakDays = profile?.streakDays ?: 1L
    val totalXp = profile?.totalXp ?: 0L
    val level = profile?.level ?: JournalRepository.computeLevel(totalXp)
    val levelTitle = JournalRepository.levelTitleBn(level)
    val dailyGoal = (profile?.dailyGoalMinutes ?: 15L).coerceAtLeast(5L)
    val todayMinutes = profile?.todayStudyMinutes ?: 0L
    val dailyRatio = (todayMinutes.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1-5: Top Header (Logo, Welcome, Student Profile, Notifications, Three-Dot Menu)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ToriiCrimson,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "あ",
                                fontFamily = JapaneseFontFamily,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "THE ARIF JOURNAL",
                            style = MaterialTheme.typography.titleMedium,
                            color = ToriiCrimson,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "স্বাগতম, ${profile?.displayName?.ifBlank { "শিক্ষার্থী" } ?: "শিক্ষার্থী"}!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge { Text("$unreadNotifCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "বিজ্ঞপ্তি ও ঘোষণা"
                            )
                        }
                    }

                    // Student profile photo button
                    IconButton(
                        onClick = onOpenProfileDashboard,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_profile_button")
                    ) {
                        if (!profile?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.photoUrl,
                                contentDescription = "শিক্ষার্থীর প্রোফাইল",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = IndigoScholar.copy(alpha = 0.14f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "শিক্ষার্থীর প্রোফাইল",
                                        tint = IndigoScholar,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Three-dot menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_three_dot_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "আরও মেনু"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("শিক্ষার্থী ড্যাশবোর্ড") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenProfileDashboard()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ডার্ক থিম")
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Rounded.DarkMode, contentDescription = null) },
                                onClick = { onToggleDarkMode(!isDarkMode) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("রোমাজি দেখান")
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Switch(checked = showRomaji, onCheckedChange = onToggleRomaji)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Rounded.Translate, contentDescription = null) },
                                onClick = { onToggleRomaji(!showRomaji) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ফুরিগানা দেখান")
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Switch(checked = showFurigana, onCheckedChange = onToggleFurigana)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Rounded.MenuBook, contentDescription = null) },
                                onClick = { onToggleFurigana(!showFurigana) }
                            )
                            if (isAdmin) {
                                DropdownMenuItem(
                                    text = { Text("অ্যাডমিন প্যানেল", color = ToriiCrimson, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = ToriiCrimson
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onOpenAdminPanel()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("লগ আউট") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSignOut()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tagline + Hero Card with Continue Learning (6-10)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_japan),
                        contentDescription = "শিখছি, অনুশীলন করছি, এগিয়ে যাচ্ছি",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.80f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ToriiCrimson
                        ) {
                            Text(
                                text = "শিখছি, অনুশীলন করছি, এগিয়ে যাচ্ছি। • লেভেল $level ($levelTitle)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "পরবর্তী পাঠ: ${nextLesson.titleJp}",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JapaneseFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পাঠ ${nextLesson.lessonNumber}: ${nextLesson.titleBn}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onContinueLesson(nextLesson.bookId, nextLesson.lessonNumber) },
                            colors = ButtonDefaults.buttonColors(containerColor = XpGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_learning_button")
                        ) {
                            Text("শেখা চালিয়ে যান", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // 6, 7, 8, 9, 11: Streak, Total XP, Level, Course Progress & Daily Target
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChipCard(
                    label = "দৈনিক স্ট্রিক",
                    value = "$streakDays দিন",
                    icon = Icons.Rounded.LocalFireDepartment,
                    tint = StreakCoral,
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    label = "মোট XP",
                    value = "$totalXp",
                    icon = Icons.Rounded.Bolt,
                    tint = XpGold,
                    modifier = Modifier.weight(1f)
                )
                StatChipCard(
                    label = "অগ্রগতি",
                    value = "$overallProgressPct%",
                    icon = Icons.Rounded.School,
                    tint = MatchaJade,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("আজকের শেখার লক্ষ্য", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$todayMinutes / $dailyGoal মিনিট",
                            style = MaterialTheme.typography.labelLarge,
                            color = MatchaJade
                        )
                    }
                    LinearProgressIndicator(
                        progress = { dailyRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MatchaJade
                    )
                    Text(
                        text = "ইরোদোরি ৫৪ পাঠের মধ্যে ${completedLessons.size}টি পাঠ সম্পন্ন হয়েছে। জাপানি শিখি, আনন্দের সঙ্গে!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 9 Core Academies Quick Launch Grid
        item {
            Text("সকল লার্নিং একাডেমি (৯টি মডিউল)", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleTile(
                        badgeJp = "入門",
                        title = "ইরোদোরি স্টার্টার",
                        subtitle = "১৮ পাঠ • A1",
                        color = ToriiCrimson,
                        onClick = { onOpenIrodoriBook("starter") },
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "初級1",
                        title = "এলিমেন্টারি ১",
                        subtitle = "১৮ পাঠ • A2",
                        color = IndigoScholar,
                        onClick = { onOpenIrodoriBook("elementary_1") },
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "初級2",
                        title = "এলিমেন্টারি ২",
                        subtitle = "১৮ পাঠ • A2+",
                        color = MatchaJade,
                        onClick = { onOpenIrodoriBook("elementary_2") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleTile(
                        badgeJp = "あ",
                        title = "হিরাগানা",
                        subtitle = "সম্পূর্ণ একাডেমি",
                        color = SakuraPink,
                        onClick = onOpenHiragana,
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "ア",
                        title = "কাতাকানা",
                        subtitle = "সম্পূর্ণ একাডেমি",
                        color = StreakCoral,
                        onClick = onOpenKatakana,
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "漢字",
                        title = "কাঞ্জি একাডেমি",
                        subtitle = "N5 ও N4",
                        color = IndigoScholar,
                        onClick = onOpenKanji,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ModuleTile(
                        badgeJp = "N5",
                        title = "JLPT N5",
                        subtitle = "৬ মডিউল + মক",
                        color = EmeraldSuccess,
                        onClick = onOpenJlptN5,
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "N4",
                        title = "JLPT N4",
                        subtitle = "৬ মডিউল + মক",
                        color = XpGold,
                        onClick = onOpenJlptN4,
                        modifier = Modifier.weight(1f)
                    )
                    ModuleTile(
                        badgeJp = "JFT",
                        title = "JFT-Basic",
                        subtitle = "৬০ প্রশ্নের পরীক্ষা",
                        color = ToriiCrimson,
                        onClick = onOpenJftBasic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 13. Recommended Practice
        item {
            Text("সুপারিশকৃত অনুশীলন", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    RecommendedPracticeCard(
                        title = "JFT-Basic ৬০ মিনিটের মক",
                        desc = "২৫০ স্কেলে পূর্ণাঙ্গ পরীক্ষা দিন",
                        icon = Icons.Rounded.FactCheck,
                        accent = ToriiCrimson,
                        onClick = onOpenJftBasic
                    )
                }
                item {
                    RecommendedPracticeCard(
                        title = "হিরাগানা ও কাতাকানা ড্রিল",
                        desc = "অক্ষর চেনা, লেখা ও শ্রবণ",
                        icon = Icons.Rounded.Translate,
                        accent = SakuraPink,
                        onClick = onOpenHiragana
                    )
                }
                item {
                    RecommendedPracticeCard(
                        title = "JLPT N5 / N4 কাঞ্জি চ্যালেঞ্জ",
                        desc = "ওনিওমি ও কুনিওমি মাস্টারি",
                        icon = Icons.Rounded.AutoStories,
                        accent = IndigoScholar,
                        onClick = onOpenKanji
                    )
                }
            }
        }

        // 12. Recently Completed Lessons
        item {
            Text("সম্প্রতি সম্পন্ন করা পাঠ", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (completedLessons.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "এখনও কোনো পাঠ সম্পন্ন হয়নি। উপরে 'শেখা চালিয়ে যান' বোতামে চাপ দিয়ে প্রথম পাঠ শুরু করুন!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    completedLessons.take(3).forEach { rec ->
                        val lesson = IrodoriRepository.getLesson(rec.bookId, rec.lessonNumber.toInt())
                        if (lesson != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContinueLesson(lesson.bookId, lesson.lessonNumber) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "পাঠ ${lesson.lessonNumber}: ${lesson.titleJp}",
                                            fontFamily = JapaneseFontFamily,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${lesson.titleBn} • ${rec.completedSections.size}/১৭ ধাপ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldSuccess
                                        )
                                    }
                                    FilledTonalButton(onClick = { onContinueLesson(lesson.bookId, lesson.lessonNumber) }) {
                                        Text("পুনরালোচনা")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 14. Leaderboard Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("লিডারবোর্ড প্রিভিউ", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onOpenLeaderboard) {
                    Text("সব দেখুন")
                }
            }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (leaderboard.isEmpty()) {
                        Text("লিডারবোর্ডে প্রথম স্থান দখল করতে এখনই অনুশীলন করুন!", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        leaderboard.take(3).forEachIndexed { idx, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (idx) {
                                            0 -> XpGold
                                            1 -> IndigoScholar
                                            else -> MatchaJade
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("#${idx + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(entry.displayName.ifBlank { "শিক্ষার্থী" }, style = MaterialTheme.typography.titleSmall)
                                        Text("লেভেল ${entry.level} • স্ট্রিক ${entry.streakDays} দিন", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Text("${entry.totalXp} XP", style = MaterialTheme.typography.titleSmall, color = XpGold)
                            }
                        }
                    }
                }
            }
        }

        // 15. Announcements
        if (announcements.isNotEmpty()) {
            item {
                Text("গুরুত্বপূর্ণ ঘোষণা", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    announcements.take(3).forEach { ann ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IndigoScholar.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Rounded.Campaign, contentDescription = null, tint = IndigoScholar)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(ann.title, style = MaterialTheme.typography.titleSmall, color = IndigoScholar)
                                    Text(ann.body, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 16. Admin-uploaded Learning Videos
        item {
            Text("ভিডিও ক্লাস ও টিউটোরিয়াল", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val homeVideos = videos.filter { it.featuredOnHome }.ifEmpty { videos }
            if (homeVideos.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "অ্যাডমিন নতুন লার্নিং ভিডিও যুক্ত করলে এখানে প্রদর্শিত হবে।",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    homeVideos.take(4).forEach { vid ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(vid.videoUrl))
                                        context.startActivity(intent)
                                    }
                                }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ToriiCrimson.copy(alpha = 0.12f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.PlayCircle, contentDescription = "ভিডিও দেখুন", tint = ToriiCrimson)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vid.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${vid.descriptionBn} (বই: ${vid.bookId}, পাঠ ${vid.lessonNumber})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatChipCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModuleTile(
    badgeJp: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.14f)
            ) {
                Text(
                    text = badgeJp,
                    fontFamily = JapaneseFontFamily,
                    color = color,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecommendedPracticeCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        modifier = Modifier
            .width(230.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
    }
}
