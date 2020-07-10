package lestelabs.antenna.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.scanner.WiFiScanner


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab1.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab3 : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val size = 0
        var results: List<ScanResult?>
        val arrayList: ArrayList<String> = ArrayList()
        var adapter: ArrayAdapter<*>
        val view: View = inflater.inflate(R.layout.fragment_tab3, container, false)
        val listView: ListView = view.findViewById<View>((R.id.wifiList)) as ListView
        val wifiManager = requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiScanReceiver: BroadcastReceiver

        // Adds -------------------------------------------------------------------------------------
        mAdView = view.findViewById(R.id.adViewFragment3)
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


        // Wifimanager --------------------------------------------------------------------------------------

/*

        var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                WiFiScanner.results = wifiManager!!.scanResults
                WiFiScanner.unregisterReceiver(this)
                for (scanResult in (WiFiScanner.results as MutableList<ScanResult>?)!!) {
                    WiFiScanner.arrayList.add(scanResult.SSID + " - " + scanResult.capabilities)
                    WiFiScanner.adapter!!.notifyDataSetChanged()
                }
            }
        }


        WiFiScanner.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager!!.startScan()
        Toast.makeText(requireActivity(), "Scanning WiFi ...", Toast.LENGTH_SHORT).show()


*/
        /// wifi adapter 2 --------------------------------------------
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(requireActivity(), "WiFi is disabled, please enable it", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }

        adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, arrayList)
        listView.adapter = adapter
        arrayList.clear()

        // Registering Wifi Receiver
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                results = wifiManager!!.scanResults
                //unregisterReceiver(this)
                for (scanResult in (results as MutableList<ScanResult>?)!!) {
                    arrayList.add(scanResult.SSID + " - " + scanResult.capabilities)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)
        wifiManager.startScan()

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
        fun newInstance(param1: String?, param2: String?): Tab3 {
            val fragment = Tab3()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}

