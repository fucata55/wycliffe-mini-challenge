import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXTextArea
import dagger.DaggerSingletonComponent
import dagger.ServiceModule
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.NodeOrientation
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import javafx.scene.web.WebView
import tornadofx.*
import javax.inject.Inject
import model.*
import retrofit.Door43
import java.util.*

//this class represents the app itself
//the first view is MyView; the view passed in is the view with which we start
class MyApp: App(MyView::class)


//this class is seperate from the MyApp class but we put them together so we could see them better
class MyView : View() {
    // View() IS the controller. See TornadoFX lead dev
    // https://stackoverflow.com/questions/50977995/kotlin-fxml-file-controller/50978260
    override val root : BorderPane  by fxml("/MyView.fxml")

    private val selectedLang = SimpleStringProperty()
    private val selectedBook = SimpleStringProperty()
    private val selectedChap = SimpleStringProperty()
    private val selectedVersion = SimpleStringProperty()
    private val selectedTextSize = SimpleStringProperty()

    // pull in elements from the FXML UI file
    private val langBox : JFXComboBox<String> by fxid()
    private val versionBox : JFXComboBox<String> by fxid()
    private val bookBox : JFXComboBox<String> by fxid()
    private val chapBox : JFXComboBox<String> by fxid()
    private val textSizeBox: JFXComboBox<String> by fxid()
    private val textArea : JFXTextArea by fxid()

    //whether or not we are in night view
    private var nightView = false
    private var fontSizeNum: Int = 22
    private var textBackColor = "white"
    private var textColor = "black"

    // a bit of a hacky solution so that going to the previous book will load the last chapter
    private var wasPrevious : Boolean = false

    private var langsData : List<LanguageMetadata> = listOf()
    private var langsNames : List<String> = listOf("Loading...")

    private var currentBible : BibleMetadata? = null
    private var currentLanguage : LanguageMetadata? = null
    private var currentBook : Book? = null
    private var currentChapter: Chapter? = null

    // todo: clean up disposables
    private var catalogDisposable : Disposable? = null
    private var currentBookDisposable : Disposable? = null

    // use dagger to inject door43 (but wait until init block)
    //note that inject only appears above declarations
    @Inject
    lateinit var door43 : Door43

    //no functions can get run in the class outside of blocks and functions; you can only declare variables
    //but the init block can contain functions and it runs right away automatically when the app is launched
    init {
        // set the window title
        title = "Door43 Scripture Reader"

        // set minimum size
        setWindowMinSize(600,400)

        // use dagger to inject Door43
        door43 = DaggerSingletonComponent.builder()
                .serviceModule(ServiceModule())
                .build()
                .door43()

        setupComboBoxPropertyBindings() // bind the properties to the combo boxes

        loadCatalog() // load the door43 catalog

        //puts items into textSizeBox because not dependent on what we get from catalog
        //needs to be an observableList, a list of items that you can watch to see if they change
        textSizeBox.items = FXCollections.observableList(listOf("Very Small Text", "Small Text", "Medium Text", "Large Text"))
    }

    private fun setupComboBoxPropertyBindings() {
        // bind the properties
        langBox.bind(selectedLang)
        versionBox.bind(selectedVersion)
        bookBox.bind(selectedBook)
        chapBox.bind(selectedChap)
        textSizeBox.bind(selectedTextSize)
        //choose small text by default
        textSizeBox.selectionModel?.select("Small Text")
        // define the on Change handlers
        //basically adding an on-change listener to each combobox
        //except that the combobox automatically changes the variable that is bound to it
        //and we are listening for change to that variable, not change to the combobox itself
        selectedLang.onChange {
            if (it != null) {
                processLanguageChanged(it)
            }
        }
        selectedVersion.onChange {
            if (it != null) {
                processVersionChanged(it)
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
        selectedTextSize.onChange {
            if (it!= null) {
                processTextSizeChanged(it)
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
//                    println(langsNames)
                    langBox.items = FXCollections.observableList(langsNames) // put the names in the box
                    langBox.isDisable = false // re enable the box

                    // load english as the default if available
                    if ("English" in langsNames)
                    {
                        langBox.selectionModel?.select("English")
                    } else {
                        langBox.selectionModel?.selectFirst()
                    }
                },{
                    // on Error
                    it.printStackTrace()
                    langBox.items = FXCollections.observableList(listOf("Error"))
                    langBox.selectionModel?.selectFirst()
                })
    }

    private fun processLanguageChanged(languageTitle: String) {
        currentLanguage = langsData.filter { it.title == languageTitle }.firstOrNull()
        if (currentLanguage != null) {
            //check if it is oriya or arabic and load appropriate font if so
            when(currentLanguage!!.identifier) {
                "or" -> {
                    var NotoOr = loadFont("../../resources/NotoSansOriya-Regular.ttf", fontSizeNum);
                    textArea.font = NotoOr;
                    render();
                }
                "ar" -> {
                    var NotoOr = loadFont("../../resources/NotoSansArabic-ExtraCondensedLight.ttf", fontSizeNum);
                    textArea.font = NotoOr;
                    render();
                }
                else -> {
                    textArea.font = Font.font("System Regular", fontSizeNum.toDouble());
                }
            }

            val versionNames = currentLanguage!!.bibles.keys.toList()
            versionBox.items = FXCollections.observableList(versionNames)
            versionBox.isDisable = false
            versionBox.selectionModel?.selectFirst()
        }
    }

    private fun processVersionChanged(versionTitle: String) {
        if (currentLanguage != null) {
            currentBible = currentLanguage!!.bibles[versionTitle]
            if (currentBible != null) {
                val bookNames = currentBible!!.books.map { it.title }
                bookBox.items = FXCollections.observableList(bookNames) // put books in the box
                bookBox.isDisable = false // re enable books
                bookBox.selectionModel?.selectFirst() // move to first book
            }
        }
    }

    private fun processBookChanged(bookTitle: String, previous: Boolean) {
        if (currentBible != null) {
            chapBox.items = FXCollections.observableList(listOf("Loading..."))
            chapBox.selectionModel?.selectFirst()

            val thisBook = currentBible!!.books.filter { it.title == bookTitle }.firstOrNull()
            if (thisBook != null) {
                chapBox.isDisable = true // disable the box until the books are loaded
                val currentBookObservable = door43.getBook(currentBible!!, thisBook.identifier)
                if (currentBookObservable != null) {
                    currentBookDisposable = currentBookObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(JavaFxScheduler.platform())
                            .subscribe({
                                currentBook = it
                                val chapterNumbers = (1..currentBook!!.chapters.size).map { it.toString() }
                                chapBox.items = FXCollections.observableList(chapterNumbers)
                                chapBox.isDisable = false // re enable chapBox

                                // figure out if we should load the first chapter
                                // or if this is a result of arrowing to a previous book,
                                // in which case we should load the last chapter
                                if (previous)
                                {
                                    chapBox.selectionModel?.selectLast()
                                }
                                else
                                {
                                    chapBox.selectionModel?.selectFirst()
                                }
                            },{
                                // on Error
                                // handle it semi-gracefully
                                chapBox.items = FXCollections.observableList(listOf("Error"))
                                chapBox.selectionModel?.selectFirst()
                            })
                }
            }
        }
    }

    private fun processChapterChanged(chapterInput: String)
    {
        try {
            val chapterNumber = chapterInput.toInt()
            if (currentBook != null) {
                currentChapter = currentBook!!.chapters[chapterNumber - 1]
                render();
            }
        } catch (err: NumberFormatException) {
            // not a number selected
            // fail silently
        }

    }

    private fun processTextSizeChanged(textSizeInput: String) {
        newFontSize(textSizeInput)
    }

    fun nextChapter() {
        if (currentBook != null && currentChapter != null) {
            // try to increment chapter number
            val nextChapterNumber = currentChapter!!.number + 1
            if (nextChapterNumber > currentBook!!.chapters.size) {
                // on to the next book
                bookBox.selectionModel?.selectNext()
            } else {
                // we are good in this book
                chapBox.selectionModel?.selectNext()
            }

        }
    }

    fun previousChapter() {
        if (currentBook != null && currentChapter != null) {
            // try to increment chapter number
            val nextChapterNumber = currentChapter!!.number - 1
            // make sure we don't go back from the first book
            if (nextChapterNumber <= 0 && bookBox.selectedItem != bookBox.items[0]) {
                // on to the previous book
                wasPrevious = true
                bookBox.selectionModel?.selectPrevious()
            } else {
                // we are good in this book
                chapBox.selectionModel?.selectPrevious()
            }

        }
    }

    fun changeView() {
        //change view colors to respond night reader mode
        if(!nightView) {
            textBackColor = "#383838"
            textColor = "white"
        } else {
            textBackColor = "white"
            textColor = "black"
        }
        render()
        nightView = !nightView
    }

    private fun newFontSize(fontSize: String) {
        if(fontSize == "Small Text") {
            fontSizeNum = 22
        } else if (fontSize == "Medium Text") {
            fontSizeNum = 28
        } else if (fontSize == "Large Text") {
            fontSizeNum = 32
        } else if (fontSize == "Very Small Text") {
            fontSizeNum = 16
        }
        render()
    }

    private fun render() {
        //webView: tornadoFX component that can display fxml like it's on a webpage
        //engine.loadContent(blablabla): tell the rendering engine to display blablabla
        //blablabla is an html string
        textArea.text = currentChapter?.text
//        println("font before assignment in render " + textArea.font);
        textArea.font = Font.font(textArea.font.family, fontSizeNum.toDouble())
//        println("font afterward " + textArea.font);
        if (currentLanguage?.direction == "ltr") {
            textArea.nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
        } else {
            textArea.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
        }

    }
}
