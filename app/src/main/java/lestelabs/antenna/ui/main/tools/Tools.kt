package lestelabs.antenna.ui.main.tools

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.rpc.context.AttributeContext
import lestelabs.antenna.R
import java.util.*


class Tools {

    private lateinit var mAdView: AdView

    fun loadAdds(fragmentView: View, RidAdds:Int) {
        mAdView = fragmentView.findViewById(RidAdds)
        MobileAds.initialize(fragmentView.context)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun getScreenSize(activity: Activity): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return Pair(height,width)
    }

    fun format(resources: Resources, d: Double): String {
        var l: Locale? = null
        l = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        if (d < 10) return java.lang.String.format(l, "%.2f", d)
        return if (d < 100) java.lang.String.format(l, "%.1f", d) else "" + Math.round(d)
    }

    fun mbpsToGauge(s: Double): Int {
        return (1000 * (1 - 1 / Math.pow(1.3, Math.sqrt(s)))).toInt()
    }

    fun hideKeyboard(activity: Activity, v:View, cerrar:Boolean) {
        val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (cerrar) imm.hideSoftInputFromWindow(v.windowToken, 0)
        else imm.showSoftInput(v, InputMethodManager.SHOW_FORCED)
    }
}
