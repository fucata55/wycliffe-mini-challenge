data class BibleData(
        val languages: List<Language>
)

data class Language(
        val identifier: String,
        val title: String,
        val direction: String,
        val bibles: List<Bible>
)

data class Bible(
        val identifier: String,
        val title: String,
        val books: List<UsfmBook>
)

data class UsfmBook(
        val identifier: String,
        val sort: Int,
        val size: Long,
        val title: String,
        val url: String
)