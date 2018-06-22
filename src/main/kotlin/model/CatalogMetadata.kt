package model

class CatalogMetadata(val languages: Map<String, LanguageMetadata>) {

    fun getLanguageList() : List<LanguageMetadata> {
        // grab the titles of the languages
        return languages.values.toList()
    }

    fun getBiblesForLanguage(languageIdentifier: String) : List<BibleMetadata>? {
        // check if language ID exists
        if (languages.containsKey(languageIdentifier)) {
            // get the language from the map
            // force the unwrap since we know the map has this key
            val theLanguage = languages[languageIdentifier]!!
            // return the list of Bible names
            return theLanguage.bibles.values.toList()
        }
        // return null if no language exists
        return null
    }
}

// this stores information about a particular language
data class LanguageMetadata(
        val identifier: String,
        val title: String,
        val direction: String,
        val bibles: Map<String, BibleMetadata>
)

// this stores info about a particular bible from a language
data class BibleMetadata(
        val identifier: String,
        val title: String,
        val books: List<UsfmBookMetadata>
)

// this stores the information about the book USFM file
data class UsfmBookMetadata(
        val identifier: String,
        val sort: Int,
        val fileSize: Long,
        val title: String,
        val url: String
)