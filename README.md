# Door43 Reader Application

## `Door43` Class

You can use the `Door43` class to access the Scripture from Door43. The program below is an example.

```kotlin
fun main(args: Array<String>) {
    // create a door43 object
    val door43 = Door43()

    // grab the catalog
    val catalog = door43.fetchCatalog()

    // use the catalog
    if (catalog != null) {
        val languages = catalog.getLanguageList() // gets a list of the available languages

        // each language object has information like the identifier, title, and list of available Bibles
        println("Language Sample: " + languages[0].title + " (" + languages[0].identifier + ") \n")

        // alternatively, you can directly get all the Bibles for a language if you
        // know the identifier/slug
        val bibles = catalog.getBiblesForLanguage("en")

        // once you have the Bible metadata from the catalog, you can use it to grab all the chapters in a book
        if (bibles != null) {
            // get book information
            val bookTitle = bibles[0].books[0].title
            val bookIdentifier = bibles[0].books[0].identifier


            // get the book
            val book = door43.getBook(bibles[0], bookIdentifier)

            // get the chapter text
            if (book != null) {
                val chapter = book.chapters[0]
                // print the chapter information
                println(bookTitle + " " + chapter.number)
                println(chapter.text)
            }
        }
    }
}
```

