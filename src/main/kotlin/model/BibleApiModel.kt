class BibleData(val languages: Map<String, Language>) {

    fun getLanguageList() : List<Language> {
        // grab the titles of the languages
        return languages.values.toList()
    }

    fun getBiblesForLanguage(languageIdentifier: String) : List<String>? {
        // check if language ID exists
        if (languages.containsKey(languageIdentifier)) {
            // get the language from the map
            // force the unwrap since we know the map has this key
            val theLanguage = languages[languageIdentifier]!!
            // return the list of Bible names
            return theLanguage.bibles.values.map { it.title }
        }
        // return null if no language exists
        return null
    }
}

data class Language(
        val identifier: String,
        val title: String,
        val direction: String,
        val bibles: Map<String, Bible>
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