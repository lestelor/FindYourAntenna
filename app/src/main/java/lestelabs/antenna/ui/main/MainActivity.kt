package lestelabs.antenna.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import lestelabs.antenna.R

class MainActivity : AppCompatActivity(), Tab1.OnFragmentInteractionListener,
    Tab2.OnFragmentInteractionListener , FetchCompleteListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_1))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_2))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter =
            PagerAdapter(
                supportFragmentManager,
                tabLayout.tabCount
            )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })





    }


    override fun onFragmentInteraction(uri: Uri?) {
        //TODO("Not yet implemented")

    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}