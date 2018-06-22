import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

// this is the information Retrofit needs to create the API service
// it tells Retrofit where the API calls should go and what data objects should be returned
interface Door43ApiService {
    @GET("catalog.json")
    fun getCatalog() : Observable<Door43Response>

    @GET
    fun getUsfmFromUrl(@Url url: String) : Observable<ResponseBody>
}