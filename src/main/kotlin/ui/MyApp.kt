import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.intellij.lang.annotations.Language
import tornadofx.*
class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()
    val input = SimpleStringProperty()
    val door43 = Door43();

    override val root = form {
        fieldset {
            field("Input") {
                textfield(input)
            }

            button("Commit") {
                action {
                    controller.writeToDb(input.value)
                    input.value = ""
                }
            }
            var catalog = door43.fetchCatalog();
            if(catalog != null) {
                //gets language names and associated data(an arraylist)
                val langsdata = catalog.getLanguageList();
                //gets just names of languages
                //map does the same operation to each elem of the list, storing the result in a new list
                val langsnames = langsdata.map { it.title };
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
                        var metaDataMyLang: LanguageMetadata;
                        //gets first language in list langsdata that has name selected
                        metaDataMyLang = langsdata.filter { it.title == myLang }[0];
                        //holds bible data:  has ulb's data
                        var bible = metaDataMyLang.bibles["ulb"];
                        if(bible != null) {
                            println(bible.books);
                        }
                    }
                }

//                books = bible.books[ind].title

                //prints the books available in the ulb bible in the selected language
                //gets language data from language name



                /*
                combobox<String> {
                    items = FXCollections.observableList(langsnames);
                }
                */
                button("Choose this language") {
                    action {
                        controller.writeToDb(input.value)
                        input.value = "";
                    }
                }


            } else {
                combobox<String> {
                    items = FXCollections.observableList(listOf("no available langs"));
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


//class MasterView: View() {
//    override val root = borderpane {
//        top<TopView>()
//        bottom<BottomView>()
//    }
//}
class MasterView : View() {
    // Explicitly retrieve TopView
    val topView = find(TopView::class)
    // Create a lazy reference to BottomView
    val bottomView: BottomView by inject()

    override val root = borderpane {
        top = topView.root
        bottom = bottomView.root
    }
}

class TopView: View() {
    override val root = label("Top View")
}

class BottomView: View() {
    override val root = label("Bottom View")
}

