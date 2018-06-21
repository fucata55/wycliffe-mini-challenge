import retrofit2.Call
import retrofit2.http.GET

// this is the information Retrofit needs to create the API service
// it tells Retrofit where the API calls should go and what data objects should be returned
interface Door43ApiService {
    @GET("catalog.json")
    fun getCatalog() : Call<Door43Response>
}