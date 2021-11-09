package lestelabs.antenna.ui.main


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab1.*
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.MyApplication.Companion.ctx
import lestelabs.antenna.ui.main.core.Speedtest
import lestelabs.antenna.ui.main.core.Speedtest.SpeedtestHandler
import lestelabs.antenna.ui.main.core.config.SpeedtestConfig
import lestelabs.antenna.ui.main.core.config.TelemetryConfig
import lestelabs.antenna.ui.main.core.serverSelector.TestPoint
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.data.*
import lestelabs.antenna.ui.main.scanner.*
import lestelabs.antenna.ui.main.ui.GaugeView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.EOFException
import java.io.InputStreamReader
import java.net.InetAddress
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

    private var reinitOnResume = false
    private var st: Speedtest? = null
    private val availableServers: ArrayList<TestPoint> = ArrayList()
    private lateinit var spinner:Spinner
    private lateinit var startButton:FrameLayout
    private lateinit var stopButton:FrameLayout
    private lateinit var tvComments:  TextView
    private lateinit var progress: ProgressBar
    private  var download_value: Double = 0.0
    private  var upload_value: Double = 0.0
    private  var ping_value: Double = 0.0

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    lateinit var mAdView : AdView
    private lateinit var fragmentView: View
    private lateinit var connectivity: Connectivity
    val db = FirebaseFirestore.getInstance()


    private lateinit var telephonyManager: TelephonyManager
    private var pDevice: DevicePhone? = DevicePhone()
    private var networkType: String = ""
    private var deviceWifi: DeviceWiFi = DeviceWiFi()
    private var fusedLocationClient: FusedLocationProviderClient? = null

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
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        // Adds -------------------------------------------------------------------------------------
        mAdView = fragmentView.findViewById(R.id.adViewFragment1)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        spinner = fragmentView.findViewById(R.id.serverList)
        startButton = fragmentView.findViewById(R.id.restartButton)
        stopButton = fragmentView.findViewById(R.id.restopButton)
        tvComments = fragmentView.findViewById(R.id.Tab1Comments)
        progress = fragmentView.findViewById(R.id.Tab1ProgressBar)


        val screenSize = getScreenSize()
        Log.d("Tab1", screenSize.first.toString())
        if (screenSize.first < MIN_HEIGHT_ADS) {
            fragmentView.adViewFragment1.visibility = View.GONE
        }
        page_init()
        list_servers()
        start_onclick()

        Log.d("cfauli", "Oncreateview tab1 final")
        return fragmentView
    }



    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val MIN_HEIGHT_ADS = 2000
        private const val TRANSITION_LENGTH = 300
        private const val TAG = "TAB1"

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


    private fun isConnectedMobile(): Boolean {
        return  connectivity.isConnectedMobile()
    }
    private fun isConnectedWifi(): Boolean {
        return  connectivity.isConnectedWifi()
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
    fun writeCloudFirestoreDB(downlink: String?, uplink: String?, latency: String?) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        pDevice = loadCellInfo(telephonyManager)
        deviceWifi = context?.let { connectivity.getWifiParam(it) } ?: DeviceWiFi()
        if (isConnectedMobile()) networkType = "MOBILE"
        if (isConnectedWifi()) networkType = "WIFI"

        Log.d("TAB1", "network type " + networkType + " " + downlink + " " + uplink + " " + ping_value)

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

                // Control point for Crashlitycs
                crashlyticsKeyAnt = controlPointCrashlytics(
                    tabName,
                    Thread.currentThread().stackTrace,
                    crashlyticsKeyAnt
                )

                val dateNow = Date()
                val documentId = DateFormat.format("yyyyMMdd", dateNow)
                    .toString() + "/" + DateFormat.format("HHmmss", dateNow)
                    .toString() + "/" + pDevice?.cid + ";" + deviceWifi.ssid

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


    fun page_init() {
        object : Thread() {
            override fun run() {
                var config: SpeedtestConfig? = null
                var telemetryConfig: TelemetryConfig? = null
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
                    abort_test()
                    st = Speedtest()
                    st?.setSpeedtestConfig(config)
                    st?.setTelemetryConfig(telemetryConfig)

                } catch (e: Throwable) {
                    System.err.println(e)
                    reinit()
                    return
                }
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



/*    fun page_privacy() {
        transition(R.id.page_privacy, TRANSITION_LENGTH)
        reinitOnResume = false
        (fragmentView.findViewById(R.id.privacy_policy) as WebView).loadUrl(getString(R.string.privacy_policy))
        val t = fragmentView.findViewById(R.id.privacy_close) as TextView
        t.setOnClickListener {
            transition(R.id.page_serverSelect, TRANSITION_LENGTH)
            reinitOnResume = true
        }
    }*/

    fun page_test(selected: TestPoint) {
        //transition(R.id.page_test, TRANSITION_LENGTH)
        st?.setSelectedServer(selected)
        (fragmentView.findViewById(R.id.dlText) as TextView).setText(format(0.0))
        (fragmentView.findViewById(R.id.ulText) as TextView).setText(format(0.0))
        (fragmentView.findViewById(R.id.pingText) as TextView).setText(format(0.0))
        (fragmentView.findViewById(R.id.jitterText) as TextView).setText(format(0.0))
        (fragmentView.findViewById(R.id.dlProgress) as ProgressBar).progress = 0
        (fragmentView.findViewById(R.id.ulProgress) as ProgressBar).progress = 0
        (fragmentView.findViewById(R.id.dlGauge) as GaugeView).value = 0
        (fragmentView.findViewById(R.id.ulGauge) as GaugeView).value = 0
        (fragmentView.findViewById(R.id.ipInfo) as TextView).text = ""
/*        (fragmentView.findViewById(R.id.logo_inapp) as ImageView).setOnClickListener(View.OnClickListener {
            val url = getString(R.string.logo_inapp_link)
            if (url.isEmpty()) return@OnClickListener
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })*/
/*        val endTestArea: View = fragmentView.findViewById(R.id.endTestArea)
        val endTestAreaHeight = endTestArea.height
        val p = endTestArea.layoutParams
        p.height = 0
        endTestArea.layoutParams = p*/
        st?.start(object : SpeedtestHandler() {
            override fun onDownloadUpdate(dl: Double, progress: Double) {
                activity?.runOnUiThread(Runnable {
                    (fragmentView.findViewById(R.id.dlText) as TextView).text =
                        if (progress == 0.0) "..." else format(dl)
                    (fragmentView.findViewById(R.id.dlGauge) as GaugeView).setValue(
                        if (progress == 0.0) 0 else mbpsToGauge(
                            dl
                        )
                    )
                    (fragmentView.findViewById(R.id.dlProgress) as ProgressBar).progress =
                        (100 * progress).toInt()
                })
                download_value = dl
            }

            override fun onUploadUpdate(ul: Double, progress: Double) {
                activity?.runOnUiThread(Runnable {
                    (fragmentView.findViewById(R.id.ulText) as TextView).text =
                        if (progress == 0.0) "..." else format(ul)
                    (fragmentView.findViewById(R.id.ulGauge) as GaugeView).setValue(
                        if (progress == 0.0) 0 else mbpsToGauge(
                            ul
                        )
                    )
                    (fragmentView.findViewById(R.id.ulProgress) as ProgressBar).progress =
                        (100 * progress).toInt()
                })
                upload_value = ul
            }

            override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) {
                activity?.runOnUiThread(Runnable {
                    (fragmentView.findViewById(R.id.pingText) as TextView).text =
                        if (progress == 0.0) "..." else format(ping)
                    (fragmentView.findViewById(R.id.jitterText) as TextView).text =
                        if (progress == 0.0) "..." else format(jitter)
                    Log.d("TAB1", "Ping " + ping + "Jitter " + jitter)
                })
                ping_value = ping
            }

            override fun onIPInfoUpdate(ipInfo: String) {
                activity?.runOnUiThread(Runnable { (fragmentView.findViewById(R.id.ipInfo) as TextView).text = ipInfo })
            }

            override fun onTestIDReceived(id: String, shareURL: String) {
                if (shareURL == null || shareURL.isEmpty() || id == null || id.isEmpty()) return

            }

            override fun onEnd() {
                Log.d("Tab1", "onEnd")
                val startT = System.currentTimeMillis()
                val endT: Long = startT + TRANSITION_LENGTH
                var first = true

                object : Thread() {
                    override fun run() {
                        while (System.currentTimeMillis() < endT) {
                            val f = (System.currentTimeMillis() - startT).toDouble() / (endT - startT).toDouble()
                        }

                        activity?.runOnUiThread(Runnable {
                            progress.visibility = View.GONE
                            stopButton.visibility = View.GONE
                            startButton.visibility = View.VISIBLE

                            if (tvComments.currentTextColor == getColor(requireContext(), R.color.commentsTextAlert)) {
                                download_value = 0.0
                                upload_value = 0.0
                                ping_value = 0.0
                                tvComments.gravity = Gravity.CENTER
                                tvComments.text = getString(R.string.testFail_err)
                            } else {
                                tvComments.setTextColor(getColor(requireContext(),R.color.commentsText))
                                tvComments.gravity = Gravity.CENTER
                                tvComments.text = getString(R.string.serverSelect_message)
                            }
                            stopButton.setOnClickListener(null)
                            start_onclick()
                            if (download_value > 0.5 && upload_value > 0.5 && ping_value > 0.0) {
                                Log.d("TAB1", "inapp review evaluate")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    ctx?.let { it1 -> run(){
                                        Log.d("TAB1", "inappreview evaluate")
                                        InAppReview.inAppReview(it1, requireActivity())
                                    } }
                                }, 7000)
                            }

/*                                val p = endTestArea.layoutParams
                            p.height = (endTestAreaHeight * f).toInt()
                            endTestArea.layoutParams = p*/
                            //Write data to firestore
                            writeCloudFirestoreDB(format(download_value),format(upload_value),format(ping_value))
                        })
                        try {
                            sleep(10)
                        } catch (t: Throwable) {
                        }

                    }
                }.start()
            }

            override fun onCriticalFailure(err: String) {
                activity?.runOnUiThread(Runnable {
                    tvComments.setTextColor(getColor(requireContext(), R.color.commentsTextAlert))
                    tvComments.gravity = Gravity.CENTER
                    tvComments.text = getString(R.string.testFail_err)
                    spinner.setSelection(spinner.selectedItemPosition+1)

                    progress.visibility = View.GONE
                    stopButton.visibility = View.GONE
                    startButton.visibility = View.VISIBLE
                    stopButton.setOnClickListener(null)
                    start_onclick()


                /*transition(R.id.page_fail, TRANSITION_LENGTH)
                    (fragmentView.findViewById(R.id.fail_text) as TextView).text =
                        getString(R.string.testFail_err)
                    val b = fragmentView.findViewById(R.id.fail_button) as Button
                    b.setText(R.string.testFail_retry)
                    b.setOnClickListener {
                        page_init()
                        b.setOnClickListener(null)
                    }
                     */
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
/*    fun hideView(id: Int) {
        val v: View = fragmentView.findViewById(id)
        if (v != null) v.visibility = View.GONE
    }*/

    fun start_onclick() {
        startButton.setOnClickListener {
            Log.d("Tab1", "Start onclick")
            activity?.runOnUiThread(Runnable {
                progress.visibility = View.VISIBLE
                page_test(availableServers[spinner.selectedItemPosition])
            })
            startButton.setOnClickListener(null)
            stopButton.visibility = View.VISIBLE
            startButton.visibility = View.GONE
            tvComments.setTextColor(getColor(requireContext(), R.color.commentsText))
            tvComments.text = getString(R.string.testStarted)
            stop_onclick()
        }


    }

    fun stop_onclick() {
            stopButton.setOnClickListener {
                Log.d("Tab1", "Stop onclick")
                activity?.runOnUiThread(Runnable {
                    progress.visibility = View.GONE
                    abort_test()
                })
                download_value = 0.0
                upload_value = 0.0
                ping_value = 0.0
                stopButton.setOnClickListener(null)
                stopButton.visibility = View.GONE
                startButton.visibility = View.VISIBLE
                tvComments.setTextColor(getColor(requireContext(), R.color.commentsText))
                tvComments.gravity = Gravity.CENTER
                tvComments.text = getString(R.string.serverSelect_message)
                start_onclick()
        }
    }

    fun abort_test() {
        if (st != null) {
            try {
                st?.abort()
            } catch (e: Throwable) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (reinitOnResume) {
            reinitOnResume = false
            page_init()
        }
    }

    fun parseServersToTestPoint(servers:MutableList<Server>): ArrayList<TestPoint> {
        val testPoints: ArrayList<TestPoint> = arrayListOf()
        for (i in 0..servers.size-1) {
            val testPoint:TestPoint = TestPoint(servers[i].name,servers[i].server, servers[i].dlURL,servers[i].ulURL,servers[i].pingURL,servers[i].getIpURL,servers[i].sponsorName,servers[i].sponsorURL)
            testPoints.add(testPoint)
        }
        return testPoints
    }

    fun list_servers() {
        // val servers: String = readFileFromAssets("ServerList.json")
        // val serversJASON = JSONArray(servers)

        progress.visibility = View.VISIBLE
        tvComments.text = getString(R.string.SelectingServers)

       getServers() {
           val testPoints: ArrayList<TestPoint> = parseServersToTestPoint(it)
           val serversList: ArrayList<String> = arrayListOf()

           orderServersByPing(testPoints) { testPointsOrdered:ArrayList<TestPoint> ->
               for (i in 0..testPoints.size - 1) {
                   serversList.add(testPointsOrdered[i].name)
                   availableServers.add(testPointsOrdered[i])
               }

               activity?.runOnUiThread(Runnable {
                   spinner.adapter = ArrayAdapter<String>(
                       requireContext(),
                       android.R.layout.simple_spinner_item,
                       serversList
                   )
                   Log.d(TAG, "ping first server " + serversList[0] + " posicion " + spinner.selectedItemPosition)
                   tvComments.text = getString(R.string.PressStartButton)
                   tvComments.gravity = Gravity.CENTER
                   startButton.visibility = View.VISIBLE
                   progress.visibility = View.GONE
               })


               /*spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    serverSelectedInt = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // vacio
                }
            }*/
           }
       }

    }

    private fun orderServersByPing(testPoints: ArrayList<TestPoint>, callback: (ArrayList<TestPoint>) -> Unit) {

        val testPointsPing = ArrayList<Pair<Int,Long>>()
        val testPointsOrdered: ArrayList<TestPoint> = ArrayList()

        for (i in 0..testPoints.size-1) {
            //pingg("https:" + testPoints[i].server + "/" + testPoints[i].pingURL) {
            //val server = testPoints[i].server.replace("https://","").replace("http://","").replace("//","").split("/")
                val server = "https:" + testPoints[i].server

            pingg(server) {
                if (it != null) {
                    testPointsPing.add(Pair(i,it))
                } else testPointsPing.add(Pair(i,10000))
                Log.d(TAG, "TestPointsPings " + i + " " + testPointsPing[i].second + " " + testPoints[testPointsPing[i].first].name)
            }
        }
        val result = testPointsPing.sortedWith(compareBy({it.second }))
        for (i in 0..testPoints.size-1) {
            testPointsOrdered.add (testPoints[result[i].first])
        }
        callback(testPointsOrdered)
    }


    fun reinit() {
        st = null
/*,                 transition(R.id.page_fail, TRANSITION_LENGTH)
                    runOnUiThread(Runnable {
                        (fragmentView.findViewById(R.id.fail_text) as TextView).text =
                            getString(R.string.initFail_configError) + ": " + e.message
                        val b = fragmentView.findViewById(R.id.fail_button) as Button
                        b.setText(R.string.initFail_retry)
                        b.setOnClickListener {
                            page_init()
                            b.setOnClickListener(null)
                        }
                    })*/

    }
    fun getScreenSize(): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return Pair(height,width)
    }


    // Get Servers and Update UI
    private fun getServers(callback: (MutableList<Server>) -> Unit) {
        var servers: MutableList<Server> = mutableListOf()
        FirestoreDB.loadServersFromFirestore() {
            if (it.count() > 0) {
                saveServersToLocalDatabase(it.toTypedArray())
                Log.d(TAG, "finish loading #sites from Firestore " + it.count())
                callback(it)
            } else {
                // Load whatever is stored locally
                loadServersFromLocalDb() {
                    if (it.count()>0) {
                        callback(it)
                        Log.d(TAG, "finish loading #sites from local db " + it.count())
                    } else {
                        //use provided server list in JSON file
                        val c = readFileFromAssets("ServerList.json")
                        val a = JSONArray(c)
                        if (a.length() == 0) throw Exception("No test points")
                        for (i in 0..a.length() - 1) {
                            val server = TestPoint(a.getJSONObject(i))
                            servers.add(Server(i, server.name,server.server, server.dlURL,
                                    server.ulURL, server.pingURL, server.getIpURL,
                                    server.getsponsorName(), server.getsponsorURL()))
                        }
                        Log.d(TAG, "finish loading #sites from JSON file " + servers.count())
                        callback(servers)
                    }
                }
            }
        }


    }

    // Load Books from Room
    private fun loadServersFromLocalDb(callback: (MutableList<Server>) -> Unit) {
        val serversInteractor: ServersInteractor = MyApplication.serversInteractor
        // Run in Background, accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            // Get Books
            val servers: MutableList<Server> = serversInteractor.getAllServers()

            Log.d(TAG, "found #servers in local dB " + servers?.count())
            if (servers != null) {
                callback(servers)
            } else callback(mutableListOf())
        }
    }

    // Save Books to Local Storage
    private fun saveServersToLocalDatabase(servers: Array<Server>) {
        val serversInteractor: ServersInteractor = MyApplication.serversInteractor
        // Run in Background; accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            serversInteractor.saveServers(*servers)
            Log.d(TAG, "finish saving #sites in local database " + servers.count())
        }
    }


    fun pingg(domain: String, callback: (Long?) -> Unit) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        val runtime = Runtime.getRuntime()
        var timeofping: Array<Long> = arrayOf(0)
        val domainTriggered = domain.replace("https://","").replace("http://","").replace("//","").split("/")[0]
        val inetAddress: InetAddress = InetAddress.getAllByName(domainTriggered)[0]
        val inetAddressIp = inetAddress.toString().split("/")[1]
            Log.d(TAG, "ping server "+ inetAddress + " ip " + inetAddressIp)

        if (inetAddress.isReachable(80)) {
            try {
                for (i in 0..0) {
                    var a = System.currentTimeMillis() % 1000000
                    //var ipProcess = runtime.exec("/system/bin/ping -c 1 $domain")
                    var ipProcess = runtime.exec("/system/bin/ping -c 1 $inetAddressIp")
                    val exitvalue: Int = ipProcess.waitFor()
                    var b = System.currentTimeMillis() % 1000000
                    Log.d(TAG, "ping errorstream " + exitvalue)
                    timeofping[i] = if(exitvalue != 0) {
                       10000
                    } else if(b <= a) {
                        1000000 - a + b
                    } else {
                        b - a
                    }
                }
            } catch (e: Exception) {
            }
        } else {
            timeofping[0] = 10000
        }
        callback(timeofping.maxOrNull()?.toLong())
        //callback(timeofping[0].toLong())
    }

}

