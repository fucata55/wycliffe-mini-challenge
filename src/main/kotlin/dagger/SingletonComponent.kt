package dagger
import retrofit.Door43ApiService
import javax.inject.Singleton
import retrofit.Door43

// provide methods to get the objects
@Component(modules = [ServiceModule::class])
@Singleton
interface SingletonComponent {
    fun door43Service() : Door43ApiService
    fun door43() : Door43
}