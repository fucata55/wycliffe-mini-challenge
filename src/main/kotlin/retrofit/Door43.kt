import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import model.*
import retrofit.Door43ApiService
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Door43 @Inject constructor(var apiService: Door43ApiService) {

    fun fetchCatalog() : Observable<CatalogMetadata>  {
        // grab the catalog data from the API
        return apiService.getCatalog()
                .map {
                    parseDoor43Response(it)
                }
    }

    fun getBook(bibleMetadata: BibleMetadata, bookIdentifier: String) : Observable<Book>? {
        // bookIndex must be between 0 and 65
        // bookChapter must be a valid number
        // try to find the book data
        var usfmBookMetadata = bibleMetadata.books.filter { it.identifier == bookIdentifier }.first()
        // we found the book!
        return apiService.getUsfmFromUrl(usfmBookMetadata.url).subscribeOn(Schedulers.io())
                .map {
                    parseUsfmBook(it.string())
                }
    }

    private fun parseDoor43Response(catalog: Door43Response): CatalogMetadata {
        // we want to extract the USFM Bible information from the response
        val languages = HashMap<String, LanguageMetadata>()
        for (language in catalog.languages) {
            val bibles = HashMap<String, BibleMetadata>()

            // search for ULB Bible
            for (resource in language.resources) {
                // check if ULB Bible
                if (resource.identifier == "ulb") {
                    val books = ArrayList<UsfmBookMetadata>()
                    // we need to get the book data
                    for (project in resource.projects) {
                        // this should be a book of the Bible
                        // we need the USFM data
                        for (format in project.formats) {
                            // check for USFM
                            if (format.format == "text/usfm") {
                                // found the USFM format
                                // save the book information
                                val thisBook = UsfmBookMetadata(
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
                    val thisBible = BibleMetadata(
                            resource.identifier,
                            resource.title,
                            books
                    )
                    // add the Bible to the map
                    bibles[thisBible.identifier] = thisBible
                }
            }
            // only add if there is at least one Bible
            if (bibles.size > 0) {
                val thisLanguage = LanguageMetadata(
                        language.identifier,
                        language.title,
                        language.direction,
                        bibles
                )
                // add the language to the map
                languages[thisLanguage.identifier] = thisLanguage
            }
        }

        // create the Bible data
        return CatalogMetadata(
                languages
        )
    }

    // parse the usfm file
    private fun parseUsfmBook(usfm: String): Book {
        var lines = usfm.split("\n")

        // temporary variables to store data as we go
        var thisBookTitle = ""
        var thisChapterNumber = 0
        var thisChapterText = ""
        var thisBookChapters = ArrayList<Chapter>()

        for (line in lines) {
            if (line.startsWith("\\h")) {
                thisBookTitle = line.removePrefix("\\h ")
            } else if (line.startsWith("\\c")) {
                // new chapter
                if (thisChapterNumber > 0) {
                    // we have at least one whole chapter
                    // create the chapter object
                    var chapter = Chapter(thisChapterNumber, thisChapterText)
                    thisBookChapters.add(chapter)

                    // remove chapter details
                    thisChapterText = ""
                }
                thisChapterNumber += 1
            } else if (line.startsWith("\\v")) {
                // show each verse on a new line
                thisChapterText += line.removePrefix("\\v") + "\n"
            }
        }

        // create the book
        var book = Book(thisBookTitle, thisBookChapters)

        return book
    }
}