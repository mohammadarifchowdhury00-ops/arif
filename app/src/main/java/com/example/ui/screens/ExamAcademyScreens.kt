package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.JapaneseSpeechManager
import com.example.data.curriculum.IrodoriRepository
import com.example.data.curriculum.PracticeQuestion
import com.example.data.model.ExamAttemptRecord
import com.example.ui.components.InteractiveExamRunner
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold
import com.example.ui.viewmodel.JournalViewModel

private data class ActiveExamSession(
    val examType: String,
    val title: String,
    val durationMinutes: Int,
    val questions: List<PracticeQuestion>
)

@Composable
fun ExamAcademyScreen(
    initialTab: String = "jft", // "jft", "n5", "n4"
    examAttempts: List<ExamAttemptRecord>,
    speechManager: JapaneseSpeechManager,
    viewModel: JournalViewModel
) {
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    var activeExam by remember { mutableStateOf<ActiveExamSession?>(null) }

    if (activeExam != null) {
        BackHandler { activeExam = null }
        val session = activeExam!!
        InteractiveExamRunner(
            title = session.title,
            questions = session.questions,
            durationMinutes = session.durationMinutes,
            speechManager = speechManager,
            onExit = { activeExam = null },
            onCompleted = { score, total, durationSec, breakdown, passed ->
                viewModel.submitExamResult(
                    examType = session.examType,
                    title = session.title,
                    score = score,
                    totalQuestions = total,
                    durationSeconds = durationSec,
                    categoryBreakdown = breakdown,
                    passed = passed
                )
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("পরীক্ষা ও JLPT/JFT প্রস্তুতি কেন্দ্র", style = MaterialTheme.typography.headlineMedium, color = ToriiCrimson)
            Text(
                text = "JFT-Basic পূর্ণাঙ্গ ৬০ প্রশ্নের মক টেস্ট এবং JLPT N5 ও N4 মডিউল",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeTab == "jft",
                    onClick = { activeTab = "jft" },
                    label = { Text("JFT-Basic") },
                    modifier = Modifier.testTag("exam_tab_jft")
                )
                FilterChip(
                    selected = activeTab == "n5",
                    onClick = { activeTab = "n5" },
                    label = { Text("JLPT N5") },
                    modifier = Modifier.testTag("exam_tab_n5")
                )
                FilterChip(
                    selected = activeTab == "n4",
                    onClick = { activeTab = "n4" },
                    label = { Text("JLPT N4") },
                    modifier = Modifier.testTag("exam_tab_n4")
                )
            }
        }

        when (activeTab) {
            "jft" -> {
                item {
                    Card(shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                        Column {
                            Box(modifier = Modifier.height(155.dp).fillMaxWidth()) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_exam_badge),
                                    contentDescription = "JFT-Basic পরীক্ষা কেন্দ্র",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))))
                                )
                                Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                                    Surface(color = XpGold, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = "অফিসিয়াল ফরম্যাট • ২৫০ স্কেল • পাস মার্ক ২০০",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "JFT-Basic পূর্ণাঙ্গ মক পরীক্ষা",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("৬০ মিনিট সময় • ৬০টি র‍্যান্ডমাইজড প্রশ্ন (প্রতিবার নতুন প্রশ্ন সেট): স্ক্রিপ্ট ও শব্দভাণ্ডার (১৫), কথোপকথন (১৫), শ্রবণ (১৫), পঠন (১৫)।")
                                Button(
                                    onClick = {
                                        activeExam = ActiveExamSession(
                                            examType = "jft_full",
                                            title = "JFT-Basic পূর্ণাঙ্গ মক টেস্ট (৬০ প্রশ্ন)",
                                            durationMinutes = 60,
                                            questions = viewModel.buildJftQuestionsWithCustom()
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ToriiCrimson),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_jft_full_exam")
                                ) {
                                    Icon(Icons.Rounded.FactCheck, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("৬০ প্রশ্নের পূর্ণাঙ্গ পরীক্ষা শুরু করুন", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("বিভাগভিত্তিক JFT-Basic অনুশীলন (১৫ প্রশ্ন)", style = MaterialTheme.typography.titleMedium)
                }

                val jftCats = listOf(
                    Triple("Script & Vocabulary", "১. অক্ষর ও শব্দভাণ্ডার (文字と語彙)", Icons.Rounded.Translate),
                    Triple("Conversation & Expression", "২. কথোপকথন ও অভিব্যক্তি (会話と表現)", Icons.Rounded.RecordVoiceOver),
                    Triple("Listening Comprehension", "৩. শ্রবণ অনুশীলন (聴解)", Icons.Rounded.Headphones),
                    Triple("Reading Comprehension", "৪. পঠন অনুশীলন (読解)", Icons.Rounded.MenuBook)
                )
                items(jftCats) { (catKey, label, icon) ->
                    ExamModuleCard(
                        title = label,
                        subtitle = "১৫টি প্রশ্ন • ১৫ মিনিট",
                        icon = icon,
                        accent = IndigoScholar,
                        onClick = {
                            activeExam = ActiveExamSession(
                                examType = "jft_section",
                                title = label,
                                durationMinutes = 15,
                                questions = IrodoriRepository.buildJftSectionQuestions(catKey, 15)
                            )
                        }
                    )
                }
            }

            else -> {
                val level = if (activeTab == "n5") "N5" else "N4"
                val accent = if (level == "N5") EmeraldSuccess else XpGold
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("JLPT $level একাডেমি ও মক পরীক্ষা", style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (level == "N5") "জাপানি ভাষার প্রাথমিক ভিত্তি: শব্দভাণ্ডার, ব্যাকরণ, N5 কাঞ্জি, পঠন ও শ্রবণ।"
                                else "মধ্যবর্তী স্তরের প্রস্তুতি: সম্মানসূচক ভাষা, জটিল ব্যাকরণ, N4 কাঞ্জি ও দীর্ঘ অনুচ্ছেদ।",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    activeExam = ActiveExamSession(
                                        examType = "jlpt_${level.lowercase()}",
                                        title = "JLPT $level পূর্ণাঙ্গ মক পরীক্ষা (৩০ প্রশ্ন)",
                                        durationMinutes = 35,
                                        questions = IrodoriRepository.buildJlptQuestions(level, "full", 30)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_jlpt_${level}_mock")
                            ) {
                                Icon(Icons.Rounded.School, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("JLPT $level পূর্ণাঙ্গ মক টেস্ট দিন", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                val jlptModules = listOf(
                    Triple("vocabulary", "শব্দভাণ্ডার অনুশীলন (Vocabulary)", Icons.Rounded.Translate),
                    Triple("grammar", "ব্যাকরণ অনুশীলন (Grammar)", Icons.Rounded.AutoStories),
                    Triple("kanji", "কাঞ্জি পরীক্ষা (Kanji)", Icons.Rounded.School),
                    Triple("reading", "পঠন অনুশীলন (Reading)", Icons.Rounded.MenuBook),
                    Triple("listening", "শ্রবণ অনুশীলন (Listening)", Icons.Rounded.Headphones)
                )
                items(jlptModules) { (modKey, label, icon) ->
                    ExamModuleCard(
                        title = "$level $label",
                        subtitle = "১৫টি প্রশ্ন • ১৫ মিনিট • বিস্তারিত বাংলা ব্যাখ্যা",
                        icon = icon,
                        accent = IndigoScholar,
                        onClick = {
                            activeExam = ActiveExamSession(
                                examType = "jlpt_${level.lowercase()}_$modKey",
                                title = "JLPT $level — $label",
                                durationMinutes = 15,
                                questions = IrodoriRepository.buildJlptQuestions(level, modKey, 15)
                            )
                        }
                    )
                }
            }
        }

        // Previous Exam History from Firestore
        if (examAttempts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("পূর্ববর্তী পরীক্ষার ফলাফল", style = MaterialTheme.typography.titleLarge)
            }
            items(examAttempts.take(8), key = { it.id }) { attempt ->
                val pct = if (attempt.totalQuestions > 0) (attempt.score * 100) / attempt.totalQuestions else 0
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(attempt.title, style = MaterialTheme.typography.titleSmall)
                            Text("সময়: ${attempt.durationSeconds / 60} মি. ${attempt.durationSeconds % 60} সে.", style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (attempt.passed) EmeraldSuccess.copy(alpha = 0.14f) else SakuraPink.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${attempt.score}/${attempt.totalQuestions} ($pct%)",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (attempt.passed) EmeraldSuccess else SakuraPink,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ExamModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "শুরু করুন")
        }
    }
}
