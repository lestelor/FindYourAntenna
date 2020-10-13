package lestelabs.antenna.ui.main


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.github.anastr.speedviewlib.SpeedView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import fr.bmartel.speedtest.utils.SpeedTestUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.scanner.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


/*
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab1.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    //private var mListener: Tab1.OnFragmentInteractionListener? = null
    private val speedTestSocket = SpeedTestSocket()
    private var internetSpeedDownload: Float = 0.0f
    private var internetSpeedUpload: Float = 0.0f
    private var internetSpeedUploadAnt: Float = 0.0f
    private var listLatency = ""
    private var listNetwork = ""
    private var listDownload = ""
    private var listUpload = ""
    private var speedTestRunningStep = 0
    lateinit var mAdView : AdView
    private lateinit var fragmentView: View
    private var listener: GetfileState? = null

    private lateinit var speedometer:SpeedView
    private val speedTestType = 0 // ikoula/scaleway/tele2
    private val speedTestFile = 2 // 1/10/1000MB
    private var lastUpload = 0.0f
    private var lastDownload = 0.0f

    var mHandler: Handler = Handler()

    var clockTimerHanlerActive = false
    private lateinit var mHandlerTask: Runnable
    private lateinit var mHandlerRater: Runnable
    private var firstOnResume = true
    private var minTime:Long = 10000

    private lateinit var tvDownload:TextView
    private lateinit var tvUpload:TextView
    private lateinit var tvLatency:TextView
    private lateinit var ivButton:ImageView

    private var testTimeStart: Long = 0
    private var timeAnt: Long = 0

    private lateinit var telephonyManager: TelephonyManager
    private var pDevice:DevicePhone = DevicePhone()
    private var networkType: String = ""
    private var deviceWifi:DeviceWiFi = DeviceWiFi()
    private lateinit var db:FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
        mHandler.removeCallbacks(mHandlerTask)
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


            // layout widgets
            tvDownload = fragmentView.findViewById<View>(R.id.tvSpeedtestDownload) as TextView
            tvUpload = fragmentView.findViewById<View>(R.id.tvSpeedtestUpload) as TextView
            tvLatency = fragmentView.findViewById<View>(R.id.tvLatency) as TextView
            ivButton = fragmentView.findViewById<View>(R.id.fab_tab1_onoff) as ImageView
            speedometer = fragmentView.findViewById<SpeedView>(R.id.speedView)
            //val fab: ImageView = fragmentView.findViewById(R.id.btSpeedTest)
            db = FirebaseFirestore.getInstance()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


            val arrayOfSpeed: ArrayList<SpeedTest> = ArrayList<SpeedTest>()
            val adapter = SpeedAdapter(activity, arrayOfSpeed)
            // Attach the adapter to a ListView
            val listView = fragmentView.findViewById(R.id.speedList) as ListView
            listView.adapter = adapter
            adapter.clear()
            var listOfSpeedTest = fillSpeedList(false, "", "", "", "")
            adapter.addAll(listOfSpeedTest)
            adapter.notifyDataSetChanged()


            val downLoadFile = Constants.SPEEDTESTDOWNLOAD[3 * (speedTestType) + speedTestFile]
            var upLoadFile: String = ""
            when (speedTestType) {
                0, 1 -> upLoadFile = Constants.SPEEDTESTUPLOAD[0]
                2 -> {
                    var fileName = SpeedTestUtils.generateFileName() + ".txt"
                    upLoadFile = Constants.SPEEDTESTUPLOAD[1].toString() + fileName.toString()
                }
            }

            Log.d("cfauli", "down file " + downLoadFile)
            Log.d("cfauli", "up file " + upLoadFile)


            // Admod
            mAdView = fragmentView.findViewById(R.id.adViewFragment1)
            MobileAds.initialize(requireActivity())

            val adRequest = AdRequest.Builder()
                //.addTestDevice(getString(R.string.TestDeviceID))
                .build()

            mAdView.loadAd(adRequest)
            mAdView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                    Log.d("cfauli", " ad TAB1 failed")
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            }
            // Set a fixed minTime in order to control whether the speedtesttimer is exceeded
            //val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            //minTime  = sharedPreferences.getInt("num_time_samples", getString(R.string.minTimeSample).toInt()).toLong() * 1000
            startMobileScannerTab1(fragmentView)


            // speedometer parametters
            speedometer.unit = "Mbps"
            speedometer.minSpeed = 0.0f
            speedometer.maxSpeed = 120.0f
            speedometer.withTremble = false
            speedometer.sections[0].color = Color.RED
            speedometer.sections[0].endOffset = (1f / 1.2f) * 0.1f
            speedometer.sections[1].color = Color.YELLOW
            speedometer.sections[1].startOffset = (1f / 1.2f) * 0.1f
            speedometer.sections[1].endOffset = (1f / 1.2f) * 0.2f
            speedometer.sections[2].color = Color.GREEN
            speedometer.sections[2].startOffset = (1f / 1.2f) * 0.2f

            /*speedTestSocket.downloadSetupTime = 1000
        speedTestSocket.uploadSetupTime = 1000
        speedTestSocket.socketTimeout = 1000*/

            var buttonColor: Boolean

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            speedometer.setOnClickListener {
                // Force a crash
                //throw RuntimeException("Test Crash") // Force a crash

                testTimeStart = System.currentTimeMillis() % 1000000
                fillNetworkTextView(requireView())


                if (speedTestRunningStep == 0) {
                    Toast.makeText(context, getString(R.string.SpeedTestStarted), Toast.LENGTH_SHORT).show()
                    Log.d("cfauli speedtest step0", speedTestRunningStep.toString())
                    buttonColor = false
                    ivButton.setImageResource(R.drawable.ic_switch_on_off_grey)

                    tvDownload.text = "-"
                    tvUpload.text = "-"
                    tvLatency.text = "-"
                    testTimeStart = System.currentTimeMillis() % 1000000
                    speedTestRunningStep = 1

                    // Here we choose the file to dowload fron Object Constants
                    speedTestSocket.startDownloadRepeat(downLoadFile,
                        10000, 1000, object : IRepeatListener {
                            override fun onCompletion(report: SpeedTestReport) {
                                testTimeStart = System.currentTimeMillis() % 1000000
                                speedTestRunningStep = 2
                                internetSpeedDownload = report.transferRateBit.toFloat() / 1000000.0f
                                listDownload = "%.1f".format(internetSpeedDownload)
                                requireActivity().runOnUiThread(Runnable {
                                    speedometer.speedTo(0.0f, 1000)

                                    Log.d("cfauli", "[COMPLETED] step" + speedTestRunningStep)
                                    tvDownload.text = listDownload

                                    //    Thread.sleep(2000)
                                    //Log.d("cfauli up file size", ((((10).toDouble().pow(speedTestFile).toInt()))*1000000).toString())
                                    // Start upload test (once the download test finishes)---------------------------------------------------------

                                    speedTestSocket.clearListeners()
                                    speedTestSocket.startUploadRepeat(upLoadFile,
                                        10000, 1000, (10.0.pow(speedTestFile).toInt()) * 1000000, object : IRepeatListener {

                                            override fun onCompletion(report: SpeedTestReport) {
                                                ivButton.setBackgroundResource(R.drawable.ic_switch_on_off)

                                                Log.d("cfauli up file size", (((10.0.pow(speedTestFile).toInt())) * 1000000).toString())
                                                internetSpeedUpload = report.transferRateBit.toFloat() / 1000000.0f

                                                speedTestSocket.clearListeners()
                                                speedTestSocket.closeSocket()

                                                if (internetSpeedUpload > internetSpeedDownload + 5.0f) internetSpeedUpload = internetSpeedDownload + 5.0f
                                                if (internetSpeedUpload == 0.0f) internetSpeedUpload = internetSpeedUploadAnt

                                                listUpload = "%.1f".format(internetSpeedUpload)
                                                requireActivity().runOnUiThread(Runnable {
                                                    speedometer.speedTo(0.0f, 1000)
                                                    testTimeStart = System.currentTimeMillis() % 1000000
                                                    speedTestRunningStep = 3
                                                    tvUpload.text = listUpload


                                                    // Test latency (once the upload test finishes)-----------------
                                                    pingg("http://www.google.com") { it ->
                                                        listLatency = it.toString()
                                                        tvLatency.text = listLatency
                                                        // Fill the Speed List fragment view
                                                        // Create the adapter to convert the array to views

                                                        adapter.clear()
                                                        writeCloudFirestoreDB(listDownload, listUpload, listLatency)
                                                        listOfSpeedTest = fillSpeedList(true, listNetwork, listDownload, listUpload, listLatency)
                                                        adapter.addAll(listOfSpeedTest)
                                                        adapter.notifyDataSetChanged()

                                                        var numRater = 0
                                                        mHandlerRater = object : Runnable {
                                                            override fun run() {
                                                                when (numRater) {
                                                                    0 -> {
                                                                        mHandler.postDelayed(this, 10000)
                                                                        numRater += 1
                                                                    }
                                                                    1 -> {
                                                                        AppRater.app_launched((requireContext()))
                                                                        mHandler.removeCallbacks(this)
                                                                        numRater += 1
                                                                    }
                                                                    else -> {}
                                                                }


                                                            }
                                                        }

                                                        mHandlerRater.run()

                                                    }

                                                    Log.d("cfauli speedtest", "end...................")
                                                    speedTestRunningStep = 0
                                                    lastUpload = 0.0f

                                                    ivButton.setImageResource(R.drawable.ic_switch_on_off)
                                                    speedTestSocket.clearListeners()
                                                    speedTestSocket.closeSocket()
                                                    speedTestSocket.forceStopTask()


                                                    // End test latency  --------------------------------------------
                                                })
                                            }

                                            override fun onReport(report: SpeedTestReport) {
                                                // called when a upload report is dispatched
                                                buttonColor = !buttonColor
                                                // Only the original thread that created a view hierarchy can touch its views. en Android
                                                activity!!.runOnUiThread {
                                                    if (buttonColor) ivButton.setImageResource(R.drawable.ic_switch_on_off)
                                                    else ivButton.setImageResource(R.drawable.ic_switch_on_off_grey)
                                                }

                                                internetSpeedUpload = report.transferRateBit.toFloat() / 1000000.0f

                                                if (internetSpeedUpload > internetSpeedDownload + 5.0f) internetSpeedUpload = internetSpeedDownload + 5.0f
                                                if (internetSpeedUpload == 0.0f) internetSpeedUpload = internetSpeedUploadAnt
                                                internetSpeedUploadAnt = internetSpeedUpload
                                                Log.d("cfauli speedtest upload", internetSpeedUpload.toString() + " " + internetSpeedDownload.toString())
                                                requireActivity().runOnUiThread(Runnable {
                                                    speedometer.speedTo(internetSpeedUpload, 1000)
                                                    lastUpload = internetSpeedUpload
                                                })
                                            }


                                        })
                                    // End upload test---------------------------------------------------------------------------------------------
                                })
                                //
                            }

                            override fun onReport(report: SpeedTestReport) {
                                buttonColor = !buttonColor
                                activity!!.runOnUiThread {
                                    if (buttonColor) ivButton.setImageResource(R.drawable.ic_switch_on_off)
                                    else ivButton.setImageResource(R.drawable.ic_switch_on_off_grey)
                                }

                                // called when a download report is dispatched
                                internetSpeedDownload = report.transferRateBit.toFloat() / 1000000.0f
                                if (internetSpeedDownload == 0.0f) internetSpeedDownload = lastDownload
                                else lastDownload = internetSpeedDownload
                                Log.d("cfauli speedtest downlo", internetSpeedDownload.toString())
                                requireActivity().runOnUiThread(Runnable {
                                    speedometer.speedTo(internetSpeedDownload, 1000)
                                })
                            }


                        })
                    ivButton.setBackgroundResource(R.drawable.ic_switch_on_off)

                } else {
                    stopSpeedTest()

                }

            }
            //fillNetworkTextView(view)
            // [START shared_app_measurement]
            // Obtain the FirebaseAnalytics instance.
            /*
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

            // [END shared_app_measurement]
            val params = Bundle()
            params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "screen")
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, "Tab1")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params)
            */
            Log.d("cfauli", "Oncreateview tab1 final")
            return fragmentView

    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("cfauli", "OnAttach TAB1")
        /*mListener = if (context is Tab1.OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }*/
        try {
            listener = activity as GetfileState
            // listener.showFormula(show?);
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }

    }

    override fun onDetach() {
        super.onDetach()
        Log.d("cfauli", "OnDetach TAB1")
        listener = null
    }

    override fun onResume() {
        super.onResume()

        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        //minTime  = sharedPreferences.getInt("num_time_samples", 10).toLong() * 1000

        // firstOnResume = true if activity is destroyed (back) and goes trough a oncreateview, in order not to repeat the scanners
        if (!firstOnResume) startMobileScannerTab1(requireView())
        firstOnResume = false
        Log.d("cfauli", "OnResume tab1")

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    /*interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }*/

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

    fun pingg(domain: String, callback: (Long) -> Unit) {
        val runtime = Runtime.getRuntime()
        var timeofping: Array<Long> = arrayOf(0, 0, 0, 0)
        try {
            for (i in 0..3) {
                var a = System.currentTimeMillis() % 1000000
                var ipProcess = runtime.exec("/system/bin/ping -c 1 $domain")
                ipProcess.waitFor()
                var b = System.currentTimeMillis() % 1000000
                timeofping[i] = if (b <= a) {
                    1000000 - a + b
                } else {
                    b - a
                }
            }
        } catch (e: Exception) {
        }
        callback(timeofping.min()?.toLong()!!)
    }

    /*@RequiresApi(Build.VERSION_CODES.M)
    fun setNetworkType(context:Context):List<Any?> {
        var type:String = ""
     if (Connectivity.isConnectedWifi(context)){
         type = Connectivity.getWifiParam(context).toString()
     } else if (Connectivity.isConnected(context)) {
         type = listOf(Connectivity.connectionType(Connectivity.networkType(context), Connectivity.networkSubtype(context)).toString()).toString()
     } else type =""
        return listOf(type)
    }*/



    //@RequiresApi(Build.VERSION_CODES.M)
    fun fillNetworkTextView(view: View) {
        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
        //Log.d("cfauli", "fillMobileTextView")
        val tvNetwork = view.findViewById<View>(R.id.tvTab1MobileNetworkType) as TextView
        if (Connectivity.isConnectedMobile(requireContext())) networkType = "MOBILE"
        else if (Connectivity.isConnectedWifi(requireContext())) networkType = "WIFI"

        if (firstOnResume)   telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pDevice = loadCellInfo(telephonyManager)
        deviceWifi = Connectivity.getWifiParam(requireContext())

        if (Connectivity.isConnectedMobile(requireContext())) {
            listNetwork = pDevice.type + " " + "%.1f".format(calculateFreq(pDevice.type, pDevice.band)) + "MHz " + pDevice.dbm + "dBm id: " + pDevice.mcc + "-" + pDevice.mnc + "-" + pDevice.lac + "-" + pDevice.cid
            tvNetwork.text = listNetwork

        } else if (Connectivity.isConnectedWifi(requireContext()))  {

            Log.d("cfauli deviceWifi ", deviceWifi.toString())
            val freq = deviceWifi.centerFreq2
            val channel: Int = getChannel(freq)

            listNetwork = deviceWifi.ssid + " ch: " + channel + " " + deviceWifi.centerFreq2 + "MHz " + deviceWifi.level + "dBm"
            tvNetwork.text = "WIFI " + listNetwork

        }

        saveSamplesFirebase()
    }

    fun saveSamplesFirebase() {


        // if duration > x seg then
        val a = timeAnt
        val b = System.currentTimeMillis() % 1000000

        var duration:Long
        if (b <= a) {
            duration = 1000000 - a + b
        } else {
            duration = b - a
        }

        var minDuration:Long  = 0
        db.collection("SampleDuration").document("kEMpdsQpBLnFf0wKJ951")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    minDuration = task.result!!.data!!["time"].toString().toLong()
                    Log.d("cfauli document", "minDuration " + duration + " " + minDuration)
                    if (duration > minDuration) {
                        timeAnt = b
                        saveDocument()
                    }
                } else {
                    saveDocument()
                    Log.w("Error", "Error getting Firebase minDuration", task.exception)
                }
            }


    }

fun saveDocument() {


        try {
            //Thread.sleep(1000)
            if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE)
            }
            var lat = 0.0
            var lon = 0.0
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
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
                        speedTestSample["MobileNetwork"] = pDevice.type
                        speedTestSample["MobileMcc"] = pDevice.mcc
                        speedTestSample["MobileMnc"] = pDevice.mnc
                        speedTestSample["MobileLac"] = pDevice.lac
                        speedTestSample["MobileCid"] = pDevice.cid
                        speedTestSample["MobiledBm"] = pDevice.dbm
                        speedTestSample["WifiNetwork"] = deviceWifi.ssid
                        speedTestSample["WifidBm"] = deviceWifi.level

                        val sharedPreferences = requireContext().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
                        val pleaseContribute = sharedPreferences.getBoolean("please_contribute", true)


                        if (networkType != "" && pleaseContribute) {
                            val documentId = DateFormat.format("yyyyMMdd", dateNow).toString() + "/" + DateFormat.format("HHmmss", dateNow).toString() + "/" + pDevice.cid + ";" + deviceWifi.ssid
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


        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        var numSpeedTest = sharedPreferences.getInt("num_speed_test", 0)
        var textDate:String
        var textNetwork:String
        var textSpeed:String


        if (new) {
            val dateNow = Date()
            val dateFormated = DateFormat.format("dd/MM/yyy HH:mm", dateNow)
            var day =
                dateFormated.toString() +
                        if (Connectivity.isConnectedWifi(requireContext())) " WIFI"
                        else " " + pDevice.type


            Log.d("cfauli", "numspeedtest " + numSpeedTest)
            numSpeedTest += 1

            editor.putInt("num_speed_test", numSpeedTest)
            editor.apply()

            val textDate= "speed_test_date" + numSpeedTest
            val textNetwork = "speed_test_network" + numSpeedTest
            val textSpeed = "speed_test_speed" + numSpeedTest

            editor.putString(textDate, day)
            editor.apply()

            editor.putString(textNetwork, network)
            editor.apply()

            editor.putString(textSpeed, getString(R.string.Download) + download + "Mbps " + getString(R.string.Upload) + upload + "Mbps " + "ping: " + ping + "ms")
            editor.apply()
            editor.commit()

        }

        var listOfSpeedTest: ArrayList<SpeedTest> = arrayListOf()
        if (numSpeedTest>=1) {
            for (i in 1..numSpeedTest) {
                textNetwork = "speed_test_network" + i
                textSpeed = "speed_test_speed" + i
                textDate = "speed_test_date" + i

                Log.d("cfauli", "textNetwork " + textNetwork)
                listOfSpeedTest.add(0, SpeedTest(sharedPreferences.getString(textDate, ""), sharedPreferences.getString(textNetwork, ""), sharedPreferences.getString(textSpeed, "")))
            }
        }


        //return listOfSpeedTest.sortedByDescending { it.date }
        return listOfSpeedTest

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


    }
    fun getChannel(freq: Int?):Int {
        val channel = if (freq!! > 5000) {
            (freq - 5180) / 5 + 36
        } else {
            (freq - 2412) / 5 + 1
        }
        return channel
    }
    fun writeCloudFirestoreDB(downlink: String, uplink: String, latency: String) {

        var lat = 0.0
        var lon = 0.0
        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE)
            Thread.sleep(1000)
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                lat = location!!.latitude
                lon = location.longitude
                val dateNow = Date()
                val documentId = DateFormat.format("yyyyMMdd",dateNow).toString() + "/" +  DateFormat.format("HHmmss",dateNow).toString() + "/"  + pDevice.cid + ";" + deviceWifi.ssid

                val speedTestSample: MutableMap<String, Any?> = HashMap()

                speedTestSample["date"] = DateFormat.format("yyyy/MM/dd HH:mm:ss", dateNow)
                speedTestSample["type"] = networkType
                speedTestSample["lat"] = lat
                speedTestSample["lon"] = lon
                speedTestSample["downlink"] = downlink
                speedTestSample["uplink"] = uplink
                speedTestSample["latency"] = latency


                    speedTestSample["MobileNetwork"] = pDevice.type
                    speedTestSample["MobileMcc"] = pDevice.mcc
                    speedTestSample["MobileMnc"] = pDevice.mnc
                    speedTestSample["MobileLac"] = pDevice.lac
                    speedTestSample["MobileCid"] = pDevice.cid
                    speedTestSample["MobileCh"] = pDevice.band
                    speedTestSample["MobileFreq"] = calculateFreq(pDevice.type, pDevice.band)
                    speedTestSample["MobiledBm"] = pDevice.dbm


                    speedTestSample["WifiNetwork"] = deviceWifi.ssid
                    speedTestSample["WifiFreq"] = deviceWifi.centerFreq2
                    speedTestSample["WifiCh"] = getChannel(deviceWifi.centerFreq2)
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

}

