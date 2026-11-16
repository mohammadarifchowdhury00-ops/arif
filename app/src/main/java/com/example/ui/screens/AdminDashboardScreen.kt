package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminActivityLogRecord
import com.example.data.model.AnnouncementRecord
import com.example.data.model.CustomLessonRecord
import com.example.data.model.CustomQuestionRecord
import com.example.data.model.LearningVideoRecord
import com.example.data.model.UserProfile
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.ToriiCrimson
import com.example.ui.viewmodel.JournalViewModel

@Composable
fun AdminDashboardScreen(
    isAdmin: Boolean,
    students: List<UserProfile>,
    announcements: List<AnnouncementRecord>,
    videos: List<LearningVideoRecord>,
    customLessons: List<CustomLessonRecord>,
    customQuestions: List<CustomQuestionRecord>,
    adminLogs: List<AdminActivityLogRecord>,
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    if (!isAdmin) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("অননুমোদিত প্রবেশাধিকার", style = MaterialTheme.typography.headlineSmall, color = ToriiCrimson)
            Spacer(modifier = Modifier.height(8.dp))
            Text("শুধুমাত্র অনুমোদিত অ্যাডমিন এই প্যানেল ব্যবহার করতে পারবেন।")
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = onBack) { Text("ফিরে যান") }
        }
        return
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboardData()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("শিক্ষার্থী", "পাঠ CMS", "ভিডিও", "প্রশ্নব্যাংক", "ঘোষণা ও লগ")

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
                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = ToriiCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("অ্যাডমিন কন্ট্রোল প্যানেল", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson, fontWeight = FontWeight.Bold)
                    Text("শিক্ষার্থী, কনটেন্ট, ভিডিও ও পরীক্ষা ব্যবস্থাপনা", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { idx, label ->
                    item {
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label) },
                            modifier = Modifier.testTag("admin_tab_$idx")
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Students management
                item {
                    Text("নিবন্ধিত শিক্ষার্থী (${students.size})", style = MaterialTheme.typography.titleMedium)
                }
                items(students, key = { it.userId }) { stu ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stu.displayName.ifBlank { "শিক্ষার্থী" }, style = MaterialTheme.typography.titleSmall)
                                Text(stu.email, style = MaterialTheme.typography.bodySmall)
                                Text("XP: ${stu.totalXp} • সম্পন্ন পাঠ: ${stu.completedLessonsCount} • স্ট্রিক: ${stu.streakDays}", style = MaterialTheme.typography.labelSmall, color = IndigoScholar)
                            }
                            if (stu.role != "admin") {
                                OutlinedButton(
                                    onClick = { viewModel.toggleStudentSuspension(stu.userId, stu.isSuspended) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (stu.isSuspended) EmeraldSuccess else ToriiCrimson
                                    )
                                ) {
                                    Text(if (stu.isSuspended) "সক্রিয় করুন" else "স্থগিত করুন")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Custom Lessons CMS
                item {
                    var bookId by remember { mutableStateOf("starter") }
                    var lessonNum by remember { mutableStateOf("1") }
                    var titleJp by remember { mutableStateOf("") }
                    var titleBn by remember { mutableStateOf("") }
                    var canDoBn by remember { mutableStateOf("") }
                    var vocabSummary by remember { mutableStateOf("") }
                    var grammarSummary by remember { mutableStateOf("") }
                    var cultureNoteBn by remember { mutableStateOf("") }

                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("নতুন অতিরিক্ত পাঠ যুক্ত করুন", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("starter", "elementary_1", "elementary_2").forEach { id ->
                                    FilterChip(selected = bookId == id, onClick = { bookId = id }, label = { Text(id) })
                                }
                            }
                            OutlinedTextField(value = lessonNum, onValueChange = { lessonNum = it }, label = { Text("পাঠ নম্বর (১-১৮)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = titleJp, onValueChange = { titleJp = it }, label = { Text("জাপানি শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("বাংলা শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = canDoBn, onValueChange = { canDoBn = it }, label = { Text("ক্যান-ডু লক্ষ্য (বাংলায়)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = vocabSummary, onValueChange = { vocabSummary = it }, label = { Text("শব্দ সারাংশ") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = grammarSummary, onValueChange = { grammarSummary = it }, label = { Text("ব্যাকরণ সারাংশ") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = cultureNoteBn, onValueChange = { cultureNoteBn = it }, label = { Text("সংস্কৃতি নোট") }, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    if (titleJp.isNotBlank() && titleBn.isNotBlank()) {
                                        viewModel.adminAddCustomLesson(
                                            bookId, lessonNum.toLongOrNull() ?: 1L, titleJp, titleBn, canDoBn, vocabSummary, grammarSummary, cultureNoteBn
                                        )
                                        titleJp = ""; titleBn = ""; canDoBn = ""; vocabSummary = ""; grammarSummary = ""; cultureNoteBn = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_add_lesson_btn")
                            ) { Text("পাঠ প্রকাশ করুন") }
                        }
                    }
                }
                items(customLessons, key = { it.id }) { cl ->
                    AdminDeleteItemCard(
                        title = "${cl.bookId} • পাঠ ${cl.lessonNumber}: ${cl.titleBn}",
                        subtitle = cl.canDoBn,
                        onDelete = { viewModel.adminDeleteCustomLesson(cl.id) }
                    )
                }
            }

            2 -> {
                // Videos CMS
                item {
                    var title by remember { mutableStateOf("") }
                    var desc by remember { mutableStateOf("") }
                    var url by remember { mutableStateOf("") }
                    var bookId by remember { mutableStateOf("starter") }
                    var lessonNum by remember { mutableStateOf("1") }
                    var featured by remember { mutableStateOf(true) }

                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("নতুন লার্নিং ভিডিও যুক্ত করুন", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ভিডিও শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বাংলা বিবরণ") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("ভিডিও লিংক (YouTube / URL)") }, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("starter", "elementary_1", "elementary_2").forEach { id ->
                                    FilterChip(selected = bookId == id, onClick = { bookId = id }, label = { Text(id) })
                                }
                            }
                            OutlinedTextField(value = lessonNum, onValueChange = { lessonNum = it }, label = { Text("পাঠ নম্বর") }, modifier = Modifier.fillMaxWidth())
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = featured, onCheckedChange = { featured = it })
                                Text("হোমপেজে প্রদর্শন করুন")
                            }
                            Button(
                                onClick = {
                                    if (title.isNotBlank() && url.isNotBlank()) {
                                        viewModel.adminAddVideo(title, desc, url, bookId, lessonNum.toLongOrNull() ?: 1L, featured)
                                        title = ""; desc = ""; url = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_add_video_btn")
                            ) { Text("ভিডিও যুক্ত করুন") }
                        }
                    }
                }
                items(videos, key = { it.id }) { vid ->
                    AdminDeleteItemCard(
                        title = vid.title,
                        subtitle = "${vid.bookId} পাঠ ${vid.lessonNumber} • ${vid.videoUrl}",
                        onDelete = { viewModel.adminDeleteVideo(vid.id) }
                    )
                }
            }

            3 -> {
                // Question Bank CMS
                item {
                    var category by remember { mutableStateOf("script_vocab") }
                    var promptJp by remember { mutableStateOf("") }
                    var promptBn by remember { mutableStateOf("") }
                    var opt0 by remember { mutableStateOf("") }
                    var opt1 by remember { mutableStateOf("") }
                    var opt2 by remember { mutableStateOf("") }
                    var opt3 by remember { mutableStateOf("") }
                    var correctIdx by remember { mutableIntStateOf(0) }
                    var explanationBn by remember { mutableStateOf("") }

                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("প্রশ্নব্যাংকে নতুন প্রশ্ন যোগ করুন", style = MaterialTheme.typography.titleMedium)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("script_vocab", "conversation", "listening", "reading").forEach { c ->
                                    item { FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) }
                                }
                            }
                            OutlinedTextField(value = promptJp, onValueChange = { promptJp = it }, label = { Text("জাপানি প্রশ্ন") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = promptBn, onValueChange = { promptBn = it }, label = { Text("বাংলা নির্দেশনা") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = opt0, onValueChange = { opt0 = it }, label = { Text("অপশন ১") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("অপশন ২") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("অপশন ৩") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = opt3, onValueChange = { opt3 = it }, label = { Text("অপশন ৪") }, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..3).forEach { i ->
                                    FilterChip(selected = correctIdx == i, onClick = { correctIdx = i }, label = { Text("সঠিক #${i + 1}") })
                                }
                            }
                            OutlinedTextField(value = explanationBn, onValueChange = { explanationBn = it }, label = { Text("বাংলা ব্যাখ্যা") }, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    val opts = listOf(opt0, opt1, opt2, opt3)
                                    if (promptJp.isNotBlank() && promptBn.isNotBlank() && opts.all { it.isNotBlank() }) {
                                        viewModel.adminAddQuestion(category, promptJp, promptBn, opts, correctIdx, explanationBn, "normal")
                                        promptJp = ""; promptBn = ""; opt0 = ""; opt1 = ""; opt2 = ""; opt3 = ""; explanationBn = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_add_question_btn")
                            ) { Text("প্রশ্ন সংরক্ষণ করুন") }
                        }
                    }
                }
                items(customQuestions, key = { it.id }) { q ->
                    AdminDeleteItemCard(
                        title = "${q.examCategory}: ${q.promptJp}",
                        subtitle = q.promptBn,
                        onDelete = { viewModel.adminDeleteQuestion(q.id) }
                    )
                }
            }

            else -> {
                // Announcements & Audit Logs
                item {
                    var title by remember { mutableStateOf("") }
                    var body by remember { mutableStateOf("") }
                    var category by remember { mutableStateOf("সাধারণ") }

                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("নতুন ঘোষণা প্রকাশ করুন", style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ঘোষণার শিরোনাম") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত বার্তা") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("বিভাগ") }, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    if (title.isNotBlank() && body.isNotBlank()) {
                                        viewModel.adminPublishAnnouncement(title, body, category)
                                        title = ""; body = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_publish_announcement_btn")
                            ) { Text("ঘোষণা প্রকাশ করুন") }
                        }
                    }
                }
                items(announcements, key = { it.id }) { ann ->
                    AdminDeleteItemCard(
                        title = ann.title,
                        subtitle = ann.body,
                        onDelete = { viewModel.adminDeleteAnnouncement(ann.id) }
                    )
                }
                item {
                    Text("অ্যাডমিন অ্যাক্টিভিটি লগ (${adminLogs.size})", style = MaterialTheme.typography.titleMedium)
                }
                items(adminLogs.take(15), key = { it.id }) { log ->
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${log.action} (${log.targetType})", style = MaterialTheme.typography.titleSmall, color = MatchaJade)
                            Text("টার্গেট: ${log.targetId} • ${log.adminEmail}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun AdminDeleteItemCard(
    title: String,
    subtitle: String,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "মুছে ফেলুন", tint = ToriiCrimson)
            }
        }
    }
}
