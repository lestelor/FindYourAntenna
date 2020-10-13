package lestelabs.antenna.ui.main

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.findOperatorName

/**
 * Created by Chirag on 30-Jul-17.
 */

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PagerAdapter(fm: FragmentManager?, var mNoOfTabs: Int, pDevice:DevicePhone?, myLocation: Location?) : FragmentStatePagerAdapter(fm!!) {
    val location:Location? = myLocation
    val device:DevicePhone? = pDevice
    var texteTrueFalse:String =""

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> Tab1()
            1 -> Tab2()
            2 -> Tab3()
            3 -> {
                Log.d("cfauli", "pagerAdapter " + location?.latitude.toString() +  " " +location?.longitude+ " " + device?.mnc)
                //val url="http://www.google.com"
                var url:String =""
                var operator = ""
                if (location!=null && device !=null) {
                    when (device.mnc) {
                        5,7 -> texteTrueFalse = "true,false,false,false,false"
                        1,18,6 -> texteTrueFalse = "false,true,false,false,false"
                        3,11,19,21,9 -> texteTrueFalse = "false,false,true,false,false"
                        4 -> texteTrueFalse = "false,false,false,true,false"
                        else -> texteTrueFalse = "false,false,false,false,true"
                    }
                    url = "https://antenasgsm.com/" + location.latitude.toString() + "/" + location.longitude.toString() + "/" +
                            "15" + "/" + texteTrueFalse
                    Log.d("cfauli", "pagerAdapter " + location.latitude.toString() +  " " +location.longitude+ " " + device.mnc)
                } else url = "https://antenasgsm.com"


                Tab4().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, url)
                    }
                }
            }
            else -> null
        }!!
    }

    override fun getCount(): Int {
        return mNoOfTabs
    }

}