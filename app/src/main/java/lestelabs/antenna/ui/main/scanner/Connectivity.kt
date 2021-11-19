package lestelabs.antenna.ui.main.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import lestelabs.antenna.ui.main.crashlytics.Crashlytics
import java.lang.Error


class Connectivity(context: Context, telephonyManager: TelephonyManager) {

    val mContext= context
    val tm = telephonyManager

    //@RequiresApi(Build.VERSION_CODES.M)
    fun getWifiParam(): DeviceWiFi {
        val deviceWiFi: DeviceWiFi = DeviceWiFi()
        val wifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
        // for older phones
        //val info = getNetworkInfo()
        //return info != null && info.isConnected

        // For new phones
        return getConnectionType() != 0
    }

    //@RequiresApi(Build.VERSION_CODES.M)
    fun isConnectedWifi(): Boolean {

        // older phones
        //val info = getNetworkInfo()
        //return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
        return getConnectionType() == 2
    }

    fun isConnectedMobile(): Boolean {
        // older phones
        //val info = getNetworkInfo()
        //return info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
        return getConnectionType() == 1
    }

    fun getOperatorName():String {
        val sm = SubscriptionManager.from(mContext)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return ""
        }  else {
            val sis = sm.activeSubscriptionInfoList
            return try {
                val si = sis[0]
                si.displayName.toString()
            } catch(e: Exception) {
                ""
            }

        }
    }


    fun getConnectionType(): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        result = 2
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        result = 1
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        result = 3
                    }
                }
            }
        }
        return result
    }

    @SuppressLint("MissingPermission")
    fun loadCellInfo(): DevicePhone {


        val pDevicePhone:DevicePhone = DevicePhone("", "", 0, 0, 0, 0, 0, 0, 0)
        val lCurrentApiVersion = Build.VERSION.SDK_INT


        try {

            val cellInfoList = tm.allCellInfo
            //Log.d("cfauli", "gps cellinfolist" + cellInfoList )
            if (cellInfoList != null && cellInfoList.size>0) {
                //Log.d("cfauli", "gps cellinfolist 2" + cellInfoList )
                val info = cellInfoList.first()

                //Network Type
                pDevicePhone.networkType=tm.networkType

                if (info is CellInfoGsm) {
                    pDevicePhone.type = "2G"
                    val gsm =
                        info.cellSignalStrength
                    val identityGsm = info.cellIdentity
                    // Signal Strength
                    pDevicePhone.dbm=gsm.dbm // [dBm]
                    // Cell Identity
                    pDevicePhone.cid=identityGsm.cid
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pDevicePhone.mcc = identityGsm.mccString?.toInt()
                        pDevicePhone.mnc = identityGsm.mncString?.toInt()
                    } else {
                        pDevicePhone.mcc = identityGsm.mcc.toInt()
                        pDevicePhone.mnc = identityGsm.mnc.toInt()
                    }

                    pDevicePhone.lac=identityGsm.lac
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pDevicePhone.band=identityGsm.arfcn
                    //pDevicePhone.operator = identityGsm.mobileNetworkOperator

                } else if (info is CellInfoCdma) {
                    pDevicePhone.type = "3G"
                    val cdma =
                        info.cellSignalStrength
                    val identityCdma =
                        info.cellIdentity
                    // Signal Strength
                    pDevicePhone.dbm =cdma.dbm

                    // Cell Identity
                    pDevicePhone.cid = identityCdma.basestationId
                    pDevicePhone.mnc = identityCdma.systemId
                    pDevicePhone.lac = identityCdma.networkId
                    //pDevicePhone.operator=identityCdma.operatorAlphaShort.toString()

                } else if (info is CellInfoLte) {
                    pDevicePhone.type = "4G"
                    val lte =
                        info.cellSignalStrength
                    val identityLte = info.cellIdentity
                    pDevicePhone.dbm = lte.dbm
                    // Cell Identity
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pDevicePhone.mcc = identityLte.mccString?.toInt()
                        pDevicePhone.mnc = identityLte.mncString?.toInt()
                    } else {
                        pDevicePhone.mcc = identityLte.mcc.toInt()
                        pDevicePhone.mnc = identityLte.mnc.toInt()
                    }

                    pDevicePhone.lac = identityLte.tac
                    pDevicePhone.cid = identityLte.ci
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pDevicePhone.band = identityLte.earfcn
                    //pDevicePhone.operator = identityLte.mobileNetworkOperator

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr) {
                    pDevicePhone.type = "5G"
                    val cincoG=info.cellSignalStrength
                    val identityCincoG: CellIdentityNr = info.cellIdentity as CellIdentityNr
                    // Signal Strength
                    pDevicePhone.dbm = cincoG.dbm
                    // Cell Identity
                    pDevicePhone.mcc = identityCincoG.mccString?.toInt()
                    pDevicePhone.mnc = identityCincoG.mncString?.toInt()
                    pDevicePhone.lac = identityCincoG.tac
                    pDevicePhone.cid = identityCincoG.nci.toInt()
                    pDevicePhone.band = identityCincoG.nrarfcn
                    pDevicePhone.operator = identityCincoG.operatorAlphaShort.toString()

                }
                else if (lCurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2 && info is CellInfoWcdma) {
                    pDevicePhone.type = "3G"
                    val wcdma =
                        info.cellSignalStrength
                    val identityWcdma =
                        info.cellIdentity
                    // Signal Strength
                    pDevicePhone.dbm = wcdma.dbm
                    // Cell Identity
                    pDevicePhone.lac = identityWcdma.lac
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pDevicePhone.mcc = identityWcdma.mccString?.toInt()
                        pDevicePhone.mnc = identityWcdma.mncString?.toInt()
                    } else {
                        pDevicePhone.mcc = identityWcdma.mcc.toInt()
                        pDevicePhone.mnc = identityWcdma.mnc.toInt()
                    }

                    pDevicePhone.cid = identityWcdma.cid
                    pDevicePhone.psc = identityWcdma.psc
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pDevicePhone.band  = identityWcdma.uarfcn
                    //pDevicePhone.operator = identityWcdma.mobileNetworkOperator
                } else {
                    Log.d("cfauli", "loadCellInfo Unknown Cell")
                }
                pDevicePhone.totalCellId = pDevicePhone.mcc.toString() + pDevicePhone.mnc.toString() + pDevicePhone.lac.toString() + pDevicePhone.cid.toString()
                pDevicePhone.freq = calculateFreq(pDevicePhone.type,pDevicePhone.band)
            }

        } catch (e: Error) {
            Log.d("cfauli", "loadCellInfo: Unable to obtain cell signal information: ", e)
        }


        return pDevicePhone
    }


    fun calculateFreq (type: String?, arfcn:Int?):Double {
        var offsetDL = 0
        var initialDL: Double = 0.0
        var FREFOffs: Double = 0.0
        var FGlobal: Int = 0
        var NREFOff: Int = 0

        when (type) {
            "2G" -> {
                return when {
                    arfcn!! <= 599 -> ((arfcn * 0.2 + 890) + 45)
                    arfcn <= 1023 -> (890 + 0.2 * (arfcn - 1024) + 45)
                    else -> 0.0
                }
            }

            "3G" -> {
                return when {
                    arfcn!! <=763 -> 735.0+arfcn/5 // band19
                    arfcn <=912 -> 1326.0+arfcn/5 // band21
                    arfcn <=1513 -> 1575.0+arfcn/5 // band3
                    arfcn <=1738 -> 1805.0+arfcn/5 // band4
                    arfcn <=2563 -> 2175.0+arfcn/5 // band7
                    arfcn <=3088 -> 340.0+arfcn/5 // band8
                    arfcn <=3388 -> 1490.0+arfcn/5 // band10
                    arfcn <=3787 -> 736.0+arfcn/5 // band11
                    arfcn <=3903 -> -37.0+arfcn/5 // band12
                    arfcn <=4043 -> -55.0+arfcn/5 // band13
                    arfcn <=4143 -> -63.0+arfcn/5 // band14
                    arfcn <=4638 -> -109.0+arfcn/5 // band20
                    arfcn <=5038 -> 2580.0+arfcn/5 // band22
                    arfcn <=5413 -> 910.0+arfcn/5 // band25
                    arfcn <=5913 -> -291.0+arfcn/5 // band26
                    arfcn <=6813 -> 131.0+arfcn/5 // band32
                    else -> arfcn/5.toDouble()
                }
            } // https://www.sqimway.com/umts_band.php

            //https://5g-tools.com/4g-lte-earfcn-calculator/
            "4G" -> {
                when {
                    arfcn!! <= 599 -> {
                        initialDL = 2110.0
                        offsetDL = 0
                    }// band 1
                    arfcn <= 1199 -> {
                        initialDL = 1930.0
                        offsetDL = 600
                    }// band 2
                    arfcn <= 1949 -> {
                        initialDL = 1805.0
                        offsetDL = 1200
                    }// band 3
                    arfcn <= 2399 -> {
                        initialDL = 2110.0
                        offsetDL = 1950
                    }// band 4
                    arfcn <= 2649 -> {
                        initialDL = 869.0
                        offsetDL = 2400
                    }// band 5
                    arfcn <= 2749 -> {
                        initialDL = 875.0
                        offsetDL = 2650
                    }// band 6
                    arfcn <= 3449 -> {
                        initialDL = 2620.0
                        offsetDL = 2750
                    }// band 7
                    arfcn <= 3799 -> {
                        initialDL = 925.0
                        offsetDL = 3450
                    }// band 8
                    arfcn <= 4149 -> {
                        initialDL = 1844.9
                        offsetDL = 3800
                    }// band 9
                    arfcn <= 4749 -> {
                        initialDL = 2110.0
                        offsetDL = 4150
                    }// band 10
                    arfcn <= 4949 -> {
                        initialDL = 1475.9
                        offsetDL = 4750
                    }// band 11
                    arfcn <= 5179 -> {
                        initialDL = 729.0
                        offsetDL = 5010
                    }// band 12
                    arfcn <= 5279 -> {
                        initialDL = 746.0
                        offsetDL = 5180
                    }// band 13
                    arfcn <= 5379 -> {
                        initialDL = 758.0
                        offsetDL = 5280
                    }// band 14
                    arfcn <= 5849 -> {
                        initialDL = 734.0
                        offsetDL = 5730
                    }// band 17
                    arfcn <= 5999 -> {
                        initialDL = 860.0
                        offsetDL = 5850
                    }// band 18
                    arfcn <= 6149 -> {
                        initialDL = 875.0
                        offsetDL = 6000
                    }// band 19
                    arfcn <= 6449 -> {
                        initialDL = 791.0
                        offsetDL = 6150
                    }// band 20
                    arfcn <= 6599 -> {
                        initialDL = 1495.9
                        offsetDL = 6450
                    }// band 21
                    arfcn <= 7399 -> {
                        initialDL = 3510.0
                        offsetDL = 6600
                    }// band 22
                    arfcn <= 7699 -> {
                        initialDL = 2180.0
                        offsetDL = 7500
                    }// band 23
                    arfcn <= 8039 -> {
                        initialDL = 1525.0
                        offsetDL = 7700
                    }// band 24
                    arfcn <= 8689 -> {
                        initialDL = 1930.0
                        offsetDL = 8040
                    }// band 25
                    arfcn <= 9039 -> {
                        initialDL = 859.0
                        offsetDL = 8690
                    }// band 26
                    arfcn <= 9209 -> {
                        initialDL = 852.0
                        offsetDL = 9040
                    }// band 27
                    arfcn <= 9659 -> {
                        initialDL = 758.0
                        offsetDL = 9210
                    }// band 28
                    arfcn <= 9769 -> {
                        initialDL = 717.0
                        offsetDL = 9660
                    }// band 292
                    arfcn <= 9869 -> {
                        initialDL = 2350.0
                        offsetDL = 9770
                    }// band 30
                    arfcn <= 9919 -> {
                        initialDL = 462.5
                        offsetDL = 9870
                    }// band 31
                    arfcn <= 10359 -> {
                        initialDL = 1452.0
                        offsetDL = 9920
                    }// band 322
                    arfcn <= 36199 -> {
                        initialDL = 1900.0
                        offsetDL = 36000
                    }// band 33
                    arfcn <= 36349 -> {
                        initialDL = 2010.0
                        offsetDL = 36200
                    }// band 34
                    arfcn <= 36949 -> {
                        initialDL = 1850.0
                        offsetDL = 36350
                    }// band 35
                    arfcn <= 37549 -> {
                        initialDL = 1930.0
                        offsetDL = 36950
                    }// band 36
                    arfcn <= 37749 -> {
                        initialDL = 1910.0
                        offsetDL = 37550
                    }// band 37
                    arfcn <= 38249 -> {
                        initialDL = 2570.0
                        offsetDL = 37750
                    }// band 38
                    arfcn <= 38649 -> {
                        initialDL = 1880.0
                        offsetDL = 38250
                    }// band 39
                    arfcn <= 39649 -> {
                        initialDL = 2300.0
                        offsetDL = 38650
                    }// band 40
                    arfcn <= 41589 -> {
                        initialDL = 2496.0
                        offsetDL = 39650
                    }// band 41
                    arfcn <= 43589 -> {
                        initialDL = 3400.0
                        offsetDL = 41590
                    }// band 42
                    arfcn <= 45589 -> {
                        initialDL = 3600.0
                        offsetDL = 43590
                    }// band 43
                    arfcn <= 46589 -> {
                        initialDL = 703.0
                        offsetDL = 45590
                    }// band 44
                    arfcn <= 46789 -> {
                        initialDL = 1447.0
                        offsetDL = 46590
                    }// band 45
                    arfcn <= 54539 -> {
                        initialDL = 5150.0
                        offsetDL = 46790
                    }// band 46
                    arfcn <= 55239 -> {
                        initialDL = 5855.0
                        offsetDL = 54540
                    }// band 47
                    arfcn <= 56739 -> {
                        initialDL = 3550.0
                        offsetDL = 55240
                    }// band 48
                    arfcn <= 58239 -> {
                        initialDL = 3550.0
                        offsetDL = 56740
                    }// band 49
                    arfcn <= 59089 -> {
                        initialDL = 1432.0
                        offsetDL = 58240
                    }// band 50
                    arfcn <= 59139 -> {
                        initialDL = 1427.0
                        offsetDL = 59090
                    }// band 51
                    arfcn <= 60139 -> {
                        initialDL = 3300.0
                        offsetDL = 59140
                    }// band 52
                    arfcn <= 66435 -> {
                        initialDL = 2110.0
                        offsetDL = 65536
                    }// band 65
                    arfcn <= 67335 -> {
                        initialDL = 2110.0
                        offsetDL = 66436
                    }// band 665
                    arfcn <= 67535 -> {
                        initialDL = 738.0
                        offsetDL = 67336
                    }// band 672
                    arfcn <= 67835 -> {
                        initialDL = 753.0
                        offsetDL = 67536
                    }// band 68
                    arfcn <= 68335 -> {
                        initialDL = 2570.0
                        offsetDL = 67836
                    }// band 692
                    arfcn <= 68585 -> {
                        initialDL = 1995.0
                        offsetDL = 68336
                    }// band 706
                    arfcn <= 68935 -> {
                        initialDL = 617.0
                        offsetDL = 68586
                    }// band 71
                    arfcn <= 68985 -> {
                        initialDL = 461.0
                        offsetDL = 68936
                    }// band 72
                    arfcn <= 69035 -> {
                        initialDL = 460.0
                        offsetDL = 68986
                    }// band 73
                    arfcn <= 69465 -> {
                        initialDL = 1475.0
                        offsetDL = 69036
                    }// band 74
                    arfcn <= 70315 -> {
                        initialDL = 1432.0
                        offsetDL = 69466
                    }// band 752
                    arfcn <= 70365 -> {
                        initialDL = 1427.0
                        offsetDL = 70316
                    }// band 762
                    arfcn <= 70545 -> {
                        initialDL = 728.0
                        offsetDL = 70366
                    }// band 85"

                    else -> {
                        initialDL = 0.0
                        offsetDL = 0
                    }
                }
                return (initialDL + 0.1 * (arfcn - offsetDL))
            }

            // The formula for 5G NR ARFCN is described in 3GPP TS 38.104 chapter 5.4.2.1.
            "5G" -> {
                when {
                    arfcn!! <= 599999 -> {
                        FREFOffs = 0.0
                        FGlobal = 5
                        NREFOff = 0
                    }// band 0-3000MHz
                    arfcn <= 2016666 -> {
                        FREFOffs = 3000.0
                        FGlobal = 15
                        NREFOff = 600000
                    }// band 3000-24.500MHz
                    arfcn <= 3279165 -> {
                        FREFOffs = 24250.08
                        FGlobal = 60
                        NREFOff = 2016667
                    }// 24.500-100.000MHz
                }
                return FREFOffs + FGlobal * (arfcn!! - NREFOff)
            }

            else -> return 0.0
        }
    }

    fun getWifiChannel(freq: Int?):Int {
        val channel = if (freq!! > 5000) {
            (freq - 5180) / 5 + 36
        } else {
            (freq - 2412) / 5 + 1
        }
        return channel
    }

    // No longer possible from android 10 onwards
    /*fun getIccId():String {
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
    }*/




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


