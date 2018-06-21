import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun main(args: Array<String>) {
    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.door43.org/v3/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    val door43 = retrofit.create(Door43ApiService::class.java)

    val catalog = door43.getCatalog().execute().body()

    // print out some demo data
    // check for null response
    if (catalog != null) {
        val bibleData = processBibleData(catalog)
        for (language in bibleData.languages) {
            println(language.title)
            for (bible in language.bibles) {
                println("    " + bible.title)
            }
        }
    }
}


