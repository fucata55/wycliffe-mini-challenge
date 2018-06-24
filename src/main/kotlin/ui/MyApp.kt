import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import dagger.DaggerSingletonComponent
import dagger.ServiceModule
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import javafx.scene.web.WebView
import tornadofx.*
import javax.inject.Inject
import model.*
import retrofit.Door43

class MyApp: App(MyView::class)

class MyView : View() {
    // View() IS the controller. See TornadoFX lead dev
    // https://stackoverflow.com/questions/50977995/kotlin-fxml-file-controller/50978260
    override val root : BorderPane by fxml("/MyView.fxml")

    val selectedLang = SimpleStringProperty()
    val selectedBook = SimpleStringProperty()
    val selectedChap = SimpleStringProperty()

    // elements from the FXML UI file
    val langBox : JFXComboBox<String> by fxid()
    val bookBox : JFXComboBox<String> by fxid()
    val chapBox : JFXComboBox<String> by fxid()
    val webView : WebView by fxid()

    // a bit of a hacky solution so that going to the previous book will load the last chapter
    var wasPrevious : Boolean = false

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
        // set the window title
        title = "Door43 Scripture Reader"
        // use dagger to inject Door43
        door43 = DaggerSingletonComponent.builder()
                .serviceModule(ServiceModule())
                .build()
                .door43()

        setupComboBoxPropertyBindings() // bind the properties to the combo boxes

        loadCatalog() // load the door43 catalog
    }

    private fun setupComboBoxPropertyBindings() {
        // bind the properties
        langBox.bind(selectedLang)
        bookBox.bind(selectedBook)
        chapBox.bind(selectedChap)
        // define the on Change handlers
        selectedLang.onChange {
            if (it != null) {
                processLanguageChanged(it)
            }
        }
        selectedBook.onChange {
            if (it != null) {
                processBookChanged(it, wasPrevious)
                wasPrevious = false
            }
        }
        selectedChap.onChange {
            if (it != null) {
                processChapterChanged(it)
            }
        }
    }

    private fun loadCatalog() {
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
                    langBox?.items = FXCollections.observableList(langsNames) // put the names in the box
                    langBox?.isDisable = false // re enable the box

                    // load english as the default if available
                    if ("English" in langsNames)
                    {
                        langBox?.selectionModel?.select("English")
                    } else {
                        langBox?.selectionModel?.selectFirst()
                    }
                },{
                    // on Error
                    langBox?.items = FXCollections.observableList(listOf("Error"))
                    langBox?.selectionModel?.selectFirst()
                })
    }

    fun processLanguageChanged(languageTitle: String) {
        val theLanguage = langsData.filter { it.title == languageTitle }.firstOrNull()
        if (theLanguage != null) {
            currentBible = theLanguage.bibles["ulb"]
            if (currentBible != null) {
                val bookNames = currentBible!!.books.map { it.title }
                bookBox?.items = FXCollections.observableList(bookNames) // put books in the box
                bookBox?.isDisable = false // re enable books
                bookBox?.selectionModel?.selectFirst() // move to first book
            }
        }
    }

    fun processBookChanged(bookTitle: String, previous: Boolean) {
        if (currentBible != null) {
            chapBox?.items = FXCollections.observableList(listOf("Loading..."))
            chapBox?.selectionModel?.selectFirst()

            val thisBook = currentBible!!.books.filter { it.title == bookTitle }.firstOrNull()
            if (thisBook != null) {
                chapBox?.isDisable = true // disable the box until the books are loaded
                val currentBookObservable = door43.getBook(currentBible!!, thisBook.identifier)
                if (currentBookObservable != null) {
                    currentBookDisposable = currentBookObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(JavaFxScheduler.platform())
                            .subscribe({
                                currentBook = it
                                val chapterNumbers = (1..currentBook!!.chapters.size).map { it.toString() }
                                chapBox?.items = FXCollections.observableList(chapterNumbers)
                                chapBox?.isDisable = false // re enable chapBox

                                // figure out if we should load the first chapter
                                // or if this is a result of arrowing to a previous book,
                                // in which case we should load the last chapter
                                if (previous)
                                {
                                    chapBox?.selectionModel?.selectLast()
                                }
                                else
                                {
                                    chapBox?.selectionModel?.selectFirst()
                                }
                            },{
                                // on Error
                                // handle it semi-gracefully
                                chapBox?.items = FXCollections.observableList(listOf("Error"))
                                chapBox?.selectionModel?.selectFirst()
                            })
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
                // todo: add html/css to set font size and other styling
                val chapterText = "${currentChapter!!.text}"
                webView.engine.loadContent(chapterText)
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
                bookBox?.selectionModel?.selectNext()
            } else {
                // we are good in this book
                chapBox?.selectionModel?.selectNext()
            }

        }
    }

    fun previousChapter() {
        if (currentBook != null && currentChapter != null) {
            // try to increment chapter number
            var nextChapterNumber = currentChapter!!.number - 1
            if (nextChapterNumber <= 0) {
                // on to the previous book
                wasPrevious = true
                bookBox?.selectionModel?.selectPrevious()
            } else {
                // we are good in this book
                chapBox?.selectionModel?.selectPrevious()
            }

        }
    }

    fun makeTextBigger() {
        //val currentSize = textArea.font.size
        //textArea.font = Font.font(currentSize + 5)
    }

    fun makeTextSmaller() {
        //val currentSize = textArea.font.size
        //textArea.font = Font.font(currentSize - 5)
    }


}