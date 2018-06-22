import javafx.beans.property.SimpleStringProperty
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
        val selected = SimpleStringProperty();
        combobox(selected, langsnames);
        //make a submit button that tells the program when to act upon what is stored in submit
        var myLang: String;
        button("SUBMIT") {
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

//                books = bible.books[ind].title

                /*
                combobox<String> {
                    items = FXCollections.observableList(langsnames);
                }
                */

        }
    }


class MyController: Controller() {
    fun writeToDb(inputValue: String) {
        println("Writing $inputValue to database!")
    }
}