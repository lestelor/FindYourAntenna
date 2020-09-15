package lestelabs.antenna.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun checkAllPermission(context:Context, callback: (Boolean) -> Unit) {
    while ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        Thread.sleep(1000)
    }
    callback(true)
}