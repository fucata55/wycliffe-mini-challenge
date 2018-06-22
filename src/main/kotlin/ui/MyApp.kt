import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import tornadofx.*
class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()
    val selectedLang = SimpleStringProperty()
    val selectedBook = SimpleStringProperty()
    val selectedChap = SimpleStringProperty()

    var currentBible : BibleMetadata? = null

    val door43 = Door43();

    override val root = form {
        //gets catalog
        var catalog: CatalogMetadata?
        catalog = door43.fetchCatalog()
        //gets language names and associated data(an arraylist) from catalog
        val langsdata = catalog?.getLanguageList()
        //gets just names of languages as strings
        //(map does the same operation to each elem of the list, storing the result in a new list)
        val langsnames = langsdata?.map { it.title }
        //we make a drop-down menu out of a combobox and tell the combobox to store the selection of the user in selected
        val langBox = combobox(selectedLang, langsnames)
        val bookBox = combobox(selectedBook,listOf("Select a language"));
        val chapBox = combobox(selectedChap, listOf("Select a book"));

        selectedLang.onChange { languageTitle ->
            val theLanguage = langsdata?.filter { it.title == languageTitle }?.firstOrNull()
            if (theLanguage != null) {
                currentBible = theLanguage.bibles["ulb"]
                if (currentBible != null) {
                    val bookNames = currentBible!!.books.map { it.title }
                    bookBox.items = FXCollections.observableList(bookNames)
                }
            }
        }

        selectedBook.onChange { bookTitle ->
            if (currentBible != null) {
                val thisBook = currentBible!!.books.filter { it.title == bookTitle }.firstOrNull()
                if (thisBook != null) {
                    val bookContents = door43.getBook(currentBible!!, thisBook.identifier)
                    if (bookContents != null) {
                        val chapterNumbers = (1..bookContents.chapters.size).map { it.toString() }
                        chapBox.items = FXCollections.observableList(chapterNumbers)
                    }
                }
            }
        }
    }
}


class MyController: Controller() {
    fun writeToDb(inputValue: String) {
        println("Writing $inputValue to database!")
    }
}