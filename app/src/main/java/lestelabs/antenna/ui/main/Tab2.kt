package lestelabs.antenna.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab1.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    lateinit var mAdView : AdView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val size = 0
        var results: List<ScanResult?>
        val arrayList: ArrayList<String> = ArrayList()

        //val listView: ListView = view.findViewById<View>((R.id.wifiList)) as ListView
        val wifiManager = requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiScanReceiver: BroadcastReceiver

        val view: View = inflater.inflate(R.layout.fragment_tab2, container, false)

        // Adds -------------------------------------------------------------------------------------
        mAdView = view.findViewById(R.id.adViewFragment2)
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

        /// fill the mobile layout --------------------------------------------
        // Lookup view for data population
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
        tvOperator.text = getString(R.string.Operator) + pDevice.mcc.toString() + "-" + pDevice.mnc.toString()
        tvLac.text = "lac: " + pDevice.lac.toString()
        tvId.text = "id: " + pDevice.cid.toString()
        tvNetwork.text = getString(R.string.Network)
        tvNetworkType.text = pDevice.type

        val openCellIdInterface = retrofitFactory()
        findTower(openCellIdInterface, pDevice)
        { coordenadas ->
            tvLat.text = "lat: " + "%.4f".format(coordenadas.lat)
            tvLon.text = "lon: " + "%.4f".format(coordenadas.lon)
        }



        if (pDevice.dbm!! >= -100) {
            Log.d("cfauli", "GREEN " + pDevice.dbm + "dBm")
            ivIconLevel.setImageResource(R.drawable.ic_network_green)
        } else if (pDevice.dbm!! >= -115) {
            Log.d("cfauli", "YELLOW " + pDevice.dbm + "dBm")
            ivIconLevel.setImageResource(R.drawable.ic_network_yellow)
        } else {
            Log.d("cfauli", "RED " + pDevice.dbm + "dBm")
            ivIconLevel.setImageResource(R.drawable.ic_network_red)
        }


        /// fill the wifi list --------------------------------------------
        // Construct the data source
        val arrayOfWifis: ArrayList<DeviceWiFi> = ArrayList<DeviceWiFi>()
        // Create the adapter to convert the array to views
        var adapter = WifiAdapter(activity, arrayOfWifis)
        // Attach the adapter to a ListView
        val listView = view.findViewById(R.id.wifiList) as ListView
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



        return view
    }








    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
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
        fun newInstance(param1: String?, param2: String?): Tab2 {
            val fragment = Tab2()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setNetworkType(context:Context):List<Any?> {
        var type:String = ""
        if (Connectivity.isConnectedWifi(context)){
            type = Connectivity.getSsid(context).toString()
        } else if (Connectivity.isConnected(context)) {
            type = listOf(Connectivity.connectionType(Connectivity.networkType(context), Connectivity.networkSubtype(context)).toString()).toString()
        } else type =""
        return listOf(type)
    }

}

