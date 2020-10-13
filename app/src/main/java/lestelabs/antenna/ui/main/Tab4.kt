package lestelabs.antenna.ui.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import lestelabs.antenna.R


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab4.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab4 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fragmentView: View
    private lateinit var webview:WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = inflater.inflate(R.layout.fragment_tab4, container, false)
        // Inflate the layout for this fragment
        var url = param1
        //var url = "https://antenasgsm.com/41.34550920162557/2.123826073262074/15/false,false,true,false,false"
        //url = "http://www.google.com/"
        //url = "Fragment Tab1{8951ffb} (7534be32-cbaa-4e03-ae22-36e850455cf4)} not attached to a context."

        webview = fragmentView.findViewById(R.id.myWebView) as WebView
        //next line explained below
        //next line explained below
        webview.setWebViewClient(MyWebViewClient())
        webview.getSettings().setJavaScriptEnabled(true)
        webview.settings.domStorageEnabled = true
        webview.loadUrl(url.toString())

            val cm = CookieManager.getInstance()
            cm.flush()

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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab4.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab4().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}