package lestelabs.antenna.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
    private var mParam1: String? = null
    private var mParam2: String? = null
    //private var mListener: Tab2.OnFragmentInteractionListener? = null
    lateinit var mAdView : AdView
    private var firstOnResume = true
    private var minTime:Long = 1000

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


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("cfauli", "OnCreate Tab2")
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong() * 1000

    }

    override fun onStart() {
        // call the superclass method first
        super.onStart()
        Log.d("cfauli","OnStart Tab2")
    }
    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli","OnStop Tab2")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val size = 0
        var results: List<ScanResult?>
        val arrayList: ArrayList<String> = ArrayList()
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        //val listView: ListView = view.findViewById<View>((R.id.wifiList)) as ListView
        wifiManager = requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiScanReceiver: BroadcastReceiver

        fragmentView= inflater.inflate(R.layout.fragment_tab2, container, false)
        Log.d("cfauli", "OnCreateView Tab2")
        // Adds -------------------------------------------------------------------------------------
        mAdView = fragmentView.findViewById(R.id.adViewFragment2)
        MobileAds.initialize(requireActivity())
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                // Code to be executed when an ad request fails.
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
        // fill the mobile list every mintime secs

        minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong() * 1000
        startMobileScanner(fragmentView)

        /// fill the wifi list --------------------------------------------
        // Construct the data source
        val arrayOfWifis: ArrayList<DeviceWiFi> = ArrayList<DeviceWiFi>()
        // Create the adapter to convert the array to views
        var adapter = WifiAdapter(activity, arrayOfWifis)
        // Attach the adapter to a ListView
        val listView = fragmentView.findViewById(R.id.wifiList) as ListView
        listView.adapter = adapter

        scanWifi.scanwifi(requireActivity(), wifiManager) {
            adapter.clear()

            val showedWiFiList = scanWifi.devices.sortedByDescending { it.level }
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
        Log.d ("cfauli", "OnDetach TAB2")
    }


    override fun onResume() {
        super.onResume()
        Log.d("cfauli","OnResume Tab2 " + firstOnResume)
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minTime  = sharedPreferences.getInt("num_time_samples",10).toLong() * 1000

        // firstOnResume = true if activity is destroyed (back) and goes trough a oncreateview, in order not to repeat the scanners
        if (!firstOnResume) startMobileScanner(requireView())
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

    fun startMobileScanner(view:View) {
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

    fun fillMobileTextView(view: View){
        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
        Log.d ("cfauli", "fillMobileTextView")
        val tvLevel = view.findViewById<View>(R.id.tv_mobile_level) as TextView
        val tvOperator = view.findViewById<View>(R.id.tv_mobile_operator) as TextView
        val tvLac = view.findViewById<View>(R.id.tv_mobile_lac) as TextView
        val tvId = view.findViewById<View>(R.id.tv_mobile_id) as TextView
        val tvNetwork = view.findViewById<View>(R.id.tv_mobile_network) as TextView
        val tvNetworkType = view.findViewById<View>(R.id.tv_mobile_networkType) as TextView
        val tvLat = view.findViewById<View>(R.id.tv_mobile_lat) as TextView
        val tvLon = view.findViewById<View>(R.id.tv_mobile_lon) as TextView
        val ivIconLevel = view.findViewById<View>(R.id.iv_mobile_signalIcon) as ImageView

        val tvMobile = view.findViewById<View>(R.id.tv_mobile_txt) as TextView

        var pDevice: DevicePhone = DevicePhone()

        if (Connectivity.isConnectedMobile(requireContext())) {
            pDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Connectivity.getpDevice(requireContext())

            } else {
                TODO("VERSION.SDK_INT < P")
            }
        }

        tvMobile.text = getString(R.string.MobileTxt)
        tvLevel.text = pDevice.dbm.toString() + "dBm"
        if (pDevice.mcc==0) {
            tvOperator.text = getString(R.string.MobileDetected)
        } else tvOperator.text = getString(R.string.Operator) + pDevice.mcc.toString() + "-" + pDevice.mnc.toString()
        tvLac.text = "lac: " + pDevice.lac.toString()
        tvId.text = "id: " + pDevice.cid.toString()
        tvNetwork.text = getString(R.string.Network)
        tvNetworkType.text = pDevice.type + " " + "%.1f".format(calculateFreq(pDevice.type,pDevice.band)) + " MHz"

        if (totalCidAnt != pDevice.totalCellId) {
            val openCellIdInterface = retrofitFactory()
            findTower(openCellIdInterface, pDevice)
            { coordenadas ->
                if (coordenadas.lat!! >0.0) {
                    tvLat.text = getString(R.string.Tower) + "lat: " + "%.4f".format(coordenadas.lat)
                    tvLon.text = "lon: " + "%.4f".format(coordenadas.lon)
                } else {
                    tvLat.text = getString(R.string.TowerCoordinatesNotFound)
                    tvLon.text = ""
                }
            }
            totalCidAnt = pDevice.totalCellId
        }
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        thresMobBlack = sharedPreferences.getString("thres_mob_black",Constants.MINMOBILESIGNALBLACK)!!.toInt()
        thresMobRed = sharedPreferences.getString("thres_mob_red",Constants.MINMOBILESIGNALRED)!!.toInt()
        thresMobYellow = sharedPreferences.getString("thres_mob_yellow",Constants.MINMOBILESIGNALYELLOW)!!.toInt()
        thresMobGreen = Constants.MINMOBILESIGNALGREEN.toInt()

        if (pDevice.dbm!! >= (-1*thresMobYellow)) {
            ivIconLevel.setImageResource(R.drawable.ic_network_green)
        } else if (pDevice.dbm!! >= (-1*thresMobRed)) {
            ivIconLevel.setImageResource(R.drawable.ic_network_yellow)
        } else if (pDevice.dbm!! >= (-1*thresMobBlack)) {
            ivIconLevel.setImageResource(R.drawable.ic_network_red)
        } else {
            ivIconLevel.setImageResource(R.drawable.ic_network_black)
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


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab2.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): Tab2 {
            val fragment = Tab2()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


}

