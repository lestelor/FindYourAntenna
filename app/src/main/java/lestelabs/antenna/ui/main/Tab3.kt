package lestelabs.antenna.ui.main


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.telephony.TelephonyManager
import android.text.format.DateFormat
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.algorithms.UTM
import lestelabs.antenna.ui.main.algorithms.WGS84
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.rest.findTower
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.calculateFreq
import lestelabs.antenna.ui.main.scanner.loadCellInfo
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.util.*
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
    private var mAdView : AdView? = null
    private var gpsActive = false
    private var firstOnResume = true
    private var listener: GetfileState? = null


    // As indicated in android developers https://developers.google.com/maps/documentation/android-sdk/start
    // Previously it is necessary to get the google API key from the Google Cloud Platform Console
    // (Maps SDK for Android) and store them in the manifest
    // The app build gradle is sync with the maps library

    private lateinit var mMap: GoogleMap
    private var mMapInitialized: Boolean = false
    private lateinit var fragmentView: View
    private lateinit var mapFragment: SupportMapFragment
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var telephonyManager:TelephonyManager
    private  var pDevice:DevicePhone? = null
    private val openCellIdInterface = retrofitFactory()
    private var listTowersFound:MutableList<DevicePhone> = mutableListOf(DevicePhone())
    private var locationAnt: Location? = null
    private lateinit var locationOk: Location
    private lateinit var locationManager: LocationManager
    private var towerinListInt: Int = -1
    private var minDist: Float?=null
    private var minTime:Long?=null
    private var okSaveTowers:Boolean? = null
    private var okSaveSamples: Boolean? = null
    private var previousTower: DevicePhone? = DevicePhone()
    private var isFileSamplesOpened: Boolean = false
    private var isFileTowersCreated: Boolean = false
    private var fabSaveClicked = false

    private val fileTowers = "towers.csv"
    private var sampleFile: String? = null
    private var storageDir: String?=null
    private  var towersFilePath: File? = null
    private  var storageDirTowers: File? = null
    private  var sampleFilePath: File? = null
    private  var storageDirFile: File? = null


    private var distance:Double=0.0
    private var distanceAnt:Double = 100000.0

    private var networkList: Array<String> = arrayOf("")
    private var checkedItems = booleanArrayOf(false)

    private val tabName = "Tab3"
    private var crashlyticsKeyAnt = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("cfauli", "OnCreate Tab3")
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

    }
    override fun onStart() {
        // call the superclass method first
        super.onStart()
        Log.d("cfauli", "OnStart Tab3")
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli", "OnStop Tab3")
    }


    // Called at the end of the active lifetime.
    override fun onPause() {
        super.onPause()
        Log.d("cfauli", "OnPause Tab3")
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("cfauli", "OnCreateView Tab3")
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_tab3, container, false)
        // prevent window from going to sleep
        fragmentView.keepScreenOn = true

        mAdView = view?.findViewById(R.id.adViewFragment3)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()

        mAdView?.loadAd(adRequest)

        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)


        return fragmentView

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("cfauli", "OnAtach Tab3")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("cfauli", "OnDetach Tab3")
    }



    override fun onResume() {
        super.onResume()
        Log.d("cfauli", "OnResume tab3")
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        mMap = googleMap
        mMapInitialized = true
        mMap.isMyLocationEnabled = true

        fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location: Location? ->
                if (location !=null) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location?.latitude,
                                location?.longitude
                            ), 9f
                        )
                    )
                }
            }

    }





    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }



    fun selectOperatorIcon(mcc: Int, net: Int): Array<Any?> {
        var icon: Int? =null
        var operator =""

        when (mcc) {
            214 -> when (net) {
                1 -> {
                    operator = "Vodafone"
                    icon = R.drawable.ic_vodafone
                }
                3 -> {
                    operator = "Orange"
                    icon = R.drawable.ic_orange
                }
                4 -> {
                    operator = "Yoigo"
                }
                7 -> {
                    operator = "Movistar"
                    icon = R.drawable.ic_movistar
                }
            }

            }
            return arrayOf(operator, icon)
        }

    fun arrayNetworks(mcc: Int):Array<String> {
        var list: Array<String> = emptyArray()
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
                when (pDevice?.mcc) {
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

