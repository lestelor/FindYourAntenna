package lestelabs.antenna.ui.main



import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import lestelabs.antenna.ui.main.MainActivity.Companion.PERMISSION_REQUEST_CODE
import lestelabs.antenna.ui.main.MyApplication.Companion.ctx
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.loadCellInfo
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess


private lateinit var telephonyManager: TelephonyManager
private var pDevice: DevicePhone? = DevicePhone()

fun checkAllPermission(activity: Activity, callback: (Boolean) -> Unit) {

    if ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        Thread.sleep(1000)
    }

    while ((ctx?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) } != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(ctx!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        Thread.sleep(1000)
    }
    callback(true)
}