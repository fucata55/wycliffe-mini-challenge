import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    // create a door43 object
    val door43 = Door43()

    // grab the catalog
    door43.fetchCatalog()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe {
                for (language in it.getLanguageList()) {
                    println(language.title)
                }
            }

    TimeUnit.SECONDS.sleep(10)
}


