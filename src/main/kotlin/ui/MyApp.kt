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
    var currentBook : Book? = null

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

        //we make a drop-down menu out of a combobox and tell the combobox
        // to store the selection of the user in selected
        val langBox = combobox(selectedLang, langsnames)
        val bookBox = combobox(selectedBook,listOf("Select a language")); // create book combo box
        val chapBox = combobox(selectedChap, listOf("Select a book"));    // create chapter combo box

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
                    currentBook = door43.getBook(currentBible!!, thisBook.identifier)
                    if (currentBook != null) {
                        val chapterNumbers = (1..currentBook!!.chapters.size).map { it.toString() }
                        chapBox.items = FXCollections.observableList(chapterNumbers)
                    }
                }
            }
        }

        // create a textfield for the Scripture
        val textField = textarea {
            editableProperty().set(false)
            wrapTextProperty().set(true)
        }

        // update text field when chapter changes
        selectedChap.onChange {
            if (it != null) {
                val chapterNumber = it.toInt()
                if (currentBook != null) {
                    val chapterText = currentBook!!.chapters[chapterNumber - 1].text
                    textField.text = chapterText
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