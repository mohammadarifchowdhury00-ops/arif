package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.curriculum.IrodoriRepository
import com.example.data.model.CustomLessonRecord
import com.example.data.model.LessonProgressRecord
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoScholar
import com.example.ui.theme.JapaneseFontFamily
import com.example.ui.theme.MatchaJade
import com.example.ui.theme.ToriiCrimson

@Composable
fun IrodoriHubScreen(
    selectedBookId: String,
    onSelectBook: (String) -> Unit,
    lessonProgress: List<LessonProgressRecord>,
    customLessons: List<CustomLessonRecord>,
    onOpenLesson: (bookId: String, lessonNumber: Int) -> Unit
) {
    val bookMeta = IrodoriRepository.books.find { it.id == selectedBookId } ?: IrodoriRepository.books.first()
    val lessons = IrodoriRepository.getLessonsForBook(bookMeta.id)
    val bookCustomLessons = customLessons.filter { it.bookId == bookMeta.id }
    val progressMap = lessonProgress
        .filter { it.bookId == bookMeta.id }
        .associateBy { it.lessonNumber.toInt() }

    val completedCount = lessons.count { progressMap[it.lessonNumber]?.completed == true }
    val bookProgressFraction = completedCount.toFloat() / lessons.size.coerceAtLeast(1).toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("ইরোদোরি সম্পূর্ণ একাডেমি", style = MaterialTheme.typography.headlineMedium, color = ToriiCrimson)
            Text(
                text = "বই ১, ২ এবং ৩ — সর্বমোট ৫৪টি পূর্ণাঙ্গ পাঠ ও প্রতিটি পাঠে ১৭টি শেখার ধাপ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IrodoriRepository.books.forEach { b ->
                    val bookDone = lessonProgress.count { it.bookId == b.id && it.completed }
                    FilterChip(
                        selected = b.id == bookMeta.id,
                        onClick = { onSelectBook(b.id) },
                        label = { Text("বই ${b.order} ($bookDone/18)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("irodori_book_tab_${b.id}")
                    )
                }
            }
        }

        // Book Introduction & Objectives Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(modifier = Modifier.height(145.dp).fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_irodori_starter),
                            contentDescription = bookMeta.titleBn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Surface(color = ToriiCrimson, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = bookMeta.levelTag,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bookMeta.titleJp,
                                fontFamily = JapaneseFontFamily,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bookMeta.titleBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(bookMeta.introBn, style = MaterialTheme.typography.bodyMedium)
                        Text("বইয়ের লক্ষ্যসমূহ:", style = MaterialTheme.typography.titleSmall, color = IndigoScholar)
                        bookMeta.objectivesBn.forEach { obj ->
                            Text("• $obj", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("বইয়ের অগ্রগতি", style = MaterialTheme.typography.labelLarge)
                            Text("$completedCount / ১৮ পাঠ সম্পন্ন", style = MaterialTheme.typography.labelLarge, color = MatchaJade)
                        }
                        LinearProgressIndicator(
                            progress = { bookProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MatchaJade
                        )
                    }
                }
            }
        }

        // All 18 lessons sequence
        items(lessons, key = { "${it.bookId}_${it.lessonNumber}" }) { lesson ->
            val rec = progressMap[lesson.lessonNumber]
            val sectionsDone = rec?.completedSections?.size ?: 0
            val isCompleted = rec?.completed == true

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable { onOpenLesson(lesson.bookId, lesson.lessonNumber) }
                    .testTag("lesson_card_${lesson.bookId}_${lesson.lessonNumber}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCompleted) EmeraldSuccess else ToriiCrimson.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "সম্পন্ন", tint = Color.White)
                            } else {
                                Text(
                                    text = "${lesson.lessonNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ToriiCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lesson.titleJp,
                                fontFamily = JapaneseFontFamily,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndigoScholar.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = lesson.topicTagBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoScholar,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(lesson.titleBn, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "ক্যান-ডু: ${lesson.canDoGoalsBn.firstOrNull().orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { (sectionsDone / 17f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isCompleted) EmeraldSuccess else ToriiCrimson
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$sectionsDone/১৭",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "পাঠ খুলুন")
                }
            }
        }

        // Admin-added supplementary custom lessons for this book
        if (bookCustomLessons.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("অ্যাডমিন সংযুক্ত অতিরিক্ত পাঠ", style = MaterialTheme.typography.titleMedium, color = IndigoScholar)
            }
            items(bookCustomLessons, key = { it.id }) { custom ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoScholar.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = IndigoScholar, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "পাঠ ${custom.lessonNumber}: ${custom.titleJp} — ${custom.titleBn}",
                                style = MaterialTheme.typography.titleSmall,
                                color = IndigoScholar
                            )
                        }
                        Text("লক্ষ্য: ${custom.canDoBn}", style = MaterialTheme.typography.bodySmall)
                        if (custom.vocabSummary.isNotBlank()) {
                            Text("শব্দ: ${custom.vocabSummary}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (custom.grammarSummary.isNotBlank()) {
                            Text("ব্যাকরণ: ${custom.grammarSummary}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (custom.cultureNoteBn.isNotBlank()) {
                            Text("সংস্কৃতি: ${custom.cultureNoteBn}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
