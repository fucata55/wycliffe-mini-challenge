import dagger.DaggerSingletonComponent
import dagger.ServiceModule
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import tornadofx.*
import javax.inject.Inject
import model.*
import retrofit.Door43

class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()

    val selectedLang = SimpleStringProperty()
    val selectedBook = SimpleStringProperty()
    val selectedChap = SimpleStringProperty()

    var langBox : ComboBox<String>? = null
    var bookBox : ComboBox<String>? = null
    var chapBox : ComboBox<String>? = null

    var textField : TextArea? = null

    // a bit of a hacky solution so that going to the previous book will load the last chapter
    var wasPrevious : Boolean = false


    override val root = borderpane {
        top = hbox {
            //we make a drop-down menu out of a combobox and tell the combobox
            // to store the selection of the user in selected
            langBox = combobox(selectedLang, controller.langsNames)
            bookBox = combobox(selectedBook,listOf("Select a book")); // create book combo box
            chapBox = combobox(selectedChap, listOf("Select a chapter"));    // create chapter combo box

            // start with all combobox disabled
            langBox?.isDisable = true
            bookBox?.isDisable = true
            chapBox?.isDisable = true

            langBox?.selectionModel?.selectFirst()
            bookBox?.selectionModel?.selectFirst()
            chapBox?.selectionModel?.selectFirst()


            selectedLang.onChange { languageTitle ->
                if (languageTitle != null) {
                    controller.processLanguageChanged(languageTitle)
                }
            }

            selectedBook.onChange { bookTitle ->
                if (bookTitle != null) {
                    controller.processBookChanged(bookTitle, wasPrevious)
                    wasPrevious = false
                }
            }

            // update text field when chapter changes
            selectedChap.onChange {
                if (it != null) {
                    controller.processChapterChanged(it)
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

        right = hbox {
            // add next button
            button(">") {
                action {
                    // go to next chapter
                    controller.nextChapter()
                }
            }
        }

        left = hbox {
            // add previous button
            button("<") {
                action {
                    // go to previous chapter
                    controller.previousChapter()
                }
            }
        }
    }
}


class MyController: Controller() {
    val view : MyView by inject()
    var langsData : List<LanguageMetadata> = listOf()
    var langsNames : List<String> = listOf("Loading...")

    var currentBible : BibleMetadata? = null
    var currentBook : Book? = null
    var currentChapter: Chapter? = null

    // todo: clean up disposables
    var catalogDisposable : Disposable? = null
    var currentBookDisposable : Disposable? = null

    // user dagger to inject door43
    @Inject
    lateinit var door43 : Door43

    init {
        // use dagger to inject Door43
        door43 = DaggerSingletonComponent.builder()
                .serviceModule(ServiceModule())
                .build()
                .door43()

        //gets catalog
        catalogDisposable = door43.fetchCatalog()
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({
                    //gets language names and associated data(an arraylist) from catalog
                    langsData = it.getLanguageList()
                    //gets just names of languages as strings
                    //(map does the same operation to each elem of the list, storing the result in a new list)
                    langsNames = langsData.map { it.title }
                    println(langsNames)
                    view.langBox?.items = FXCollections.observableList(langsNames) // put the names in the box
                    view.langBox?.isDisable = false // re enable the box

                    // load english as the default if available
                    if ("English" in langsNames)
                    {
                        view.langBox?.selectionModel?.select("English")
                    } else {
                        view.langBox?.selectionModel?.selectFirst()
                    }
                },{
                    // on Error
                    view.langBox?.items = FXCollections.observableList(listOf("Error"))
                    view.langBox?.selectionModel?.selectFirst()
                })
    }

    fun processLanguageChanged(languageTitle: String) {
        val theLanguage = langsData.filter { it.title == languageTitle }.firstOrNull()
        if (theLanguage != null) {
            currentBible = theLanguage.bibles["ulb"]
            if (currentBible != null) {
                val bookNames = currentBible!!.books.map { it.title }
                view.bookBox?.items = FXCollections.observableList(bookNames) // put books in the box
                view.bookBox?.isDisable = false // re enable books
                view.bookBox?.selectionModel?.selectFirst() // move to first book
            }
        }
    }

    fun processBookChanged(bookTitle: String, previous: Boolean) {
        if (currentBible != null) {
            view.chapBox?.items = FXCollections.observableList(listOf("Loading..."))
            view.chapBox?.selectionModel?.selectFirst()

            val thisBook = currentBible!!.books.filter { it.title == bookTitle }.firstOrNull()
            if (thisBook != null) {
                view.chapBox?.isDisable = true // disable the box until the books are loaded
                val currentBookObservable = door43.getBook(currentBible!!, thisBook.identifier)
                if (currentBookObservable != null) {
                    currentBookDisposable = currentBookObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(JavaFxScheduler.platform())
                            .subscribe {
                                currentBook = it
                                val chapterNumbers = (1..currentBook!!.chapters.size).map { it.toString() }
                                view.chapBox?.items = FXCollections.observableList(chapterNumbers)
                                view.chapBox?.isDisable = false // re enable chapBox

                                // figure out if we should load the first chapter
                                // or if this is a result of arrowing to a previous book,
                                // in which case we should load the last chapter
                                if (previous)
                                {
                                    view.chapBox?.selectionModel?.selectLast()
                                }
                                else
                                {
                                    view.chapBox?.selectionModel?.selectFirst()
                                }
                            }
                }
            }
        }
    }

    fun processChapterChanged(chapterInput: String)
    {
        try {
            val chapterNumber = chapterInput.toInt()
            if (currentBook != null) {
                currentChapter = currentBook!!.chapters[chapterNumber - 1]
                val chapterText = currentChapter!!.text
                view.textField?.text = chapterText
            }
        } catch (err: NumberFormatException) {
            // not a number selected
            // fail silently
        }

    }

    fun nextChapter() {
        if (currentBook != null && currentChapter != null) {
            // try to increment chapter number
            var nextChapterNumber = currentChapter!!.number + 1
            if (nextChapterNumber > currentBook!!.chapters.size) {
                // on to the next book
                view.bookBox?.selectionModel?.selectNext()
            } else {
                // we are good in this book
                view.chapBox?.selectionModel?.selectNext()
            }

        }
    }

    fun previousChapter() {
        if (currentBook != null && currentChapter != null) {
            // try to increment chapter number
            var nextChapterNumber = currentChapter!!.number - 1
            if (nextChapterNumber <= 0) {
                // on to the previous book
                view.wasPrevious = true
                view.bookBox?.selectionModel?.selectPrevious()
            } else {
                // we are good in this book
                view.chapBox?.selectionModel?.selectPrevious()
            }

        }
    }
}