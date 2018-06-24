//the dagger component

//Dagger makes sure that the dependencies in our app are all set up in the same way:
//MyApp needs Door43 and Door43 needs API Service
//Usually, we would create Door43 to create the API Service and MyApp would create Door43,
//but we use dagger to make all the parts of MyApp and Door43 that depend on something outside their own class

//Component stores all the parts made in the module
//We look at the component to know what we can get from dagger
package dagger
import retrofit.Door43ApiService
import javax.inject.Singleton
import retrofit.Door43

// provide methods to get the objects
@Component(modules = [ServiceModule::class])
@Singleton
interface SingletonComponent {
    //We can get the Door43 API Service and a Door43 object from dagger.
    //If part of our code asks for one of these, dagger will either make it or,
    //if dagger has made it already, dagger will just pass it along
    //so at most one Door43 API Service and one Door43 object exists at any given time
    //(hence Singleton in the header)
    fun door43Service() : Door43ApiService
    fun door43() : Door43
}