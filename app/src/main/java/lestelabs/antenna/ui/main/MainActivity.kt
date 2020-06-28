package lestelabs.antenna.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.menu.PopUpSettings



class MainActivity : AppCompatActivity(), Tab1.OnFragmentInteractionListener, Tab2.OnFragmentInteractionListener , FetchCompleteListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: View? = null

    @RequiresApi(Build.VERSION_CODES.P)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        //val toolbar = findViewById<Toolbar>(R.id.toolbar)

    // Floating button

    fab.setOnClickListener { view ->
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }
        //setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(true)
        //supportActionBar?.setDisplayUseLogoEnabled(true)
        val drawerLayout = findViewById<View>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout as DrawerLayout?, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)






    // Navigation menu



    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        Thread.sleep(1000)
    }

    checkAllPermission(){
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_1))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_2))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    fab.hide()
                } else {
                    fab.show()
                }
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


    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        val title: Int
        when (p0.getItemId()) {
            R.id.navSettings -> {
                val intent = Intent(this, PopUpSettings::class.java)
                intent.putExtra("popuptitle", "Error")
                intent.putExtra("popuptext", "Sorry, that email address is already used!")
                intent.putExtra("popupbtn", "OK")
                intent.putExtra("darkstatusbar", false)
                startActivity(intent)
                p0.isChecked = true
                //(drawerLayout as DrawerLayout).closeDrawer(GravityCompat.START)
            }
            else -> throw IllegalArgumentException("menu option not implemented!!")
        }
        return true
    }

}










