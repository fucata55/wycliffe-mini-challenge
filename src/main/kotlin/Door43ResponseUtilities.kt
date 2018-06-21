fun processBibleData(catalog: Door43Response) : BibleData {
    // we want to extract the USFM Bible information from the response
    val languages = ArrayList<Language>()

    for (language in catalog.languages) {
        val bibles = ArrayList<Bible>()

        // search for ULB Bible
        for (resource in language.resources) {
            // check if ULB Bible
            if (resource.identifier == "ulb") {
                val books = ArrayList<UsfmBook>()
                // we need to get the book data
                for (project in resource.projects) {
                    // this should be a book of the Bible
                    // we need the USFM data
                    for (format in project.formats) {
                        // check for USFM
                        if (format.format == "text/usfm") {
                            // found the USFM format
                            // save the book information
                            val thisBook = UsfmBook(
                                    project.identifier,
                                    project.sort,
                                    format.size,
                                    project.title,
                                    format.url)
                            // add the book to the list
                            books.add(thisBook)
                        }
                    }
                }

                // check for 66 books
                if (books.size < 66) {
                    // uh oh. not enough books
                    // fail silently by continuing on to the next resource
                    continue
                }

                // add the books to the Bible data
                val thisBible = Bible(
                        resource.identifier,
                        resource.title,
                        books
                )
                // add the Bible to the list
                bibles.add(thisBible)
            }
        }
        // only add if there is at least one Bible
        if (bibles.size > 0) {
            val thisLanguage = Language(
                    language.identifier,
                    language.title,
                    language.direction,
                    bibles
            )
            languages.add(thisLanguage)
        }
    }

    // create the Bible data
    val bibleData = BibleData(
            languages
    )

    return bibleData
}