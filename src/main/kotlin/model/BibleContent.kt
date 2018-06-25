package model

data class Book(val title: String, val chapters: List<Chapter>)

data class Chapter(
        val number: Int,
        val text: String
)