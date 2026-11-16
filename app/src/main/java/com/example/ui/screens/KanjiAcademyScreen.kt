package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.JapaneseSpeechManager
import com.example.data.curriculum.KanjiItem
import com.example.data.curriculum.KanjiRepository
import com.example.data.curriculum.PracticeQuestion
import com.example.data.model.ScriptMasteryRecord
import com.example.ui.components.InteractiveExamRunner
import com.example.ui.components.WritingPracticeCanvas
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold
import com.example.ui.viewmodel.JournalViewModel

private enum class KanjiQuizMode {
    NONE, MEANING, READING, TIMED
}

@Composable
fun KanjiAcademyScreen(
    scriptMastery: List<ScriptMasteryRecord>,
    speechManager: JapaneseSpeechManager,
    viewModel: JournalViewModel,
    onSwitchToKana: (String) -> Unit = {}
) {
    var selectedLevel by remember { mutableStateOf("N5") }
    var searchQuery by remember { mutableStateOf("") }
    var writingKanji by remember { mutableStateOf<KanjiItem?>(null) }
    var quizMode by remember { mutableStateOf(KanjiQuizMode.NONE) }

    val activePool = if (selectedLevel == "N5") KanjiRepository.n5Kanji else KanjiRepository.n4Kanji
    val filteredList = remember(activePool, searchQuery) {
        if (searchQuery.isBlank()) activePool
        else activePool.filter {
            it.kanji.contains(searchQuery, true) ||
                it.meaningBn.contains(searchQuery, true) ||
                it.romaji.contains(searchQuery, true) ||
                it.bengaliPron.contains(searchQuery, true)
        }
    }

    val masteredMap = scriptMastery.filter { it.scriptType == "kanji" }.associateBy { it.character }
    val totalMastered = KanjiRepository.allKanji.count { masteredMap[it.kanji]?.mastered == true }

    if (quizMode != KanjiQuizMode.NONE) {
        BackHandler { quizMode = KanjiQuizMode.NONE }
        val qs = remember(selectedLevel, quizMode) { buildKanjiQuiz(activePool, quizMode) }
        InteractiveExamRunner(
            title = "JLPT $selectedLevel কাঞ্জি পরীক্ষা",
            questions = qs,
            durationMinutes = if (quizMode == KanjiQuizMode.TIMED) 3 else 6,
            speechManager = speechManager,
            onExit = { quizMode = KanjiQuizMode.NONE },
            onCompleted = { score, total, durationSec, breakdown, passed ->
                viewModel.submitExamResult(
                    examType = "kanji_${selectedLevel.lowercase()}",
                    title = "কাঞ্জি $selectedLevel অনুশীলন",
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

    if (writingKanji != null) {
        BackHandler { writingKanji = null }
        val kj = writingKanji!!
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("কাঞ্জি লেখা অনুশীলন: ${kj.kanji} (${kj.meaningBn})", style = MaterialTheme.typography.titleLarge, color = IndigoScholar)
            WritingPracticeCanvas(
                guideCharacter = kj.kanji,
                hintBn = "${kj.strokes} স্ট্রোক • ${kj.mnemonicBn}",
                onPracticeDone = {
                    viewModel.recordScriptPractice("kanji", kj.kanji, true)
                    writingKanji = null
                }
            )
            OutlinedButton(onClick = { writingKanji = null }) { Text("তালিকায় ফিরুন") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("কাঞ্জি একাডেমি (漢字)", style = MaterialTheme.typography.headlineMedium, color = IndigoScholar)
            Text(
                text = "JLPT N5 ও N4 কাঞ্জি — ওনিওমি, কুনিওমি, স্মৃতি কৌশল ও বাক্যে ব্যবহার",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { onSwitchToKana("hiragana") }, label = { Text("হিরাগানা (あ)") })
                FilterChip(selected = false, onClick = { onSwitchToKana("katakana") }, label = { Text("কাতাকানা (ア)") })
                FilterChip(selected = true, onClick = {}, label = { Text("কাঞ্জি (漢字)") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedLevel == "N5",
                    onClick = { selectedLevel = "N5" },
                    label = { Text("JLPT N5 (${KanjiRepository.n5Kanji.size})") },
                    modifier = Modifier.testTag("kanji_n5_tab")
                )
                FilterChip(
                    selected = selectedLevel == "N4",
                    onClick = { selectedLevel = "N4" },
                    label = { Text("JLPT N4 (${KanjiRepository.n4Kanji.size})") },
                    modifier = Modifier.testTag("kanji_n4_tab")
                )
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট কাঞ্জি দক্ষতা", style = MaterialTheme.typography.titleSmall)
                        Text("$totalMastered / ${KanjiRepository.allKanji.size} কাঞ্জি", color = MatchaJade, style = MaterialTheme.typography.titleSmall)
                    }
                    LinearProgressIndicator(
                        progress = { totalMastered.toFloat() / KanjiRepository.allKanji.size.coerceAtLeast(1).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MatchaJade
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Button(onClick = { quizMode = KanjiQuizMode.MEANING }, colors = ButtonDefaults.buttonColors(containerColor = ToriiCrimson)) {
                        Text("কাঞ্জি → অর্থ কুইজ")
                    }
                }
                item {
                    Button(onClick = { quizMode = KanjiQuizMode.READING }, colors = ButtonDefaults.buttonColors(containerColor = IndigoScholar)) {
                        Text("উচ্চারণ কুইজ")
                    }
                }
                item {
                    Button(onClick = { quizMode = KanjiQuizMode.TIMED }, colors = ButtonDefaults.buttonColors(containerColor = XpGold)) {
                        Text("স্পিড চ্যালেঞ্জ")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("কাঞ্জি, অর্থ বা উচ্চারণ খুঁজুন...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredList, key = { "${it.jlptLevel}_${it.kanji}" }) { kj ->
            val isMastered = masteredMap[kj.kanji]?.mastered == true
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isMastered) EmeraldSuccess.copy(alpha = 0.14f) else ToriiCrimson.copy(alpha = 0.12f),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = kj.kanji,
                                        fontSize = 34.sp,
                                        fontFamily = JapaneseFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMastered) EmeraldSuccess else ToriiCrimson
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("${kj.meaningBn} (${kj.strokes} স্ট্রোক)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("ওনিওমি: ${kj.onyomi} | কুনিওমি: ${kj.kunyomi}", style = MaterialTheme.typography.bodySmall, color = IndigoScholar)
                                Text("${kj.romaji} • ${kj.bengaliPron}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { speechManager.speak(kj.exampleSentenceJp) }) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "উচ্চারণ শুনুন", tint = ToriiCrimson)
                        }
                    }

                    Text("স্মৃতি কৌশল: ${kj.mnemonicBn}", style = MaterialTheme.typography.bodySmall, color = MatchaJade)
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("উদাহরণ বাক্য: ${kj.exampleSentenceJp}", fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodyMedium)
                            Text("${kj.exampleSentenceFurigana} → ${kj.exampleSentenceBn}", style = MaterialTheme.typography.bodySmall)
                            kj.examples.forEach { ex ->
                                Text("• ${ex.wordJp} (${ex.furigana} / ${ex.bengaliPron}) = ${ex.meaningBn}", fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { writingKanji = kj }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("লিখুন")
                        }
                        Button(
                            onClick = { viewModel.recordScriptPractice("kanji", kj.kanji, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isMastered) EmeraldSuccess else MatchaJade),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isMastered) "আয়ত্তকৃত ✓" else "আয়ত্ত (+১০ XP)")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private fun buildKanjiQuiz(pool: List<KanjiItem>, mode: KanjiQuizMode): List<PracticeQuestion> {
    return pool.shuffled().take(15).mapIndexed { i, kj ->
        val dist = pool.filter { it.kanji != kj.kanji }.shuffled().take(3)
        val opts = (dist + kj).shuffled()
        when (mode) {
            KanjiQuizMode.READING -> PracticeQuestion(
                id = "kj_r_$i",
                category = "কাঞ্জি উচ্চারণ",
                promptJp = "${kj.kanji} — ${kj.meaningBn}",
                promptBn = "「${kj.kanji}」 কাঞ্জির সঠিক ওনিওমি/কুনিওমি ও বাংলা উচ্চারণ কোনটি?",
                audioTextJp = kj.exampleSentenceJp,
                options = opts.map { "${it.onyomi} / ${it.kunyomi} (${it.bengaliPron})" },
                correctIndex = opts.indexOf(kj),
                explanationBn = "「${kj.kanji}」 (${kj.bengaliPron}) অর্থ '${kj.meaningBn}'। উদাহরণ: ${kj.exampleSentenceJp}"
            )
            else -> PracticeQuestion(
                id = "kj_m_$i",
                category = "কাঞ্জি অর্থ",
                promptJp = "${kj.kanji} (${kj.exampleSentenceJp})",
                promptBn = "「${kj.kanji}」 কাঞ্জির সঠিক বাংলা অর্থ কোনটি?",
                audioTextJp = kj.exampleSentenceJp,
                options = opts.map { it.meaningBn },
                correctIndex = opts.indexOf(kj),
                explanationBn = "「${kj.kanji}」 অর্থ '${kj.meaningBn}'। কৌশল: ${kj.mnemonicBn}"
            )
        }
    }
}
