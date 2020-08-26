package lestelabs.antenna.ui.main


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.app_bar_main.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.menu.PopUpSettings
import java.io.File
import java.util.*

interface GetfileState {
    fun getFileState():List<Int>
}


 class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener, NavigationView.OnNavigationItemSelectedListener, GetfileState {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: View? = null
    private var getFileStateButtonPressed: Int = 0
    private lateinit var toggle: ActionBarDrawerToggle
    private var tabSelectedInt: Int = -1

     private var mFileList: Array<String>? = null
     private var mPath: File = File(Environment.DIRECTORY_DOWNLOADS)
     private var mChosenFile: String? = null
     private val FTYPE = ".txt"
     private val DIALOG_LOAD_FILE = 1000
     var gps_enabled:Boolean = false
     private lateinit var lm:LocationManager



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        //val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout?
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout



        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713

        MobileAds.initialize(this)
        RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("9E4FF26E78ADE9DFD73A3F51A0D208CA"))
        val adRequest  =  AdRequest.Builder()
        adRequest.addTestDevice("9E4FF26E78ADE9DFD73A3F51A0D208CA")



        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)


        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)



        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            Thread.sleep(1000)
        }


        checkAllPermission {

            tabLayout.addTab(tabLayout.newTab())
            tabLayout.addTab(tabLayout.newTab())
            tabLayout.addTab(tabLayout.newTab())

            tabLayout.tabGravity = TabLayout.GRAVITY_FILL
            val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
            tabLayout.setupWithViewPager(viewPager)
            val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
            viewPager.adapter = adapter
            viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
            // Define the number of adjacent TABs that are preloaded. Cannot be set to 0.
            // Lifecycle:
            // OnAttach/Oncreate/OnCreateview/OnStart/OnResume
            // Back  -> OnPause/OnStop/OnDetach -> OnAttach/OnCreate/OnCreateView/OnStart/OnResume
            // Home -> OnPause/OnStop -> OnStart/OnResume
            // Since Back is implementd as home button, the oncreateview is only trigered once.
            // offscreenlimit =1 means that only the adjacent tab is preloaded, =2 all tabs are preloaded (they do not go to onpause when deselected)
            // Choose =2 since =1 leaks and performs bad. Keep an eye on battery performance.

            viewPager.offscreenPageLimit = 2

            tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_speed)
            tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_coverage)
            tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_map)

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {

                    tabSelectedInt = tab.position
                    viewPager.currentItem = tab.position
                    tab.select()

                    Log.d("cfauli", "TAB" + tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }


    /*override fun onFragmentInteraction(uri: Uri?) {
    //TODO("Not yet implemented")
    }*/
    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }*/



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

        when (p0.getItemId()) {
            R.id.nav_settings -> {
                val intent = Intent(this, PopUpSettings::class.java)
                //intent.putExtra("popuptitle", "Error")
                startActivity(intent)
                //(drawerLayout as DrawerLayout).closeDrawer(GravityCompat.START)
            }
            R.id.nav_tab1 -> {
                val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
                val tab = tabLayout.getTabAt(0)
                tab!!.select()
            }
            R.id.nav_tab2 -> {
                val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
                val tab = tabLayout.getTabAt(1)
                tab!!.select()
            }
            R.id.nav_tab3 -> {
                val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
                val tab = tabLayout.getTabAt(2)
                tab!!.select()
            }
            R.id.nav_exit -> {
                finish();
                System.exit(0);
            }
            else -> throw IllegalArgumentException("menu option not implemented!!")
        }
        p0.isChecked = false
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        return true
    }

    override fun getFileState():List<Int> {
        val outputListener: MutableList<Int> = mutableListOf(0, 0)
        outputListener[0] = getFileStateButtonPressed
        outputListener[1] = tabSelectedInt
        return outputListener
    }

    override fun onBackStackChanged() {
        toggle.syncState();
    }
    override fun onBackPressed() {

        Log.d("cfauli", "gps lifecycle " + lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED).toString())
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
        else if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){


            //finishAffinity() // back
            // Implement back as home so as no to trigger oncreateview which would leak memory
            Log.d("cfauli", "gps main finishactivity")
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        } else {
            val startGps = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            Log.d("cfauli", "gps finishactivity")
            finishAffinity()
        }
    }




     override fun onCreateDialog(id: Int): Dialog? {
         val dialog: Dialog?
         val builder: AlertDialog.Builder = AlertDialog.Builder(this)
         when (id) {
             DIALOG_LOAD_FILE -> {
                 builder.setTitle("Choose your file")
                 if (mFileList == null) {
                     Log.e("cfauli", "Showing file picker before loading the file list")
                     dialog = builder.create()
                     return dialog
                 }
                 builder.setItems(mFileList) { dialog, which ->
                     mChosenFile = mFileList!![which]
                     //you can do stuff with the file here too
                 }
             }
         }
         dialog = builder.show()
         return dialog
     }
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(view_pager)
    }*/


     override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
         super.onActivityResult(requestCode, resultCode, resultData)

         if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

             Log.d("cfauli", "gps result" + resultData)

             try {
                 gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                 if (gps_enabled) {
                     Log.d("cfauli", "gps enabled")
                 } else Log.d("cfauli", "gps NOT enabled")

             } catch (ex: Exception) {
             }
         }





     }
 }

