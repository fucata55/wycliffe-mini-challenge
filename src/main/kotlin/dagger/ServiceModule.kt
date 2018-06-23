package dagger

import retrofit.Door43ApiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class ServiceModule {
    val DOOR43_API_ENDPOINT = "https://api.door43.org/v3/"

    @Provides
    @Singleton
    fun provideDoor43ApiService() : Door43ApiService =
            Retrofit.Builder()
                    .baseUrl(DOOR43_API_ENDPOINT)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava adapter to Observable
                    .addConverterFactory(MoshiConverterFactory.create())       // moshi JSON parse
                    .build()
                    .create(Door43ApiService::class.java)

    // no provides for Door43 object since Dagger can infer the setup

}