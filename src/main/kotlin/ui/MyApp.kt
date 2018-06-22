import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import tornadofx.*
class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()
    val input = SimpleStringProperty()
    val door43 = Door43();

    override val root = form {
        //gets catalog
        var catalog: CatalogMetadata?;
        catalog = door43.fetchCatalog();
        //gets language names and associated data(an arraylist) from catalog
        val langsdata = catalog?.getLanguageList();
        //gets just names of languages as strings
        //(map does the same operation to each elem of the list, storing the result in a new list)
        val langsnames = langsdata?.map { it.title };
        //we make a drop-down menu out of a combobox and tell the combobox to store the selection of the user in selected
        var selectedLang = SimpleStringProperty();
        val langBox = combobox(selectedLang, langsnames);
        val selectedBook = SimpleStringProperty();
        var bookBox = combobox(selectedBook,listOf("Select a language"));
        val selectedChap = SimpleStringProperty();
        var chapBox = combobox(selectedChap, listOf("Select a book"));

        selectedLang.addListener{observable, oldValue, newValue ->
            println("hi")
        }
        selectedLang.onChange{ println("hello") }
        langBox.itemsProperty().onChange { println("hey") }
        langBox.addEventHandler()


        //make a submit button that tells the program when to act upon what is stored in submit
        /*
        var myLang: String;

        button("SUBMIT LANG") {
            action {
                if (selectedLang.value == null) {
                    println("No selection; defaults to Eng")
                    myLang = "English";
                } else {
                    myLang = selectedLang.value;
                }
                var metaDataMyLang: LanguageMetadata?;
                //gets first language in list langsdata that has name selected
                metaDataMyLang = langsdata?.filter { it.title == myLang }?.get(0);
                //holds bible data:  has ulb's data
                var bible = metaDataMyLang?.bibles?.get("ulb");
                if(bible != null) {
                    println(bible.books);
                }
            }
        }
        /*
        button("SUBMIT BOOK") {
            action {
                if (selected.value == null) {
                    println("No selection; defaults to Eng")
                    myLang = "English";
                } else {
                    myLang = selected.value;
                }
                var metaDataMyLang: LanguageMetadata?;
                //gets first language in list langsdata that has name selected
                metaDataMyLang = langsdata?.filter { it.title == myLang }?.get(0);
                //holds bible data:  has ulb's data
                var bible = metaDataMyLang?.bibles?.get("ulb");
                if(bible != null) {
                    println(bible.books);
                }
            }
        }
        button("SUBMIT CHAPTER") {
            action {
                if (selected.value == null) {
                    println("No selection; defaults to Eng")
                    myLang = "English";
                } else {
                    myLang = selected.value;
                }
                var metaDataMyLang: LanguageMetadata?;
                //gets first language in list langsdata that has name selected
                metaDataMyLang = langsdata?.filter { it.title == myLang }?.get(0);
                //holds bible data:  has ulb's data
                var bible = metaDataMyLang?.bibles?.get("ulb");
                if(bible != null) {
                    println(bible.books);
                }
            }
        }
*/*/

    }
}


class MyController: Controller() {
    fun writeToDb(inputValue: String) {
        println("Writing $inputValue to database!")
    }
}