package lestelabs.antenna.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri


import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isInvisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.app_bar_main.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.menu.PopUpSettings

interface GetfileState {
    fun getFileState():Boolean
}

class MainActivity : AppCompatActivity(), Tab1.OnFragmentInteractionListener, Tab3.OnFragmentInteractionListener, Tab2.OnFragmentInteractionListener , FetchCompleteListener, NavigationView.OnNavigationItemSelectedListener, GetfileState {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: View? = null
    private var getFileStateButtonPressed: Boolean = false




override fun onCreate(savedInstanceState: Bundle?) {


    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val navigationView: NavigationView = findViewById(R.id.nav_view)

    //val toolbar = findViewById<Toolbar>(R.id.toolbar)
    val drawerLayout = findViewById<View>(R.id.drawer_layout)
    val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
    val fab = findViewById<View>(R.id.fab) as ImageView

    // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
    MobileAds.initialize(this, "ca-app-pub-8346072505648333~5697526220")

    // Floating button
    fab.setBackgroundResource(R.drawable.ic_diskette)
    fab.setOnClickListener { view ->
        changebutton(view)
        Log.d("cfauli","onclick buttom")
    }


    setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)


        val toggle = ActionBarDrawerToggle(
            this, drawerLayout as DrawerLayout?, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)



    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        Thread.sleep(1000)
    }



    checkAllPermission(){

        tabLayout.addTab(tabLayout.newTab())
        tabLayout.addTab(tabLayout.newTab())
        tabLayout.addTab(tabLayout.newTab())
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        tabLayout.setupWithViewPager(viewPager)
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_speed)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_map)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_coverage)
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                fab.isInvisible = tab.position == 0 || tab.position ==2
                viewPager.currentItem = tab.position

                Log.d("cfauli", "TAB" + tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}


    override fun onFragmentInteraction(uri: Uri?) {
    //TODO("Not yet implemented")
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.view_pager)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }*/


    override fun onBackPressed() {
        if ((drawerLayout as DrawerLayout?)?.isDrawerOpen(GravityCompat.START) == true) {
            (drawerLayout as DrawerLayout?)?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun checkAllPermission(callback: (Boolean) -> Unit) {
        while ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Thread.sleep(1000)
        }
        callback(true)
    }


    fun changebutton(view:View) {
        fab.setBackgroundColor(resources.getColor(R.color.black));
        if (getFileStateButtonPressed) {

            fab.setImageResource(R.drawable.ic_diskette)
            fab.setBackgroundColor(resources.getColor(R.color.colorPrimary))

        } else {
            fab.setImageResource(R.drawable.ic_stop)
            fab.setBackgroundColor(resources.getColor(R.color.black))
        }
        getFileStateButtonPressed = !getFileStateButtonPressed // reverse
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        val title: Int
        when (p0.getItemId()) {
            R.id.navSettings -> {
                val intent = Intent(this, PopUpSettings::class.java)
                //intent.putExtra("popuptitle", "Error")
                startActivity(intent)
                p0.isChecked = false
                //(drawerLayout as DrawerLayout).closeDrawer(GravityCompat.START)
            }
            else -> throw IllegalArgumentException("menu option not implemented!!")
        }
        return true
    }

    override fun getFileState():Boolean {
        return getFileStateButtonPressed
    }

}












