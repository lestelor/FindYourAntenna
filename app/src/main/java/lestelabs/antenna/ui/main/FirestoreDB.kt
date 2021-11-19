package lestelabs.antenna.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.ui.main.crashlytics.Crashlytics
import lestelabs.antenna.ui.main.data.Server
import lestelabs.antenna.ui.main.data.Site
import lestelabs.antenna.ui.main.scanner.Connectivity
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.DeviceWiFi
import java.util.*

object FirestoreDB {



    fun getFromFirestore(collection: String, document: String, field: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        var output:String? = null


        db.collection(collection).document(document)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    output = task.result?.data!![field].toString()
                    Log.d("cfauli", "SpeedTest output " + output)
                    callback(output)
                } else {
                    Log.d("cfauli", "SpeedTest Error getting Firebase SpeedTestFiles ", task.exception)
                    callback(output)
                }
            }


    }

    fun writeCloudFirestoredB(context: Context, activity: Activity, connectivity: Connectivity?, downlink:String, uplink:String, latency:String)  {

        var fusedLocationClient: FusedLocationProviderClient? = null
        val db = FirebaseFirestore.getInstance()

        val pDevice = connectivity?.loadCellInfo()
        val deviceWifi = connectivity?.getWifiParam()
        var networkType = ""
        if (connectivity?.isConnectedMobile() == true) networkType = "MOBILE"
        if (connectivity?.isConnectedWifi() == true) networkType = "WIFI"

        if ((context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) } != PackageManager.PERMISSION_GRANTED) ||
            (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED)) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE) }
            //Thread.sleep(1000)
        }
        fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location: Location? ->
                val lat = location?.latitude
                val lon = location?.longitude
                val dateNow = Date()
                val documentId = DateFormat.format("yyyyMMdd", dateNow)
                    .toString() + "/" + DateFormat.format("HHmmss", dateNow)
                    .toString() + "/" + pDevice?.cid + ";" + deviceWifi?.ssid

                val speedTestSample: MutableMap<String, Any?> = HashMap()

                speedTestSample["date"] = DateFormat.format("yyyy/MM/dd HH:mm:ss", dateNow)
                speedTestSample["type"] = networkType
                speedTestSample["lat"] = lat
                speedTestSample["lon"] = lon
                speedTestSample["downlink"] = downlink
                speedTestSample["uplink"] = uplink
                speedTestSample["latency"] = latency
                speedTestSample["MobileNetwork"] = pDevice?.type
                speedTestSample["MobileMcc"] = pDevice?.mcc
                speedTestSample["MobileMnc"] = pDevice?.mnc
                speedTestSample["MobileLac"] = pDevice?.lac
                speedTestSample["MobileCid"] = pDevice?.cid
                speedTestSample["MobileCh"] = pDevice?.band
                speedTestSample["MobileFreq"] = pDevice?.freq
                speedTestSample["MobiledBm"] = pDevice?.dbm
                speedTestSample["WifiNetwork"] = deviceWifi?.ssid
                speedTestSample["WifiFreq"] = deviceWifi?.centerFreq
                speedTestSample["WifiCh"] = connectivity?.getWifiChannel((deviceWifi?.centerFreq))
                speedTestSample["WifidBm"] = deviceWifi?.level

                if (networkType != "") {
                    // Add a new document with a generated ID
                    db.collection("SpeedTest").document(documentId)
                        .set(speedTestSample)
                        .addOnSuccessListener {
                            Log.d(
                                "cfauli",
                                "DocumentSnapshot SpeedTest added with ID: " + documentId
                            )
                        }
                        .addOnFailureListener {
                            Log.d("cfauli", "Error adding SpeedTest document ", it)
                        }
                }

            }



    }

    fun loadServersFromFirestore(callback: (MutableList<Server>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        var servers : MutableList<Server> = mutableListOf()
        // Internet connection is available, get remote data
        db.collection("servers")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val server: Server = Server()
                    server.id = document.id.toInt()
                    server.name  = document.data["name"].toString()
                    server.server  = document.data["server"].toString()
                    server.dlURL  = document.data["dlURL"].toString()
                    server.ulURL  = document.data["ulURL"].toString()
                    server.pingURL  = document.data["pingURL"].toString()
                    server.getIpURL  = document.data["getIpURL"].toString()
                    server.sponsorName  = document.data["sponsorName"].toString()
                    server.sponsorURL  = document.data["sponsorURL"].toString()
                    servers.add(server)
                }
                Log.d("TAB1", "Found servers in Firestore " + servers.count())
                callback(servers)
            }
            .addOnFailureListener { exception ->
                callback(servers)
                Log.e("TAB1", "Error getting servers: ", exception)
            }
    }


}