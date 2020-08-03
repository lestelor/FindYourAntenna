package lestelabs.antenna.ui.main.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import lestelabs.antenna.ui.main.MainActivity


/**
 * This class is taking in consideration newly available network info items
 * that are only available in the AOS API 18 and above. In this case we're
 * concerned with Wcdma Cell info (CellInfoWcdma)
 *
 * See: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
 *
 */

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.P)
fun loadCellInfo(tm: TelephonyManager): DevicePhone {


    var pDevicePhone:DevicePhone = DevicePhone("","",0,0,0,0,0,0,0)
    val lCurrentApiVersion = Build.VERSION.SDK_INT



    try {

        val cellInfoList = tm.allCellInfo
        if (cellInfoList != null) {
        val info = cellInfoList.first()

            //Network Type
            pDevicePhone.networkType=tm.networkType

            if (info is CellInfoGsm) {
                pDevicePhone.type = "GSM"
                val gsm =
                    info.cellSignalStrength
                val identityGsm = info.cellIdentity
                // Signal Strength
                pDevicePhone.dbm=gsm.dbm // [dBm]
                // Cell Identity
                pDevicePhone.cid=identityGsm.cid
                pDevicePhone.mcc= identityGsm.mccString.toInt()
                pDevicePhone.mnc = identityGsm.mncString.toInt()
                pDevicePhone.lac=identityGsm.lac
                pDevicePhone.band=identityGsm.arfcn
                pDevicePhone.operator = identityGsm.mobileNetworkOperator

            } else if (info is CellInfoCdma) {
                pDevicePhone.type = "CDMA"
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
                pDevicePhone.operator=identityCdma.operatorAlphaShort.toString()

            } else if (info is CellInfoLte) {
                pDevicePhone.type = "LTE"
                val lte =
                    info.cellSignalStrength
                val identityLte = info.cellIdentity
                pDevicePhone.dbm = lte.dbm
                // Cell Identity
                pDevicePhone.mcc = identityLte.mccString.toInt()
                pDevicePhone.mnc = identityLte.mncString.toInt()
                pDevicePhone.lac = identityLte.tac
                pDevicePhone.cid = identityLte.ci
                pDevicePhone.band = identityLte.earfcn
                pDevicePhone.operator = identityLte.mobileNetworkOperator

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr) {
                pDevicePhone.type = "5G"
                val cincoG=info.cellSignalStrength
                val identityCincoG:CellIdentityNr = info.cellIdentity as CellIdentityNr
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
                pDevicePhone.type = "WCDMA"
                val wcdma =
                    info.cellSignalStrength
                val identityWcdma =
                    info.cellIdentity
                // Signal Strength
                pDevicePhone.dbm = wcdma.dbm
                // Cell Identity
                pDevicePhone.lac = identityWcdma.lac
                pDevicePhone.mcc = identityWcdma.mccString.toInt()
                pDevicePhone.mnc = identityWcdma.mncString.toInt()
                pDevicePhone.cid = identityWcdma.cid
                pDevicePhone.psc = identityWcdma.psc
                pDevicePhone.band  = identityWcdma.uarfcn
                pDevicePhone.operator = identityWcdma.mobileNetworkOperator
            } else {
                Log.d("cfauli", "loadCellInfo Unknown Cell")
                }
            pDevicePhone.totalCellId = pDevicePhone.mcc.toString() + pDevicePhone.mnc.toString() + pDevicePhone.lac.toString() + pDevicePhone.cid.toString()
            }

        } catch (npe: NullPointerException) {
            Log.e("cfauli", "loadCellInfo: Unable to obtain cell signal information: ", npe)
        }
        return pDevicePhone
    }
