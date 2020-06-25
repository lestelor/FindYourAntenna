package lestelabs.antenna.ui.main


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.rest.Coordenadas
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.loadCellInfo


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab2.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab2.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab2 : Fragment() , OnMapReadyCallback {
    // TODO: Rename and change types of parameters
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
    private var locationAnt:Location? = null
    private val mLocationManager: LocationManager? = null
    private var towerinListBool: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }

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

        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isMyLocationEnabled = true


            /*pDevice.mcc=214
        pDevice.mnc=3
        pDevice.lac=2426
        pDevice.cid = 12834060*/
            /*pDevice.mcc=214
        pDevice.mnc= 3
        pDevice.lac=2320
        pDevice.cid = 12924929
        pDevice.mcc=214
        pDevice.mnc= 3
        pDevice.lac= 137
        //pDevice.lac= 2426
        pDevice.cid = 12834060*/


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation

            .addOnSuccessListener { location: Location? ->
                val centroMapa = location?.let { onLocationChanged(it) }
                /*if (location == null) {
                } else if ((!towerinListBool)) {
                    findTower(openCellIdInterface, pDevice)
                    { coordenadas ->
                        locateTowerMap(location, coordenadas)
                    }
                }*/
            }

            .addOnFailureListener { e ->
                Log.d("MapDemoActivity", "Error trying to get last GPS location")
                e.printStackTrace()
            }

        mMap.clear()



    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun onLocationChanged(location: Location) {
        // New location has now been determined
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pDevice = loadCellInfo(telephonyManager)
        towerinListBool = ckeckTowerinList(pDevice)
        Log.d("cfauli", "TowerinList " + towerinListBool)
        Log.d("cfauli", "pDevice " + pDevice.networkType + " " + pDevice.mcc + " " + pDevice.mnc + " " + pDevice.cid + " " + pDevice.lac)

        if (checkDistaceLocations(location,10.0f) && (!towerinListBool)){
            findTower(openCellIdInterface, pDevice)
             { coordenadas ->
                locateTowerMap(location, coordenadas)
            }
        }

        // You can now create a LatLng Object for use with maps
        //val latLng = LatLng(location.latitude, location.longitude)
        //return latLng
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
    private fun checkDistaceLocations(location: Location, distanceLocations:Float):Boolean {
        val distanceLocs: Float
        if (locationAnt != null) {
            distanceLocs = location.distanceTo(locationAnt)
        } else {
            locationAnt = location
            distanceLocs = 10000.0f
        }
        Log.d("cfauli", "checkDistaceLocations distance " + distanceLocs)
        if (distanceLocs > distanceLocations) {
            locationAnt = location
            return true
        } else {
            return false
        }

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

}


interface FetchCompleteListener {

}
