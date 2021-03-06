package lestelabs.antenna.ui.main


import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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
import com.google.android.gms.maps.model.*
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
import java.lang.reflect.Method
import java.util.*


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
    private val tabName = "Tab3"
    private var crashlyticsKeyAnt = ""
    val db = FirebaseFirestore.getInstance()
    private var sitesListener: ListenerRegistration? = null
    private var listener: GetfileState? = null
    private var operadorAnt: String = ""
    private var sitesAnt: Array<Site> = arrayOf()
    var markerTotal:MutableList<Marker?> = mutableListOf()


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
        mAdView = fragmentView.findViewById(R.id.adViewFragment3)
        MobileAds.initialize(context)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        //initialitze maps
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)

        // Set menu settings
        context?.let { setOperatorPopupMenu(it, fragmentView) }
        editTextSearchOnclickListener(fragmentView)
        backButtonOnclickListener(fragmentView)
        initLayoutSearch(fragmentView)

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
            sitesAnt = sites
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
                Log.d(TAG, "Found #sites in firestore for " + operador+ " " + tempList.size)
                sites = tempList.toTypedArray()
                sitesAnt = sites
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

            Log.d(TAG, "found #sites in local dB for" + operador + " " + sites?.count())
            if (sites != null) {
                callback(sites)
            } else callback(arrayOf())
        }
    }

    private fun printSites(sites: Array<Site>, operador: String) {
        var marker: Marker? = null

        activity?.runOnUiThread {
            Tab3tvCargando.text = ""
            val iconOperatorSelected = Operators.getIconoByOperator(operador)
            Log.d(TAG, "Printing #sites " + sites.size + " site " + sites[0])
            for (i in 0..sites.count() - 1) {
                sites[i].lat?.let { it1 ->
                    sites[i].lon?.let { it2 ->
                        marker = mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(it1.toDouble(), it2.toDouble()))
                                .title(sites[i].codigo)
                                .snippet(sites[i].direccion.toString() + "#" + sites[i].frecuencias.toString() + "#" + sites[i].operador)
                                .icon(BitmapDescriptorFactory.fromResource(iconOperatorSelected))
                        )
                        markerTotal.add(marker)
                    }
                }
            }
            progressBarTab3.visibility = View.GONE
        }
    }

    fun zoomToBounds() {
        var bounds:LatLngBounds? = null
        if (markerTotal.size > 1) {
            val long:Array<Double> = arrayOf(0.0,0.0)
            val lat:Array<Double> = arrayOf(0.0,0.0)
            val long1: Double = markerTotal[0]?.position?.longitude as Double
            val long2: Double = markerTotal[1]?.position?.longitude as Double
            if (long1<long2) {
                long[0] = long1
                long[1] = long2
            }   else {
                long[1] = long1
                long[0] = long2
            }
            val lat1: Double = markerTotal[0]?.position?.latitude as Double
            val lat2: Double = markerTotal[1]?.position?.latitude as Double
            if (lat1<lat2) {
                lat[0] = lat1
                lat[1] = lat2
            }   else {
                lat[1] = lat1
                lat[0] = lat2
            }
            bounds = LatLngBounds(LatLng(lat[0],long[0]), LatLng(lat[1],long[1]))
            for (i in 2..markerTotal.size-1) {
                bounds = bounds?.including(markerTotal[i]?.position)
            }
            Log.d(TAG, "sites bounds 0 " + bounds.toString())
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10))
        } else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerTotal[0]?.position, 16f))
    }


    // Save Books to Local Storage
    private fun saveSitesToLocalDatabase(sites: Array<Site>) {
        val sitesInteractor: SitesInteractor = sitesInteractor
        // Run in Background; accessing the local database is a memory-expensive operation
        AsyncTask.execute {
            sitesInteractor.saveSites(*sites)
        }
    }

    private fun setOperatorPopupMenu(context:Context, fragmentView: View) {
        val menuOperators: Button = fragmentView.findViewById(R.id.Tab3OperatorButton) as Button
        menuOperators.setOnClickListener {
            val popup =  PopupMenu(context, menuOperators)
            popup.inflate(R.menu.operators_menu)
            popup.menu.getItem(0).icon = ContextCompat.getDrawable(context, R.drawable.ic_movistar)
            popup.menu.getItem(1).icon = ContextCompat.getDrawable(context, R.drawable.ic_orange)
            popup.menu.getItem(2).icon = ContextCompat.getDrawable(context, R.drawable.ic_vodafone)
            popup.menu.getItem(3).icon = ContextCompat.getDrawable(context, R.drawable.ic_masmovil)
            popup.menu.getItem(4).icon = ContextCompat.getDrawable(context, R.drawable.ic_omv_green)
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.opheaderTelefonica -> {
                        menuOperatorOnClick("Telefonica")
                    }
                    R.id.opheaderOrange -> {
                        menuOperatorOnClick("Orange")
                    }
                    R.id.opheaderVodafone -> {
                        menuOperatorOnClick("Vodafone")
                    }
                    R.id.opheaderMasMovil -> {
                        menuOperatorOnClick("MasMovil")
                    }
                    R.id.opheaderOMV -> {
                        menuOperatorOnClick("OMV")
                    }
                }

                true
            })


            // show icons on popup menu
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                popup.setForceShowIcon(true)
//            }else{
                try {
                    val fields = popup.javaClass.declaredFields
                    for (field in fields) {
                        if ("mPopup" == field.name) {
                            field.isAccessible = true
                            val menuPopupHelper = field[popup]
                            val classPopupHelper =
                                Class.forName(menuPopupHelper.javaClass.name)
                            val setForceIcons: Method = classPopupHelper.getMethod(
                                "setForceShowIcon",
                                Boolean::class.javaPrimitiveType
                            )
                            setForceIcons.invoke(menuPopupHelper, true)
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            //}
            popup.show()
        }
    }

    fun menuOperatorOnClick(operador:String) {
        if (operador!=operadorAnt) {
            changeOperator(operador)
        }
    }

    fun changeOperator(operador: String) {
        mMap.clear()
        markerTotal = mutableListOf()
        when(operador) {
            "OMV" -> for(i in 0..Operators.mvnos.size-1) {
                getSites(Operators.mvnos[i])
            }
            else -> getSites(operador)
        }
        operadorAnt = operador
    }

    fun backButtonOnclickListener(fragmentView: View) {
        val backButton: Button = fragmentView.findViewById(R.id.Tab3BackButton) as Button
        backButton.setOnClickListener {
            changeOperator(operadorAnt)
        }
    }

    fun initLayoutSearch(fragmentView: View) {
        val editText: EditText = fragmentView.findViewById(R.id.Tab3etSearch) as EditText
        val barraFiltro: LinearLayoutCompat = fragmentView.findViewById(R.id.Tab3LinearLayoutSearch) as LinearLayoutCompat
        barraFiltro.setOnClickListener(View.OnClickListener() {
            Log.d(TAG, "click layout")
            editText.requestFocus()
            editText.isFocusableInTouchMode = true
            //hideKeyboard(editText, false)
        })
    }

    fun editTextSearchOnclickListener(fragmentView: View) {
        val editText: EditText = fragmentView.findViewById(R.id.Tab3etSearch) as EditText

        editText.setOnFocusChangeListener { view, b ->
            Log.d(TAG, "click editText")
            editText.text.clear()
            editText.setTextColor(resources.getColor(R.color.black))
        }

        editText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            val procesado = false
            val sitesAntSize = sitesAnt.size
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                var sitesMostrar = sitesAnt
                // Mostrar mensaje
                val listToSearch: List<String> = v.text.toString().split(" ")
                Log.d(TAG, "sites listtosearch " + listToSearch)
                var sitesFiltered: Array<Site> = arrayOf()

                for (i in 0..listToSearch.size-1) {
                    sitesFiltered = sitesMostrar.filter{ it.codigo == listToSearch[i].toUpperCase(Locale.ROOT)}.toTypedArray()
                    Log.d(TAG, "sites search #sitesfound cod " + sitesFiltered.size)
                    if (sitesFiltered.size>0) sitesMostrar = sitesFiltered
                }
                for (i in 0..listToSearch.size-1) {
                    sitesFiltered = sitesMostrar.filter{ it.direccion.contains(listToSearch[i].toUpperCase(Locale.ROOT))}.toTypedArray()
                    Log.d(TAG, "sites search #sitesfound dir  " + sitesFiltered.size)
                    if (sitesFiltered.size>0) sitesMostrar = sitesFiltered
                }
                for (i in 0..listToSearch.size-1) {
                    sitesFiltered = sitesMostrar.filter{ it.frecuencias.contains(listToSearch[i].toUpperCase())}.toTypedArray()
                    Log.d(TAG, "sites search #sitesfound frec  " + sitesFiltered.size)
                    if (sitesFiltered.size>0) sitesMostrar = sitesFiltered
                }
                if (sitesMostrar.size<sitesAntSize) {
                    hideKeyboard(v, true)
                    mMap.clear()
                    markerTotal = mutableListOf()
                    printSites(sitesMostrar, operadorAnt)
                    zoomToBounds()
                    // zoom to the selected sites
                } else Toast.makeText(context, "Sin coincidencias", Toast.LENGTH_LONG).show()

            }
            procesado
        })

//        editText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable) {}
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//        })
    }
    
    fun hideKeyboard(v:View, cerrar:Boolean) {
        val imm: InputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (cerrar) imm.hideSoftInputFromWindow(v.windowToken, 0)
        else imm.showSoftInput(v, InputMethodManager.SHOW_FORCED)
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