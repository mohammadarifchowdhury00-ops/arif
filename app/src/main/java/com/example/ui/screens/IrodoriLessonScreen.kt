package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.JapaneseSpeechManager
import com.example.data.curriculum.IrodoriLesson
import com.example.data.curriculum.IrodoriRepository
import com.example.data.model.LearningVideoRecord
import com.example.data.model.LessonProgressRecord
import com.example.ui.components.InteractiveExamRunner
import com.example.ui.components.JapaneseWordCard
import com.example.ui.components.WritingPracticeCanvas
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold
import com.example.ui.viewmodel.JournalViewModel
import kotlinx.coroutines.delay

private data class LessonSectionSpec(val key: String, val labelBn: String)

private val lessonSections = listOf(
    LessonSectionSpec("overview", "১. পরিচিতি"),
    LessonSectionSpec("cando", "২. লক্ষ্য"),
    LessonSectionSpec("vocab", "৩. শব্দভাণ্ডার"),
    LessonSectionSpec("pic_vocab", "৪. ছবি ও শব্দ"),
    LessonSectionSpec("grammar", "৫. ব্যাকরণ"),
    LessonSectionSpec("conversation", "৬. কথোপকথন"),
    LessonSectionSpec("listening", "৭. শ্রবণ"),
    LessonSectionSpec("reading", "৮. পঠন"),
    LessonSectionSpec("speaking", "৯. বলা"),
    LessonSectionSpec("writing", "১০. লেখা"),
    LessonSectionSpec("kanji", "১১. কাঞ্জি"),
    LessonSectionSpec("culture", "১২. সংস্কৃতি"),
    LessonSectionSpec("vocab_practice", "১৩. শব্দ অনুশীলন"),
    LessonSectionSpec("grammar_practice", "১৪. ব্যাকরণ চর্চা"),
    LessonSectionSpec("game", "১৫. গেম"),
    LessonSectionSpec("challenge", "১৬. চ্যালেঞ্জ"),
    LessonSectionSpec("results", "১৭. ফলাফল")
)

@Composable
fun IrodoriLessonScreen(
    bookId: String,
    lessonNumber: Int,
    progressRecord: LessonProgressRecord?,
    lessonVideos: List<LearningVideoRecord>,
    showFurigana: Boolean,
    showRomaji: Boolean,
    speechManager: JapaneseSpeechManager,
    viewModel: JournalViewModel,
    onNavigateLesson: (bookId: String, lessonNumber: Int) -> Unit,
    onBackToBook: () -> Unit
) {
    BackHandler { onBackToBook() }

    val lesson: IrodoriLesson = IrodoriRepository.getLesson(bookId, lessonNumber)
        ?: IrodoriRepository.allLessons.first()

    var currentSectionIdx by remember(bookId, lessonNumber) { mutableIntStateOf(0) }
    val completedSections = progressRecord?.completedSections.orEmpty()
    val currentSection = lessonSections[currentSectionIdx]

    fun markCurrentDone(
        vocabMastered: Int = 0,
        grammarScore: Int = 0,
        gameHighScore: Int = 0,
        challengePassed: Boolean = false
    ) {
        viewModel.saveLessonSection(
            bookId = lesson.bookId,
            lessonNumber = lesson.lessonNumber,
            sectionKey = currentSection.key,
            vocabMastered = vocabMastered,
            grammarScore = grammarScore,
            gameHighScore = gameHighScore,
            challengePassed = challengePassed
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Lesson Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToBook, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "তালিকায় ফিরুন")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পাঠ ${lesson.lessonNumber}: ${lesson.titleJp}",
                            fontFamily = JapaneseFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lesson.titleBn} • সম্পন্ন ${completedSections.size}/১৭ ধাপ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(
                            onClick = { if (lessonNumber > 1) onNavigateLesson(bookId, lessonNumber - 1) },
                            enabled = lessonNumber > 1
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "আগের পাঠ")
                        }
                        IconButton(
                            onClick = { if (lessonNumber < 18) onNavigateLesson(bookId, lessonNumber + 1) },
                            enabled = lessonNumber < 18
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "পরবর্তী পাঠ")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(lessonSections) { idx, sec ->
                        val done = completedSections.contains(sec.key)
                        FilterChip(
                            selected = idx == currentSectionIdx,
                            onClick = { currentSectionIdx = idx },
                            leadingIcon = if (done) {
                                { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp)) }
                            } else null,
                            label = { Text(sec.labelBn) },
                            modifier = Modifier.testTag("lesson_sec_tab_${sec.key}")
                        )
                    }
                }
            }
        }

        // Section Content
        Box(modifier = Modifier.weight(1f)) {
            when (currentSection.key) {
                "overview" -> OverviewSection(
                    lesson = lesson,
                    videos = lessonVideos,
                    onMarkDone = {
                        markCurrentDone()
                        if (currentSectionIdx < lessonSections.lastIndex) currentSectionIdx++
                    }
                )
                "cando" -> CanDoSection(
                    lesson = lesson,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "vocab" -> VocabSection(
                    lesson = lesson,
                    showFurigana = showFurigana,
                    showRomaji = showRomaji,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone(vocabMastered = lesson.vocab.size)
                        currentSectionIdx++
                    }
                )
                "pic_vocab" -> PictureVocabSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "grammar" -> GrammarSection(
                    lesson = lesson,
                    showFurigana = showFurigana,
                    showRomaji = showRomaji,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "conversation" -> ConversationSection(
                    lesson = lesson,
                    showFurigana = showFurigana,
                    showRomaji = showRomaji,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "listening" -> ListeningSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "reading" -> ReadingSection(
                    lesson = lesson,
                    showFurigana = showFurigana,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "speaking" -> SpeakingSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "writing" -> WritingSection(
                    lesson = lesson,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "kanji" -> KanjiFocusSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "culture" -> CultureSection(
                    lesson = lesson,
                    onMarkDone = {
                        markCurrentDone()
                        currentSectionIdx++
                    }
                )
                "vocab_practice" -> VocabPracticeSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = {
                        markCurrentDone(vocabMastered = lesson.vocab.size)
                        currentSectionIdx++
                    }
                )
                "grammar_practice" -> GrammarPracticeSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = { score ->
                        markCurrentDone(grammarScore = score)
                        currentSectionIdx++
                    }
                )
                "game" -> GameSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onMarkDone = { score ->
                        markCurrentDone(gameHighScore = score)
                        currentSectionIdx++
                    }
                )
                "challenge" -> ChallengeSection(
                    lesson = lesson,
                    speechManager = speechManager,
                    onChallengeCompleted = { score, total, passed ->
                        markCurrentDone(challengePassed = passed)
                        if (passed) {
                            viewModel.awardActivityXp(
                                activityKey = "challenge_${lesson.bookId}_${lesson.lessonNumber}",
                                sourceType = "lesson_challenge",
                                xpAmount = 50L,
                                description = "পাঠ ${lesson.lessonNumber} চ্যালেঞ্জ পাস ($score/$total)",
                                studyMinutesDelta = 4L,
                                badgeUnlock = if (lesson.lessonNumber == 18) "book_${lesson.bookId}_finisher" else null
                            )
                        }
                        currentSectionIdx = lessonSections.lastIndex
                    },
                    onExit = { currentSectionIdx = lessonSections.lastIndex }
                )
                "results" -> ResultsSection(
                    lesson = lesson,
                    completedSections = completedSections,
                    progressRecord = progressRecord,
                    onNextLesson = {
                        if (lessonNumber < 18) {
                            onNavigateLesson(bookId, lessonNumber + 1)
                        } else {
                            onBackToBook()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(
    lesson: IrodoriLesson,
    videos: List<LearningVideoRecord>,
    onMarkDone: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("১. পাঠ পরিচিতি (Lesson Overview)", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
                    Text(lesson.titleJp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.headlineSmall)
                    Text(lesson.titleBn, style = MaterialTheme.typography.titleMedium, color = IndigoScholar)
                    Text(lesson.overviewBn, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (videos.isNotEmpty()) {
            item {
                Text("এই পাঠের সংযুক্ত ভিডিও ক্লাস", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                videos.forEach { vid ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(vid.videoUrl))) }
                        }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PlayCircle, contentDescription = "ভিডিও", tint = ToriiCrimson)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(vid.title, style = MaterialTheme.typography.titleSmall)
                                Text(vid.descriptionBn, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCompleteButton("পরিচিতি সম্পন্ন ও পরবর্তী ধাপ", onMarkDone)
        }
    }
}

@Composable
private fun CanDoSection(lesson: IrodoriLesson, onMarkDone: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("২. ক্যান-ডু লক্ষ্যসমূহ (Can-do Goals)", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
            Text("এই পাঠ শেষে আপনি যা করতে পারবেন:", style = MaterialTheme.typography.bodyMedium)
        }
        itemsIndexed(lesson.canDoGoalsBn) { idx, goal ->
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MatchaJade)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("${idx + 1}. $goal", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item { SectionCompleteButton("লক্ষ্য বুঝেছি, শব্দভাণ্ডারে যান", onMarkDone) }
    }
}

@Composable
private fun VocabSection(
    lesson: IrodoriLesson,
    showFurigana: Boolean,
    showRomaji: Boolean,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("৩. শব্দভাণ্ডার (Vocabulary)", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
            Text("প্রতিটি শব্দের পাশে স্পিকার আইকনে চাপ দিয়ে জাপানি উচ্চারণ শুনুন।", style = MaterialTheme.typography.bodySmall)
        }
        itemsIndexed(lesson.vocab) { _, v ->
            JapaneseWordCard(vocab = v, showFurigana = showFurigana, showRomaji = showRomaji, speechManager = speechManager)
        }
        item { SectionCompleteButton("শব্দভাণ্ডার পড়া শেষ (+১৫ XP)", onMarkDone) }
    }
}

@Composable
private fun PictureVocabSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    val vocab = lesson.vocab
    var qIdx by remember(lesson) { mutableIntStateOf(0) }
    var selectedOpt by remember(qIdx, lesson) { mutableStateOf<Int?>(null) }
    val currentWord = vocab[qIdx % vocab.size]
    val options = remember(qIdx, lesson) {
        val distractors = vocab.filter { it.jp != currentWord.jp }.shuffled().take(3)
        (distractors + currentWord).shuffled()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("৪. ছবিসহ শব্দ চেনা (Picture Vocabulary)", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentWord.visualEmoji, fontSize = 68.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("অর্থ: ${currentWord.meaningBn}", style = MaterialTheme.typography.titleMedium, color = MatchaJade)
                Text("উপরের প্রতীকের সাথে মানানসই জাপানি শব্দটি বেছে নিন", style = MaterialTheme.typography.bodySmall)
            }
        }
        options.forEachIndexed { idx, opt ->
            val picked = selectedOpt == idx
            val correct = opt.jp == currentWord.jp
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when {
                    !picked -> MaterialTheme.colorScheme.surfaceContainerLowest
                    correct -> EmeraldSuccess.copy(alpha = 0.16f)
                    else -> ToriiCrimson.copy(alpha = 0.15f)
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().clickable {
                    selectedOpt = idx
                    speechManager.speak(opt.jp)
                }
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${opt.jp} (${opt.bengaliPron})", fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleMedium)
                    if (picked) Text(if (correct) "✅ সঠিক" else "❌ ভুল")
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { qIdx = (qIdx + 1) % vocab.size },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("পরের ছবি") }
            Button(
                onClick = onMarkDone,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MatchaJade)
            ) { Text("সম্পন্ন করুন") }
        }
    }
}

@Composable
private fun GrammarSection(
    lesson: IrodoriLesson,
    showFurigana: Boolean,
    showRomaji: Boolean,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("৫. ব্যাকরণ (Grammar Explanation)", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
        }
        itemsIndexed(lesson.grammar) { _, gp ->
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = IndigoScholar.copy(alpha = 0.12f)) {
                        Text(
                            text = gp.pattern,
                            fontFamily = JapaneseFontFamily,
                            style = MaterialTheme.typography.titleMedium,
                            color = IndigoScholar,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Text(gp.explanationBn, style = MaterialTheme.typography.bodyLarge)
                    Text("গঠন: ${gp.structure}", style = MaterialTheme.typography.labelLarge, color = MatchaJade)
                    Surface(shape = RoundedCornerShape(10.dp), color = ToriiCrimson.copy(alpha = 0.08f)) {
                        Text(
                            text = "সাধারণ ভুল সতর্কতা: ${gp.commonMistakeBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ToriiCrimson,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    gp.examples.forEach { ex ->
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.jp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleSmall)
                                    if (showFurigana) Text(ex.furigana, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodySmall, color = ToriiCrimson)
                                    if (showRomaji) Text("${ex.romaji} • ${ex.bengaliPron}", style = MaterialTheme.typography.bodySmall, color = IndigoScholar)
                                    Text("অর্থ: ${ex.meaningBn}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { speechManager.speak(ex.jp) }) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "শুনুন", tint = ToriiCrimson)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { SectionCompleteButton("ব্যাকরণ সম্পন্ন (+১৫ XP)", onMarkDone) }
    }
}

@Composable
private fun ConversationSection(
    lesson: IrodoriLesson,
    showFurigana: Boolean,
    showRomaji: Boolean,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("৬. কথোপকথন (Conversation)", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
                FilledTonalButton(onClick = { speechManager.speak(lesson.dialogue.joinToString(" ") { it.jp }) }) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পুরো সংলাপ শুনুন")
                }
            }
        }
        itemsIndexed(lesson.dialogue) { _, line ->
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(line.speaker, style = MaterialTheme.typography.labelMedium, color = ToriiCrimson, fontWeight = FontWeight.Bold)
                        Text(line.jp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleMedium)
                        if (showFurigana) Text(line.furigana, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (showRomaji) Text("${line.romaji} • ${line.bengaliPron}", style = MaterialTheme.typography.bodySmall, color = IndigoScholar)
                        Text("অর্থ: ${line.meaningBn}", style = MaterialTheme.typography.bodyMedium, color = MatchaJade)
                    }
                    IconButton(onClick = { speechManager.speak(line.jp) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "সংলাপ শুনুন", tint = ToriiCrimson)
                    }
                }
            }
        }
        item { SectionCompleteButton("কথোপকথন অনুশীলন সম্পন্ন", onMarkDone) }
    }
}

@Composable
private fun ListeningSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    var showTranscript by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("৭. শ্রবণ অনুশীলন (Listening)", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("সারাংশ: ${lesson.listeningSummaryBn}", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { speechManager.speak(lesson.listeningDialogueJp, slow = false) }, colors = ButtonDefaults.buttonColors(containerColor = ToriiCrimson)) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("স্বাভাবিক গতি")
                    }
                    OutlinedButton(onClick = { speechManager.speak(lesson.listeningDialogueJp, slow = true) }) {
                        Icon(Icons.Rounded.SlowMotionVideo, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ধীর গতি")
                    }
                }
                OutlinedButton(onClick = { showTranscript = !showTranscript }) {
                    Text(if (showTranscript) "জাপানি স্ক্রিপ্ট লুকান" else "জাপানি স্ক্রিপ্ট দেখুন")
                }
                if (showTranscript) {
                    Text(lesson.listeningDialogueJp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodyLarge, color = IndigoScholar)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        SectionCompleteButton("শ্রবণ ধাপ সম্পন্ন (+১৫ XP)", onMarkDone)
    }
}

@Composable
private fun ReadingSection(
    lesson: IrodoriLesson,
    showFurigana: Boolean,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    var showMeaning by remember { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("৮. পঠন অনুশীলন (Reading)", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(lesson.readingPassageJp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleMedium)
                if (showFurigana) {
                    Text(lesson.readingFurigana, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodySmall, color = ToriiCrimson)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = { speechManager.speak(lesson.readingPassageJp) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পড়ে শোনান")
                    }
                    OutlinedButton(onClick = { showMeaning = !showMeaning }) {
                        Text(if (showMeaning) "অর্থ লুকান" else "বাংলা অর্থ দেখুন")
                    }
                }
                if (showMeaning) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MatchaJade.copy(alpha = 0.1f)) {
                        Text("বাংলা অর্থ: ${lesson.readingTranslationBn}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        SectionCompleteButton("পঠন ধাপ সম্পন্ন (+১৫ XP)", onMarkDone)
    }
}

@Composable
private fun SpeakingSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(speechManager.hasRecording()) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            permissionMessage = null
            if (speechManager.startRecording()) {
                isRecording = true
            }
        } else {
            permissionMessage = "ভয়েস রেকর্ড করতে মাইক্রোফোন অনুমতি প্রয়োজন। আপনি তবুও শুনে মুখে অনুশীলন করতে পারেন।"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("৯. বলা অনুশীলন (Speaking Practice)", style = MaterialTheme.typography.titleLarge, color = SakuraPink)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("নিচের বাক্যটি শুনে জোরে বলুন এবং নিজের কণ্ঠ রেকর্ড করুন:", style = MaterialTheme.typography.bodyMedium)
                Text(lesson.speakingTargetJp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.headlineSmall, color = ToriiCrimson)
                Text("${lesson.speakingTargetRomaji} • অর্থ: ${lesson.speakingTargetBn}", style = MaterialTheme.typography.bodyMedium, color = IndigoScholar)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = { speechManager.speak(lesson.speakingTargetJp) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মডেল অডিও")
                    }
                    Button(
                        onClick = {
                            if (isRecording) {
                                hasRecorded = speechManager.stopRecording()
                                isRecording = false
                            } else {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    if (speechManager.startRecording()) isRecording = true
                                } else {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) ToriiCrimson else MatchaJade),
                        modifier = Modifier.testTag("speaking_record_button")
                    ) {
                        Icon(if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRecording) "রেকর্ড থামান" else "ভয়েস রেকর্ড করুন")
                    }
                }
                if (hasRecorded && !isRecording) {
                    OutlinedButton(onClick = { speechManager.playRecording() }) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নিজের রেকর্ডিং শুনুন")
                    }
                }
                if (permissionMessage != null) {
                    Text(permissionMessage!!, style = MaterialTheme.typography.bodySmall, color = ToriiCrimson)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        SectionCompleteButton("বলা অনুশীলন সম্পন্ন (+১৫ XP)", onMarkDone)
    }
}

@Composable
private fun WritingSection(lesson: IrodoriLesson, onMarkDone: () -> Unit) {
    val chars = lesson.writingFocusChars.ifEmpty { listOf("あ", "い") }
    var idx by remember(lesson) { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("১০. লেখা অনুশীলন (Writing Practice)", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chars.forEachIndexed { i, ch ->
                FilterChip(selected = i == idx, onClick = { idx = i }, label = { Text(ch, fontFamily = JapaneseFontFamily) })
            }
        }
        WritingPracticeCanvas(
            guideCharacter = chars[idx],
            hintBn = "সঠিক দিক ও স্ট্রোক অনুসরণ করে লিখুন (${idx + 1}/${chars.size})",
            onPracticeDone = {
                if (idx < chars.lastIndex) idx++ else onMarkDone()
            }
        )
    }
}

@Composable
private fun KanjiFocusSection(lesson: IrodoriLesson, speechManager: JapaneseSpeechManager, onMarkDone: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("১১. পাঠের কাঞ্জি (Kanji Focus)", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
        }
        itemsIndexed(lesson.kanjiFocus) { _, kj ->
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = ToriiCrimson.copy(alpha = 0.12f), modifier = Modifier.size(56.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(kj.kanji, fontSize = 30.sp, fontFamily = JapaneseFontFamily, color = ToriiCrimson, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("অর্থ: ${kj.meaningBn}", style = MaterialTheme.typography.titleMedium)
                                Text("ওনিওমি: ${kj.onyomi} | কুনিওমি: ${kj.kunyomi}", style = MaterialTheme.typography.bodySmall, color = IndigoScholar)
                                Text("${kj.romaji} • ${kj.bengaliPron}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { speechManager.speak(kj.exampleSentenceJp) }) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "উচ্চারণ শুনুন", tint = ToriiCrimson)
                        }
                    }
                    Text("মনে রাখার কৌশল: ${kj.mnemonicBn}", style = MaterialTheme.typography.bodySmall, color = MatchaJade)
                    Text("বাক্য: ${kj.exampleSentenceJp} (${kj.exampleSentenceBn})", fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { SectionCompleteButton("কাঞ্জি ধাপ সম্পন্ন", onMarkDone) }
    }
}

@Composable
private fun CultureSection(lesson: IrodoriLesson, onMarkDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("১২. জাপানি জীবন ও সংস্কৃতি", style = MaterialTheme.typography.titleLarge, color = MatchaJade)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(lesson.cultureTitleBn, style = MaterialTheme.typography.titleMedium, color = IndigoScholar, fontWeight = FontWeight.Bold)
                Text(lesson.cultureNoteBn, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        SectionCompleteButton("সংস্কৃতি পড়া শেষ (+১৫ XP)", onMarkDone)
    }
}

@Composable
private fun VocabPracticeSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: () -> Unit
) {
    var mode by remember { mutableIntStateOf(0) } // 0: Flashcard, 1: JP->BN, 2: BN->JP, 3: Listening
    var idx by remember(lesson, mode) { mutableIntStateOf(0) }
    var flipped by remember(idx, mode) { mutableStateOf(false) }
    var picked by remember(idx, mode) { mutableStateOf<Int?>(null) }

    val words = lesson.vocab
    val word = words[idx % words.size]
    val options = remember(idx, mode) {
        val dist = words.filter { it.jp != word.jp }.shuffled().take(3)
        (dist + word).shuffled()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("১৩. শব্দভাণ্ডার অনুশীলন", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val modes = listOf("ফ্ল্যাশকার্ড", "JP → বাংলা", "বাংলা → JP", "শুনে চেনা")
            itemsIndexed(modes) { i, m ->
                FilterChip(selected = mode == i, onClick = { mode = i }, label = { Text(m) })
            }
        }

        if (mode == 0) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth().height(200.dp).clickable {
                    flipped = !flipped
                    speechManager.speak(word.jp)
                }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (!flipped) word.jp else word.meaningBn, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.headlineMedium, color = ToriiCrimson)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (!flipped) "অর্থ দেখতে কার্ডে ট্যাপ করুন" else "${word.romaji} (${word.bengaliPron})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { idx = (idx + 1) % words.size }, modifier = Modifier.weight(1f)) { Text("পরবর্তী কার্ড") }
                Button(onClick = onMarkDone, modifier = Modifier.weight(1f)) { Text("সম্পন্ন") }
            }
        } else {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (mode) {
                        1 -> Text("${word.jp} (${word.furigana}) এর বাংলা অর্থ কী?", fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleMedium)
                        2 -> Text("'${word.meaningBn}' এর জাপানি শব্দ কোনটি?", style = MaterialTheme.typography.titleMedium)
                        else -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("অডিও শুনে সঠিক শব্দ বেছে নিন", style = MaterialTheme.typography.titleMedium)
                                FilledTonalButton(onClick = { speechManager.speak(word.jp) }) {
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
                                    Text(" শুনুন")
                                }
                            }
                        }
                    }
                }
            }
            options.forEachIndexed { oIdx, opt ->
                val isCorrect = opt.jp == word.jp
                val chosen = picked == oIdx
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        !chosen -> MaterialTheme.colorScheme.surfaceContainerLowest
                        isCorrect -> EmeraldSuccess.copy(alpha = 0.16f)
                        else -> ToriiCrimson.copy(alpha = 0.14f)
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().clickable { picked = oIdx }
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (mode == 1) opt.meaningBn else "${opt.jp} (${opt.bengaliPron})", fontFamily = JapaneseFontFamily)
                        if (chosen) Text(if (isCorrect) "✅" else "❌")
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { idx = (idx + 1) % words.size }, modifier = Modifier.weight(1f)) { Text("পরের প্রশ্ন") }
                Button(onClick = onMarkDone, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MatchaJade)) { Text("অনুশীলন শেষ") }
            }
        }
    }
}

@Composable
private fun GrammarPracticeSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: (Int) -> Unit
) {
    val grammarQs = remember(lesson) {
        lesson.questions.filter { it.category.contains("ব্যাকরণ") || it.category.contains("Grammar") }
            .ifEmpty { lesson.questions.take(2) }
    }
    InteractiveExamRunner(
        title = "১৪. ব্যাকরণ অনুশীলন",
        questions = grammarQs,
        durationMinutes = 5,
        speechManager = speechManager,
        onExit = { onMarkDone(100) },
        onCompleted = { score, total, _, _, _ ->
            val pct = if (total > 0) (score * 100) / total else 100
            onMarkDone(pct)
        }
    )
}

@Composable
private fun GameSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onMarkDone: (Int) -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(30) }
    var score by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val words = lesson.vocab
    val target = words[round % words.size]
    val choices = remember(round, lesson) {
        (words.filter { it.jp != target.jp }.shuffled().take(3) + target).shuffled()
    }

    LaunchedEffect(finished) {
        while (!finished && timeLeft > 0) {
            delay(1000L)
            timeLeft--
            if (timeLeft <= 0) finished = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("১৫. ভাষা স্পিড ম্যাচ গেম 🎮", style = MaterialTheme.typography.titleLarge, color = XpGold)
        if (finished) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = XpGold.copy(alpha = 0.14f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("গেম সমাপ্ত!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("আপনার স্কোর: $score পয়েন্ট", style = MaterialTheme.typography.titleLarge, color = ToriiCrimson)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onMarkDone(score) }) { Text("স্কোর সেভ করুন ও পরবর্তী ধাপে যান") }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("সময়: $timeLeft সেকেন্ড", style = MaterialTheme.typography.titleMedium, color = ToriiCrimson)
                Text("স্কোর: $score", style = MaterialTheme.typography.titleMedium, color = MatchaJade)
            }
            LinearProgressIndicator(
                progress = { timeLeft / 30f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = XpGold
            )
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(target.visualEmoji, fontSize = 42.sp)
                    Text(target.jp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.headlineMedium, color = IndigoScholar)
                    Text("দ্রুত সঠিক বাংলা অর্থ ট্যাপ করুন!", style = MaterialTheme.typography.bodySmall)
                }
            }
            choices.forEach { ch ->
                Button(
                    onClick = {
                        if (ch.jp == target.jp) {
                            score += 10
                            speechManager.speak(target.jp)
                        }
                        round++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(ch.meaningBn, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun ChallengeSection(
    lesson: IrodoriLesson,
    speechManager: JapaneseSpeechManager,
    onChallengeCompleted: (score: Int, total: Int, passed: Boolean) -> Unit,
    onExit: () -> Unit
) {
    InteractiveExamRunner(
        title = "১৬. পাঠ ${lesson.lessonNumber} চ্যালেঞ্জ",
        questions = lesson.questions,
        durationMinutes = 8,
        speechManager = speechManager,
        onExit = onExit,
        onCompleted = { score, total, _, _, passed ->
            onChallengeCompleted(score, total, passed)
        }
    )
}

@Composable
private fun ResultsSection(
    lesson: IrodoriLesson,
    completedSections: List<String>,
    progressRecord: LessonProgressRecord?,
    onNextLesson: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("১৭. পাঠের ফলাফল ও সারসংক্ষেপ", style = MaterialTheme.typography.titleLarge, color = EmeraldSuccess)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("পাঠ ${lesson.lessonNumber}: ${lesson.titleBn}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("সম্পন্ন ধাপ: ${completedSections.size} / ১৭", style = MaterialTheme.typography.titleSmall, color = IndigoScholar)
                Text("শব্দ আয়ত্ত: ${progressRecord?.vocabMastered ?: 0} • গেম সেরা স্কোর: ${progressRecord?.gameHighScore ?: 0}")
                Text(
                    text = if (progressRecord?.challengePassed == true) "চ্যালেঞ্জ স্ট্যাটাস: উত্তীর্ণ ✅" else "চ্যালেঞ্জ স্ট্যাটাস: ১৬ নং ধাপে চ্যালেঞ্জ পরীক্ষা দিন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progressRecord?.challengePassed == true) EmeraldSuccess else ToriiCrimson
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNextLesson,
            colors = ButtonDefaults.buttonColors(containerColor = ToriiCrimson),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("next_lesson_button")
        ) {
            Text(if (lesson.lessonNumber < 18) "পরবর্তী পাঠে যান (পাঠ ${lesson.lessonNumber + 1})" else "বইয়ের তালিকায় ফিরুন", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SectionCompleteButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MatchaJade),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("section_complete_button")
    ) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
