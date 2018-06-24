//the dagger module

//Dagger makes sure that the dependencies in our app are all set up in the same way:
//MyApp needs Door43 and Door43 needs API Service
//Usually, we would create Door43 to create the API Service and MyApp would create Door43,
//but we use dagger to make all the parts of MyApp and Door43 that depend on something outside their own class

//The module tells dagger how to create each individual parts of MyApp and Door43
//Doesn't make anything in the API Service; nothing in the API Service depends on anything else
package dagger

import retrofit.Door43ApiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

//
@Module
class ServiceModule {
    val DOOR43_API_ENDPOINT = "https://api.door43.org/v3/"

    //tells dagger what to give any part of MyApp that asks for the API Service
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