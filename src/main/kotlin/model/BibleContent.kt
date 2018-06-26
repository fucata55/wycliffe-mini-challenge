package model
//all the objects we use to keep track of the data from the USFM files
//just data storage used in Door43 objects

data class Book(val title: String, val chapters: List<Chapter>)

data class Chapter(
        val number: Int,
        val text: String
)