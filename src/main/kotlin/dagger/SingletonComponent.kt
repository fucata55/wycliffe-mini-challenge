package dagger

import retrofit.Door43ApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Component(modules = [ServiceModule::class])
@Singleton
interface SingletonComponent {
    fun retrofit() : Retrofit
    fun door43Service() : Door43ApiService
}