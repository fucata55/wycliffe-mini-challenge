package model

class Book(val title: String, val chapters: List<Chapter>) {
    fun getAllChapters() {
        var allChapters = ""
        // note: the chapter numbers are not included
        for (chapter in chapters) {
            allChapters += chapter.text
        }
    }
}

data class Chapter(
        val number: Int,
        val text: String
)