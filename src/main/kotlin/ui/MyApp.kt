import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import tornadofx.*
class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()
    val selectedLang = SimpleStringProperty()
    val selectedBook = SimpleStringProperty()
    val selectedChap = SimpleStringProperty()

    var langsData : List<LanguageMetadata> = listOf()
    var langsNames : List<String> = listOf()

    var currentBible : BibleMetadata? = null
    var currentBook : Book? = null

    var catalogDisposable : Disposable? = null
    var currentBookDisposable : Disposable? = null

    val door43 = Door43()

    var textField : TextArea? = null

    override val root = borderpane {
        top = hbox {
            //we make a drop-down menu out of a combobox and tell the combobox
            // to store the selection of the user in selected
            val langBox = combobox(selectedLang, langsNames)
            val bookBox = combobox(selectedBook,listOf("Select a language")); // create book combo box
            val chapBox = combobox(selectedChap, listOf("Select a book"));    // create chapter combo box

            // start with all combobox disabled
            langBox.isDisable = true
            bookBox.isDisable = true
            chapBox.isDisable = true

            //gets catalog
            var catalog: CatalogMetadata?
            catalogDisposable = door43.fetchCatalog()
                    .subscribeOn(Schedulers.io())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe {
                        //gets language names and associated data(an arraylist) from catalog
                        langsData = it.getLanguageList()
                        //gets just names of languages as strings
                        //(map does the same operation to each elem of the list, storing the result in a new list)
                        langsNames = langsData.map { it.title }
                        langBox.items = FXCollections.observableList(langsNames) // put the names in the box
                        langBox.isDisable = false // re enable the box
                    }

            selectedLang.onChange { languageTitle ->
                val theLanguage = langsData.filter { it.title == languageTitle }.firstOrNull()
                if (theLanguage != null) {
                    currentBible = theLanguage.bibles["ulb"]
                    if (currentBible != null) {
                        val bookNames = currentBible!!.books.map { it.title }
                        bookBox.items = FXCollections.observableList(bookNames) // put books in the box
                        bookBox.isDisable = false // re enable books
                        bookBox.selectionModel.selectFirst() // move to first book
                    }
                }
            }

            selectedBook.onChange { bookTitle ->
                if (currentBible != null) {
                    val thisBook = currentBible!!.books.filter { it.title == bookTitle }.firstOrNull()
                    if (thisBook != null) {
                        chapBox.isDisable = true // disable the box until the books are loaded
                        val currentBookObservable = door43.getBook(currentBible!!, thisBook.identifier)
                        if (currentBookObservable != null) {
                            currentBookDisposable = currentBookObservable
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(JavaFxScheduler.platform())
                                    .subscribe {
                                        currentBook = it
                                        val chapterNumbers = (1..currentBook!!.chapters.size).map { it.toString() }
                                        chapBox.items = FXCollections.observableList(chapterNumbers)
                                        chapBox.isDisable = false // re enable chapBox
                                        chapBox.selectionModel.selectFirst() // move to first chapter
                                    }
                        }
                    }
                }
            }

            // update text field when chapter changes
            selectedChap.onChange {
                if (it != null) {
                    val chapterNumber = it.toInt()
                    if (currentBook != null) {
                        val chapterText = currentBook!!.chapters[chapterNumber - 1].text
                        textField?.text = chapterText
                    }
                }
            }
        }

        center = hbox {
            // create a textfield for the Scripture
            textField = textarea {
                editableProperty().set(false)
                wrapTextProperty().set(true)
            }
        }
    }
}


class MyController: Controller() {
    fun writeToDb(inputValue: String) {
        println("Writing $inputValue to database!")
    }
}