package lestelabs.antenna.ui.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import lestelabs.antenna.ui.main.data.ApplicationDatabase
import lestelabs.antenna.ui.main.data.SitesInteractor

class MyApplication : Application() {


    // To access the content use the reference (application as MyApplication)
    companion object {
        var ctx: Context? = null
        var internetOn: Boolean? = null
        lateinit var sitesInteractor: SitesInteractor
    }

    override fun onCreate() {
        super.onCreate()

        ctx = applicationContext
        internetOn = hasInternetConnection()

        // Init Database
        val database = Room.databaseBuilder(applicationContext,
            ApplicationDatabase::class.java, "app_database").build()
        // Init BooksInteractor
        sitesInteractor = SitesInteractor(database.siteDao())
    }


    fun hasInternetConnection(): Boolean? {
        // Check Internet Check connection
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.isDefaultNetworkActive
    }
}