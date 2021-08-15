package lestelabs.antenna.ui.main


import android.Manifest
import android.R.transition
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.*
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.anastr.speedviewlib.SpeedView
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import kotlinx.android.synthetic.main.fragment_tab1.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.core.Speedtest
import lestelabs.antenna.ui.main.core.Speedtest.SpeedtestHandler
import lestelabs.antenna.ui.main.core.config.SpeedtestConfig
import lestelabs.antenna.ui.main.core.config.TelemetryConfig
import lestelabs.antenna.ui.main.core.serverSelector.TestPoint
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.scanner.*
import lestelabs.antenna.ui.main.ui.GaugeView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.EOFException
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


/*
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab1.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
open class Tab1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    //private var mListener: Tab1.OnFragmentInteractionListener? = null
    private val speedTestSocket = SpeedTestSocket()
    private var speedTestRunningStep = 0
    lateinit var mAdView: AdView
    private lateinit var fragmentView: View
    private lateinit var connectivity: Connectivity
    private var listener: GetfileState? = null

    private lateinit var speedometer: SpeedView

    //private val speedTestType = 2 // ikoula/scaleway/tele2
    //private val speedTestFile = 2 // 1/10/1000MB
    private var lastUpload = 0.0f
    private var lastDownload = 0.0f

    var mHandler: Handler = Handler()

    var clockTimerHanlerActive = false
    private lateinit var mHandlerTask: Runnable
    private lateinit var mHandlerRater: Runnable
    private var firstOnResume = true
    private var minTime: Long = 30000

    private lateinit var tvDownload: TextView
    private lateinit var tvUpload: TextView
    private lateinit var tvLatency: TextView
    private lateinit var ivButton: ImageView
    private lateinit var ibCopyToClipboard: ImageButton

    private var testTimeStart: Long = 0
    private var timeAnt: Long = 0

    private lateinit var telephonyManager: TelephonyManager
    private var pDevice: DevicePhone? = DevicePhone()
    private var networkType: String = ""
    private var deviceWifi: DeviceWiFi = DeviceWiFi()
    private lateinit var db: FirebaseFirestore
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val tabName = "Tab1"
    private var crashlyticsKeyAnt = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("cfauli", "OnCreate TAB1")



        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }

    }

    override fun onStart() {
        // call the superclass method first
        super.onStart()

        Log.d("cfauli", "OnStart Tab1")
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli", "OnStop Tab1")
    }

    override fun onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause()
        if (this::mHandlerTask.isInitialized) mHandler.removeCallbacks(mHandlerTask)
        clockTimerHanlerActive = false
        Log.d("cfauli", "OnPause Tab1")
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("cfauli", "OnCreateView TAB1")

        // Inflate view
        fragmentView = inflater.inflate(R.layout.fragment_tab1, container, false)

        // connectivity context
        connectivity = Connectivity(fragmentView.context)
        
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)


        // layout widgets
        tvDownload = fragmentView.findViewById<View>(R.id.tvSpeedtestDownload) as TextView

        Log.d("cfauli", "Oncreateview tab1 final")
        return fragmentView
    }



    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab1.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): Tab1 {
            val fragment = Tab1()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }



    /*@RequiresApi(Build.VERSION_CODES.M)
    fun setNetworkType(context:Context):List<Any?> {
        var type:String = ""
     if (connectivity.isConnectedWifi(context)){
         type = connectivity.getWifiParam(context).toString()
     } else if (connectivity.isConnected(context)) {
         type = listOf(connectivity.connectionType(connectivity.networkType(context), connectivity.networkSubtype(context)).toString()).toString()
     } else type =""
        return listOf(type)
    }*/



    //@RequiresApi(Build.VERSION_CODES.M)
    fun fillNetworkTextView(view: View) {
        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
        //Log.d("cfauli", "fillMobileTextView")
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        val ivType = view.findViewById<View>(R.id.ivTab1Type) as ImageView
        val ivLevel = view.findViewById<View>(R.id.ivTab1Level) as ImageView

        val tvStart = view.findViewById<View>(R.id.tvTab1StartTest) as TextView
        val tvType = view.findViewById<View>(R.id.tvTab1Type) as TextView
        val tvName = view.findViewById<View>(R.id.tvTab1Name) as TextView
        val tvMCC = view.findViewById<View>(R.id.tvTab1MCC) as TextView
        val tvMNC = view.findViewById<View>(R.id.tvTab1MNC) as TextView
        val tvLAC = view.findViewById<View>(R.id.tvTab1LAC) as TextView
        val tvCID = view.findViewById<View>(R.id.tvTab1CID) as TextView
        val tvSignal = view.findViewById<View>(R.id.tvTab1Signal) as TextView
        val tvChannel = view.findViewById<View>(R.id.tvTab1Channel) as TextView
        val tvFrequency = view.findViewById<View>(R.id.tvTab1Frequency) as TextView



        if (isConnectedMobile()) networkType = "MOBILE"
        if (isConnectedWifi()) networkType = "WIFI"


        if (firstOnResume)   telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        pDevice = loadCellInfo(telephonyManager)
        deviceWifi = context?.let { connectivity.getWifiParam(it) } ?: DeviceWiFi()

        if (isConnectedMobile()) {
            //listNetwork = pDevice?.type + " " + "%.1f".format(calculateFreq(pDevice?.type, pDevice?.band)) + "MHz " + pDevice?.dbm + "dBm id: " + pDevice?.mcc + "-" + pDevice?.mnc + "-" + pDevice?.lac + "-" + pDevice?.cid
            tvType.text = pDevice?.type ?: ""
            when (pDevice?.type) {
                "2G" -> ivType.setImageResource(R.drawable.icon_2g_48)
                "3G" -> ivType.setImageResource(R.drawable.icon_3g_48)
                "4G" -> ivType.setImageResource(R.drawable.icon_4g_48)
                "5G" -> ivType.setImageResource(R.drawable.icon_5g_48)
            }

            lifecycleScope.launch(Dispatchers.IO) {
                findOperatorName(pDevice) { it ->
                    tvName.text = it
                }
            }

            tvName.text = ""
            tvMCC.text = "MCC: " + pDevice?.mcc ?: ""
            tvMNC.text = "MNC: " + pDevice?.mnc ?: ""
            tvLAC.text = "LAC: " + pDevice?.lac ?: ""
            tvCID.text = "CID: " + pDevice?.cid ?: ""

            val sharedPreferences = activity?.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)

            val thresMobBlack = sharedPreferences?.getString("thres_mob_black", Constants.MINMOBILESIGNALBLACK)?.toInt() ?: Constants.MINMOBILESIGNALBLACK.toInt()
            val thresMobRed = sharedPreferences?.getString("thres_mob_red", Constants.MINMOBILESIGNALRED)?.toInt() ?: Constants.MINMOBILESIGNALRED.toInt()
            val thresMobYellow = sharedPreferences?.getString("thres_mob_yellow", Constants.MINMOBILESIGNALYELLOW)?.toInt() ?: Constants.MINMOBILESIGNALYELLOW.toInt()
            val thresMobGreen = Constants.MINMOBILESIGNALGREEN.toInt()

            try {
                pDevice?.let {
                    if (it.dbm!! >= (-1 * thresMobYellow)) {
                        ivLevel.setImageResource(R.drawable.ic_network_green)
                    } else if (pDevice?.dbm!! >= (-1 * thresMobRed)) {
                        ivLevel.setImageResource(R.drawable.ic_network_yellow)
                    } else if (pDevice?.dbm!! >= (-1 * thresMobBlack)) {
                        ivLevel.setImageResource(R.drawable.ic_network_red)
                    } else {
                        ivLevel.setImageResource(R.drawable.ic_network_black)
                    }
                }

                tvSignal.text = pDevice?.dbm.toString() + " dBm"
                tvChannel.text = "arfcn: " + pDevice?.band.toString()
                tvFrequency.text = "freq: " + "%.1f".format(calculateFreq(pDevice?.type, pDevice?.band)) + " MHz"
                //tvTab1MobileNetworkType.text = ""
            } catch (e: Exception) {
                Log.d("cfauli", "Tab1 FillNetworkTextView exception")
            }

        } else if (isConnectedWifi()) {

            Log.d("cfauli deviceWifi ", deviceWifi.toString())
            val freq = deviceWifi.centerFreq
            val channel: Int = getChannel(freq)

            //listNetwork = deviceWifi.ssid + " ch: " + channel + " " + deviceWifi.centerFreq + "MHz " + deviceWifi.level + "dBm"
            tvType.text = ""
            ivType.setImageResource(R.drawable.icon_wifi_48)

            tvName.text = ""
            tvMCC.text = ""
            tvMNC.text = ""
            tvLAC.text = "WIFI"
            tvCID.text = deviceWifi.ssid

            val sharedPreferences = activity?.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val thresWifiBlack = sharedPreferences?.getString("thres_wifi_black", Constants.MINWIFISIGNALBLACK) ?: Constants.MINWIFISIGNALBLACK
            val thresWifiRed = sharedPreferences?.getString("thres_wifi_red", Constants.MINWIFISIGNALRED) ?: Constants.MINWIFISIGNALRED
            val thresWifiYellow = sharedPreferences?.getString("thres_wifi_yellow", Constants.MINWIFISIGNALYELLOW) ?: Constants.MINWIFISIGNALYELLOW
            val thresWifiGreen = Constants.MINWIFISIGNALGREEN


            if (deviceWifi.level!! >= -1 * thresWifiYellow.toInt()) {
                ivLevel.setImageResource(R.drawable.ic_network_green)
            } else if (deviceWifi.level!! >= -1 * thresWifiRed.toInt()) {
                ivLevel.setImageResource(R.drawable.ic_network_yellow)
            } else if (deviceWifi.level!! >= -1 * thresWifiBlack.toInt()) {
                ivLevel.setImageResource(R.drawable.ic_network_red)
            } else {
                ivLevel.setImageResource(R.drawable.ic_network_black)
            }

            tvSignal.text = deviceWifi.level.toString() + " dBm"
            tvChannel.text = "ch:" + channel
            tvFrequency.text = "freq: " + freq + " MHz"
        }
        this.context?.let { saveDocument(it) }
    }


fun saveDocument(context: Context) {
    // Control point for Crashlitycs
    crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        try {
            //Thread.sleep(1000)
            if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ) {
                activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE) }
            }
            var lat = 0.0
            var lon = 0.0
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->

                    // Control point for Crashlitycs
                    crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                    
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                        val dateNow = Date()
                        val dateFormated = DateFormat.format("yyyy/MM/dd HH:mm:ss", dateNow)

                        // Create a new user with a first and last name
                        val speedTestSample: MutableMap<String, Any?> = HashMap()

                        speedTestSample["date"] = dateFormated
                        speedTestSample["lat"] = lat
                        speedTestSample["lon"] = lon
                        speedTestSample["type"] = networkType
                        speedTestSample["MobileNetwork"] = pDevice?.type
                        speedTestSample["MobileMcc"] = pDevice?.mcc
                        speedTestSample["MobileMnc"] = pDevice?.mnc
                        speedTestSample["MobileLac"] = pDevice?.lac
                        speedTestSample["MobileCid"] = pDevice?.cid
                        speedTestSample["MobiledBm"] = pDevice?.dbm
                        speedTestSample["WifiNetwork"] = deviceWifi.ssid
                        speedTestSample["WifidBm"] = deviceWifi.level

                        var pleaseContribute = false
                        val sharedPreferences = activity?.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
                        if (sharedPreferences != null) {
                            pleaseContribute = sharedPreferences.getBoolean("please_contribute", true)
                        }


                        if (networkType != "" && pleaseContribute) {

                            // Control point for Crashlitycs
                            crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                            
                            val documentId = DateFormat.format("yyyyMMdd", dateNow).toString() + "/" + DateFormat.format("HHmmss", dateNow).toString() + "/" + pDevice?.cid + ";" + deviceWifi.ssid
                            // Add a new document with a generated ID
                            db.collection("Samples").document(documentId)
                                .set(speedTestSample)
                                .addOnSuccessListener {
                                    Log.d("cfauli", "DocumentSnapshot added with ID: " + documentId)
                                }

                                .addOnFailureListener {

                                    Log.d("cfauli", "Error adding document: " + it)
                                }
                        }
                    }

                }
        } catch (npe: NullPointerException) {
            Log.d("cfauli", "saveDoument: Unable to save samples: ", npe)
        }
        catch (e: IOException) {
            Log.d("cfauli", "saveDoument: Unable to save samples: ", e)
        }

}



    fun fillSpeedList(new: Boolean, network: String, download: String, upload: String, ping: String): List<SpeedTest> {

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        val sharedPreferences = activity?.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val listOfSpeedTest: ArrayList<SpeedTest> = arrayListOf()

        var textDate: String
        var textNetwork: String
        var textSpeedUp: String
        var textSpeedDown: String
        var textLatency: String


        if (sharedPreferences !=null) {

            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            var numSpeedTest = sharedPreferences.getInt("num_speed_test", 0)


            if (new) {
                val dateNow = Date()
                val dateFormated = DateFormat.format("dd/MM/yyy HH:mm", dateNow)
                val day =
                    dateFormated.toString() +
                            if (isConnectedWifi()) {
                                " WIFI " + deviceWifi.ssid + " ch: " + getChannel(deviceWifi.centerFreq) + " " + deviceWifi.level + "dBm"
                            }
                            else {
                                " " + pDevice?.type + " " + "%.1f".format(pDevice?.freq) + "MHz " + pDevice?.dbm.toString() + "dBm"
                            }


                Log.d("cfauli", "numspeedtest " + numSpeedTest)
                numSpeedTest += 1

                editor.putInt("num_speed_test", numSpeedTest)
                editor.apply()

                textDate = "speed_test_date" + numSpeedTest
                textNetwork = "speed_test_network" + numSpeedTest
                textSpeedUp = "speed_test_speedUp" + numSpeedTest
                textSpeedDown = "speed_test_speedDown" + numSpeedTest
                textLatency = "speed_test_latency" + numSpeedTest

                editor.putString(textDate, day)
                editor.apply()

                editor.putString(textNetwork, network)
                editor.apply()

                editor.putString(textSpeedUp, upload)
                editor.apply()

                editor.putString(textSpeedDown, download)
                editor.apply()

                editor.putString(textLatency, ping)
                editor.apply()

                editor.commit()

            }

            if (numSpeedTest >= 1) {
                for (i in 1..numSpeedTest) {
                    textNetwork = "speed_test_network" + i
                    textSpeedUp = "speed_test_speedUp" + i
                    textSpeedDown = "speed_test_speedDown" + i
                    textLatency = "speed_test_latency" + i
                    textDate = "speed_test_date" + i

                    Log.d("cfauli", "textNetwork " + textNetwork)


                    listOfSpeedTest.add(
                        0, SpeedTest(
                            sharedPreferences.getString(textDate, ""), sharedPreferences.getString(textNetwork, ""), sharedPreferences.getString(textSpeedUp, ""), sharedPreferences.getString(
                                textSpeedDown,
                                ""
                            ), sharedPreferences.getString(textLatency, "")
                        )
                    )
                }
            }
            // Control point for Crashlitycs
            crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        }
        //return listOfSpeedTest.sortedByDescending { it.date }
        return listOfSpeedTest

    }


    private fun isConnectedMobile(): Boolean {
        return  connectivity.isConnectedMobile()
    }
    private fun isConnectedWifi(): Boolean {
        return  connectivity.isConnectedWifi()
    }


    fun startMobileScannerTab1(view: View) {
        if (!clockTimerHanlerActive) {
            mHandlerTask = object : Runnable {
                override fun run() {
                    fillNetworkTextView(view)
                    if (speedTestRunningStep != 0) {
                        val a = testTimeStart
                        val b = System.currentTimeMillis() % 1000000
                        var duration:Long
                        if (b <= a) {
                            duration = 1000000 - a + b
                        } else {
                            duration = b - a
                        }
                        if (duration > 11000) stopSpeedTest()
                    }
                    mHandler.postDelayed(this, minTime)
                }
            }
        }
        mHandlerTask.run()
        clockTimerHanlerActive = true
        // In case the test does not change status in several sec then abort test

        Log.d("cfauli", "startMobileScanner")
    }


    fun stopSpeedTest(){

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        
        Log.d("cfauli speedtest step", speedTestRunningStep.toString())
        speedometer.speedTo(0.0f, 0)
        speedTestSocket.clearListeners()
        speedTestSocket.closeSocket()
        speedTestRunningStep=0
        ivButton.setImageResource(R.drawable.ic_switch_on_off)
        tvUpload.text = "-"
        tvDownload.text = "-"
        tvLatency.text = "-"
        speedTestSocket.forceStopTask()
        Toast.makeText(context, getString(R.string.SpeedTestStoped), Toast.LENGTH_LONG).show()
        tvTab1StartTest.text = getString(R.string.click_to_start_the_test)


    }
    fun getChannel(freq: Int?):Int {
        val channel = if (freq!! > 5000) {
            (freq - 5180) / 5 + 36
        } else {
            (freq - 2412) / 5 + 1
        }
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        return channel


    }
    fun writeCloudFirestoreDB(downlink: String, uplink: String, latency: String) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        
        var lat: Double? = 0.0
        var lon: Double? = 0.0
        if ((context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) } != PackageManager.PERMISSION_GRANTED) ||
            (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED)) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE) }
            //Thread.sleep(1000)
        }

        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location: Location? ->

                // Control point for Crashlitycs
                crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                
                lat = location?.latitude
                lon = location?.longitude
                val dateNow = Date()
                val documentId = DateFormat.format("yyyyMMdd", dateNow).toString() + "/" +  DateFormat.format("HHmmss", dateNow).toString() + "/"  + pDevice?.cid + ";" + deviceWifi.ssid

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
                    speedTestSample["MobileFreq"] = calculateFreq(pDevice?.type, pDevice?.band)
                    speedTestSample["MobiledBm"] = pDevice?.dbm


                    speedTestSample["WifiNetwork"] = deviceWifi.ssid
                    speedTestSample["WifiFreq"] = deviceWifi.centerFreq
                    speedTestSample["WifiCh"] = getChannel(deviceWifi.centerFreq)
                    speedTestSample["WifidBm"] = deviceWifi.level

                if (networkType!="") {
                    // Add a new document with a generated ID
                    db.collection("SpeedTest").document(documentId)
                        .set(speedTestSample)
                        .addOnSuccessListener {
                                Log.d("cfauli", "DocumentSnapshot SpeedTest added with ID: " + documentId)
                        }
                        .addOnFailureListener {
                            Log.d("cfauli", "Error adding SpeedTest document ", it)
                        }
                }
            }
    }

    fun copyToClipboardOnClickListener() {

            val clipboard = context?.let { getSystemService(it, ClipboardManager::class.java) }
            val listSpeedTest = fillSpeedList(false, "", "", "", "")
            var text = ""
            for (list in listSpeedTest) {
                text = text + " " + list.date + " " + list.network + " Down: " + list.speedDown + " Mbps Up: " + list.speedUp + " Mbps Ping: " + list.latency + " ms" + "\n"
            }
            val clip: ClipData = ClipData.newPlainText("Network Test", text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, getString(R.string.CopiedToClipboard), Toast.LENGTH_SHORT).show()
    }



    suspend fun initSpeedometer(maxSpeed: Float?) {
        var finalMaxSpeed: Float? = null

        if (maxSpeed !=null)  {
            finalMaxSpeed = maxSpeed
        } else {
            finalMaxSpeed = 50.0f
        }

        speedometer.speedTo(0.0f)
        speedometer.unit = "Mbps"
        speedometer.minSpeed = 0.0f
        speedometer.maxSpeed = finalMaxSpeed
        Log.d("cfauli", "setspeedometer maxspeed " + maxSpeed)
        val ratio = 1f / (speedometer.maxSpeed / 100)
        // Avoid spurious tick moving when speedtest already finished!!
        speedometer.withTremble = false
        speedometer.sections[0].color = Color.RED
        speedometer.sections[0].endOffset = (ratio) * 0.03f
        speedometer.sections[1].color = Color.YELLOW
        speedometer.sections[1].startOffset = (ratio) * 0.03f
        speedometer.sections[1].endOffset = (ratio) * 0.1f
        speedometer.sections[2].color = Color.GREEN
        speedometer.sections[2].startOffset = (ratio) * 0.1f

        //speedometer.visibility = View.VISIBLE
        //ivButton.visibility = View.VISIBLE
        //tvTab1StartTest.visibility = View.VISIBLE



    }



    fun getMaxDownload(downLoadFile: String, time: Int, callback: (Float?) -> Unit) {

        var maxDownloadSpeed: Float
        val testTimeFinish = System.currentTimeMillis() % 1000000 + time*1.2
        var allOk = false

        Log.d("cfauli", "tab1 maxdownspeed downloadFile " + downLoadFile)

        speedTestSocket.startDownloadRepeat(downLoadFile,
            time, time / 2.toInt(), object : IRepeatListener {
                override fun onCompletion(report: SpeedTestReport) {
                    Log.d("cfauli", "tab1 maxdownspeed oncompletion report " + report.transferRateBit)
                    maxDownloadSpeed = report.transferRateBit.toFloat() / 1000000.0f
                    Log.d("cfauli", "tab1 maxdownspeed " + maxDownloadSpeed)
                    allOk = true
                    callback(((maxDownloadSpeed / 10).toInt() + 1) * 20.toFloat())
                }

                override fun onReport(report: SpeedTestReport?) {
                    Log.d("cfauli", "tab1 maxdownspeed onReport report " + report?.transferRateBit)
                    //TODO("Not yet implemented")
                }
            })
        var testTimeCurrent = System.currentTimeMillis() % 1000000
        while (testTimeCurrent<testTimeFinish) {
            Log.d("cfauli", "tab1 maxdownspeed testTimeCurrent testTimeFinish " + testTimeCurrent + " " + testTimeFinish)
            Thread.sleep(time / 2.toLong())
            testTimeCurrent = System.currentTimeMillis() % 1000000
        }
        if (allOk == false) {
            speedTestSocket.clearListeners()
            speedTestSocket.forceStopTask()
            callback(30.0f)
        }
    }
    private var st: Speedtest? = null
    fun page_init() {
        object : Thread() {
            override fun run() {
                var config: SpeedtestConfig? = null
                var telemetryConfig: TelemetryConfig? = null
                var servers: Array<TestPoint?>? = null
                try {
                    var c: String = readFileFromAssets("SpeedtestConfig.json")
                    var o = JSONObject(c)
                    config = SpeedtestConfig(o)
                    c = readFileFromAssets("TelemetryConfig.json")
                    o = JSONObject(c)
                    telemetryConfig = TelemetryConfig(o)
                    if (telemetryConfig.getTelemetryLevel()
                            .equals(TelemetryConfig.LEVEL_DISABLED)
                    ) {
                        //activity?.runOnUiThread(Runnable { hideView(R.id.privacy_open) })
                    }
                    if (st != null) {
                        try {
                            st?.abort()
                        } catch (e: Throwable) {
                        }
                    }
                    st = Speedtest()
                    st?.setSpeedtestConfig(config)
                    st?.setTelemetryConfig(telemetryConfig)
                    c = readFileFromAssets("ServerList.json")
                    if (c.startsWith("\"") || c.startsWith("'")) { //fetch server list from URL
                        if ((st?.loadServerList(c.subSequence(1, c.length - 1).toString()))==null) {
                            throw java.lang.Exception("Failed to load server list")
                        }
                    } else { //use provided server list
                        val a = JSONArray(c)
                        if (a.length() == 0) throw java.lang.Exception("No test points")
                        val s: ArrayList<TestPoint> = ArrayList()
                        for (i in 0 until a.length()) s.add(TestPoint(a.getJSONObject(i)))
                        servers = s.toArray(arrayOfNulls<TestPoint>(0))
                        st?.addTestPoints(servers)
                    }
                    val testOrder: String = config.getTest_order()
                    activity?.runOnUiThread(Runnable {
                        if (!testOrder.contains("D")) {
                            hideView(R.id.dlArea)
                        }
                        if (!testOrder.contains("U")) {
                            hideView(R.id.ulArea)
                        }
                        if (!testOrder.contains("P")) {
                            hideView(R.id.pingArea)
                        }
                        if (!testOrder.contains("I")) {
                            hideView(R.id.ipInfo)
                        }
                    })
                } catch (e: Throwable) {
                    System.err.println(e)
                    st = null
/*,                 transition(R.id.page_fail, TRANSITION_LENGTH)
                    runOnUiThread(Runnable {
                        (findViewById(R.id.fail_text) as TextView).text =
                            getString(R.string.initFail_configError) + ": " + e.message
                        val b = findViewById(R.id.fail_button) as Button
                        b.setText(R.string.initFail_retry)
                        b.setOnClickListener {
                            page_init()
                            b.setOnClickListener(null)
                        }
                    })*/
                    return
                }
                activity?.runOnUiThread(Runnable { t.setText(R.string.init_selecting) })
                st?.selectServer(object : Speedtest.ServerSelectedHandler() {
                    override fun onServerSelected(server: TestPoint?) {
                        activity?.runOnUiThread(Runnable {
                            if (server == null) {
/*                                transition(R.id.page_fail, TRANSITION_LENGTH)
                                (fragmentView.findViewById(R.id.fail_text) as TextView).text =
                                    getString(R.string.initFail_noServers)
                                val b = fragmentView.findViewById(R.id.fail_button) as Button
                                b.setText(R.string.initFail_retry)
                                b.setOnClickListener {
                                    page_init()
                                    b.setOnClickListener(null)
                                }*/
                            } else {
                                page_serverSelect(server, st?.getTestPoints())
                            }
                        })
                    }
                })
            }
        }.start()
    }


    fun format(d: Double): String? {
        var l: Locale? = null
        l = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        if (d < 10) return java.lang.String.format(l, "%.2f", d)
        return if (d < 100) java.lang.String.format(l, "%.1f", d) else "" + Math.round(d)
    }

     fun mbpsToGauge(s: Double): Int {
        return (1000 * (1 - 1 / Math.pow(1.3, Math.sqrt(s)))).toInt()
    }

    private open fun page_serverSelect(selected: TestPoint, servers: Array<TestPoint>) {
        transition(R.id.page_serverSelect, TRANSITION_LENGTH)
        reinitOnResume = true
        val availableServers: ArrayList<TestPoint> = ArrayList()
        for (t in servers) {
            if (t.ping != -1f) availableServers.add(t)
        }
        val selectedId = availableServers.indexOf(selected)
        val spinner = findViewById(R.id.serverList) as Spinner
        val options = ArrayList<String>()
        for (t in availableServers) {
            options.add(t.name)
        }
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            options.toArray(arrayOfNulls<String>(0))
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selectedId)
        val b = findViewById(R.id.start) as Button
        b.setOnClickListener {
            reinitOnResume = false
            page_test(availableServers[spinner.selectedItemPosition])
            b.setOnClickListener(null)
        }
        val t = findViewById(R.id.privacy_open) as TextView
        t.setOnClickListener { page_privacy() }
    }

    private open fun page_privacy() {
        transition(R.id.page_privacy, TRANSITION_LENGTH)
        reinitOnResume = false
        (findViewById(R.id.privacy_policy) as WebView).loadUrl(getString(R.string.privacy_policy))
        val t = findViewById(R.id.privacy_close) as TextView
        t.setOnClickListener {
            transition(R.id.page_serverSelect, TRANSITION_LENGTH)
            reinitOnResume = true
        }
    }

    private open fun page_test(selected: TestPoint) {
        transition(R.id.page_test, TRANSITION_LENGTH)
        st!!.setSelectedServer(selected)
        (findViewById(R.id.serverName) as TextView).text = selected.name
        (findViewById(R.id.dlText) as TextView).setText(format(0.0))
        (findViewById(R.id.ulText) as TextView).setText(format(0.0))
        (findViewById(R.id.pingText) as TextView).setText(format(0.0))
        (findViewById(R.id.jitterText) as TextView).setText(format(0.0))
        (findViewById(R.id.dlProgress) as ProgressBar).progress = 0
        (findViewById(R.id.ulProgress) as ProgressBar).progress = 0
        (findViewById(R.id.dlGauge) as GaugeView).value = 0
        (findViewById(R.id.ulGauge) as GaugeView).value = 0
        (findViewById(R.id.ipInfo) as TextView).text = ""
        (findViewById(R.id.logo_inapp) as ImageView).setOnClickListener(View.OnClickListener {
            val url = getString(R.string.logo_inapp_link)
            if (url.isEmpty()) return@OnClickListener
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })
        val endTestArea: View = findViewById(R.id.endTestArea)
        val endTestAreaHeight = endTestArea.height
        val p = endTestArea.layoutParams
        p.height = 0
        endTestArea.layoutParams = p
        findViewById(R.id.shareButton).setVisibility(View.GONE)
        st!!.start(object : SpeedtestHandler() {
            override fun onDownloadUpdate(dl: Double, progress: Double) {
                runOnUiThread(Runnable {
                    (findViewById(R.id.dlText) as TextView).text =
                        if (progress == 0.0) "..." else format(dl)
                    (findViewById(R.id.dlGauge) as GaugeView).setValue(
                        if (progress == 0.0) 0 else mbpsToGauge(
                            dl
                        )
                    )
                    (findViewById(R.id.dlProgress) as ProgressBar).progress =
                        (100 * progress).toInt()
                })
            }

            override fun onUploadUpdate(ul: Double, progress: Double) {
                runOnUiThread(Runnable {
                    (findViewById(R.id.ulText) as TextView).text =
                        if (progress == 0.0) "..." else format(ul)
                    (findViewById(R.id.ulGauge) as GaugeView).setValue(
                        if (progress == 0.0) 0 else mbpsToGauge(
                            ul
                        )
                    )
                    (findViewById(R.id.ulProgress) as ProgressBar).progress =
                        (100 * progress).toInt()
                })
            }

            override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) {
                runOnUiThread(Runnable {
                    (findViewById(R.id.pingText) as TextView).text =
                        if (progress == 0.0) "..." else format(ping)
                    (findViewById(R.id.jitterText) as TextView).text =
                        if (progress == 0.0) "..." else format(jitter)
                })
            }

            override fun onIPInfoUpdate(ipInfo: String) {
                runOnUiThread(Runnable { (findViewById(R.id.ipInfo) as TextView).text = ipInfo })
            }

            override fun onTestIDReceived(id: String, shareURL: String) {
                if (shareURL == null || shareURL.isEmpty() || id == null || id.isEmpty()) return
                runOnUiThread(Runnable {
                    val shareButton = findViewById(R.id.shareButton) as Button
                    shareButton.visibility = View.VISIBLE
                    shareButton.setOnClickListener {
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "text/plain"
                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                        share.putExtra(Intent.EXTRA_TEXT, shareURL)
                        startActivity(Intent.createChooser(share, getString(R.string.test_share)))
                    }
                })
            }

            override fun onEnd() {
                runOnUiThread(Runnable {
                    val restartButton = findViewById(R.id.restartButton) as Button
                    restartButton.setOnClickListener {
                        page_init()
                        restartButton.setOnClickListener(null)
                    }
                })
                val startT = System.currentTimeMillis()
                val endT: Long = startT + TRANSITION_LENGTH
                object : Thread() {
                    override fun run() {
                        while (System.currentTimeMillis() < endT) {
                            val f =
                                (System.currentTimeMillis() - startT).toDouble() / (endT - startT).toDouble()
                            runOnUiThread(Runnable {
                                val p = endTestArea.layoutParams
                                p.height = (endTestAreaHeight * f).toInt()
                                endTestArea.layoutParams = p
                            })
                            try {
                                sleep(10)
                            } catch (t: Throwable) {
                            }
                        }
                    }
                }.start()
            }

            override fun onCriticalFailure(err: String) {
                runOnUiThread(Runnable {
                    transition(R.id.page_fail, TRANSITION_LENGTH)
                    (findViewById(R.id.fail_text) as TextView).text =
                        getString(R.string.testFail_err)
                    val b = findViewById(R.id.fail_button) as Button
                    b.setText(R.string.testFail_retry)
                    b.setOnClickListener {
                        page_init()
                        b.setOnClickListener(null)
                    }
                })
            }
        })
    }



    @Throws(java.lang.Exception::class)
    fun readFileFromAssets(name: String): String {
        val b = BufferedReader(InputStreamReader(context?.getAssets()?.open(name)))
        var ret: String = ""
        try {
            while (true) {
                val s: String = b.readLine() ?: break
                ret += s
            }
        } catch (e: EOFException) {
        }
        return ret
    }
    fun hideView(id: Int) {
        val v: View = fragmentView.findViewById(id)
        if (v != null) v.visibility = View.GONE
    }
}

