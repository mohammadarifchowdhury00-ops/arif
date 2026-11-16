package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Quiz
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.JapaneseSpeechManager
import com.example.data.curriculum.KanaChar
import com.example.data.curriculum.KanaGroup
import com.example.data.curriculum.KanaRepository
import com.example.data.curriculum.PracticeQuestion
import com.example.data.model.ScriptMasteryRecord
import com.example.ui.components.InteractiveExamRunner
import com.example.ui.components.WritingPracticeCanvas
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.SakuraPink
import com.example.ui.theme.StreakCoral
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold
import com.example.ui.viewmodel.JournalViewModel

private enum class KanaPracticeMode {
    NONE, CHAR_ID, AUDIO_REC, WORD_READING, TIMED_QUIZ, FULL_EXAM
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KanaAcademyScreen(
    initialScript: String = "hiragana",
    scriptMastery: List<ScriptMasteryRecord>,
    speechManager: JapaneseSpeechManager,
    viewModel: JournalViewModel,
    onSwitchToKanji: () -> Unit = {}
) {
    var activeScript by remember(initialScript) { mutableStateOf(initialScript) }
    var selectedGroup by remember(activeScript) { mutableStateOf(KanaGroup.BASIC) }
    var selectedChar by remember(activeScript) {
        mutableStateOf(
            if (activeScript == "hiragana") KanaRepository.hiraganaAll.first()
            else KanaRepository.katakanaAll.first()
        )
    }
    var showWritingCanvas by remember { mutableStateOf(false) }
    var practiceMode by remember { mutableStateOf(KanaPracticeMode.NONE) }

    if (practiceMode != KanaPracticeMode.NONE) {
        BackHandler { practiceMode = KanaPracticeMode.NONE }
        val chars = if (activeScript == "hiragana") KanaRepository.hiraganaAll else KanaRepository.katakanaAll
        val qCount = if (practiceMode == KanaPracticeMode.FULL_EXAM) 25 else 12
        val durationMin = if (practiceMode == KanaPracticeMode.FULL_EXAM) 10 else 4
        val questions = remember(activeScript, practiceMode) {
            buildKanaQuestions(chars, practiceMode, qCount)
        }
        InteractiveExamRunner(
            title = "${if (activeScript == "hiragana") "হিরাগানা" else "কাতাকানা"} অনুশীলন",
            questions = questions,
            durationMinutes = durationMin,
            speechManager = speechManager,
            onExit = { practiceMode = KanaPracticeMode.NONE },
            onCompleted = { score, total, durationSec, breakdown, passed ->
                viewModel.submitExamResult(
                    examType = "${activeScript}_quiz",
                    title = "${if (activeScript == "hiragana") "হিরাগানা" else "কাতাকানা"} পরীক্ষা",
                    score = score,
                    totalQuestions = total,
                    durationSeconds = durationSec,
                    categoryBreakdown = breakdown,
                    passed = passed
                )
                if (passed) {
                    chars.take(6).forEach {
                        viewModel.recordScriptPractice(activeScript, it.char, true)
                    }
                }
            }
        )
        return
    }

    val allChars = if (activeScript == "hiragana") KanaRepository.hiraganaAll else KanaRepository.katakanaAll
    val filteredChars = allChars.filter { it.group == selectedGroup }
    val masteryMap = scriptMastery
        .filter { it.scriptType == activeScript }
        .associateBy { it.character }
    val masteredCount = allChars.count { masteryMap[it.char]?.mastered == true }
    val accentColor = if (activeScript == "hiragana") SakuraPink else StreakCoral

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (activeScript == "hiragana") "হিরাগানা একাডেমি (ひらがな)" else "কাতাকানা একাডেমি (カタカナ)",
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor
            )
            Text(
                text = "মৌলিক ৪৬ অক্ষর, দাকুতেন, হানদাকুতেন, যুক্তাক্ষর এবং বিশেষ নিয়ম",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeScript == "hiragana",
                    onClick = { activeScript = "hiragana" },
                    label = { Text("হিরাগানা (あ)") },
                    modifier = Modifier.testTag("switch_hiragana_tab")
                )
                FilterChip(
                    selected = activeScript == "katakana",
                    onClick = { activeScript = "katakana" },
                    label = { Text("কাতাকানা (ア)") },
                    modifier = Modifier.testTag("switch_katakana_tab")
                )
                FilterChip(
                    selected = false,
                    onClick = onSwitchToKanji,
                    label = { Text("কাঞ্জি (漢字)") },
                    modifier = Modifier.testTag("switch_kanji_tab")
                )
            }
        }

        // Mastery Progress Card
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("অক্ষর আয়ত্ত অগ্রগতি", style = MaterialTheme.typography.titleSmall)
                        Text("$masteredCount / ${allChars.size} অক্ষর", style = MaterialTheme.typography.titleSmall, color = MatchaJade)
                    }
                    LinearProgressIndicator(
                        progress = { masteredCount.toFloat() / allChars.size.coerceAtLeast(1).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MatchaJade
                    )
                }
            }
        }

        // Practice Modes Row
        item {
            Text("অনুশীলন মোডসমূহ", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Button(
                        onClick = { practiceMode = KanaPracticeMode.CHAR_ID },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.testTag("kana_quiz_char_id")
                    ) {
                        Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অক্ষর চেনা কুইজ")
                    }
                }
                item {
                    Button(
                        onClick = { practiceMode = KanaPracticeMode.AUDIO_REC },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoScholar)
                    ) { Text("শুনে চেনা (Audio)") }
                }
                item {
                    Button(
                        onClick = { practiceMode = KanaPracticeMode.WORD_READING },
                        colors = ButtonDefaults.buttonColors(containerColor = MatchaJade)
                    ) { Text("শব্দ পড়া") }
                }
                item {
                    Button(
                        onClick = { practiceMode = KanaPracticeMode.FULL_EXAM },
                        colors = ButtonDefaults.buttonColors(containerColor = XpGold)
                    ) { Text("পূর্ণাঙ্গ কানা পরীক্ষা") }
                }
            }
        }

        // Selected Character Detail + Writing Canvas
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = accentColor.copy(alpha = 0.14f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedChar.char,
                                        fontSize = 36.sp,
                                        fontFamily = JapaneseFontFamily,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "${selectedChar.romaji} • ${selectedChar.bengaliPron}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = IndigoScholar
                                )
                                Text(
                                    text = "উদাহরণ: ${selectedChar.exampleWordJp} (${selectedChar.exampleRomaji}) = ${selectedChar.exampleBn}",
                                    fontFamily = JapaneseFontFamily,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "লেখার টিপস: ${selectedChar.strokeTipBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { speechManager.speak(selectedChar.char) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "উচ্চারণ শুনুন", tint = accentColor)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showWritingCanvas = !showWritingCanvas },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showWritingCanvas) "বোর্ড বন্ধ করুন" else "হাতে লিখে চর্চা")
                        }
                        Button(
                            onClick = {
                                viewModel.recordScriptPractice(activeScript, selectedChar.char, true)
                                speechManager.speak(selectedChar.exampleWordJp)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MatchaJade),
                            modifier = Modifier.weight(1f).testTag("mark_kana_mastered")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আয়ত্ত (+১০ XP)")
                        }
                    }

                    if (showWritingCanvas) {
                        WritingPracticeCanvas(
                            guideCharacter = selectedChar.char,
                            hintBn = selectedChar.strokeTipBn,
                            onPracticeDone = {
                                viewModel.recordScriptPractice(activeScript, selectedChar.char, true)
                                showWritingCanvas = false
                            }
                        )
                    }
                }
            }
        }

        // Group Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(KanaGroup.entries) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group.labelBn) }
                    )
                }
            }
        }

        // Interactive Kana Grid
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredChars.forEach { kc ->
                    val isSelected = kc.char == selectedChar.char
                    val isMastered = masteryMap[kc.char]?.mastered == true
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isSelected -> accentColor.copy(alpha = 0.16f)
                            isMastered -> EmeraldSuccess.copy(alpha = 0.10f)
                            else -> MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .size(width = 66.dp, height = 74.dp)
                            .clickable {
                                selectedChar = kc
                                speechManager.speak(kc.char)
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = kc.char,
                                fontFamily = JapaneseFontFamily,
                                fontSize = if (kc.char.length > 2) 15.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMastered) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = kc.romaji,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private fun buildKanaQuestions(
    chars: List<KanaChar>,
    mode: KanaPracticeMode,
    count: Int
): List<PracticeQuestion> {
    return chars.shuffled().take(count).mapIndexed { idx, kc ->
        val distractors = chars.filter { it.romaji != kc.romaji }.shuffled().take(3)
        when (mode) {
            KanaPracticeMode.AUDIO_REC -> {
                val opts = (distractors + kc).shuffled()
                PracticeQuestion(
                    id = "kana_aud_$idx",
                    category = "অডিও শনাক্তকরণ",
                    promptJp = "অডিও বাজান ও অক্ষর চিনুন",
                    promptBn = "শোনা উচ্চারণের সঠিক জাপানি অক্ষর কোনটি?",
                    audioTextJp = kc.char,
                    options = opts.map { "${it.char} (${it.bengaliPron})" },
                    correctIndex = opts.indexOf(kc),
                    explanationBn = "「${kc.char}」 এর উচ্চারণ '${kc.romaji}' (${kc.bengaliPron})। উদাহরণ: ${kc.exampleWordJp} = ${kc.exampleBn}"
                )
            }
            KanaPracticeMode.WORD_READING -> {
                val opts = (distractors + kc).shuffled()
                PracticeQuestion(
                    id = "kana_wd_$idx",
                    category = "শব্দ পড়া",
                    promptJp = kc.exampleWordJp,
                    promptBn = "'${kc.exampleWordJp}' শব্দের সঠিক অর্থ ও উচ্চারণ কোনটি?",
                    audioTextJp = kc.exampleWordJp,
                    options = opts.map { "${it.exampleBn} (${it.exampleRomaji})" },
                    correctIndex = opts.indexOf(kc),
                    explanationBn = "${kc.exampleWordJp} (${kc.exampleRomaji}) এর বাংলা অর্থ '${kc.exampleBn}'।"
                )
            }
            else -> {
                val opts = (distractors + kc).shuffled()
                PracticeQuestion(
                    id = "kana_id_$idx",
                    category = "অক্ষর পরিচয়",
                    promptJp = kc.char,
                    promptBn = "「${kc.char}」 অক্ষরটির সঠিক রোমাজি ও বাংলা উচ্চারণ কী?",
                    audioTextJp = kc.char,
                    options = opts.map { "${it.romaji} (${it.bengaliPron})" },
                    correctIndex = opts.indexOf(kc),
                    explanationBn = "「${kc.char}」 = ${kc.romaji} (${kc.bengaliPron})। স্ট্রোক টিপস: ${kc.strokeTipBn}"
                )
            }
        }
    }
}
