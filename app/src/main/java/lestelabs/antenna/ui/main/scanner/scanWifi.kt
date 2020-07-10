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


    val arrayList = ArrayList<String>()
    var wifiScanReceiver: BroadcastReceiver? = null
    var devices: MutableList<DeviceWiFi> = mutableListOf()
    var i=0

    fun scanwifi (context: FragmentActivity, wifiManager: WifiManager, callback: (Boolean) -> Unit ) {

        var results: List<ScanResult>? = null
        devices = mutableListOf()
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(context, "WiFi is disabled, please enable it", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
        // Registering Wifi Receiver
        wifiScanReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceive(c: Context, intent: Intent) {
                i=1
                results = wifiManager.scanResults
                Log.d("cfauli wifi results", (results as MutableList<ScanResult>?)?.size.toString())
                //unregisterReceiver(this)
                for (scanResult in (results as MutableList<ScanResult>?)!!) {
                    if (i==1) {
                        devices = mutableListOf(DeviceWiFi())
                        devices[0] = DeviceWiFi(
                            scanResult.SSID, scanResult.BSSID, scanResult.capabilities, scanResult.centerFreq0, scanResult.centerFreq1,
                            scanResult.frequency, scanResult.channelWidth, scanResult.level, scanResult.operatorFriendlyName.toString())
                    } else {
                        devices.add(DeviceWiFi(
                            scanResult.SSID, scanResult.BSSID, scanResult.capabilities, scanResult.centerFreq0, scanResult.centerFreq1,
                            scanResult.frequency, scanResult.channelWidth, scanResult.level, scanResult.operatorFriendlyName.toString()))
                    }
                    if (i >= (results as MutableList<ScanResult>?)?.size!!) {
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


