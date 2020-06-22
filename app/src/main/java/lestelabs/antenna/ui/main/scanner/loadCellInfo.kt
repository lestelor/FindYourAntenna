package lestelabs.antenna.ui.main.scanner

import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi


/**
 * This class is taking in consideration newly available network info items
 * that are only available in the AOS API 18 and above. In this case we're
 * concerned with Wcdma Cell info (CellInfoWcdma)
 *
 * See: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
 *
 */

@RequiresApi(Build.VERSION_CODES.P)
fun loadCellInfo(tm: TelephonyManager): DevicePhone {
     val TAG = "AICDL"
     val mTAG = "XXX"

    var pDevicePhone:DevicePhone = DevicePhone("",0,0,0,0,0,0,0)
    val lCurrentApiVersion = Build.VERSION.SDK_INT


    try {

        val cellInfoList = tm.allCellInfo
        if (cellInfoList != null) {
        val info = cellInfoList.first()

            //Network Type
            pDevicePhone.networkType=tm.networkType

            if (info is CellInfoGsm) {
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
            } else if (info is CellInfoCdma) {
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
            } else if (info is CellInfoLte) {


                val lte =
                    info.cellSignalStrength
                val identityLte = info.cellIdentity
                // Signal Strength
                pDevicePhone.type = "lte"
                pDevicePhone.dbm = lte.dbm
                // Cell Identity

                pDevicePhone.mcc = identityLte.mccString.toInt()
                pDevicePhone.mnc = identityLte.mncString.toInt()
                pDevicePhone.lac = identityLte.tac
                pDevicePhone.cid = identityLte.ci

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                }
            } else if (lCurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2 && info is CellInfoWcdma) {
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
            } else {
                Log.d("cfauli", "loadCellInfo Unknown Cell")
                }
            pDevicePhone.totalCellId = pDevicePhone.mcc.toString() + pDevicePhone.mnc.toString() + pDevicePhone.lac.toString() + pDevicePhone.cid.toString()
            }

        } catch (npe: NullPointerException) {
            Log.e(
                TAG,
                mTAG + "loadCellInfo: Unable to obtain cell signal information: ",
                npe
            )
        }
        return pDevicePhone
    }
