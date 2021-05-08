package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_tab1.*
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.MyApplication.Companion.sitesInteractor
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.data.Site
import lestelabs.antenna.ui.main.data.SitesInteractor
import lestelabs.antenna.ui.main.scanner.DevicePhone


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

    private var mAdView : AdView? = null

    private lateinit var mMap: GoogleMap
    private var mMapInitialized: Boolean = false
    private lateinit var fragmentView: View
    private lateinit var mapFragment: SupportMapFragment
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private  var pDevice:DevicePhone? = null
    private var networkList: Array<String> = arrayOf("")
    private var checkedItems = booleanArrayOf(false)
    private val tabName = "Tab3"
    private var crashlyticsKeyAnt = ""
    val db = FirebaseFirestore.getInstance()
    private var sitesListener: ListenerRegistration? = null
    private var listener: GetfileState? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("cfauli", "OnCreate Tab3")
        // Control point for Crashlitycs
        crashlyticsKeyAnt = controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        mcc = listener?.getFileState()?.get(2)
        mnc = listener?.getFileState()?.get(3)
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

        // Initialize ads
        mAdView = view?.findViewById(R.id.adViewFragment3)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        //initialitze maps
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        return fragmentView

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("cfauli", "OnAtach Tab3")
        try {
            listener = activity as GetfileState
            // listener.showFormula(show?);
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
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
                                location.latitude,
                                location.longitude
                            ), 16f
                        )
                    )
                }
            }

        // Display sites
        Log.d(TAG,"listener " + mcc + " " + mnc)
        mcc?.let { mnc?.let { it1 -> getSites(it, it1) } }
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
        private const val TAG = "Tab3"
    }

    // Get Books and Update UI
    private fun getSites(mcc: Int, mnc: Int) {
        // start progress bar
        progressBarTab3.visibility = View.VISIBLE
        Tab3tvCargando.text = getString(R.string.Loading)
        // Find operator
        val operador = "Orange"
        val iconOperatorSelected = selectOperatorIcon(mcc, mnc)

        // First load whatever is stored locally
//       loadSitesFromLocalDb()
        // Check if Internet is available
        //internetOn?.let {
            //if (internetOn as Boolean) {
                Log.d(TAG, "internet on")
                val sites: MutableList<Site> = mutableListOf()
                // Internet connection is available, get remote data
                db.collection("sites").document("sites").collection(operador)
                    .get()
                    .addOnSuccessListener { documents ->
                        progressBarTab3.visibility = View.GONE
                        Tab3tvCargando.text = ""
                        for (document in documents) {
                            val site:Site = Site()

                            site.codigo = document.id
                            site.operador = operador
                            site.lat = document.data["lat"].toString()?:"0.0"
                            site.long = document.data["long"].toString()?: "0.0"
                            sites.add(site)

                            site.lat?.let {it1 -> site.long?.let { it2 ->
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(it1.replace(",",".").toDouble(), it2.replace(",",".").toDouble()))
                                        .title(document.id + "-" + it1 + "-" + it2)
                                        .icon(BitmapDescriptorFactory.fromResource(iconOperatorSelected[1] as Int))
                                )
                            }}

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
                    operator = "MasMovil"
                }
                7 -> {
                    operator = "Telefonica"
                    icon = R.drawable.ic_movistar
                }
                else -> {
                    operator = "Otros"
                    icon = R.drawable.circle_dot_black_icon
                }
            }

        }
        return arrayOf(operator, icon)
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