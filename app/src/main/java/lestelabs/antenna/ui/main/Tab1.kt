package lestelabs.antenna.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.anastr.speedviewlib.SpeedView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import fr.bmartel.speedtest.utils.SpeedTestUtils
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.scanner.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


/**
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
    private var listLatency = ""
    private var listNetwork = ""
    private var listDownload = ""
    private var listUpload = ""
    private var speedTestRunningStep = 0
    lateinit var mAdView : AdView
    private lateinit var fragmentView: View
    private var listener: GetfileState? = null

    private val speedTestType = 0
    private val speedTestFile = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d ("cfauli", "OnCreate TAB1")

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }

    }

    override fun onStart() {
        // call the superclass method first
        super.onStart()
        Log.d("cfauli","OnStart Tab1")
    }
    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli","OnStop Tab1")
    }
    override fun onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause()
        Log.d("cfauli","OnPause Tab1")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d ("cfauli", "OnCreateView TAB1")
        // Inflate view
        fragmentView = inflater.inflate(R.layout.fragment_tab1, container, false)
        // layout widgets
        val tvDownload = fragmentView.findViewById<View>(R.id.tvSpeedtestDownload) as TextView
        val tvUpload = fragmentView.findViewById<View>(R.id.tvSpeedtestUpload) as TextView
        val tvLatency = fragmentView.findViewById<View>(R.id.tvLatency) as TextView
        val speedometer = fragmentView.findViewById<SpeedView>(R.id.speedView)
        //val fab: ImageView = fragmentView.findViewById(R.id.btSpeedTest)

        val arrayOfSpeed: ArrayList<SpeedTest> = ArrayList<SpeedTest>()
        val adapter = SpeedAdapter(activity, arrayOfSpeed)
        // Attach the adapter to a ListView
        val listView = fragmentView.findViewById(R.id.speedList) as ListView
        listView.adapter = adapter
        adapter.clear()
        var listOfSpeedTest =fillSpeedList(false, "", "", "", "")
        adapter.addAll(listOfSpeedTest)
        adapter.notifyDataSetChanged()

/* Not used, by default speedTestType = 0 and speedTestFile = 0
        //Read shared preferences for speedtest
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        var speedTestType = sharedPreferences.getInt("speed_test_type",0)
        var speedTestFile = sharedPreferences.getInt("speed_test_file",0)

        Log.d("cfauli","speedtesttypeandfile"+ speedTestType + " " +speedTestFile)

        //var speedTestType = 0
        //var speedTestFile = 0
        //editor.putInt("speed_test_file",0)
        //editor.putInt("speed_test_type",0)
        //editor.commit()
        
        var downLoadFile = ""
        var upLoadFile = ""
        when(speedTestType) {
            0 -> rbHttp.isChecked=true
            1 -> rbBittorrent.isChecked=true
            2 -> rbFtp.isChecked=true
        }
        when(speedTestFile) {
            0 -> rb1MB.isChecked=true
            1 -> rb10MB.isChecked =true
            2 -> rb100MB.isChecked=true
        }
   */


        var downLoadFile = Constants.SPEEDTESTDOWNLOAD[3*(speedTestType)+speedTestFile]
        var upLoadFile: String =""
        when(speedTestType) {
            0,1-> upLoadFile = Constants.SPEEDTESTUPLOAD[0]
            2-> {
                var fileName = SpeedTestUtils.generateFileName() + ".txt"
                upLoadFile = Constants.SPEEDTESTUPLOAD[1].toString() + fileName.toString()
            }
        }

        Log.d ("cfauli", "down file " + downLoadFile)
        Log.d ("cfauli", "up file " + upLoadFile)

        /*
        var checkedIdConverted: Int = 0
        radioGroupType.setOnCheckedChangeListener { group, checkedId ->

            downLoadFile = Constants.SPEEDTESTDOWNLOAD[3*(speedTestType)+speedTestFile]

            when (checkedId) {
                R.id.rbHttp -> checkedIdConverted = 0
                R.id.rbBittorrent -> checkedIdConverted = 1
                R.id.rbFtp -> checkedIdConverted = 2
            }
            when(checkedIdConverted) {
                0, 1 -> upLoadFile = Constants.SPEEDTESTUPLOAD[0]
                2 -> {
                    val fileName = SpeedTestUtils.generateFileName() + ".txt"
                    Log.d("cfauli","generatefilename" + fileName)
                    upLoadFile = Constants.SPEEDTESTUPLOAD[1].toString() + fileName.toString()
                }
            }

            speedTestType = checkedIdConverted
            editor.putInt("speed_test_type",checkedIdConverted)
            editor.commit()
            Log.d("cfauli" , "checkedidtype" + checkedIdConverted)
        }
        Log.d ("cfauli","speedtes files int " + speedTestType + " " + speedTestFile)
        radioGroupFile.setOnCheckedChangeListener { group, checkedId ->
            Log.d("cfauli" , "checkedidfile" + checkedIdConverted)
            downLoadFile = Constants.SPEEDTESTDOWNLOAD[3*(speedTestType)+speedTestFile]
            when (checkedId) {
                R.id.rb1MB -> checkedIdConverted = 0
                R.id.rb10MB -> checkedIdConverted = 1
                R.id.rb100MB -> checkedIdConverted = 2
            }

            speedTestFile = checkedIdConverted
            Log.d("cfauli", "checkedidfile" + checkedIdConverted)
            editor.putInt("speed_test_file",checkedIdConverted)
            editor.commit()
        }
        */

        // Admod
        mAdView = fragmentView.findViewById(R.id.adViewFragment1)
        val adView = AdView(requireActivity())


        MobileAds.initialize(requireActivity())

        val adRequest = AdRequest.Builder()
            .addTestDevice(getString(R.string.TestDeviceID))
            .build()

        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                // Code to be executed when an ad request fails.
                Log.d("cfauli"," ad TAB1 failed")
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

        // speedometer parametters
        speedometer.unit = "Mbps"
        speedometer.minSpeed = 0.0f
        speedometer.maxSpeed = 120.0f
        speedometer.withTremble = false
        speedometer.sections[0].color = Color.RED
        speedometer.sections[0].endOffset = (1f/1.2f) * 0.1f
        speedometer.sections[1].color = Color.YELLOW
        speedometer.sections[1].startOffset = (1f/1.2f) * 0.1f
        speedometer.sections[1].endOffset = (1f/1.2f) * 0.2f
        speedometer.sections[2].color = Color.GREEN
        speedometer.sections[2].startOffset = (1f/1.2f) * 0.2f

        speedTestSocket.downloadSetupTime = 1000
        speedTestSocket.uploadSetupTime = 1000
        speedTestSocket.socketTimeout = 5000

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        speedometer.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fillNetworkTextView(requireView())
            }
            if (speedTestRunningStep==0) {
                tvDownload.text="-"
                tvUpload.text="-"
                tvLatency.text ="-"

                // Here we choose the file to dowload fron Object Constants
                speedTestSocket.startDownloadRepeat(downLoadFile,
                    5000, 1000, object : IRepeatListener {
                        override fun onCompletion(report: SpeedTestReport) {

                            internetSpeedDownload = report.transferRateBit.toFloat()/1000000.0f
                            listDownload = "%.1f".format(internetSpeedDownload)
                                requireActivity().runOnUiThread(Runnable {
                                speedometer.speedTo(0.0f,1000)
                                speedTestRunningStep = 1
                                Log.d("cfauli","[COMPLETED] step" + speedTestRunningStep)
                                tvDownload.text = listDownload
                                Log.d("cfauli up file size", ((((10).toDouble().pow(speedTestFile).toInt()))*1000000).toString())
                                // Start upload test (once the download test finishes)---------------------------------------------------------
                                speedTestSocket.startUploadRepeat(upLoadFile,
                                    5000, 1000, ((10).toDouble().pow(speedTestFile).toInt())*1000000, object : IRepeatListener {

                                        override fun onCompletion(report: SpeedTestReport) {
                                            Log.d("cfauli up file size", ((((10).toDouble().pow(speedTestFile).toInt()))*1000000).toString())
                                            internetSpeedUpload= report.transferRateBit.toFloat()/1000000.0f
                                            listUpload = "%.1f".format(internetSpeedUpload)
                                            requireActivity().runOnUiThread(Runnable {
                                                speedometer.speedTo(0.0f,1000)
                                                speedTestRunningStep = 2
                                                tvUpload.text = listUpload


                                                // Test latency (once the upload test finishes)-----------------
                                                 pingg("http://www.google.com") { it ->
                                                     listLatency = it.toString()
                                                     tvLatency.text = listLatency
                                                     // Fill the Speed List fragment view
                                                     // Create the adapter to convert the array to views
                                                     adapter.clear()
                                                     listOfSpeedTest =fillSpeedList(true, listNetwork, listDownload, listUpload, listLatency)
                                                     adapter.addAll(listOfSpeedTest)
                                                     adapter.notifyDataSetChanged()

                                                 }
                                                speedTestRunningStep = 0
                                                //fab.setBackgroundResource(R.drawable.ic_switch_on_off)
                                                // End test latency  --------------------------------------------
                                            })
                                        }
                                        override fun onReport(report: SpeedTestReport) {
                                            // called when a upload report is dispatched
                                            internetSpeedUpload= report.transferRateBit.toFloat()/1000000.0f
                                            requireActivity().runOnUiThread(Runnable {
                                                speedometer.speedTo(internetSpeedUpload,1000)
                                            })
                                        }
                                    })
                                // End upload test---------------------------------------------------------------------------------------------
                            })
                            //
                        }

                        override fun onReport(report: SpeedTestReport) {
                            // called when a download report is dispatched
                            internetSpeedDownload = report.transferRateBit.toFloat()/1000000.0f
                            requireActivity().runOnUiThread(Runnable {
                                speedometer.speedTo(internetSpeedDownload,1000)
                            })
                        }
                    })
                //fab.setBackgroundResource(R.drawable.ic_switch_on_off_grey)
            }
        }
        //fillNetworkTextView(view)
        return fragmentView
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d ("cfauli", "OnAttach TAB1")
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
        Log.d ("cfauli", "OnDetach TAB1")
        listener = null
    }

    override fun onResume() {
        super.onResume()
        Log.d("cfauli","OnResume tab1")

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
        var timeofping: Array<Long> = arrayOf(0,0,0,0)
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun fillNetworkTextView(view: View) {
        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
        Log.d("cfauli", "fillMobileTextView")
        val tvNetwork = view.findViewById<View>(R.id.tvTab1MobileNetworkType) as TextView
        var pDevice: DevicePhone = DevicePhone()

        if (Connectivity.isConnectedMobile(requireContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pDevice = Connectivity.getpDevice(requireContext())
                listNetwork = pDevice.type + " " + "%.1f".format(calculateFreq(pDevice.type, pDevice.band)) + "MHz " + pDevice.dbm + "dBm id: " + pDevice.mcc + "-" + pDevice.mnc + "-" + pDevice.lac + "-" + pDevice.cid
                tvNetwork.text = listNetwork
            } else {
                TODO("VERSION.SDK_INT < P")
            }
        } else if (Connectivity.isConnectedWifi(requireContext()))  {
            val deviceWifi = Connectivity.getWifiParam(requireContext())
            val freq = deviceWifi.centerFreq2
            val channel: Int
            channel = if (freq!! > 5000) {
                (freq - 5180) / 5 + 36
            } else {
                (freq - 2412) / 5 + 1
            }
            tvNetwork.text = "WIFI " + deviceWifi.ssid + " ch: " + channel + " " + deviceWifi.centerFreq2 + "MHz " + deviceWifi.level + "dBm"


        }
    }

    fun fillSpeedList(new: Boolean, network:String, download:String, upload:String, ping:String): List<SpeedTest> {


        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        var numSpeedTest = sharedPreferences.getInt("num_speed_test",0)
        var textDate:String
        var textNetwork:String
        var textSpeed:String


        if (new) {
            val calendar = Calendar.getInstance()
            val day =
                calendar[Calendar.DAY_OF_MONTH].toString() + "/" + calendar[Calendar.MONTH] + "/" + calendar[Calendar.YEAR] + " " + calendar[Calendar.HOUR_OF_DAY] + ":" + calendar[Calendar.MINUTE]

            Log.d("cfauli", "numspeedtest " + numSpeedTest)
            numSpeedTest += 1

            editor.putInt("num_speed_test", numSpeedTest)
            editor.apply()

            var textDate = "speed_test_date" + numSpeedTest
            var textNetwork = "speed_test_network" + numSpeedTest
            var textSpeed = "speed_test_speed" + numSpeedTest

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
                listOfSpeedTest.add (SpeedTest(sharedPreferences.getString(textDate, ""),sharedPreferences.getString(textNetwork, ""), sharedPreferences.getString(textSpeed, "")))
            }
        }


        return listOfSpeedTest.sortedByDescending { it.date }

    }

}

