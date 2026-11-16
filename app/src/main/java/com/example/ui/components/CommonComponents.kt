package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.JapaneseSpeechManager
import com.example.data.curriculum.PracticeQuestion
import com.example.data.curriculum.VocabItem
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.ToriiCrimson
import com.example.ui.theme.XpGold
import kotlinx.coroutines.delay

@Composable
fun JapaneseWordCard(
    vocab: VocabItem,
    showFurigana: Boolean,
    showRomaji: Boolean,
    speechManager: JapaneseSpeechManager,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(vocab.visualEmoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (showFurigana && vocab.furigana != vocab.jp) {
                            Text(
                                text = vocab.furigana,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = JapaneseFontFamily,
                                color = ToriiCrimson
                            )
                        }
                        Text(
                            text = vocab.jp,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JapaneseFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        if (showRomaji) {
                            Text(
                                text = "${vocab.romaji} • ${vocab.bengaliPron}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IndigoScholar
                            )
                        } else {
                            Text(
                                text = vocab.bengaliPron,
                                style = MaterialTheme.typography.bodySmall,
                                color = IndigoScholar
                            )
                        }
                    }
                }
                Row {
                    IconButton(
                        onClick = { speechManager.speak(vocab.jp, slow = true) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SlowMotionVideo,
                            contentDescription = "ধীরে উচ্চারণ শুনুন",
                            tint = IndigoScholar
                        )
                    }
                    IconButton(
                        onClick = { speechManager.speak(vocab.jp, slow = false) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "জাপানি উচ্চারণ শুনুন",
                            tint = ToriiCrimson
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MatchaJade.copy(alpha = 0.09f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "অর্থ: ${vocab.meaningBn}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MatchaJade
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vocab.exampleJp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = JapaneseFontFamily
                    )
                    Text(
                        text = vocab.exampleBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WritingPracticeCanvas(
    guideCharacter: String,
    hintBn: String,
    onPracticeDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember(guideCharacter) { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember(guideCharacter) { mutableStateOf<List<Offset>>(emptyList()) }
    val strokeColor = ToriiCrimson
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "আঙুল দিয়ে নিচের বক্সে '$guideCharacter' লিখুন",
            style = MaterialTheme.typography.titleSmall,
            color = IndigoScholar
        )
        Text(
            text = hintBn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = guideCharacter,
                fontSize = 148.sp,
                fontFamily = JapaneseFontFamily,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(guideCharacter) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.isNotEmpty()) {
                                    strokes.add(currentStroke)
                                    currentStroke = emptyList()
                                }
                            }
                        )
                    }
            ) {
                // Guide crosshairs
                drawLine(gridColor, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2f)
                drawLine(gridColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2f)

                val allStrokes = strokes + if (currentStroke.isNotEmpty()) listOf(currentStroke) else emptyList()
                allStrokes.forEach { pts ->
                    if (pts.size > 1) {
                        val path = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            pts.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { strokes.clear(); currentStroke = emptyList() }) {
                Icon(Icons.Rounded.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("মুছে ফেলুন")
            }
            Button(
                onClick = { onPracticeDone() },
                enabled = strokes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MatchaJade)
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("লেখা সম্পন্ন")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveExamRunner(
    title: String,
    questions: List<PracticeQuestion>,
    durationMinutes: Int,
    speechManager: JapaneseSpeechManager,
    onExit: () -> Unit,
    onCompleted: (score: Int, total: Int, durationSec: Int, categoryBreakdown: Map<String, Long>, passed: Boolean) -> Unit
) {
    if (questions.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("কোনো প্রশ্ন পাওয়া যায়নি।", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onExit) { Text("ফিরে যান") }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var remainingSeconds by remember { mutableIntStateOf(durationMinutes * 60) }
    var isSubmitted by remember { mutableStateOf(false) }
    var showNavigatorGrid by remember { mutableStateOf(false) }
    val totalSecondsInitial = durationMinutes * 60

    fun submitNow() {
        if (isSubmitted) return
        isSubmitted = true
        var correctCount = 0
        val breakdown = mutableMapOf<String, Long>()
        questions.forEachIndexed { idx, q ->
            if (selectedAnswers[idx] == q.correctIndex) {
                correctCount++
                breakdown[q.category] = (breakdown[q.category] ?: 0L) + 1L
            } else {
                breakdown.putIfAbsent(q.category, breakdown[q.category] ?: 0L)
            }
        }
        val elapsed = (totalSecondsInitial - remainingSeconds).coerceAtLeast(1)
        val passThreshold = (questions.size * 0.60).toInt().coerceAtLeast(1)
        onCompleted(correctCount, questions.size, elapsed, breakdown, correctCount >= passThreshold)
    }

    LaunchedEffect(isSubmitted) {
        while (!isSubmitted && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            if (remainingSeconds <= 0) {
                submitNow()
            }
        }
    }

    if (isSubmitted) {
        val correctCount = questions.indices.count { selectedAnswers[it] == questions[it].correctIndex }
        val pct = (correctCount * 100) / questions.size
        val passed = pct >= 60
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (passed) MatchaJade.copy(alpha = 0.14f) else ToriiCrimson.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (passed) "অভিনন্দন! উত্তীর্ণ হয়েছেন 🎉" else "আরও অনুশীলন প্রয়োজন 💪",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (passed) MatchaJade else ToriiCrimson,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "স্কোর: $correctCount / ${questions.size} ($pct%)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (questions.size == 60) {
                        val jftScale = (correctCount * 250) / 60
                        Text(
                            text = "JFT-Basic স্কেল: $jftScale / 250 (পাস মার্ক: ২০০)",
                            style = MaterialTheme.typography.titleSmall,
                            color = IndigoScholar
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Category breakdown
                    val groups = questions.withIndex().groupBy { it.value.category }
                    groups.forEach { (cat, list) ->
                        val catCorrect = list.count { selectedAnswers[it.index] == it.value.correctIndex }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat, style = MaterialTheme.typography.bodyMedium)
                            Text("$catCorrect / ${list.size}", style = MaterialTheme.typography.titleSmall, color = IndigoScholar)
                        }
                    }
                }
            }

            Text("বিস্তারিত উত্তর ও বাংলা ব্যাখ্যা", style = MaterialTheme.typography.titleMedium)

            questions.forEachIndexed { idx, q ->
                val userChoice = selectedAnswers[idx]
                val isCorrect = userChoice == q.correctIndex
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "প্রশ্ন ${idx + 1} (${q.category}) — ${if (isCorrect) "✅ সঠিক" else "❌ ভুল"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCorrect) EmeraldSuccess else ToriiCrimson
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.promptJp, fontFamily = JapaneseFontFamily, style = MaterialTheme.typography.titleSmall)
                        Text(q.promptBn, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সঠিক উত্তর: ${q.options.getOrElse(q.correctIndex) { "" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (userChoice != null && !isCorrect) {
                            Text(
                                text = "আপনার উত্তর: ${q.options.getOrElse(userChoice) { "" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ToriiCrimson
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ব্যাখ্যা: ${q.explanationBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(onClick = onExit, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("সমাপ্ত করুন ও ফিরে যান")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        return
    }

    val currentQ = questions[currentIndex]
    val min = remainingSeconds / 60
    val sec = remainingSeconds % 60
    val timerText = "%02d:%02d".format(min, sec)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Bar with Countdown Timer & Grid Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (remainingSeconds < 120) ToriiCrimson.copy(alpha = 0.15f) else IndigoScholar.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "সময় বাকি",
                        tint = if (remainingSeconds < 120) ToriiCrimson else IndigoScholar,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timerText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (remainingSeconds < 120) ToriiCrimson else IndigoScholar
                    )
                }
            }
            IconButton(onClick = { showNavigatorGrid = !showNavigatorGrid }) {
                Icon(Icons.Rounded.GridView, contentDescription = "প্রশ্ন তালিকা")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = ToriiCrimson
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "প্রশ্ন ${currentIndex + 1} / ${questions.size} • ${currentQ.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "উত্তরিত: ${selectedAnswers.size}/${questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MatchaJade
            )
        }

        if (showNavigatorGrid) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    questions.indices.forEach { idx ->
                        val answered = selectedAnswers.containsKey(idx)
                        val active = idx == currentIndex
                        Surface(
                            shape = CircleShape,
                            color = when {
                                active -> ToriiCrimson
                                answered -> MatchaJade
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    currentIndex = idx
                                    showNavigatorGrid = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active || answered) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentQ.promptJp,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = JapaneseFontFamily,
                            modifier = Modifier.weight(1f)
                        )
                        if (!currentQ.audioTextJp.isNullOrBlank()) {
                            FilledTonalButton(
                                onClick = { speechManager.speak(currentQ.audioTextJp) }
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "অডিও শুনুন")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("শুনুন")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQ.promptBn,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            currentQ.options.forEachIndexed { optIdx, optionText ->
                val isSelected = selectedAnswers[currentIndex] == optIdx
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) ToriiCrimson.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) ToriiCrimson else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable { selectedAnswers[currentIndex] = optIdx }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) ToriiCrimson else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = listOf("A", "B", "C", "D").getOrElse(optIdx) { "" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = JapaneseFontFamily
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("পূর্ববর্তী")
            }
            if (currentIndex < questions.lastIndex) {
                Button(
                    onClick = { currentIndex++ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoScholar)
                ) {
                    Text("পরবর্তী")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = { submitNow() },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("submit_exam_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = XpGold)
                ) {
                    Text("জমা দিন (${selectedAnswers.size}/${questions.size})", color = Color.White)
                }
            }
        }
    }
}
