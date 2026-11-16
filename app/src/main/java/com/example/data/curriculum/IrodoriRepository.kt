package com.example.data.curriculum

object IrodoriRepository {

    val books: List<IrodoriBookMeta> = listOf(
        IrodoriBookMeta(
            id = "starter",
            order = 1,
            titleJp = "いろどり 入門 (Starter)",
            titleBn = "ইরোদোরি স্টার্টার (বই ১)",
            subtitleBn = "শুরু থেকে দৈনন্দিন কথোপকথন • ১৮টি পাঠ",
            levelTag = "A1 / JLPT N5 ভিত্তি",
            introBn = "জাপানে বসবাস ও কাজ শুরু করার জন্য প্রয়োজনীয় প্রাথমিক শুভেচ্ছা, আত্মপরিচয়, খাবার, দিকনির্দেশনা, কেনাকাটা ও দৈনন্দিন রুটিন শেখার পূর্ণাঙ্গ ১৮ পাঠের কোর্স।",
            objectivesBn = listOf(
                "সহজ ভাষায় নিজের পরিচয় ও পরিবারের বিবরণ দেওয়া",
                "রেস্তোরাঁ ও দোকানে খাবার এবং জিনিসপত্র কেনা",
                "ট্রেন, বাস ও দিকনির্দেশনা বুঝে যাতায়াত করা"
            )
        ),
        IrodoriBookMeta(
            id = "elementary_1",
            order = 2,
            titleJp = "いろどり 初級1 (Elementary 1)",
            titleBn = "ইরোদোরি এলিমেন্টারি ১ (বই ২)",
            subtitleBn = "কর্মক্ষেত্র, আবহাওয়া ও সামাজিক জীবন • ১৮টি পাঠ",
            levelTag = "A2 / JFT-Basic মধ্যবর্তী",
            introBn = "নতুন শহরে বাসা, অফিসের কাজ, ডাক্তারের কাছে যাওয়া, ফোনে বুকিং, সামাজিক নিয়ম এবং ভবিষ্যৎ পরিকল্পনা নিয়ে ১৮টি বাস্তবধর্মী পাঠ।",
            objectivesBn = listOf(
                "হাসপাতাল, সিটি অফিস ও কর্মক্ষেত্রে প্রয়োজনীয় কথোপকথন",
                "অনুষ্ঠান, রান্না ও নিয়মকানুন সম্পর্কে বিস্তারিত বোঝা",
                "ফোনে বুকিং ও ছুটির আবেদন করতে পারা"
            )
        ),
        IrodoriBookMeta(
            id = "elementary_2",
            order = 3,
            titleJp = "いろどり 初級2 (Elementary 2)",
            titleBn = "ইরোদোরি এলিমেন্টারি ২ (বই ৩)",
            subtitleBn = "উচ্চতর দক্ষতা, কেইগো ও JFT প্রস্তুতি • ১৮টি পাঠ",
            levelTag = "A2+ / JLPT N4 প্রস্তুতি",
            introBn = "দুর্যোগ মোকাবিলা, সম্মানসূচক ভাষা (কেইগো), চাকরির ইন্টারভিউ, সংবাদ বিশ্লেষণ ও নিজের মতামত প্রকাশের পূর্ণাঙ্গ ১৮টি পাঠ।",
            objectivesBn = listOf(
                "কর্মক্ষেত্রে সম্মানসূচক ভাষা (কেইগো) ব্যবহার করা",
                "সংবাদ ও পণ্যের রিভিউ পড়ে সিদ্ধান্ত নেওয়া",
                "চাকরির ইন্টারভিউ ও মতামত প্রকাশে সাবলীল হওয়া"
            )
        )
    )

    val allLessons: List<IrodoriLesson> by lazy {
        IrodoriStarterLessons.lessons + IrodoriElem1Lessons.lessons + IrodoriElem2Lessons.lessons
    }

    fun getLessonsForBook(bookId: String): List<IrodoriLesson> =
        when (bookId) {
            "starter" -> IrodoriStarterLessons.lessons
            "elementary_1" -> IrodoriElem1Lessons.lessons
            "elementary_2" -> IrodoriElem2Lessons.lessons
            else -> emptyList()
        }

    fun getLesson(bookId: String, lessonNumber: Int): IrodoriLesson? =
        getLessonsForBook(bookId).find { it.lessonNumber == lessonNumber }

    /**
     * Generates a comprehensive, randomized 60-question JFT-Basic Mock Examination
     * spanning all 4 official categories across all 54 IRODORI lessons, Kana, and Kanji:
     * 1. Script & Vocabulary (文字と語彙) - 15 Qs
     * 2. Conversation & Expression (会話と表現) - 15 Qs
     * 3. Listening Comprehension (聴解) - 15 Qs
     * 4. Reading Comprehension (読解) - 15 Qs
     */
    fun buildJftFullMockQuestions(customQuestions: List<PracticeQuestion> = emptyList()): List<PracticeQuestion> {
        val scriptVocabPool = mutableListOf<PracticeQuestion>()
        val conversationPool = mutableListOf<PracticeQuestion>()
        val listeningPool = mutableListOf<PracticeQuestion>()
        val readingPool = mutableListOf<PracticeQuestion>()

        // 1. From all 54 IRODORI lessons (54 * 4 = 216 rich questions!)
        allLessons.forEach { lesson ->
            lesson.vocab.forEachIndexed { idx, v ->
                val distractors = allLessons
                    .flatMap { it.vocab }
                    .filter { it.meaningBn != v.meaningBn }
                    .shuffled()
                    .take(3)
                    .map { it.meaningBn }
                if (distractors.size == 3) {
                    val options = (distractors + v.meaningBn).shuffled()
                    val correctIdx = options.indexOf(v.meaningBn)
                    scriptVocabPool.add(
                        PracticeQuestion(
                            id = "jft_sv_${lesson.bookId}_${lesson.lessonNumber}_$idx",
                            category = "Script & Vocabulary",
                            promptJp = "${v.jp} (${v.furigana})",
                            promptBn = "「${v.jp}」 শব্দটির বাংলা অর্থ কী?",
                            audioTextJp = v.jp,
                            options = options,
                            correctIndex = correctIdx,
                            explanationBn = "「${v.jp}」 (${v.bengaliPron}) শব্দের অর্থ '${v.meaningBn}'। উদাহরণ: ${v.exampleJp}"
                        )
                    )
                }
            }

            lesson.grammar.firstOrNull()?.let { gp ->
                val ex = gp.examples.first()
                val otherBns = allLessons
                    .filter { it.lessonNumber != lesson.lessonNumber || it.bookId != lesson.bookId }
                    .shuffled()
                    .take(3)
                    .map { it.grammar.firstOrNull()?.examples?.firstOrNull()?.meaningBn ?: it.titleBn }
                val options = (otherBns + ex.meaningBn).shuffled()
                conversationPool.add(
                    PracticeQuestion(
                        id = "jft_ce_${lesson.bookId}_${lesson.lessonNumber}",
                        category = "Conversation & Expression",
                        promptJp = ex.jp,
                        promptBn = "কথোপকথনের এই বাক্যটির সঠিক অর্থ ও প্রয়োগ কোনটি?\nপ্যাটার্ন: ${gp.pattern}",
                        audioTextJp = ex.jp,
                        options = options,
                        correctIndex = options.indexOf(ex.meaningBn),
                        explanationBn = "「${ex.jp}」 (${ex.bengaliPron}) = '${ex.meaningBn}'। ${gp.explanationBn}"
                    )
                )
            }

            val line = lesson.dialogue.first()
            val otherDia = allLessons
                .filter { it.titleBn != lesson.titleBn }
                .shuffled()
                .take(3)
                .map { it.dialogue.first().meaningBn }
            val listenOpts = (otherDia + line.meaningBn).shuffled()
            listeningPool.add(
                PracticeQuestion(
                    id = "jft_ls_${lesson.bookId}_${lesson.lessonNumber}",
                    category = "Listening Comprehension",
                    promptJp = "অডিও বাজিয়ে মন দিয়ে শুনুন",
                    promptBn = "অডিওতে বক্তা কী বলেছেন? সঠিক উত্তর বেছে নিন:",
                    audioTextJp = line.jp,
                    options = listenOpts,
                    correctIndex = listenOpts.indexOf(line.meaningBn),
                    explanationBn = "অডিও বাক্য: 「${line.jp}」 (${line.romaji}) — অর্থ: '${line.meaningBn}'।"
                )
            )

            val otherReads = allLessons
                .filter { it.bookId != lesson.bookId || it.lessonNumber != lesson.lessonNumber }
                .shuffled()
                .take(3)
                .map { it.vocab.first().exampleBn }
            val readCorrect = lesson.vocab.first().exampleBn
            val readOpts = (otherReads + readCorrect).shuffled()
            readingPool.add(
                PracticeQuestion(
                    id = "jft_rd_${lesson.bookId}_${lesson.lessonNumber}",
                    category = "Reading Comprehension",
                    promptJp = lesson.readingPassageJp,
                    promptBn = "উপরের অনুচ্ছেদটি পড়ে বলুন, প্রথম বাক্যটিতে কী বলা হয়েছে?",
                    audioTextJp = lesson.vocab.first().exampleJp,
                    options = readOpts,
                    correctIndex = readOpts.indexOf(readCorrect),
                    explanationBn = "সম্পূর্ণ বাংলা অর্থ: ${lesson.readingTranslationBn}"
                )
            )
        }

        // Add Kanji questions into Script & Vocabulary
        KanjiRepository.allKanji.shuffled().take(25).forEachIndexed { idx, kj ->
            val dist = KanjiRepository.allKanji.filter { it.meaningBn != kj.meaningBn }.shuffled().take(3).map { it.meaningBn }
            val opts = (dist + kj.meaningBn).shuffled()
            scriptVocabPool.add(
                PracticeQuestion(
                    id = "jft_kj_$idx",
                    category = "Script & Vocabulary",
                    promptJp = "${kj.kanji} — ${kj.exampleSentenceJp}",
                    promptBn = "「${kj.kanji}」 কাঞ্জিটির সঠিক অর্থ কোনটি?",
                    audioTextJp = kj.exampleSentenceJp,
                    options = opts,
                    correctIndex = opts.indexOf(kj.meaningBn),
                    explanationBn = "「${kj.kanji}」 (${kj.bengaliPron}) অর্থ '${kj.meaningBn}'। স্মৃতি কৌশল: ${kj.mnemonicBn}"
                )
            )
        }

        // Merge any admin-published custom questions
        customQuestions.forEach { cq ->
            when {
                cq.category.contains("Conversation", true) -> conversationPool.add(0, cq)
                cq.category.contains("Listening", true) -> listeningPool.add(0, cq)
                cq.category.contains("Reading", true) -> readingPool.add(0, cq)
                else -> scriptVocabPool.add(0, cq)
            }
        }

        return (
            scriptVocabPool.shuffled().take(15) +
                conversationPool.shuffled().take(15) +
                listeningPool.shuffled().take(15) +
                readingPool.shuffled().take(15)
            )
    }

    fun buildJftSectionQuestions(category: String, count: Int = 15): List<PracticeQuestion> =
        buildJftFullMockQuestions().filter { it.category == category }.take(count)

    fun buildJlptQuestions(level: String, module: String, count: Int = 15): List<PracticeQuestion> {
        val targetBooks = if (level == "N5") listOf("starter", "elementary_1") else listOf("elementary_1", "elementary_2")
        val sourceLessons = allLessons.filter { it.bookId in targetBooks }
        val pool = mutableListOf<PracticeQuestion>()

        when (module) {
            "vocabulary" -> {
                sourceLessons.flatMap { it.vocab }.shuffled().take(count).forEachIndexed { i, v ->
                    val dist = sourceLessons.flatMap { it.vocab }.filter { it.meaningBn != v.meaningBn }.shuffled().take(3).map { it.meaningBn }
                    val opts = (dist + v.meaningBn).shuffled()
                    pool.add(
                        PracticeQuestion(
                            id = "jlpt_${level}_v_$i",
                            category = "$level শব্দভাণ্ডার",
                            promptJp = "${v.jp} (${v.furigana})",
                            promptBn = "'${v.jp}' শব্দের সঠিক অর্থ কোনটি?",
                            audioTextJp = v.jp,
                            options = opts,
                            correctIndex = opts.indexOf(v.meaningBn),
                            explanationBn = "${v.jp} (${v.bengaliPron}) = ${v.meaningBn}"
                        )
                    )
                }
            }
            "grammar" -> {
                sourceLessons.shuffled().take(count).forEachIndexed { i, l ->
                    val gp = l.grammar.first()
                    val ex = gp.examples.first()
                    val dist = sourceLessons.filter { it.titleBn != l.titleBn }.shuffled().take(3).map { it.grammar.first().examples.first().meaningBn }
                    val opts = (dist + ex.meaningBn).shuffled()
                    pool.add(
                        PracticeQuestion(
                            id = "jlpt_${level}_g_$i",
                            category = "$level ব্যাকরণ",
                            promptJp = "${ex.jp}\n[${gp.pattern}]",
                            promptBn = "ব্যাকরণ পয়েন্ট '${gp.pattern}' অনুযায়ী বাক্যটির অর্থ কী?",
                            audioTextJp = ex.jp,
                            options = opts,
                            correctIndex = opts.indexOf(ex.meaningBn),
                            explanationBn = "${gp.explanationBn} | উদাহরণ: ${ex.meaningBn}"
                        )
                    )
                }
            }
            "kanji" -> {
                val kanjiList = if (level == "N5") KanjiRepository.n5Kanji else KanjiRepository.n4Kanji
                kanjiList.shuffled().take(count).forEachIndexed { i, kj ->
                    val dist = kanjiList.filter { it.meaningBn != kj.meaningBn }.shuffled().take(3).map { it.meaningBn }
                    val opts = (dist + kj.meaningBn).shuffled()
                    pool.add(
                        PracticeQuestion(
                            id = "jlpt_${level}_k_$i",
                            category = "$level কাঞ্জি",
                            promptJp = "${kj.kanji} (${kj.onyomi} / ${kj.kunyomi})",
                            promptBn = "'${kj.kanji}' কাঞ্জির বাংলা অর্থ কী?",
                            audioTextJp = kj.exampleSentenceJp,
                            options = opts,
                            correctIndex = opts.indexOf(kj.meaningBn),
                            explanationBn = "${kj.kanji} (${kj.bengaliPron}) = ${kj.meaningBn}। ${kj.mnemonicBn}"
                        )
                    )
                }
            }
            "reading" -> {
                sourceLessons.shuffled().take(count).forEachIndexed { i, l ->
                    val correct = l.vocab.first().exampleBn
                    val dist = sourceLessons.filter { it.titleBn != l.titleBn }.shuffled().take(3).map { it.vocab.first().exampleBn }
                    val opts = (dist + correct).shuffled()
                    pool.add(
                        PracticeQuestion(
                            id = "jlpt_${level}_r_$i",
                            category = "$level পঠন",
                            promptJp = l.readingPassageJp,
                            promptBn = "অনুচ্ছেদটি পড়ে প্রথম বাক্যের সঠিক অর্থ নির্বাচন করুন:",
                            audioTextJp = l.readingPassageJp,
                            options = opts,
                            correctIndex = opts.indexOf(correct),
                            explanationBn = l.readingTranslationBn
                        )
                    )
                }
            }
            "listening" -> {
                sourceLessons.shuffled().take(count).forEachIndexed { i, l ->
                    val line = l.dialogue.first()
                    val dist = sourceLessons.filter { it.titleBn != l.titleBn }.shuffled().take(3).map { it.dialogue.first().meaningBn }
                    val opts = (dist + line.meaningBn).shuffled()
                    pool.add(
                        PracticeQuestion(
                            id = "jlpt_${level}_l_$i",
                            category = "$level শ্রবণ",
                            promptJp = "অডিও শুনে সঠিক উত্তর বাছাই করুন",
                            promptBn = "অডিও ক্লিপে কী বলা হয়েছে?",
                            audioTextJp = line.jp,
                            options = opts,
                            correctIndex = opts.indexOf(line.meaningBn),
                            explanationBn = "${line.jp} (${line.romaji}) = ${line.meaningBn}"
                        )
                    )
                }
            }
            else -> {
                // Full Mock Exam for JLPT N5 / N4 (30 mixed questions across all 5 modules!)
                return (
                    buildJlptQuestions(level, "vocabulary", 6) +
                        buildJlptQuestions(level, "grammar", 6) +
                        buildJlptQuestions(level, "kanji", 6) +
                        buildJlptQuestions(level, "reading", 6) +
                        buildJlptQuestions(level, "listening", 6)
                    )
            }
        }
        return pool
    }
}
