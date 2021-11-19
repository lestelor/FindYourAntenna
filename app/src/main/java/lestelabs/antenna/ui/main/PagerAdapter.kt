package lestelabs.antenna.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


/**
 * Created by Chirag on 30-Jul-17.
 */

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PagerAdapter(fm: FragmentManager?, var mNoOfTabs: Int) : FragmentStatePagerAdapter(fm!!) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> Tab1()
            1 -> Tab2()
            2 -> Tab3()
            else -> null
        }!!
    }

    override fun getCount(): Int {
        return mNoOfTabs
    }

}