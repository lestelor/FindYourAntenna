package lestelabs.antenna.ui.main.map

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import lestelabs.antenna.R


class MyInfoWindowAdapter(private val context: Fragment) : InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = context.layoutInflater.inflate(R.layout.custominfowindow, null)
        val textViewTitle = view.findViewById<View>(R.id.text_title) as TextView
        val textViewSubTitle1 = view.findViewById<View>(R.id.text_subTitle1) as TextView
        val textViewSubTitle2 = view.findViewById<View>(R.id.text_subTitle2) as TextView
        val textViewSubTitle3 = view.findViewById<View>(R.id.text_subTitle3) as TextView

        textViewTitle.text = marker.title
        textViewSubTitle1.text = "marker.snippet"
        return view
    }
//
//    //set custom infowindow adapter
//    MyInfoWindowAdapter adapter = new MyInfoWindowAdapter(MapsActivity.this)
//    map.setInfoWindowAdapter(adapter)
//
//    map.addMarker(markerOpt).showInfoWindow();
}