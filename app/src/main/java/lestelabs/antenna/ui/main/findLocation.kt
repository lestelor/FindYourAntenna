package lestelabs.antenna.ui.main

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.Location
import android.location.LocationManager
import android.telephony.TelephonyManager
import com.google.android.gms.location.LocationServices
import lestelabs.antenna.ui.main.MyApplication.Companion.ctx

private lateinit var lm:LocationManager
private lateinit var fusedLocationClient: FusedLocationProviderClient
private var myLocation:Location? = null

fun findLocation(activity: MainActivity, callback: (Location?) -> Unit) {

    fusedLocationClient = ctx?.let { LocationServices.getFusedLocationProviderClient(it) }!!
    checkAllPermission(activity) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                callback(location)
            }
    }
}


