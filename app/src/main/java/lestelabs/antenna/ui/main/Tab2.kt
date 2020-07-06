package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.rest.Coordenadas
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.loadCellInfo
import java.io.File
import java.time.LocalDateTime
import java.util.jar.Manifest


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
    private lateinit var mAdView : AdView
    private var gpsActive = false
    private var firstOnResume = true

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
    private var towerinListInt: Int = -1
    private var requestingLocationUpdates = true
    private var minDist: Float? = null
    private var minTime:Long? =null
    private var okSaveTowers:Int? = null
    private var okSaveSamples: Int? = null
    private var previousTower: String? = null
    private var listener: GetfileState? = null
    private var isFileOpened: Boolean = false
    private var sampleFilePath: File? = null
    var myLocListener:MyLocationListener = MyLocationListener()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
        Log.d("cfauli","OnCreate")
        readInitialConfiguration()
    }



    // Called at the end of the active lifetime.
    override fun onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause()
        Log.d("cfauli","OnPause")
        endGPS()
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
        Log.d("cfauli","OnCreateView")
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
        try {
           listener = activity as GetfileState
            // listener.showFormula(show?);
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
        Log.d("cfauli","OnAtach")
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        Log.d("cfauli","OnDetach")
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


    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minDist = sharedPreferences.getInt("num_dist_samples",getString(R.string.minDistSample).toInt()).toFloat()
        minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong() * 1000
        Log.d("cfauli","OnResume")
        if (!firstOnResume) startGPS()
        firstOnResume = false
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Thread.sleep(1000)
        //MapsInitializer.initialize(context)
        Log.d("cfauli", "OnmapReady")
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isMyLocationEnabled = true

        //val myLocListener:MyLocationListener = MyLocationListener()

        startGPS()
        /*locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime!!,minDist!!,myLocListener)
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime!!,minDist!!,myLocListener)
        }*/

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //val view: View = inflater.inflate(R.layout.fragment_tab2, container, false)



        mAdView = view?.findViewById(R.id.adViewFragment2)!!
        val adView = AdView(requireActivity())
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

        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(locationTower.lat!!, locationTower.lon!!))
                .title("%.4f".format(locationTower.lat) + "; " + "%.4f".format(locationTower.lon))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        if (listTowersFound.size > 1) {
            for (i in 1..listTowersFound.size) {
                if (i != towerinListInt) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(listTowersFound[i].lat, listTowersFound[i].lon))
                            .title("%.4f".format(listTowersFound[i].lat) + "; " + "%.4f".format(listTowersFound[i].lon))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }
            }
        }

        Log.d("cfauli","locateTowerMap markerok")
        val mapBounds = LatLngBounds(
            LatLng(boundsMapTowerGpsMin.lat!!, boundsMapTowerGpsMin.lon!!),
            LatLng(boundsMapTowerGpsMax.lat!!, boundsMapTowerGpsMax.lon!!)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0))

    }

    private fun ckeckTowerinList(devicePhone: DevicePhone):Int {
        //var devicePhonecheck: DevicePhone? = listTowersFound.find { it.totalCellId == devicePhone.totalCellId }
        var indexOfTower = listTowersFound.indexOfFirst { it.totalCellId == devicePhone.totalCellId }
        if (indexOfTower != -1) {
            listTowersFound.add(devicePhone)
        }
        return indexOfTower
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

            pDevice = loadCellInfo(telephonyManager)
            towerinListInt = ckeckTowerinList(pDevice)
            Log.d("cfauli", "previoustower " + previousTower)
            Log.d("cfauli", "currentTower " + pDevice.totalCellId)
            var sameTowerBool = false
            if (pDevice.totalCellId == previousTower) {
                sameTowerBool = true
            } else {
                previousTower = pDevice.totalCellId
                sameTowerBool = false
            }

            Log.d("cfauli", "TowerinList " + towerinListInt.toString())

            if (!sameTowerBool){
                findTower(openCellIdInterface, pDevice)
                { coordenadas ->
                    pDevice.lat = coordenadas.lat!!
                    pDevice.lon = coordenadas.lon!!
                    locateTowerMap(location, coordenadas)

                }
            }
            // Save samples in file
            if (listener?.getFileState()!! && okSaveSamples == 1)  {
                if (!isFileOpened) {

                    //create file
                    val sampleFile = "samples_" + LocalDateTime.now() + ".txt"
                    val storageDir=getStorageDir()

                    Log.d("cfauli", "File first created" + LocalDateTime.now())
                    val storageDirFile = File(storageDir)
                    if (!storageDirFile.exists()) {
                        storageDirFile.mkdirs()
                    }
                    sampleFilePath = File(storageDirFile, sampleFile)
                    File(sampleFilePath.toString()).writeText("time;operator;band;mcc;mnc;cid;lat;lon")
                    File(sampleFilePath.toString()).appendText("\n" + LocalDateTime.now() +";" + pDevice.operator.toString() + ";" + pDevice.band.toString() + ";" + pDevice.mcc.toString() + ";" + pDevice.mnc.toString() + ";" + pDevice.cid.toString() + ";" + "%.4f".format(location.latitude) +  ";" + "%.4f".format(location.longitude)  + ";" +  pDevice.dbm)
                    Log.d("cfauli", sampleFilePath.toString())
                    isFileOpened = true

                }
                Log.d("cfauli", "File already created " + minTime + " " + minDist + " " + LocalDateTime.now())
                File(sampleFilePath.toString()).appendText("\n" + LocalDateTime.now() + ";" + pDevice.operator.toString() + ";" + pDevice.band.toString() + pDevice.mcc.toString() + ";" + pDevice.mnc.toString() + ";" + pDevice.cid.toString() + ";" + "%.4f".format(location.latitude) +  ";" + "%.4f".format(location.longitude) + ";" +  pDevice.dbm)
            } else if (!listener?.getFileState()!! && isFileOpened) {
                isFileOpened = false
            }

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

    private fun readInitialConfiguration() {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minDist = sharedPreferences.getInt("num_dist_samples",getString(R.string.minDistSample).toInt()).toFloat()
        minTime  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt()).toLong()* 1000
        okSaveTowers = sharedPreferences.getInt("ok_save_towers",getString(R.string.oksavetowers).toInt())
        okSaveSamples = sharedPreferences.getInt("ok_save_samples",getString(R.string.oksavesamples).toInt())
    }
    // The sdcard directory is equal to the partition storage/emulated/0 which corresponds to the public external storage
    // See https://imnotyourson.com/which-storage-directory-should-i-use-for-storing-on-android-6/
    // The pricate external storage is storage/emulated/0/Android/data/edu.uoc.android
    // The pictures are in storage/emulated/0/Android/data/edu.uoc.android/files/Pictures/UOCImageAPP/
    // To save in other location needs to be analyzed if package_paths is to be used.

    private fun getStorageDir(): String {
        return requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
    }

    fun startGPS() {
        if (!gpsActive) {
            locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE)
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime!!,minDist!!,myLocListener)
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime!!,minDist!!,myLocListener)
            }
        }
        gpsActive = true
        //locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

    }

    fun endGPS() {
        try {
            locationManager.removeUpdates(myLocListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        gpsActive = false
    }

}



interface FetchCompleteListener {

}
