package lestelabs.antenna.ui.main.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build

import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity

import java.util.*


public object scanWifi {

    var results: List<ScanResult>? = null
    val arrayList = ArrayList<String>()
    var wifiScanReceiver: BroadcastReceiver? = null
    val devices: MutableList<DeviceWiFi> = mutableListOf()
    var i=0

    fun scanwifi (context: FragmentActivity, wifiManager: WifiManager, totalWiFi: Int, callback: (Boolean) -> Unit ) {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(context, "WiFi is disabled, please enable it", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
        // Registering Wifi Receiver
        wifiScanReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceive(c: Context, intent: Intent) {
                i=0
                results = wifiManager.scanResults
                Log.d("cfauli lenght results", (results as MutableList<ScanResult>?)?.size.toString())
                //unregisterReceiver(this)
                for (scanResult in (results as MutableList<ScanResult>?)!!) {

                    val mac = "0"
                    devices.add(DeviceWiFi(scanResult.SSID, mac, scanResult.capabilities, scanResult.centerFreq0, scanResult.centerFreq1, scanResult.frequency, scanResult.channelWidth))
                    Log.d("cfauli wifidevices", devices[0].ssid.toString())
                    if (i==totalWiFi-1) {
                        callback(true)
                        return
                    } else i += 1

                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
        wifiManager.startScan()

    }


}


