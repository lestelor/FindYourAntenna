package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.MyApplication.Companion.internetOn
import lestelabs.antenna.ui.main.MyApplication.Companion.sitesInteractor
import lestelabs.antenna.ui.main.crashlytics.Crashlytics.controlPointCrashlytics
import lestelabs.antenna.ui.main.data.Operators
import lestelabs.antenna.ui.main.data.Site
import lestelabs.antenna.ui.main.data.SitesInteractor
import lestelabs.antenna.ui.main.map.MyInfoWindowAdapter
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

    private var mAdView: AdView? = null

    private lateinit var mMap: GoogleMap
    private var mMapInitialized: Boolean = false
    private lateinit var fragmentView: View
    private lateinit var mapFragment: SupportMapFragment
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var pDevice: DevicePhone? = null
    private var networkList: Array<String> = arrayOf("")
    private var checkedItems = booleanArrayOf(false)
    private val tabName = "Tab3"
    private var crashlyticsKeyAnt = ""
    val db = FirebaseFirestore.getInstance()
    private var sitesListener: ListenerRegistration? = null
    private var listener: GetfileState? = null

    private var operadorAnt: String = ""


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("cfauli", "OnCreate Tab3")
        // Control point for Crashlitycs
        crashlyticsKeyAnt =
            controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        mcc = listener?.getFileState()?.get(2)
        mnc = listener?.getFileState()?.get(3)

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

        // Operator buttons click listeners
        setButtonOnclickListeners(fragmentView)

        return fragmentView

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt =
            controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        mMap = googleMap
        // personalized map label -> custominfowindow.xml
        mMap.setInfoWindowAdapter(MyInfoWindowAdapter(this.mapFragment))
        //mMap.setOnInfoWindowClickListener(this);
        // Set a listener for marker click.
        //googleMap.setOnMarkerClickListener(this)
        mMapInitialized = true
        mMap.isMyLocationEnabled = true

        fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location: Location? ->
                if (location != null) {
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
        Log.d(TAG, "listener Tab " + mcc + " " + mnc)
        val mcclocal = mcc
        val mnclocal = mnc
        if (mcclocal != null && mnclocal != null) {
            val operador: String = Operators.getOperatorByMnc(mnclocal)
            operadorAnt = operador
            changeButtonsBackgroudColor(operador)
            getSites(operador)
        }
    }


    // Get Books and Update UI
    private fun getSites(operador: String) {
        // start progress bar
        progressBarTab3.visibility = View.VISIBLE
        Tab3tvCargando.text = getString(R.string.Loading)
        // Find operator
        // First load whatever is stored locally
        loadSitesFromLocalDb(operador) {
            var sites : Array<Site> = arrayOf()
            sites = it
            Log.d(TAG, "finish loading local db #sites " + sites.count())
            // check internet connection
            if (internetOn as Boolean)  {
                if (sites.count() == 0) {
                    loadSitesFromFirestore(operador)
                } else {
                    printSites(sites, operador)
                }
            } else {
                progressBarTab3.visibility = View.GONE
                Toast.makeText(this.context, "Sin conexión a internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSitesFromFirestore(operador: String) {
        Log.d(TAG, "load firestore sites operador " + operador)
        var sites : Array<Site> = arrayOf()
        // Internet connection is available, get remote data
        db.collection("sites").document("sites").collection(operador)
            .get()
            .addOnSuccessListener { documents ->
                val tempList = mutableListOf<Site>()
                Tab3tvCargando.text = ""
                for (document in documents) {
                    val site: Site = Site()
                    site.codigo = document.id
                    site.operador = operador
                    site.lon = document.data["long"].toString().replace(",", ".")
                    site.lat = document.data["lat"].toString().replace(",", ".")
                    site.direccion = document.data["direccion"].toString()
                    site.frecuencias = document.data["frecuencias"].toString()
                    tempList.add(site)
                }
                Log.d(TAG, "Found #sites in firestore " + tempList.size)
                sites = tempList.toTypedArray()
                saveSitesToLocalDatabase(sites)
                printSites(sites, operador)
                Log.d(TAG, "sites guardados " + sites.count())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents: ", exception)
            }
    }

    // Load Books from Room
    private fun loadSitesFromLocalDb(operador: String, callback: (Array<Site>) -> Unit) {
        val sitesInteractor: SitesInteractor = sitesInteractor
        // Run in Background, accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            // Get Books
            val sites: Array<Site>? = sitesInteractor.getSiteByOperador(operador)

            Log.d(TAG, "sites local dB " + sites?.count())
            if (sites != null) {
                callback(sites)
            } else callback(arrayOf())
        }
    }

    private fun printSites(sites: Array<Site>, operador: String) {
        activity?.runOnUiThread {
            progressBarTab3.visibility = View.GONE
            Tab3tvCargando.text = ""
            val iconOperatorSelected = Operators.getIconoByOperator(operador)
            Log.d(TAG, "Printing #sites " + sites.size + " site " + sites[1])
            for (i in 1..sites.count() - 1) {
                sites[i].lat?.let { it1 ->
                    sites[i].lon?.let { it2 ->
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(it1.toDouble(), it2.toDouble()))
                                .title(sites[i].codigo)
                                .snippet(sites[i].direccion.toString() + "#" + sites[i].frecuencias.toString())
                                .icon(BitmapDescriptorFactory.fromResource(iconOperatorSelected))
                        )
                    }
                }
            }
        }
    }


    // Save Books to Local Storage
    private fun saveSitesToLocalDatabase(sites: Array<Site>) {
        val sitesInteractor: SitesInteractor = sitesInteractor
        // Run in Background; accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            sitesInteractor.saveSites(*sites)
        }
    }


    private fun changeButtonsBackgroudColor(operador: String) {
        Tab3fbMovistar.backgroundTintList =  ColorStateList.valueOf(resources.getColor(R.color.cpb_grey))
        Tab3fbOrange.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.cpb_grey))
        Tab3fbVodafone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.cpb_grey))
        Tab3fbMasMovil.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.cpb_grey))
        Tab3fbOmv.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.cpb_grey))

        when(operador) {
            "Telefonica" -> Tab3fbMovistar.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            "Orange" -> Tab3fbOrange.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            "Vodafone" -> Tab3fbVodafone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            "MasMovil" -> Tab3fbMasMovil.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            "OMV" -> Tab3fbOmv.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
        }
    }

    fun setButtonOnclickListeners(fragmentView: View) {
        val fabMovistar: FloatingActionButton = fragmentView.findViewById<View>(R.id.Tab3fbMovistar) as FloatingActionButton
        val fabOrange: FloatingActionButton = fragmentView.findViewById<View>(R.id.Tab3fbOrange) as FloatingActionButton
        val fabVodafone: FloatingActionButton = fragmentView.findViewById<View>(R.id.Tab3fbVodafone) as FloatingActionButton
        val fabMasMovil: FloatingActionButton = fragmentView.findViewById<View>(R.id.Tab3fbMasMovil) as FloatingActionButton
        val fabOMV: FloatingActionButton = fragmentView.findViewById<View>(R.id.Tab3fbOmv) as FloatingActionButton

        fabMovistar.setOnClickListener { fabOnClick("Telefonica") }
        fabOrange.setOnClickListener { fabOnClick("Orange") }
        fabVodafone.setOnClickListener { fabOnClick("Vodafone") }
        fabMasMovil.setOnClickListener { fabOnClick("MasMovil") }
        fabOMV.setOnClickListener { fabOnClick("OMV") }
    }


    fun fabOnClick(operador:String) {
        if (operador!=operadorAnt) {
            changeButtonsBackgroudColor(operador)
            mMap.clear()
            getSites(operador)
            operadorAnt = operador
        }
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
    // OnCreate
    // OnCreateView

    override fun onStart() {
        super.onStart()
        Log.d("cfauli", "OnStart Tab3")
    }
    override fun onResume() {
        super.onResume()
        Log.d("cfauli", "OnResume tab3")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("cfauli", "OnDetach Tab3")
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        Log.d("cfauli", "OnStop Tab3")
    }
    override fun onPause() {
        super.onPause()
        Log.d("cfauli", "OnPause Tab3")
    }
    override fun onDestroy() {
        // IMPORTANT! Remove Firestore Change Listener to prevent memory leaks
        sitesListener?.remove()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Tab3"
    }

}