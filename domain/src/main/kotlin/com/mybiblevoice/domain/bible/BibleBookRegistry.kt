package com.mybiblevoice.domain.bible

/**
 * Canonical registry of the 66 Bible books, chapter counts, and spoken-alias matching.
 * Canonical [BibleBook.id] values are independent of any MySword identifier scheme.
 */
object BibleBookRegistry {

    val books: List<BibleBook> = listOf(
        BibleBook("genesis", "Genesis", 50),
        BibleBook("exodus", "Exodus", 40),
        BibleBook("leviticus", "Leviticus", 27),
        BibleBook("numbers", "Numbers", 36),
        BibleBook("deuteronomy", "Deuteronomy", 34),
        BibleBook("joshua", "Joshua", 24),
        BibleBook("judges", "Judges", 21),
        BibleBook("ruth", "Ruth", 4),
        BibleBook("1_samuel", "1 Samuel", 31),
        BibleBook("2_samuel", "2 Samuel", 24),
        BibleBook("1_kings", "1 Kings", 22),
        BibleBook("2_kings", "2 Kings", 25),
        BibleBook("1_chronicles", "1 Chronicles", 29),
        BibleBook("2_chronicles", "2 Chronicles", 36),
        BibleBook("ezra", "Ezra", 10),
        BibleBook("nehemiah", "Nehemiah", 13),
        BibleBook("esther", "Esther", 10),
        BibleBook("job", "Job", 42),
        BibleBook("psalms", "Psalms", 150),
        BibleBook("proverbs", "Proverbs", 31),
        BibleBook("ecclesiastes", "Ecclesiastes", 12),
        BibleBook("song_of_solomon", "Song of Solomon", 8),
        BibleBook("isaiah", "Isaiah", 66),
        BibleBook("jeremiah", "Jeremiah", 52),
        BibleBook("lamentations", "Lamentations", 5),
        BibleBook("ezekiel", "Ezekiel", 48),
        BibleBook("daniel", "Daniel", 12),
        BibleBook("hosea", "Hosea", 14),
        BibleBook("joel", "Joel", 3),
        BibleBook("amos", "Amos", 9),
        BibleBook("obadiah", "Obadiah", 1),
        BibleBook("jonah", "Jonah", 4),
        BibleBook("micah", "Micah", 7),
        BibleBook("nahum", "Nahum", 3),
        BibleBook("habakkuk", "Habakkuk", 3),
        BibleBook("zephaniah", "Zephaniah", 3),
        BibleBook("haggai", "Haggai", 2),
        BibleBook("zechariah", "Zechariah", 14),
        BibleBook("malachi", "Malachi", 4),
        BibleBook("matthew", "Matthew", 28),
        BibleBook("mark", "Mark", 16),
        BibleBook("luke", "Luke", 24),
        BibleBook("john", "John", 21),
        BibleBook("acts", "Acts", 28),
        BibleBook("romans", "Romans", 16),
        BibleBook("1_corinthians", "1 Corinthians", 16),
        BibleBook("2_corinthians", "2 Corinthians", 13),
        BibleBook("galatians", "Galatians", 6),
        BibleBook("ephesians", "Ephesians", 6),
        BibleBook("philippians", "Philippians", 4),
        BibleBook("colossians", "Colossians", 4),
        BibleBook("1_thessalonians", "1 Thessalonians", 5),
        BibleBook("2_thessalonians", "2 Thessalonians", 3),
        BibleBook("1_timothy", "1 Timothy", 6),
        BibleBook("2_timothy", "2 Timothy", 4),
        BibleBook("titus", "Titus", 3),
        BibleBook("philemon", "Philemon", 1),
        BibleBook("hebrews", "Hebrews", 13),
        BibleBook("james", "James", 5),
        BibleBook("1_peter", "1 Peter", 5),
        BibleBook("2_peter", "2 Peter", 3),
        BibleBook("1_john", "1 John", 5),
        BibleBook("2_john", "2 John", 1),
        BibleBook("3_john", "3 John", 1),
        BibleBook("jude", "Jude", 1),
        BibleBook("revelation", "Revelation", 22)
    )

    /** Longest alias we'll ever try to match, in tokens (e.g. "song of solomon"). */
    private const val MAX_ALIAS_TOKENS = 3

    private val idToBook: Map<String, BibleBook> = books.associateBy { it.id }

    /** Hand-curated extra aliases/abbreviations, keyed by book id. Not exhaustive by design; extend as needed. */
    private val extraAliases: Map<String, List<String>> = buildMap {
        put("genesis", listOf("gen"))
        put("exodus", listOf("ex", "exod"))
        put("leviticus", listOf("lev"))
        put("numbers", listOf("num"))
        put("deuteronomy", listOf("deut"))
        put("joshua", listOf("josh"))
        put("judges", listOf("judg"))
        put("1_samuel", numbered(1, "samuel", listOf("sam")))
        put("2_samuel", numbered(2, "samuel", listOf("sam")))
        put("1_kings", numbered(1, "kings", listOf("kgs")))
        put("2_kings", numbered(2, "kings", listOf("kgs")))
        put("1_chronicles", numbered(1, "chronicles", listOf("chron", "chr")))
        put("2_chronicles", numbered(2, "chronicles", listOf("chron", "chr")))
        put("nehemiah", listOf("neh"))
        put("esther", listOf("esth"))
        put("psalms", listOf("psalm", "psa", "ps"))
        put("proverbs", listOf("prov"))
        put("ecclesiastes", listOf("eccl", "eccles"))
        put("song_of_solomon", listOf("song of songs", "songs of solomon", "song"))
        put("isaiah", listOf("isa"))
        put("jeremiah", listOf("jer"))
        put("lamentations", listOf("lam"))
        put("ezekiel", listOf("ezek", "eze"))
        put("daniel", listOf("dan"))
        put("hosea", listOf("hos"))
        put("obadiah", listOf("obad"))
        put("micah", listOf("mic"))
        put("nahum", listOf("nah"))
        put("habakkuk", listOf("hab"))
        put("zephaniah", listOf("zeph"))
        put("haggai", listOf("hag"))
        put("zechariah", listOf("zech"))
        put("malachi", listOf("mal"))
        put("matthew", listOf("matt", "mt"))
        put("mark", listOf("mk"))
        put("luke", listOf("lk"))
        put("john", listOf("jn"))
        put("romans", listOf("rom"))
        put("1_corinthians", numbered(1, "corinthians", listOf("cor")))
        put("2_corinthians", numbered(2, "corinthians", listOf("cor")))
        put("galatians", listOf("gal"))
        put("ephesians", listOf("eph"))
        put("philippians", listOf("phil"))
        put("colossians", listOf("col"))
        put("1_thessalonians", numbered(1, "thessalonians", listOf("thess", "thes")))
        put("2_thessalonians", numbered(2, "thessalonians", listOf("thess", "thes")))
        put("1_timothy", numbered(1, "timothy", listOf("tim")))
        put("2_timothy", numbered(2, "timothy", listOf("tim")))
        put("philemon", listOf("philem", "phlm"))
        put("hebrews", listOf("heb"))
        put("james", listOf("jas"))
        put("1_peter", numbered(1, "peter", listOf("pet")))
        put("2_peter", numbered(2, "peter", listOf("pet")))
        put("1_john", numbered(1, "john", listOf("jn")))
        put("2_john", numbered(2, "john", listOf("jn")))
        put("3_john", numbered(3, "john", listOf("jn")))
        put("revelation", listOf("rev", "revelations"))
    }

    /**
     * Expands a numbered book's rest-of-name into "1 samuel" / "first samuel" / "one samuel"
     * style aliases. Includes "1st"/"2nd"/"3rd" because Android's speech recognizer commonly
     * transcribes the spoken ordinal "first"/"second"/"third" as that digit+suffix form.
     */
    private fun numbered(n: Int, restFull: String, restAbbrevs: List<String> = emptyList()): List<String> {
        val prefixes = when (n) {
            1 -> listOf("1", "1st", "first", "one")
            2 -> listOf("2", "2nd", "second", "two")
            3 -> listOf("3", "3rd", "third", "three")
            else -> error("Unsupported book number: $n")
        }
        val names = listOf(restFull) + restAbbrevs
        return prefixes.flatMap { prefix -> names.map { name -> "$prefix $name" } }
    }

    private val aliasMap: Map<String, BibleBook> = buildMap {
        for (book in books) {
            put(book.canonicalName.lowercase(), book)
            for (alias in extraAliases[book.id].orEmpty()) {
                put(alias.lowercase(), book)
            }
        }
    }

    fun findById(id: String): BibleBook? = idToBook[id]

    /**
     * Matches a Bible book alias at the START of [tokens], preferring the longest alias
     * (most specific match) when multiple lengths are possible.
     *
     * @return the matched book and the number of leading tokens it consumed, or null.
     */
    fun matchAtStart(tokens: List<String>): Pair<BibleBook, Int>? {
        val maxLen = minOf(MAX_ALIAS_TOKENS, tokens.size)
        for (len in maxLen downTo 1) {
            val candidate = tokens.subList(0, len).joinToString(" ")
            aliasMap[candidate]?.let { return it to len }
        }
        return null
    }
}
