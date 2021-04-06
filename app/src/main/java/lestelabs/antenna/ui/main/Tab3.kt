package lestelabs.antenna.ui.main


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.scanner.loadCellInfo


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab4.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab3 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fragmentView: View
    private lateinit var webview: WebView

    private lateinit var telephonyManager:TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = inflater.inflate(R.layout.fragment_tab3, container, false)
        // Inflate the layout for this fragment


        telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val pDevice = loadCellInfo(telephonyManager)
        findUrl(requireActivity() as MainActivity, pDevice) { url: String ->
            webview = fragmentView.findViewById(R.id.myWebView) as WebView
            //next line explained below
            //next line explained below
            webview.setWebViewClient(MyWebViewClient())
            webview.getSettings().setJavaScriptEnabled(true)
            webview.settings.domStorageEnabled = true
            webview.loadUrl(url.toString())

            Log.d("cfauli", "Tab3 " + url)
            val cm = CookieManager.getInstance()
            cm.flush()
        }
        //var url = "https://antenasgsm.com/41.34550920162557/2.123826073262074/15/false,false,true,false,false"
        //url = "http://www.google.com/"
        //url = "Fragment Tab1{8951ffb} (7534be32-cbaa-4e03-ae22-36e850455cf4)} not attached to a context."
        return fragmentView

    }


    class MyWebViewClient : WebViewClient() {

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            //Toast.makeText(this, description?.toCharArray(), Toast.LENGTH_SHORT).show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val cm = CookieManager.getInstance()
                cm.flush()
            } else {
                CookieSyncManager.getInstance().startSync()
            }
        }

    }
}



// fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { location: Location? ->
//
//                    // Control point for Crashlitycs
//                    crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
//
//                    myLocation = location