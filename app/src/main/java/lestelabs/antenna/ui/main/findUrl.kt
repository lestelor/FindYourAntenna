package lestelabs.antenna.ui.main


import android.location.Location
import android.util.Log
import lestelabs.antenna.ui.main.MyApplication.Companion.ctx
import lestelabs.antenna.ui.main.scanner.DevicePhone

fun findUrl (activity: MainActivity, pDevice: DevicePhone?, callback: (String) -> Unit) {

    var url:String =""


    Log.d("cfauli", "findUrl device " + pDevice?.mcc.toString() + " " + pDevice?.mnc.toString())

    when(pDevice?.mcc) {
        214 -> {

            FirestoreDB.getFromFirestore("SpeedTest", "SpeedTestFiles", pDevice.mcc.toString() + "url") {
                url = it ?: "https://antenasgsm.com"

                findLocation(activity) { location: Location? ->
                    var texteTrueFalse: String = ""
                    Log.d("cfauli", "findUrl location " + location?.latitude.toString() + " " + location?.longitude)
                    //val url="http://www.google.com"

                    if (location != null && pDevice != null) {
                        when (pDevice.mnc) {
                            5, 7 -> texteTrueFalse = "true,false,false,false,false"
                            1, 18, 6 -> texteTrueFalse = "false,true,false,false,false"
                            3, 11, 19, 21, 9 -> texteTrueFalse = "false,false,true,false,false"
                            4 -> texteTrueFalse = "false,false,false,true,false"
                            else -> texteTrueFalse = "false,false,false,false,true"
                        }
                        url = url + "/" + location.latitude.toString() + "/" + location.longitude.toString() + "/" +
                                "15" + "/" + texteTrueFalse
                        Log.d("cfauli", "findUrl url " + url)
                        callback(url)

                    } else {
                        url = "https://antenasgsm.com"
                        Log.d("cfauli", "findUrl url " + url)
                        callback(url)
                    }
                }
            }
        }

        else -> {
            url =""
            Log.d("cfauli", "findUrl url " + url)
            callback(url)
        }

    }


}