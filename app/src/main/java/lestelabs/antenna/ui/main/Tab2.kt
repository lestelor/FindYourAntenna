package lestelabs.antenna.ui.main


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.rest.OpenCellIdInterface
import lestelabs.antenna.ui.main.rest.models.Towers
import lestelabs.antenna.ui.main.rest.retrofitFactory
import lestelabs.antenna.ui.main.scanner.DevicePhone
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


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

    // As indicated in android developers https://developers.google.com/maps/documentation/android-sdk/start
    // Previously it is necessary to get the google API key from the Google Cloud Platform Console
    // (Maps SDK for Android) and store them in the manifest
    // The app build gradle is sync with the maps library

    private lateinit var mMap: GoogleMap
    private lateinit var fragmentView: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var pDevice:DevicePhone
    private lateinit var openCellIdInterface: OpenCellIdInterface



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }


        if (checkPermissionsTel() && checkPermissionsNet()) {
            //telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //pDevice = loadCellInfo(telephonyManager)


            //val text = findCellIdOpenCellId("http://www.opencellid.org/cell/get?key=9abd6e867e1cf9&mcc=214&mnc=3&lac=2426&cellid=12834060")
        }





    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
    fragmentView = inflater.inflate(R.layout.fragment_tab2, container, false)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

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

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val REQUEST_FINE_LOCATION = 1
        private const val PERMISSION_REQUEST_CODE = 1


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

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        //MapsInitializer.initialize(context)

        mMap = googleMap






        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (checkPermissionsLoc()) {
            mMap.isMyLocationEnabled = true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val centroMapa = location?.let { onLocationChanged(it) }
                if (location == null) {
                } else {
                    val mapBounds = LatLngBounds(
                        LatLng(location.latitude - 0.3, location.longitude - 0.3),
                        LatLng(location.latitude + 0.1, location.longitude + 0.1)
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0))
                }


            }
            .addOnFailureListener { e ->
                Log.d("MapDemoActivity", "Error trying to get last GPS location")
                e.printStackTrace()
            }
        /*mMap.addMarker(MarkerOptions()
            .position(marcador)
            .title(llistacoordenades[i].nom)*/

       val openCellIdInterface: OpenCellIdInterface = retrofitFactory()


        openCellIdInterface.openCellIdResponse(214,3,12834060, 2426).enqueue( object :Callback<Towers> {



            override fun onResponse(call: Call<Towers>, response: Response<Towers>) {
                Log.e("cfauli","Onresponse" + response.body()?.result)
                try {
                    System.out.println(response.body())
                    Log.e("cfauli","Onresponse " + response.body()?.data?.lat + ", " +  response.body()?.data?.lon )
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                /*if (response.code() == 200) {
                    //var aa=response.body()!!
                    Log.d("cfauli",response.body()!!.toString())
                    val aaa=1
                }*/
            }

            override fun onFailure(call: Call<Towers>, t: Throwable) {
                Log.e("cfauli","RetrofitFailure")
            }
        })

        /*var aaa=getResponseText(URLTOTAL)*/
        var aaaa=1
    }



    private fun onLocationChanged(location: Location): LatLng {
        // New location has now been determined

        // You can now create a LatLng Object for use with maps
        val latLng = LatLng(location.latitude, location.longitude)
        return latLng
    }

    // Check location permissions, but it is not needed for using google maps
    private fun checkPermissionsLoc(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            requestPermissionsLoc()
            false
        }
    }

    private fun checkPermissionsTel(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED        )
            {
            true
        } else {
            requestPermissionsTel()
            false
        }
    }

    private fun checkPermissionsNet(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED        )
        {
            true
        } else {
            requestPermissionsNet()
            false
        }
    }

    private fun requestPermissionsLoc() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FINE_LOCATION
        )
    }
    private fun requestPermissionsTel() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.READ_PHONE_STATE),
            1
        )
    }

    private fun requestPermissionsNet() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
            1
        )
    }

}


interface FetchCompleteListener {

}
