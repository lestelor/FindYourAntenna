package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.data.Site
import lestelabs.antenna.ui.main.data.SitesInteractor
import lestelabs.antenna.ui.main.MyApplication.Companion.internetOn
import lestelabs.antenna.ui.main.MyApplication.Companion.sitesInteractor
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import java.io.File



/*
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab3.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab3.newInstance] factory method to
 * create an instance of this fragment.
 */


open class Tab3 : Fragment() , OnMapReadyCallback {


    private var mcc: Int? = null
    private var mnc: Int? = null
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

    val db = FirebaseFirestore.getInstance()
    private var sitesListener: ListenerRegistration? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("cfauli", "OnCreate Tab3")
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        if (arguments!=null) {
            mcc = this.arguments?.getInt(ARG_PARAM1)
            mnc = this.arguments?.getInt(ARG_PARAM2)
        } else Log.d(TAG, "arguments off")


        // get sites -> pendiente sacar dialogo si algo falla
        mcc?.let { mnc?.let { it1 -> getSites(it, it1) } }

        getSites(214,3)

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

        // retrieve mcc and mnc from activity

        //initialitze maps
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
                            ), 12f
                        )
                    )
                }
            }

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
                    operator = "Mas Movil"
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
            214 -> list = arrayOf("Movistar", "Orange", "Vodafone", "Mas Movil", "GSM", "UMTS", "LTE")
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

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val TAG = "Tab3"
    }

    // Get Books and Update UI
    private fun getSites(mcc: Int, mnc: Int) {
        // First load whatever is stored locally
//       loadSitesFromLocalDb()
        // Check if Internet is available
        //internetOn?.let {
            //if (internetOn as Boolean) {
                Log.d(TAG, "internet on")
                val sites: MutableList<Site> = mutableListOf()
                // Internet connection is available, get remote data
                db.collection("sites")
                    // Subscribe to remote book changes
                    .whereEqualTo("operador", "Orange")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val site:Site = Site()
                            site.uid = document.data["uid"].toString()
                            site.codigo = document.data["codigo"].toString()
                            site.operador = document.data["operador"].toString()
                            site.lat = document.data["lat"].toString()
                            site.long = document.data["long"].toString()
                            site.frecuencias = document.data["frecuencias"].toString()
                            sites.add(site)
                        }
                        Log.d(TAG, "sites guardados " + sites.count())
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting documents: ", exception)
                    }


            //}
        //}
    }

    // Load Books from Room
    private fun loadSitesFromLocalDb() {
        val sitesInteractor: SitesInteractor = sitesInteractor
        // Run in Background, accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            // Get Books
            val sites = sitesInteractor.getAllSites()
            // Update Adapter on the UI Thread
            activity?.runOnUiThread {
                //adapter.setBooks(books)
            }
        }
    }

    // Save Books to Local Storage
    private fun saveBooksToLocalDatabase(books: List<Site>) {
        val sitesInteractor: SitesInteractor = sitesInteractor
        // Run in Background; accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            sitesInteractor.saveSites(books)
        }
    }


    override fun onDestroy() {
        // IMPORTANT! Remove Firestore Change Listener to prevent memory leaks
        sitesListener?.remove()
        super.onDestroy()
    }
}
    


//celltower.whereEqualTo("radio", radio).whereEqualTo("net", net)
//.whereGreaterThan("lat", wgsMin.latitude.toString())
//.whereLessThan("lat", wgsMax.latitude.toString())
//
////.whereGreaterThan("lon",wgsMin.longitude.toString())
////.whereLessThan("lon",wgsMax.longitude.toString())
//.get()
//.addOnSuccessListener { documents ->
//    indeterminateBar.visibility = View.GONE
//    for (document in documents) {
//        val latDocument: Double = document.data["lat"].toString().toDouble()
//        val lonDocument: Double = document.data["lon"].toString().toDouble()
//        if (lonDocument < wgsMax.longitude && lonDocument > wgsMin.longitude) {
//            val iconOperatorSelected = selectOperatorIcon(document.data["mcc"].toString().toInt(), document.data["net"].toString().toInt())
//            //Log.d("cfauli", "document firestore" + document.data["lat"].toString() + " " + document.data["lon"].toString().toDouble())
//            if (iconOperatorSelected[1] !== null) {
//                mMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(latDocument, lonDocument))
//                        .title(document.data["mcc"].toString() + "-" + document.data["net"].toString() + "-" + document.data["area"].toString() + "-" + document.data["cell"].toString())
//                        .icon(BitmapDescriptorFactory.fromResource(iconOperatorSelected[1] as Int))
//                )
//            } else {
//                mMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(latDocument, lonDocument))
//                        .title(document.data["mcc"].toString() + "-" + document.data["net"].toString() + "-" + document.data["area"].toString() + "-" + document.data["cell"].toString())
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
//                )
//            }
//
//        }
//    }
//}
//.addOnFailureListener { exception ->
//    indeterminateBar.visibility = View.GONE
//    Log.w("cfauli", "Error getting documents: ", exception)
//}