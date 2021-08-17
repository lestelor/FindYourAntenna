package lestelabs.antenna.ui.main


import android.app.Activity
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.*


/*
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab2.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab2 : Fragment() {
    // TODO: Rename and change types of parameters
    //private var mListener: Tab2.OnFragmentInteractionListener? = null
    lateinit var mAdView : AdView
    private var firstOnResume = true
    private var minTime:Long = 10000

    private var thresMobBlack: Int = Constants.MINMOBILESIGNALBLACK.toInt()
    private var thresMobRed: Int = Constants.MINMOBILESIGNALRED.toInt()
    private var thresMobYellow: Int = Constants.MINMOBILESIGNALYELLOW.toInt()
    private var thresMobGreen: Int = Constants.MINMOBILESIGNALGREEN.toInt()


    var mHandler: Handler = Handler()
    var clockTimerHanlerActive = false
    private lateinit var mHandlerTask: Runnable
    private var totalCidAnt = ""
    private var listener: GetfileState? = null
    private lateinit var fragmentView: View
    private lateinit var wifiManager: WifiManager

    private lateinit var telephonyManager:TelephonyManager
    private lateinit var connectivity: Connectivity

    private val tabName = "Tab2"
    private var crashlyticsKeyAnt = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("cfauli", "OnCreate Tab2")

        //set a fixed mintime in order no refresh the mobile ifo every xx sec - set in the constant section
        //val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        //minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong() * 1000

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
    }

    override fun onStart() {
        // call the superclass method first
        super.onStart()

        Log.d("cfauli", "OnStart Tab2")
    }
    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli", "OnStop Tab2")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        val size = 0
        var results: List<ScanResult?>
        val arrayList: ArrayList<String> = ArrayList()
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        //val listView: ListView = view.findViewById<View>((R.id.wifiList)) as ListView
        wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager

        fragmentView= inflater.inflate(R.layout.fragment_tab2, container, false)

        connectivity = Connectivity(fragmentView.context)
        Log.d("cfauli", "OnCreateView Tab2")
        // Adds -------------------------------------------------------------------------------------
        mAdView = fragmentView.findViewById(R.id.adViewFragment2)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        // fill the mobile list every mintime secs
        //minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong() * 1000
        startMobileScannerTab2(fragmentView)

        /// fill the wifi list --------------------------------------------
        // Construct the data source
        val arrayOfWifis: ArrayList<DeviceWiFi> = ArrayList<DeviceWiFi>()
        // Create the adapter to convert the array to views
        var adapter = WifiAdapter(activity, arrayOfWifis)
        // Attach the adapter to a ListView
        val listView = fragmentView.findViewById(R.id.wifiList) as ListView
        listView.adapter = adapter


        val tvWifi = fragmentView.findViewById<View>(R.id.tv_wifi) as TextView
        tvWifi.text = getString(R.string.tvWIFIwithoutSSID)

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)



        scanWifi.scanwifi(activity, wifiManager) {

            // Control point for Crashlitycs
            crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

            tvWifi.text = "WIFI"
            adapter.clear()

            val showedWiFiList: MutableList<DeviceWiFi> = scanWifi.devices.sortedByDescending { it.level }.toMutableList()
            showedWiFiList.removeIf { device:DeviceWiFi -> device.ssid?.length == 0 }
            Log.d("cfauli size", showedWiFiList.size.toString())
            /*for (i in showedWiFiList.indices) {
                Log.d("cfauli item", showedWiFiList[i].ssid + showedWiFiList[i].level)
                deviceWiFi.ssid = showedWiFiList[i].ssid.toString()
                deviceWiFi.level = showedWiFiList[i].level
                adapter.add(deviceWiFi)
                adapter.notifyDataSetChanged()
            }*/
            adapter.addAll(showedWiFiList)
            adapter.notifyDataSetChanged()

        }



        return fragmentView
    }





    override fun onPause() {
        super.onPause()
        Log.d("cfauli", "OnPause TAB2")
        mHandler.removeCallbacks(mHandlerTask)
        wifiManager.disconnect()
        clockTimerHanlerActive = false
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("cfauli", "OnAttach Tab2")
        /*mListener = if (context is Tab2.OnFragmentInteractionListener) {
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
        listener = null
        Log.d("cfauli", "OnDetach TAB2")
    }


    override fun onResume() {
        super.onResume()
        Log.d("cfauli", "OnResume Tab2 " + firstOnResume)
        //val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        //minTime  = sharedPreferences.getInt("num_time_samples",10).toLong() * 1000

        // firstOnResume = true if activity is destroyed (back) and goes trough a oncreateview, in order not to repeat the scanners
        if (!firstOnResume) startMobileScannerTab2(requireView())
        firstOnResume = false
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

    fun startMobileScannerTab2(view: View) {
        if (!clockTimerHanlerActive) {
            mHandlerTask = object : Runnable {
                override fun run() {
                    fillMobileTextView(view)
                    mHandler.postDelayed(this, minTime)
                }
            }
        }
        Log.d("cfauli", "startMobileScanner")
        mHandlerTask.run()
        clockTimerHanlerActive = true
    }


    fun fillMobileTextView(view: View) {
    try {
        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
        Log.d("cfauli", "fillMobileTextView")
        val tvLevel = view.findViewById<View>(R.id.tv_mobile_level) as TextView
        val tvOperator = view.findViewById<View>(R.id.tv_mobile_operator) as TextView
        val tvOperatorValue = view.findViewById<View>(R.id.tv_mobile_operator_values) as TextView
        val tvOperatorName = view.findViewById<View>(R.id.tv_mobile_operator_name) as TextView
        val tvNetwork = view.findViewById<View>(R.id.tv_mobile_network) as TextView
        val tvNetworkType = view.findViewById<View>(R.id.tv_mobile_networkType) as TextView
        val tvFreq = view.findViewById<View>(R.id.tv_mobile_freq) as TextView
        val tvFreqNum = view.findViewById<View>(R.id.tv_mobile_freq_num) as TextView
        val ivIconLevel = view.findViewById<View>(R.id.iv_mobile_signalIcon) as ImageView
        val tvMobile = view.findViewById<View>(R.id.tv_mobile_txt) as TextView
        val tvIccId = view.findViewById<View>(R.id.tv_iccid) as TextView


        val pDevice = loadCellInfo(telephonyManager)

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        findOperatorName(pDevice) { it ->
            tvOperatorName.text = it
        }

        tvMobile.text = getString(R.string.MobileTxt)
        tvLevel.text = pDevice?.dbm.toString() + "dBm"
        if (pDevice?.mcc == 0) {
            tvOperator.text = ""
            tvOperatorValue.text = ""
        } else {
            tvOperator.text = getString(R.string.Operator)
            tvOperatorValue.text = pDevice?.mcc.toString() + "-" + pDevice?.mnc.toString() + "-" + pDevice?.cid.toString()
        }

        tvNetwork.text = getString(R.string.Network)
        tvNetworkType.text = pDevice?.type
        tvFreq.text = getString(R.string.Frequency)
        tvFreqNum.text = "%.1f".format(calculateFreq(pDevice?.type, pDevice?.band)) + " MHz"

        tvIccId.text = connectivity.getIccId()

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        /*if (totalCidAnt != pDevice?.totalCellId) {
            val openCellIdInterface = retrofitFactory()
            findTower(openCellIdInterface, pDevice)
            { coordenadas ->
                if (coordenadas?.lat != null && coordenadas.lat != -1000.0) {
                    tvLat.text = "lat: " + "%.5f".format(coordenadas.lat)
                    tvLon.text = "lon: " + "%.5f".format(coordenadas.lon)
                } else {
                    tvLat.text = getString(R.string.TowerCoordinatesNotFound)
                    tvLon.text = ""
                }
            }
            totalCidAnt = pDevice?.totalCellId.toString()
        }*/
        val sharedPreferences = activity?.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)

        thresMobBlack = sharedPreferences?.getString("thres_mob_black", Constants.MINMOBILESIGNALBLACK)?.toInt() ?: Constants.MINMOBILESIGNALBLACK.toInt()
        thresMobRed = sharedPreferences?.getString("thres_mob_red", Constants.MINMOBILESIGNALRED)?.toInt() ?: Constants.MINMOBILESIGNALRED.toInt()
        thresMobYellow = sharedPreferences?.getString("thres_mob_yellow", Constants.MINMOBILESIGNALYELLOW)?.toInt() ?: Constants.MINMOBILESIGNALYELLOW.toInt()
        thresMobGreen = Constants.MINMOBILESIGNALGREEN.toInt()

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        pDevice?.let {
            if (it.dbm!! >= (-1 * thresMobYellow)) {
                ivIconLevel.setImageResource(R.drawable.ic_network_green)
            } else if (pDevice?.dbm!! >= (-1 * thresMobRed)) {
                ivIconLevel.setImageResource(R.drawable.ic_network_yellow)
            } else if (pDevice?.dbm!! >= (-1 * thresMobBlack)) {
                ivIconLevel.setImageResource(R.drawable.ic_network_red)
            } else {
                ivIconLevel.setImageResource(R.drawable.ic_network_black)
            }
        }
    } catch (e:Exception) {

    }

    }

    /*interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }*/

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }

}

