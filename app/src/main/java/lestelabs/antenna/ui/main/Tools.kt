package lestelabs.antenna.ui.main.ui

import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import lestelabs.antenna.R


class Tools() {


    private lateinit var mAdView: AdView

    fun loadAdds(fragmentView: View, RidAdds:Int) {
        mAdView = fragmentView.findViewById(RidAdds)
        MobileAds.initialize(fragmentView.context)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}
