package lestelabs.antenna.ui.main.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


class Connectivity(context: Context) {

    val mContext= context

    //@RequiresApi(Build.VERSION_CODES.M)
    fun getWifiParam(context: Context): DeviceWiFi {
        val deviceWiFi: DeviceWiFi = DeviceWiFi()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo: WifiInfo? = null
        wifiInfo = wifiManager.connectionInfo
        deviceWiFi.ssid = wifiInfo.ssid
        deviceWiFi.level = wifiInfo.rssi
        deviceWiFi.mac = wifiInfo.macAddress
        val freq = wifiInfo.frequency
        deviceWiFi.centerFreq = freq

        return deviceWiFi
    }

    fun getNetworkInfo(): NetworkInfo? {
        val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }


    //@RequiresApi(Build.VERSION_CODES.M)
    fun isConnected(): Boolean {
        val info = getNetworkInfo()
        return info != null && info.isConnected
    }


    //@RequiresApi(Build.VERSION_CODES.M)
    fun isConnectedWifi(): Boolean {
        val info = getNetworkInfo()
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
    }

    fun isConnectedMobile(): Boolean {
        val info = getNetworkInfo()
        return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
    }

    fun getIccId():String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val sm = SubscriptionManager.from(mContext)
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return ""
            }  else {
                val sis = sm.activeSubscriptionInfoList
                return try {
                    val si = sis[0]
                    var iccId = si.iccId
                    if (iccId.length == 20) {
                        if (iccId[19].isLetter()) iccId = iccId.take(19)
                    }
                    iccId
                } catch(e: Exception) {
                    ""
                }

            }
        } else return ""
    }

/*
    fun networkType(context: Context): Int? {
        val info = getNetworkInfo(context)
        if (info != null && info.isConnected) return info.type else return null
    }

    fun networkSubtype(context: Context): Int? {
        val info = getNetworkInfo(context)
        if (info != null && info.isConnected) return info.subtype else return null
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
    } */
}


