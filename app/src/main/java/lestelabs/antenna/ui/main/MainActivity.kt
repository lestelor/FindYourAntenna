package lestelabs.antenna.ui.main



import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.app_bar_main.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.crashlytics.Crashlytics
import lestelabs.antenna.ui.main.scanner.DevicePhone
import lestelabs.antenna.ui.main.scanner.loadCellInfo

import lestelabs.antenna.ui.main.scanner.waitGPS

interface GetfileState {
    fun getFileState():List<Int>
}


 class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener, NavigationView.OnNavigationItemSelectedListener, GetfileState {

     private var getFileStateButtonPressed: Int = 0
     private lateinit var toggle: ActionBarDrawerToggle
     private var tabSelectedInt: Int = -1

     private var mFileList: Array<String>? = null
     private var mChosenFile: String? = null
     private val DIALOG_LOAD_FILE = 1000
     var gps_enabled:Boolean = false


     private lateinit var checkBox: CheckBox

     private lateinit var firebaseAnalytics: FirebaseAnalytics

     private lateinit var telephonyManager: TelephonyManager
     private var pDevice:DevicePhone? = DevicePhone()


     private val tabName = "MainActivity"
     private var crashlyticsKeyAnt = ""

     private lateinit var viewPager:ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // In order to show custom items in the navigationview menu
        navigationView.itemIconTintList = null

        // Control point for Crashlitycs
        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        // try catch the error
        //Caused by android.content.res.Resources$NotFoundException
        //Resource ID #0x7f0700a9 -> ic_settings
        try {
            navigationView.menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_email))
            navigationView.menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_speed_dark))
            navigationView.menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_network_dark))

            navigationView.menu.getItem(4).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_contribute))
            navigationView.menu.getItem(5).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_exit))

            // Control point for Crashlitycs
            crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        } catch (e: FileSystemException) {
            // Control point for Crashlitycs
            crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
            Log.d("cfauli", "error setting menu images e:", e)
        } catch (ex: Exception) {
            // Control point for Crashlitycs
            crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
            Log.d("cfauli", "error setting menu images ex:", ex)
        } catch (npe: NullPointerException) {
            // Control point for Crashlitycs
            crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
            Log.d("cfauli", "error setting menu images npe:", npe)
        }

        Log.d("cfauli", R.drawable.ic_settings.toString())
        Log.d("cfauli", R.drawable.ic_email.toString())
        Log.d("cfauli", R.drawable.ic_speed_dark.toString())
        Log.d("cfauli", R.drawable.ic_network_dark.toString())
        Log.d("cfauli", R.drawable.ic_map_dark.toString())
        //Log.d("cfauli", R.drawable.gsmantenas.toString())
        Log.d("cfauli", R.drawable.ic_contribute.toString())
        Log.d("cfauli", R.drawable.ic_exit.toString())


        // Control point for Crashlitycs
        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)

        //val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout?
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713

        MobileAds.initialize(this)
        RequestConfiguration.Builder().setTestDeviceIds(listOf("9E4FF26E78ADE9DFD73A3F51A0D208CA"))
        //val adRequest  =  AdRequest.Builder()
        //adRequest.addTestDevice("9E4FF26E78ADE9DFD73A3F51A0D208CA")



        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)


        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        /*Cast MenuItem to CheckBox
          CheckBox checkBox= (CheckBox) menuItem.getActionView();
          */
        Log.d(
            "cfauli", "submenu" + "0- " + navigationView.menu.getItem(0).toString()
                    + "1- " + navigationView.menu.getItem(1).toString()
                    + "2- " + navigationView.menu.getItem(2).toString()
                    + "3- " + navigationView.menu.getItem(3).toString()
                    + "4- " + navigationView.menu.getItem(4).toString()
                    + "5- " + navigationView.menu.getItem(5).toString()
        )
        checkBox = navigationView.menu.getItem(4).actionView as CheckBox

        //getApplicationContext() offers application context, destroys when close the app
        //getBaseContext() offers activity context, destroys when Ondestroy
        // So although this (for Activity) and getBaseContext() both give the activity context, they
            //(a) do not refer to the same object (this != getBaseContext()) and
            //(b) calling context through this is slightly less efficient, as the calls go through an extra level of indirection. I doubt it makes any practical difference, though.
        // context returns a nullable value while requirecontext() returns an exception when null
        // then, it is more reliant using applicationcontext for sharedprefferences between classes
        // nevertheless in this case since the activity destroys the app, basecontext and applicationcontexts are the same.
        //Moreover, this in this case is the same as activity.basecontext

        val sharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val pleaseContribute = sharedPreferences.getBoolean("please_contribute", true)
        checkBox.isChecked = pleaseContribute

        checkBox.setOnClickListener(View.OnClickListener {
            val isSelected = checkBox.isChecked
            Log.d("cfauli", "submenu pleaseContribute " + isSelected)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean("please_contribute", isSelected)
            editor.apply()
            editor.commit()
            if (isSelected) Toast.makeText(baseContext, getString(R.string.ThanksForChecking), Toast.LENGTH_LONG).show()
            else Toast.makeText(baseContext, getString(R.string.ThankForNotChecking), Toast.LENGTH_LONG).show()
        })


        Log.d("cfauli", "MainApplication waiting permissions device")
        // wait until all permissions cleared
        checkAllPermission(this) {}
        // needed to wait until location is enabled, otherwhise the wifi networks dont appear
        waitGPS(this)
        Log.d("cfauli", "MainApplication waiting permissions ok 0 ")
        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pDevice = loadCellInfo(telephonyManager)








        tabLayout.addTab(tabLayout.newTab())
        tabLayout.addTab(tabLayout.newTab())
        if (pDevice?.mcc == 214) {
            tabLayout.addTab(tabLayout.newTab())
        }

        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        tabLayout.setupWithViewPager(viewPager)
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))


        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_speed)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_coverage)

        if (pDevice?.mcc == 214) {
            tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_map)
            navigationView.menu.getItem(3).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_dark))
        } else {
            navigationView.menu.getItem(3).isVisible = false
        }


        // Define the number of adjacent TABs that are preloaded. Cannot be set to 0.
        // Lifecycle:
        // OnAttach/Oncreate/OnCreateview/OnStart/OnResume
        // Back  -> OnPause/OnStop/OnDetach -> OnAttach/OnCreate/OnCreateView/OnStart/OnResume
        // Home -> OnPause/OnStop -> OnStart/OnResume
        // Since Back is implementd as home button, the oncreateview is only trigered once.
        // offscreenlimit =1 means that only the adjacent tab is preloaded, =2 all tabs are preloaded (they do not go to onpause when deselected)
        // Choose =2 since =1 leaks and performs bad. Keep an eye on battery performance.

//        if (pDevice?.mcc == 214) {
//            viewPager.offscreenPageLimit = 3
//        } else {
//            viewPager.offscreenPageLimit = 2
//        }

        // always 1 since we do not want to wait foe sites to be downloaded. If tab2 pressed then change to 2
        viewPager.offscreenPageLimit = 1

        // Control point for Crashlitycs
        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)


        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                tabSelectedInt = tab.position
                if (tab.position==2) viewPager.offscreenPageLimit = 2
                viewPager.currentItem = tab.position
                tab.select()

                Log.d("cfauli", "TAB" + tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }


    /*override fun onFragmentInteraction(uri: Uri?) {
    //TODO("Not yet implemented")
    }*/
    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }*/

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        when (p0.itemId) {

            R.id.nav_email -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("shinnystar.labs@gmail.com"))
                intent.putExtra(Intent.EXTRA_TEXT, "Comments for cid:" + pDevice?.cid.toString() + "\n")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Comments Network Test")

                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, "Share using:"));
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
            R.id.nav_contribute -> {
                val sharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                val isSelected = !checkBox.isChecked
                checkBox.isChecked = isSelected
                editor.putBoolean("please_contribute", isSelected)
                editor.apply()
                editor.commit()
            }
            R.id.nav_exit -> {
                finish();
                System.exit(0);
            }

            else -> throw IllegalArgumentException("menu option not implemented!!")
        }


        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        return true
    }

    override fun getFileState():List<Int> {
        val outputListener: MutableList<Int> = mutableListOf(0, 0, 0, 0)
        outputListener[0] = getFileStateButtonPressed
        outputListener[1] = tabSelectedInt
        pDevice?.mcc?.let{
            outputListener[2] = it
        }
        pDevice?.mnc?.let{
            outputListener[3] = it
        }
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
            // Here is the code for starting HomeActivity
            Log.d("cfauli", "gps main finishactivity")
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
        } else {
            //val startGps = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
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

             /*try {
                 gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                 if (gps_enabled) {
                     Log.d("cfauli", "gps enabled")
                 } else Log.d("cfauli", "gps NOT enabled")

             } catch (ex: Exception) {
             }*/
         }
     }

 }

