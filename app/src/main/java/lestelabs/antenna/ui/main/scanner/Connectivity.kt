package lestelabs.antenna.ui.main.scanner

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi


public object Connectivity {



    @RequiresApi(Build.VERSION_CODES.M)
    fun getSsid(context: Context): List<Any?> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo: WifiInfo? = null
        wifiInfo = wifiManager.connectionInfo
        return listOf(wifiInfo?.ssid, wifiInfo.hiddenSSID, wifiInfo.rssi,wifiInfo.frequency,wifiInfo.ipAddress,wifiInfo.macAddress)/*you will get SSID <unknown ssid> if location turned off*/
    }

    fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isConnectedWifi(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    fun isConnectedMobile(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    fun networkType(context: Context): Int? {
        val info = getNetworkInfo(context)
        if (info != null && info.isConnected) return info.type else return null
    }

    fun networkSubtype(context: Context): Int? {
        val info = getNetworkInfo(context)
        if (info != null && info.isConnected) return info.subtype else return null
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun getpDevice(context: Context):DevicePhone {
        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return loadCellInfo(telephonyManager)
    }

    fun connectionType(type: Int?, subType: Int?): String? {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return "WIFI"
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            when (subType) {
                TelephonyManager.NETWORK_TYPE_1xRTT -> return "GSM" // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA" // ~ 14-64 kbps
                TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE" // ~ 50-100 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "EVDO 0" // ~ 400-1000 kbps
                TelephonyManager.NETWORK_TYPE_EVDO_A -> return "EVDO A" // ~ 600-1400 kbps
                TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS" // ~ 100 kbps
                TelephonyManager.NETWORK_TYPE_HSDPA -> return "HSDPA" // ~ 2-14 Mbps
                TelephonyManager.NETWORK_TYPE_HSPA -> return "HSPA" // ~ 700-1700 kbps
                TelephonyManager.NETWORK_TYPE_HSUPA -> return "HSUPA" // ~ 1-23 Mbps
                TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS" // ~ 400-7000 kbps
                TelephonyManager.NETWORK_TYPE_EHRPD -> return "EHRDP" // ~ 1-2 Mbps
                TelephonyManager.NETWORK_TYPE_EVDO_B -> return "EVDO B" // ~ 5 Mbps
                TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSAP" // ~ 10-20 Mbps
                TelephonyManager.NETWORK_TYPE_IDEN -> return "IDEN" // ~25 kbps
                TelephonyManager.NETWORK_TYPE_LTE -> return "LTE" // ~ 10+ Mbps
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> return ""
                else -> return ""
            }
        } else {
            return ""
        }
    }
}


