import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*
class MyApp: App(MyView::class)

class MyView : View() {
    val controller: MyController by inject()
    val input = SimpleStringProperty()

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
            val texasCities = FXCollections.observableArrayList("Austin",
                    "Dallas","Midland", "San Antonio","Fort Worth")

            combobox<String> {
                items = texasCities
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

