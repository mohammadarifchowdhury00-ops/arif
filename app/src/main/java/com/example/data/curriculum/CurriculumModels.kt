package com.example.data.curriculum

enum class KanaGroup(val labelBn: String) {
    BASIC("মৌলিক ৪৬ অক্ষর"),
    DAKUTEN("দাকুতেন (゛)"),
    HANDAKUTEN("হানদাকুতেন (゜)"),
    YOON("যুক্তাক্ষর (拗音)"),
    SPECIAL("ছোট っ / দীর্ঘ স্বর")
}

data class KanaChar(
    val char: String,
    val romaji: String,
    val bengaliPron: String,
    val group: KanaGroup,
    val exampleWordJp: String,
    val exampleRomaji: String,
    val exampleBn: String,
    val strokeTipBn: String
)

data class KanjiExample(
    val wordJp: String,
    val furigana: String,
    val romaji: String,
    val bengaliPron: String,
    val meaningBn: String
)

data class KanjiItem(
    val kanji: String,
    val jlptLevel: String,
    val meaningBn: String,
    val onyomi: String,
    val kunyomi: String,
    val romaji: String,
    val bengaliPron: String,
    val strokes: Int,
    val mnemonicBn: String,
    val exampleSentenceJp: String,
    val exampleSentenceFurigana: String,
    val exampleSentenceBn: String,
    val examples: List<KanjiExample>
)

data class VocabItem(
    val jp: String,
    val furigana: String,
    val romaji: String,
    val bengaliPron: String,
    val meaningBn: String,
    val exampleJp: String,
    val exampleBn: String,
    val visualEmoji: String
)

data class GrammarExample(
    val jp: String,
    val furigana: String,
    val romaji: String,
    val bengaliPron: String,
    val meaningBn: String
)

data class GrammarPoint(
    val pattern: String,
    val explanationBn: String,
    val structure: String,
    val commonMistakeBn: String,
    val examples: List<GrammarExample>
)

data class DialogueLine(
    val speaker: String,
    val jp: String,
    val furigana: String,
    val romaji: String,
    val bengaliPron: String,
    val meaningBn: String
)

data class PracticeQuestion(
    val id: String,
    val category: String,
    val promptJp: String,
    val promptBn: String,
    val audioTextJp: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String,
    val difficulty: String = "medium"
)

data class IrodoriBookMeta(
    val id: String,
    val order: Int,
    val titleJp: String,
    val titleBn: String,
    val subtitleBn: String,
    val levelTag: String,
    val introBn: String,
    val objectivesBn: List<String>
)

data class IrodoriLesson(
    val bookId: String,
    val lessonNumber: Int,
    val titleJp: String,
    val titleBn: String,
    val topicTagBn: String,
    val overviewBn: String,
    val canDoGoalsBn: List<String>,
    val vocab: List<VocabItem>,
    val grammar: List<GrammarPoint>,
    val dialogue: List<DialogueLine>,
    val listeningDialogueJp: String,
    val listeningSummaryBn: String,
    val readingPassageJp: String,
    val readingFurigana: String,
    val readingTranslationBn: String,
    val speakingTargetJp: String,
    val speakingTargetRomaji: String,
    val speakingTargetBn: String,
    val writingFocusChars: List<String>,
    val kanjiFocus: List<KanjiItem>,
    val cultureTitleBn: String,
    val cultureNoteBn: String,
    val questions: List<PracticeQuestion>
)
