package dagger

import retrofit.Door43ApiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class ServiceModule {
    val DOOR43_API_ENDPOINT = "https://api.door43.org/v3/"

    @Provides
    @Singleton
    fun retrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(DOOR43_API_ENDPOINT)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun door43Service(retrofit: Retrofit): Door43ApiService =
            retrofit.create(Door43ApiService::class.java)

}