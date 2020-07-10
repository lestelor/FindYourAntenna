package lestelabs.antenna.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.scanner.DeviceWiFi
import lestelabs.antenna.ui.main.scanner.WifiAdapter
import lestelabs.antenna.ui.main.scanner.scanWifi


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
        //var adapter: ArrayAdapter<*>
        val view: View = inflater.inflate(R.layout.fragment_tab3, container, false)
        //val listView: ListView = view.findViewById<View>((R.id.wifiList)) as ListView
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


        /// fill the list --------------------------------------------
        // Construct the data source

        // Construct the data source
        val arrayOfWifis: ArrayList<DeviceWiFi> = ArrayList<DeviceWiFi>()
        // Create the adapter to convert the array to views
        var adapter = WifiAdapter(activity, arrayOfWifis)
        // Attach the adapter to a ListView
        val listView = view.findViewById(R.id.wifiList) as ListView
        listView.adapter = adapter




        //adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, arrayList)
        listView.adapter = adapter
        var deviceWiFi:DeviceWiFi = DeviceWiFi()

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

