package retrofit

import retrofit2.Call
import retrofit2.http.*

// this is the information Retrofit needs to create the API service
// it tells Retrofit where the API calls should go and what data objects should be returned
interface Door43CdnService {
    @GET
    fun getUsfmFromUrl(@Url url: String)
}