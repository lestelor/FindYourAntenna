package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.rest.Coordenadas
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.loadCellInfo
import android.location.LocationListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab2.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab2.newInstance] factory method to
 * create an instance of this fragment.
 */


class Tab2 : Fragment() , OnMapReadyCallback {



    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var location:Location? = null

    // As indicated in android developers https://developers.google.com/maps/documentation/android-sdk/start
    // Previously it is necessary to get the google API key from the Google Cloud Platform Console
    // (Maps SDK for Android) and store them in the manifest
    // The app build gradle is sync with the maps library

    private lateinit var mMap: GoogleMap
    private lateinit var fragmentView: View
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var telephonyManager:TelephonyManager
    private lateinit var pDevice:DevicePhone
    private val openCellIdInterface = retrofitFactory()
    private var listTowersFound:MutableList<DevicePhone> = mutableListOf(DevicePhone())
    private var locationAnt: Location? = null
    private lateinit var locationManager: LocationManager
    private var towerinListBool: Boolean = false
    private var requestingLocationUpdates = true

    //private lateinit var locationCallback: LocationCallback




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }



        //val mgr = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

        /*locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    Log.d("cfauli", "locationupdaterequest")
                    // ...
                }
            }
        }*/


    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_tab2, container, false)
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment!!.getMapAsync(this)
        /*val fab: FloatingActionButton = fragmentView.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/

        return fragmentView
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

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Thread.sleep(1000)
        //MapsInitializer.initialize(context)
        Log.d("cfauli", "OnmapReady")
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isMyLocationEnabled = true

        val myLocListener:MyLocationListener = MyLocationListener()
        val minDist = 0.1f
        val minTime:Long  = 1000


        locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime,minDist,myLocListener)
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDist,myLocListener)
        }

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }




    private fun locateTowerMap(location: Location, locationTower:Coordenadas) {

        val boundsMapTowerGpsMin:Coordenadas = Coordenadas()
        val boundsMapTowerGpsMax:Coordenadas = Coordenadas()

        /*if ((locationTower.lat!! < 999) || (locationTower.lat!! == 0.0) || (locationTower.lat == null)) {
            locationTower.lat = location.latitude
            locationTower.lon = location.longitude
        }*/

        val distance = distance(locationTower.lat!!,locationTower.lon!!,location.latitude,location.longitude)

        boundsMapTowerGpsMin.lat =
            minOf(location.latitude, locationTower.lat!!) - 0.001 * distance/11119
        boundsMapTowerGpsMin.lon =
            minOf(location.longitude, locationTower.lon!!) - 0.001 * distance/839
        boundsMapTowerGpsMax.lat =
            maxOf(location.latitude, locationTower.lat!!) - 0.001 * distance/11119
        boundsMapTowerGpsMax.lon =
            maxOf(location.longitude, locationTower.lon!!) + 0.001 * distance/839


        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(locationTower.lat!!, locationTower.lon!!))
                .title(
                    "%.4f".format(locationTower.lat) + "; " + "%.4f".format(locationTower.lon) + " " + "dist: " + "%.0f".format(
                        distance
                    ) + " m"
                )
        )
        Log.d("cfauli","locateTowerMap markerok")
        val mapBounds = LatLngBounds(
            LatLng(boundsMapTowerGpsMin.lat!!, boundsMapTowerGpsMin.lon!!),
            LatLng(boundsMapTowerGpsMax.lat!!, boundsMapTowerGpsMax.lon!!)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0))

    }

    private fun ckeckTowerinList(devicePhone: DevicePhone):Boolean {
        var devicePhonecheck: DevicePhone? = listTowersFound.find { it.totalCellId == devicePhone.totalCellId }
        if (devicePhonecheck != null) {
            Log.d ("cfauli", "ckeckTowrList found " + listTowersFound[0].totalCellId!! + listTowersFound[1]!!.totalCellId)
            return true
        } else {
            Log.d ("cfauli", "ckeckTowrList NOT found " + listTowersFound[0].totalCellId!! )
            listTowersFound.add(devicePhone)
            return false
        }
    }

    private fun distance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians(lat2 - lat1.toDouble())
        val dLng = Math.toRadians(lng2 - lng1.toDouble())
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.toDouble())) * Math.cos(
            Math.toRadians(lat2.toDouble())
        ) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c =
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c)
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


    inner class MyLocationListener : LocationListener {


        @RequiresApi(Build.VERSION_CODES.P)
        override fun onLocationChanged(location: Location) {
            telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            pDevice = loadCellInfo(telephonyManager)
            towerinListBool = ckeckTowerinList(pDevice)
            Log.d("cfauli", "TowerinList " + towerinListBool)
            Log.d("cfauli", "pDevice " + pDevice.networkType + " " + pDevice.mcc + " " + pDevice.mnc + " " + pDevice.cid + " " + pDevice.lac)

            if (!towerinListBool){
                findTower(openCellIdInterface, pDevice)
                { coordenadas ->
                    locateTowerMap(location, coordenadas)
                }
            }

            // You can now create a LatLng Object for use with maps
            //val latLng = LatLng(location.latitude, location.longitude)
            //return latLng
        }

        override fun onProviderDisabled(arg0: String?) {
            // Do something here if you would like to know when the provider is disabled by the user
        }

        override fun onProviderEnabled(arg0: String?) {
            // Do something here if you would like to know when the provider is enabled by the user
        }

        override fun onStatusChanged(arg0: String?, arg1: Int, arg2: Bundle?) {
            // Do something here if you would like to know when the provider status changes
        }
    }
}



interface FetchCompleteListener {

}
