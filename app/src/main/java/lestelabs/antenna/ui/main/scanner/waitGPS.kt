package lestelabs.antenna.ui.main.scanner

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity

fun waitGPS(context: Context?){
    var gps_enabled = false
    var lm: LocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!gps_enabled) {
        Log.d("cfauli", "GPS NOT enabled")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(context, intent, null)
    }

    Log.d("cfauli", "GPS enabled" + gps_enabled.toString())


    while (!gps_enabled) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
    }

}