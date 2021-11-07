package lestelabs.antenna.ui.main

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import lestelabs.antenna.ui.main.data.ApplicationDatabaseFinal
import lestelabs.antenna.ui.main.data.ServersInteractor
import lestelabs.antenna.ui.main.data.SitesInteractor

class MyApplication : Application() {


    // To access the content use the reference (application as MyApplication)
    companion object {
        var ctx: Context? = null
        var internetOn: Boolean? = null
        lateinit var sitesInteractor: SitesInteractor
        lateinit var serversInteractor: ServersInteractor
    }

    override fun onCreate() {
        super.onCreate()

        ctx = applicationContext
        internetOn = hasInternetConnection()

        // Init Database
        val database = Room.databaseBuilder(applicationContext,
            ApplicationDatabaseFinal::class.java, "app_database").build()
        // Init SitesInteractor
        sitesInteractor = SitesInteractor(database.siteDao())
        // Init ServersInteractor
        serversInteractor = ServersInteractor(database.serverDao())
    }


    fun hasInternetConnection(): Boolean? {
        // Check Internet Check connection
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = cm?.activeNetwork
        return (activeNetwork != null)
    }
}