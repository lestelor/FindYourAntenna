package lestelabs.antenna.ui.main


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.algorithms.UTM
import lestelabs.antenna.ui.main.algorithms.WGS84
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.calculateFreq
import lestelabs.antenna.ui.main.scanner.loadCellInfo
import lestelabs.antenna.ui.main.scanner.waitGPS
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


/*
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab3.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab3.newInstance] factory method to
 * create an instance of this fragment.
 */


open class Tab3 : Fragment() , OnMapReadyCallback {



    private var mParam1: String? = null
    private var mParam2: String? = null
    //private var mListener: Tab3.OnFragmentInteractionListener? = null
    private var location:Location? = null
    private lateinit var mAdView : AdView
    private var gpsActive = false
    private var firstOnResume = true
    private var listener: GetfileState? = null


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
    private var locationOk: Location? = null
    private lateinit var locationManager: LocationManager
    private var towerinListInt: Int = -1
    private var minDist: Float? = null
    private var minTime:Long? = null
    private var okSaveTowers:Boolean? = null
    private var okSaveSamples: Boolean? = null
    private var previousTower: DevicePhone? = DevicePhone()
    private var isFileSamplesOpened: Boolean = false
    private var isFileTowersCreated: Boolean = false
    private var fabSaveClicked = false

    private val fileTowers = "towers.csv"
    private var sampleFile: String? = null
    private var storageDir: String? = null
    private  var towersFilePath: File? = null
    private  var storageDirTowers: File? = null
    private  var sampleFilePath: File? = null
    private  var storageDirFile: File? = null

    var myLocListener:MyLocationListener = MyLocationListener()

    private var distance:Double=0.0
    private var distanceAnt:Double = 100000.0

    private var networkList: Array<String>? = arrayOf("")
    private var checkedItems = booleanArrayOf(false)


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
        Log.d("cfauli", "OnCreate Tab3")
        readInitialConfiguration()





            /*while (!gps_enabled) {
                Log.d("cfauli", "GPS NOT enabled bucle")
                Thread.sleep(1000)
            }*/
    }
    override fun onStart() {
        // call the superclass method first
        super.onStart()
        Log.d("cfauli", "OnStart Tab3")
        waitGPS(requireContext())
        startGPS()
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli", "OnStop Tab3")
    }


    // Called at the end of the active lifetime.
    override fun onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause()
        Log.d("cfauli", "OnPause Tab3")
        endGPS()
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // prevent window from going to sleep
        view?.keepScreenOn = true
        locationAnt = location
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_tab3, container, false)
        val fab_save = fragmentView.findViewById(R.id.fab_tab3_save) as ImageView
        val fab_clear = fragmentView.findViewById(R.id.fab_tab3_clear) as ImageView
        val fab_load = fragmentView.findViewById(R.id.fab_tab3_open) as ImageView
        val fab_world = fragmentView.findViewById(R.id.fab_tab3_world) as ImageView
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        Log.d("cfauli", "OnCreateView Tab3")

        // if not Firestore uploaded, then hide button
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pDevice = loadCellInfo(telephonyManager)
        if (pDevice.mcc != 214) fab_world.isVisible = false







        // Floating button save
        fab_save.setBackgroundResource(R.drawable.ic_diskette)
        fab_save.setOnClickListener { view ->
            changebutton(fragmentView)
            Log.d("cfauli", "onclick buttom")
            if (fabSaveClicked) {
                storageDir = getStorageDir()
                Toast.makeText(context, getString(R.string.FileSavedIn) + storageDir, Toast.LENGTH_LONG).show()
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(getString(R.string.DialogSamplesTitle))
                builder.setMessage(getString(R.string.DialogSamplesMessage) + storageDir)
                builder.setPositiveButton("OK") { dialog, which ->
                    // Create towers file

                    storageDirTowers = File(storageDir!!)
                    if (!storageDirTowers!!.exists()) {
                        storageDirTowers!!.mkdirs()
                    }
                    towersFilePath = File(storageDirTowers, fileTowers)

                    if (!towersFilePath!!.exists()) {
                        Log.d("cfauli towersfilepath ", towersFilePath.toString())
                        File(towersFilePath.toString()).writeText("time;mcc;mnc;lac;id;lat;lon")
                    }
                    // Save tower

                    pDevice = loadCellInfo(telephonyManager)
                    findTower(openCellIdInterface, pDevice)
                    { coordenadas ->
                        pDevice.lat = coordenadas.lat!!
                        pDevice.lon = coordenadas.lon!!
                        previousTower = pDevice

                        File(towersFilePath.toString()).appendText(
                            "\n" + LocalDateTime.now() + ";" + pDevice.mcc +
                                    ";" + pDevice.mnc + ";" + pDevice.lac + ";" + pDevice.cid +
                                    ";" + "%.5f".format(pDevice.lat) + ";" + "%.5f".format(pDevice.lon)
                        )
                        isFileTowersCreated = true
                    }
                    //create samples file
                    sampleFile = "samples_" + LocalDateTime.now() + ".csv"
                    Log.d("cfauli", "File first created" + LocalDateTime.now())
                    storageDirFile = File(storageDir!!)

                    if (!storageDirFile!!.exists()) {
                        storageDirFile!!.mkdirs()
                    }
                    sampleFilePath = File(storageDirFile, sampleFile!!)
                    File(sampleFilePath.toString()).writeText("time;mcc;mnc;lac;id;type;ferquency;dBm;lat;lon;arfcn")

                    File(sampleFilePath.toString()).appendText(
                        "\n" + LocalDateTime.now() + ";" + pDevice.mcc +
                                ";" + pDevice.mnc + ";" + pDevice.lac + ";" + pDevice.cid + ";" + pDevice.type +
                                ";" + "%.1f".format(calculateFreq(pDevice.type, pDevice.band)) + ";" + pDevice.dbm +
                                ";" + "%.5f".format(locationOk!!.latitude) + ";" + "%.5f".format(locationOk!!.longitude) +
                                ";" + pDevice.band
                    )

                    Log.d("cfauli", sampleFilePath.toString())
                    plotColoredDot(LatLng(locationOk!!.latitude, locationOk!!.longitude), pDevice.dbm!!)

                    isFileSamplesOpened = true
                }
                builder.setNegativeButton("Cancel", null)

                val dialog = builder.create()
                dialog.show()

            }




        }
        // Floating button clear
        fab_clear.setOnClickListener { view ->
            mMap.clear()

            pDevice = loadCellInfo(telephonyManager)
            Log.d("cfauli", "fab clear pDevice cid" + pDevice.cid)
            val towerinListSize = listTowersFound.size
            towerinListInt = checkTowerinList(pDevice)
            // Delete if since allways print all the markers
            //if (towerinListSize == towerinListInt) {
                Log.d("cfauli", "fab clear pDevice cid 2 " + pDevice.cid)
                findTower(openCellIdInterface, pDevice)
                { coordenadas ->
                    pDevice.lat = coordenadas.lat!!
                    pDevice.lon = coordenadas.lon!!
                    Log.d("cfauli", "fab clear pDevice lat " + pDevice.lat)
                    listTowersFound[towerinListInt].lat = coordenadas.lat!!
                    listTowersFound[towerinListInt].lon = coordenadas.lon!!
                    previousTower = pDevice
                    Log.d("cfauli", "fab clear locationOk " + locationOk!!.latitude)
                    // fill the distance and tower textview, repeated since it is an async function
                    // print the tower markers (green the serving and red the others) and make appropriate zoom
                    Log.d("cfauli", "LocateTowerMap 3")
                    locateTowerMap(listTowersFound)
                    cameraAnimate(listTowersFound[towerinListInt], locationOk!!)
                    updateTextViewDistanceTower(locationOk!!)
            }
        }
        // Floating button load

        fab_load.setOnClickListener { view ->
            Log.d("cfauli", "onclick buttom")
            storageDir = getStorageDir()
            storageDirTowers = File(storageDir!!)

            towersFilePath = File(storageDirTowers, fileTowers)
            performTowerSearch()
            performFileSearch()
            Log.d("cfauli", "performfilesearch")
        }


        fab_world.setOnClickListener { view ->
            Log.d("cfauli", "onclick buttom fab_world")
            // To be defined according to the mcc. This is for 214

            networkList = pDevice.mcc?.let { arrayNetworks(it) }
            checkedItems = booleanArrayOf(true, false, false, false, false, false, true)

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.DownloadOpenCell))
            builder.setMultiChoiceItems(networkList, checkedItems) { dialog, which, isChecked ->
                checkedItems[which] = isChecked
            }
            builder.setPositiveButton("OK") { dialog, which ->
                plotTowers()
            }
            builder.setNegativeButton("Cancel", null)

            val dialog = builder.create()
            dialog.show()
        }


        return fragmentView

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("cfauli", "OnAtach Tab3")
        /*mListener = if (context is Tab3.OnFragmentInteractionListener) {
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
        Log.d("cfauli", "OnDetach Tab3")
    }



    override fun onResume() {
        super.onResume()
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minDist = sharedPreferences.getInt("num_dist_samples", getString(R.string.minDistSample).toInt()).toFloat()
        minTime  = sharedPreferences.getInt("num_time_samples", getString(R.string.minTimeSample).toInt()).toLong() * 1000
        Log.d("cfauli", "OnResume tab3")
        if (!firstOnResume) {
            waitGPS(requireContext())
            startGPS()
        }
        firstOnResume = false
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
    interface GetfileState {
        fun getFileState():String
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pDevice = loadCellInfo(telephonyManager)
        //Thread.sleep(1000)
        readInitialConfiguration()
        //MapsInitializer.initialize(context)
        Log.d("cfauli", "OnmapReady")
        Log.d("cfauli", "mMap " + mMap)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isMyLocationEnabled = true

        waitGPS(requireContext())
        startGPS()
        tv_fr3_distance.text = getString(R.string.waitingGPS)

        // First try. Same that findPdeviceAddMarkerAnimateCameraUdateTextView but always printing the Green Marker and no other else (on the contrary of locatetowermap)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // create pDevice and add green marker
                    locationOk = location
                    pDevice = loadCellInfo(telephonyManager)

                    val towerinListSize = listTowersFound.size
                    towerinListInt = checkTowerinList(pDevice)
                    Log.d("cfauli", "findPdeviceAddMarkerUpdate " + towerinListSize + " " + towerinListInt)
                    if (towerinListSize == towerinListInt) {
                        Log.d("cfauli", "gps onstart pdevicelat " + pDevice.lat)
                        findTower(openCellIdInterface, pDevice)
                        { coordenadas ->
                            pDevice.lat = coordenadas.lat!!
                            pDevice.lon = coordenadas.lon!!
                            listTowersFound[towerinListInt].lat = coordenadas.lat!!
                            listTowersFound[towerinListInt].lon = coordenadas.lon!!
                            previousTower = pDevice
                            // fill the distance and tower textview, repeated since it is an async function
                            // print the tower markers (green the serving and red the others) and make appropriate zoom
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(pDevice.lat, pDevice.lon))
                                    .title(pDevice.mcc.toString() + "-" + pDevice.mnc + "-" + pDevice.lac + "-" + pDevice.cid)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            )
                            val mZoom = mZoom(pDevice, location)
                            val myLocation = LatLng(location.latitude, location.longitude)

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, mZoom))
                            //locateTowerMap(listTowersFound[towerinListInt], locationOk!!)
                            updateTextViewDistanceTower(locationOk!!)


                        }
                    }
                }
                // check if tower exists and returns the index. If doesnt, add pDevice to towerinlist and returns the index = prev_index+1.
                // In this case towerinlistInt should be 1, the first time it creates a green marker.
                towerinListInt = checkTowerinList(pDevice)
                }


        mAdView = view?.findViewById(R.id.adViewFragment3)!!
        val adView = AdView(requireActivity())
        MobileAds.initialize(requireActivity())
        val adRequest = AdRequest.Builder().build()

        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode: Int) {
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




    private fun locateTowerMap(listTowersFound: List<DevicePhone>) {

        Log.d("cfauli", "listTowersize " + listTowersFound.size)
        if (listTowersFound.size > 1) {
            Log.d("cfauli", "listTowertotalcid " + listTowersFound[1].totalCellId)
            Log.d("cfauli", "listTowerlat " + listTowersFound[1].lat)
            for (i in 1..listTowersFound.size - 1) {
                if (i != towerinListInt) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(listTowersFound[i].lat, listTowersFound[i].lon))
                            .title(listTowersFound[i].mcc.toString() + "-" + listTowersFound[i].mnc + "-" + listTowersFound[i].lac + "-" + listTowersFound[i].cid)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                } else {
                    if (distance != 0.0) {

                        previousTower = listTowersFound[i]
                        //pDevice = listTowersFound[i]


                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(listTowersFound[i].lat, listTowersFound[i].lon))
                                .title(listTowersFound[i].mcc.toString() + "-" + listTowersFound[i].mnc + "-" + listTowersFound[i].lac + "-" + listTowersFound[i].cid)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                    }
                }
            }
        }





    }

    private fun cameraAnimate(pDevice: DevicePhone, location: Location) {
        Log.d("cfauli", "locateTowerMap markerok distance " + distance)


        val mZoom = mZoom(pDevice, location)
        val myLocation = LatLng(location.latitude, location.longitude)

        Log.d("cfauli", "LocateTowerMap function " + location.latitude + " " + location.longitude + " " + mZoom)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, mZoom))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0))
    }

    private fun checkTowerinList(devicePhone: DevicePhone):Int {
        //var devicePhonecheck: DevicePhone? = listTowersFound.find { it.totalCellId == devicePhone.totalCellId }
        var indexOfTower = listTowersFound.indexOfFirst { it.totalCellId == devicePhone.totalCellId }
        Log.d("cfauli", "checkTowerList indexofTower " + indexOfTower)
        if (indexOfTower == -1) {
            Log.d("cfauli", "checkTowerList number towers" + listTowersFound[0].totalCellId)
            listTowersFound.add(devicePhone)
            Log.d("cfauli", "checkTowerList Added tower" + devicePhone.totalCellId)
            indexOfTower = listTowersFound.size-1
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
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * Math.cos(
            Math.toRadians(lat2)
        ) *
                sin(dLng / 2) * Math.sin(dLng / 2)
        val c =
            2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
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
         * @return A new instance of fragment Tab3.
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


    inner class MyLocationListener : LocationListener {


        @RequiresApi(Build.VERSION_CODES.P)
        override fun onLocationChanged(location: Location) {
            readInitialConfiguration()


            locationOk = location
            pDevice = loadCellInfo(telephonyManager)



            val towerinListSize = listTowersFound.size
            towerinListInt = checkTowerinList(pDevice)

            Log.d("cfauli", "gps onlocationchange pdevicelat " + pDevice.lat + "towerinlistsize " + towerinListSize + "towerinlistint " + towerinListInt)

            Log.d("cfauli", "onlocationchanged towerinlist " + towerinListInt)
            Log.d("cfauli", "onlocationchanged previous tower " + previousTower?.totalCellId + " pdevice " + pDevice.totalCellId + " " + towerinListInt + " " + towerinListSize)
            //Toast.makeText(context, "onlocationchanged " + previousTower?.cid + " " + pDevice.cid + " " + towerinListInt + " " + towerinListSize, Toast.LENGTH_LONG).show()
            // First check, if tower is the same as previous do nothing except update the textview.

            // In case the tower is not found (index = size, since new pDevice is added to listtowerfound) , then complete PDevice with the lat, lon information of the tower
            if (towerinListInt == towerinListSize || previousTower?.totalCellId == "") {
                //Toast.makeText(context, "onlocationchanged newTower " + listTowersFound[towerinListInt].cid,Toast.LENGTH_LONG).show()
                findTower(openCellIdInterface, pDevice)
                { coordenadas ->
                    pDevice.lat = coordenadas.lat!!
                    pDevice.lon = coordenadas.lon!!
                    listTowersFound[towerinListInt].lat = coordenadas.lat!!
                    listTowersFound[towerinListInt].lon = coordenadas.lon!!
                    previousTower = pDevice
                    // fill the distance and tower textview, repeated since it is an async function
                    // print the tower markers (green the serving and red the others) and make appropriate zoom
                    Log.d("cfauli", "LocateTowerMap 1")
                    locateTowerMap(listTowersFound)
                    Log.d("cfauli", "mMap onlocationchanged " + mMap)
                    if (mMap != null) cameraAnimate(listTowersFound[towerinListInt], location)
                    updateTextViewDistanceTower(locationOk!!)


                    // Save towers in file
                    Log.d("cfauli", "Save towers file filestate " + fabSaveClicked + " okSaveSample " + okSaveTowers + " isfileopened " + isFileTowersCreated)
                    if (fabSaveClicked && okSaveTowers == true && isFileTowersCreated) {
                        Log.d("cfauli", "Save tower file opened: " + isFileTowersCreated)
                        if (isFileTowersCreated) {
                            File(towersFilePath.toString()).appendText(
                                "\n" + LocalDateTime.now() + ";" + pDevice.mcc +
                                        ";" + pDevice.mnc + ";" + pDevice.lac + ";" + pDevice.cid +
                                        ";" + "%.5f".format(pDevice.lat) + ";" + "%.5f".format(pDevice.lon)
                            )
                        }

                    } else if (!fabSaveClicked && isFileTowersCreated) isFileTowersCreated = false

                }
            } else  {

                //Toast.makeText(context, "onlocationchanged previousTower " + listTowersFound[towerinListInt].cid + " " + location.latitude,Toast.LENGTH_LONG).show()
                // if tower exists in list, only update the colors of the markers and the textview
                Log.d("cfauli", "LocateTowerMap 2")
                locateTowerMap(listTowersFound)
                //cameraAnimate(listTowersFound[towerinListInt], location)
                updateTextViewDistanceTower(location)
                previousTower = listTowersFound[towerinListInt]
                //val mZoom = mZoom(pDevice, location!!)
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pDevice.lat, pDevice.lon), mZoom))
            }


            // Save samples in file and draw colored dot
            Log.d("cfauli", "Save file filestate " + fabSaveClicked + " okSaveSample " + okSaveSamples + " isfileopened " + isFileSamplesOpened)
            if (fabSaveClicked && okSaveSamples == true && isFileSamplesOpened)  {



                Log.d("cfauli", "Save file opened: " + isFileSamplesOpened)
                Log.d("cfauli", "File already created " + minTime + " " + minDist + " " + LocalDateTime.now())
                File(sampleFilePath.toString()).appendText(
                    "\n" + LocalDateTime.now() + ";" + pDevice.mcc +
                            ";" + pDevice.mnc + ";" + pDevice.lac + ";" + pDevice.cid + ";" + pDevice.type +
                            ";" + calculateFreq(pDevice.type, pDevice.band) + ";" + pDevice.dbm + ";"
                            + "%.5f".format(location.latitude) + ";" + "%.5f".format(location.longitude)
                )

                plotColoredDot(LatLng(location.latitude, location.longitude), pDevice.dbm!!)




            } else if (!fabSaveClicked  && isFileSamplesOpened) {
                isFileSamplesOpened = false
            }

            locationAnt = location



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
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        minDist = sharedPreferences.getInt("num_dist_samples", getString(R.string.minDistSample).toInt()).toFloat()
        minTime  = sharedPreferences.getInt("num_time_samples", getString(R.string.minTimeSample).toInt()).toLong()* 1000
        okSaveSamples = sharedPreferences.getBoolean("chkBoxSamples", true)
        okSaveTowers = sharedPreferences.getBoolean("chkBoxTowers", true)

    }
    // The sdcard directory is equal to the partition storage/emulated/0 which corresponds to the public external storage
    // See https://imnotyourson.com/which-storage-directory-should-i-use-for-storing-on-android-6/
    // The pricate external storage is storage/emulated/0/Android/data/edu.uoc.android
    // The pictures are in storage/emulated/0/Android/data/edu.uoc.android/files/Pictures/UOCImageAPP/
    // To save in other location needs to be analyzed if package_paths is to be used.

    private fun getStorageDir(): String? {

        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("popFolder", requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
    }

    fun startGPS() {

        Log.d("cfauli", "startGPS locationmanager gpsactive " + gpsActive)
        if (!gpsActive) {

            locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
            /*if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE)
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime!!,minDist!!,myLocListener)
            }*/
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.PERMISSION_REQUEST_CODE)
                    return
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime!!, minDist!!, myLocListener)
            }
        }
        gpsActive = true
        //locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

    }

    fun endGPS() {
        Log.d("cfauli", "stopGPS locationmanager gpsactive " + gpsActive)
        try {
            locationManager.removeUpdates(myLocListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        gpsActive = false
    }


    fun plotColoredDot(location: LatLng, pDevicedbm: Int) {
        // Plot colored dots
        val markerDot:Int
        val sharedPreferences = requireActivity().getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val thresMobBlack = sharedPreferences.getString("thres_mob_black", Constants.MINMOBILESIGNALBLACK)!!.toInt()
        val thresMobRed = sharedPreferences.getString("thres_mob_red", Constants.MINMOBILESIGNALRED)!!.toInt()
        val thresMobYellow = sharedPreferences.getString("thres_mob_yellow", Constants.MINMOBILESIGNALYELLOW)!!.toInt()
        val thresMobGreen = Constants.MINMOBILESIGNALGREEN.toInt()

        if (pDevicedbm >= -1*thresMobYellow) {
            markerDot = R.drawable.circle_dot_green_icon
        } else if (pDevicedbm >= -1*thresMobRed) {
            markerDot = R.drawable.circle_dot_yellow_icon
        } else if (pDevicedbm >= -1*thresMobBlack) {
            markerDot = R.drawable.circle_dot_red_icon
        } else {
            markerDot = R.drawable.circle_dot_black_icon
        }
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(pDevice.dbm.toString() + " dBm")
                .icon(BitmapDescriptorFactory.fromResource(markerDot))
        )
    }

    fun changebutton(view: View) {
        fab_tab3_save.setBackgroundColor(resources.getColor(R.color.black));
        if (fabSaveClicked) {
            fab_tab3_save.setImageResource(R.drawable.ic_diskette)
            fab_tab3_save.setBackgroundColor(resources.getColor(R.color.colorPrimary))

        } else {
            fab_tab3_save.setImageResource(R.drawable.ic_stop)
            fab_tab3_save.setBackgroundColor(resources.getColor(R.color.black))
        }
        fabSaveClicked = !fabSaveClicked // reverse
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull the path using resultData.getData().
            resultData?.data?.also { uri ->

                Log.d("cfauli", "folder " + resultData.data?.path)
                pathString = resultData.data?.path?.replace("/tree/primary:", "/storage/emulated/0/")

            }
        }
    }*/

    fun performTowerSearch() {

        var nextLine: String? = null
        try {
            val csvfile = File(towersFilePath.toString())
            Log.d("cfauli", "csvfile" + csvfile.absolutePath)

            val reader = BufferedReader(FileReader(csvfile))
            Log.d("cfauli", "csvfile" + reader)
            nextLine = reader.readLine()
            var i =0
            while (nextLine != null) {

                val tokens: List<String> = nextLine.split(";")
                if (i>0) {
                    val latFileDouble = tokens[5].replace(",", ".").toDouble()
                    val lonFileDouble = tokens[6].replace(",", ".").toDouble()
                    val cid = tokens[1] + "-" + tokens[2] + "-" + tokens[3] + "-" + tokens[4]

                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(latFileDouble, lonFileDouble))
                            .title(cid)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )

                }


                nextLine = reader.readLine()
                i += 1
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.towerFileNotFound), Toast.LENGTH_SHORT).show()
        }


    }


    fun performFileSearch() {
        // The PopFolder textview is not clickable since this reports an writing error in folders which are different to /storage/emulated/0/Android...
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "text/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 0)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull the path using resultData.getData().
            resultData?.data?.also { uri ->

                val dotsFilePath = resultData.data?.path?.replace("/document/primary:", "/storage/emulated/0/")
                Log.d("cfauli", "performfilesearch folder " + resultData.data?.path?.replace("/document/primary:", "/storage/emulated/0/"))
                var nextLine: String? = null
                try {
                    //val filePath = File(getStorageDir())
                    //val csvfile = File(filePath, "samples.csv")
                    val csvfile = File(dotsFilePath)
                    Log.d("cfauli", "csvfile" + csvfile.absolutePath)



                    //val reader = CSVReader(FileReader(csvfile))
                    //val reader = FileReader(csvfile)
                    val reader = BufferedReader(FileReader(csvfile))
                    Log.d("cfauli", "csvfile" + reader)
                    nextLine = reader.readLine()
                    var i =0
                    while (nextLine != null) {

                        val tokens: List<String> = nextLine.split(";")
                        if (i>0) {
                            val latFileDouble = tokens[8].replace(",", ".").toDouble()
                            val lonFileDouble = tokens[9].replace(",", ".").toDouble()
                            val dBm = tokens[7].toInt()
                            Log.d("cfauli", " nextLine: " + tokens[8] + " " + tokens[9])
                            plotColoredDot(LatLng(latFileDouble, lonFileDouble), dBm)
                        }


                        nextLine = reader.readLine()
                        i += 1
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, getString(R.string.NotAValidFile), Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

    fun mZoom(pDevice: DevicePhone, location: Location): Float {
        distance = distance(pDevice.lat, pDevice.lon, location.latitude, location.longitude)
        Log.d("cfauli", "Zoom zoom " + distance)
        var mZoom = 13f
        when {
            distance < 500  -> mZoom = 15f
            distance < 2000  -> mZoom = 14f
            distance < 5000  -> mZoom = 12f
            distance < 10000  -> mZoom = 10f
            distance < 20000  -> mZoom = 9f


        }
        Log.d("cfauli", "Zoom distance " + mZoom)
        return mZoom
    }


fun updateTextViewDistanceTower(location: Location) {


    distance = distance(location.latitude, location.longitude, listTowersFound[towerinListInt].lat, listTowersFound[towerinListInt].lon)
    Log.d("cfauli distance", distance.toString() + " " + location.latitude + " " + location.longitude + " " + listTowersFound[towerinListInt].lat + " " + listTowersFound[towerinListInt].lon)
    when {
        distance==0.0 -> {
            tv_fr3_distance.text = getString(R.string.waitingGPS)
        }
        distance < 100000.0  -> {
            tv_fr3_distance.text = "%.0f".format(distance) + "m"

        }
        distanceAnt > 100000.0 -> {
            tv_fr3_distance.text = getString(R.string.towerNotFound)

        }

        else -> tv_fr3_distance.text = getString(R.string.NotAvailable)
    }
    tv_fr3_tower.text = pDevice.mcc.toString() + "-" + pDevice.mnc + "-" + pDevice.lac + "-" + pDevice.cid
    tv_fr3_coverage.text = pDevice.dbm.toString() + "dBm"
}

    private fun plotTowers() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val wgsLocation = WGS84(location.latitude, location.longitude)
                    //val utmLocation = UTM(31, 'V', 375273.85, 6207884.59)

                    val utmMin = UTM(UTM(wgsLocation).zone, UTM(wgsLocation).letter, UTM(wgsLocation).easting - 5000,
                        UTM(wgsLocation).northing - 5000)
                    val utmMax = UTM(UTM(wgsLocation).zone, UTM(wgsLocation).letter, UTM(wgsLocation).easting + 5000,
                        UTM(wgsLocation).northing + 5000)

                    val wgsMin = WGS84(utmMin)
                    val wgsMax = WGS84(utmMax)

                    Log.d("cfauli", "wgs84 " + wgsMin.latitude.toString() + " " + wgsMax.latitude.toString())

                    val db = FirebaseFirestore.getInstance()
                    val celltower = db.collection("celltower")


                    // obtain radios and networks
                    val radios = getRadios()
                    val nets = getNetworks()
                    Log.d("cfauli", "networks " + radios + " " + nets )
                    for (i in 1..radios.size-1) {
                        for (j in 1..nets.size-1) {
                            indeterminateBar.visibility = View.VISIBLE
                            val radio = radios[i]
                            val net = nets[j]
                            // Create a query against the collection. All where filters other than whereEqualTo() must be on the same field.
                            celltower.whereEqualTo("radio", radio).whereEqualTo("net", net)
                                .whereGreaterThan("lat",wgsMin.latitude.toString())
                                .whereLessThan("lat",wgsMax.latitude.toString())

                                //.whereGreaterThan("lon",wgsMin.longitude.toString())
                                //.whereLessThan("lon",wgsMax.longitude.toString())
                                .get()
                                .addOnSuccessListener { documents ->
                                    indeterminateBar.visibility = View.GONE
                                    for (document in documents) {
                                        val latDocument: Double = document.data["lat"].toString().toDouble()
                                        val lonDocument: Double = document.data["lon"].toString().toDouble()
                                        if (lonDocument < wgsMax.longitude && lonDocument > wgsMin.longitude) {
                                            val iconOperatorSelected = selectOperatorIcon(document.data["mcc"].toString().toInt(), document.data["net"].toString().toInt())
                                            //Log.d("cfauli", "document firestore" + document.data["lat"].toString() + " " + document.data["lon"].toString().toDouble())
                                            if (iconOperatorSelected[1] !== null) {
                                                mMap.addMarker(
                                                    MarkerOptions()
                                                        .position(LatLng(latDocument, lonDocument))
                                                        .title(document.data["mcc"].toString() + "-" + document.data["net"].toString() + "-" + document.data["area"].toString() + "-" + document.data["cell"].toString())
                                                        .icon(BitmapDescriptorFactory.fromResource(iconOperatorSelected[1] as Int))
                                                )
                                            } else {
                                                mMap.addMarker(
                                                    MarkerOptions()
                                                        .position(LatLng(latDocument, lonDocument))
                                                        .title(document.data["mcc"].toString() + "-" + document.data["net"].toString() + "-" + document.data["area"].toString() + "-" + document.data["cell"].toString())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                )
                                            }

                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    indeterminateBar.visibility = View.GONE
                                    Log.w("cfauli", "Error getting documents: ", exception)
                                }
                        }
                    }


                }
            }


    }

    fun selectOperatorIcon (mcc:Int, net:Int): Array<Any?> {
        var icon: Int? =null
        var operator =""

        when (mcc) {
            214 -> when (net) {
                1-> {
                   operator = "Vodafone"
                    icon = R.drawable.ic_vodafone
                }
                3-> {
                    operator = "Orange"
                    icon = R.drawable.ic_orange
                }
                4-> {
                    operator = "Yoigo"
                }
                7-> {
                    operator = "Movistar"
                    icon = R.drawable.ic_movistar
                }
            }

            }
            return arrayOf(operator,icon)
        }

    fun arrayNetworks(mcc:Int):Array<String>? {
        var list: Array<String>? = null
        when (mcc) {
            214 -> list = arrayOf("Movistar", "Orange", "Vodafone", "Yoigo", "GSM", "UMTS", "LTE")
        }
        return list
    }

    fun getRadios(): MutableList<String> {
        var salida: MutableList<String> = mutableListOf("")
        for (i in 4..6) {
            if (checkedItems[i]) {
                salida.add(networkList!![i])
            }
        }
        return salida
    }

    fun getNetworks(): MutableList<String> {
        var salida: MutableList<String> = mutableListOf("")
        for (i in 0..3) {
            if (checkedItems[i]) {
                when (pDevice.mcc) {
                    214 -> {
                        when (networkList?.get(i)) {
                            "Movistar" -> salida.add(7.toString())
                            "Vodafone" -> salida.add(1.toString())
                            "Orange" -> salida.add(3.toString())
                            "Yoigo" -> salida.add(4.toString())
                        }
                    }
                }
        }
        }
        return salida
    }
}

