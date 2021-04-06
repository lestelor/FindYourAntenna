package lestelabs.antenna.ui.main

import android.app.Activity
import android.app.Application
import android.content.Context

class MyApplication : Application() {

    // To access the content use the reference (application as MyApplication)
    companion object {
        var ctx: Context? = null

    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

    }
}