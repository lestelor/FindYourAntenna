package lestelabs.antenna.ui.main.menu

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_pop_up_settings.*
import lestelabs.antenna.R
import java.lang.reflect.Array.get
import java.lang.reflect.Array.getLength
import java.lang.reflect.Method


class PopUpSettings : AppCompatActivity(), OnSeekBarChangeListener {




    @SuppressLint("ApplySharedPref")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_settings)




        this.seekBarDistance!!.setOnSeekBarChangeListener(this)
        this.seekBarTime!!.setOnSeekBarChangeListener(this)


        readInitialConfiguration()
        this.seekBarTime.min= 1
        this.seekBarTime.max = 60

        var textSeek = getString(R.string.textSampleTime) + ": " + this.seekBarTime.progress + "s"
        this.popup_window_text_time.text = textSeek

        this.seekBarDistance.min= 5
        this.seekBarDistance.max = 50

        textSeek = getString(R.string.textSampleDistance) + ": " + this.seekBarDistance.progress + "m"
        this.popup_window_text_distance.text = textSeek

        //this.popup_window_folder.text = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()

        val chkBxTowers = findViewById<View>(R.id.pop_up_checkBox_towers) as CheckBox
        chkBxTowers.setOnCheckedChangeListener { buttonView, isChecked ->
            val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putBoolean("chkBoxTowers",isChecked)
            editor.apply()
        }
        val chkBxSamples = findViewById<View>(R.id.pop_up_checkBox_samples) as CheckBox
        chkBxSamples.setOnCheckedChangeListener { buttonView, isChecked ->
            val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putBoolean("chkBoxSamples",isChecked)
            editor.apply()
        }
        // Finally not implemented due to the restrictions to write in external storage
        /*
        val popFolder = findViewById<View>(R.id.popup_window_folder) as TextView
        popFolder.setOnClickListener {
            performFileSearch()
            }

         */
        /*popup_window_button.setOnClickListener {
            val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.commit()
            finish()
        }*/

    }

    private fun readInitialConfiguration() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        this.pop_up_checkBox_samples.isChecked = sharedPreferences.getBoolean("chkBoxSamples",true)
        this.pop_up_checkBox_towers.isChecked = sharedPreferences.getBoolean("chkBoxTowers",true)
        this.popup_window_folder.text = sharedPreferences.getString("popFolder",getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
        this.seekBarDistance.progress = sharedPreferences.getInt("num_dist_samples",10)
        this.seekBarTime.progress  = sharedPreferences.getInt("num_time_samples",10)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()

        when (seekBar!!.id) {
            R.id.seekBarDistance -> {
                val text = getString(R.string.textSampleDistance) + ": " + progress.toString() + "m"
                this.popup_window_text_distance.text = text
                editor.putInt("num_dist_samples",progress)
                editor.apply()
            }
            R.id.seekBarTime -> {
                val text = getString(R.string.textSampleTime) + ": " + progress.toString() + "s"
                this.popup_window_text_time.text = text
                editor.putInt("num_time_samples",progress)
                editor.apply()
            }
        }

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    fun performFileSearch() {
        // The PopFolder textview is not clickable since this reports an writing error in folders which are different to /storage/emulated/0/Android...
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 0)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull the path using resultData.getData().
            resultData?.data?.also { uri ->

                Log.d("cfauli", "folder " + resultData.data?.path)
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
                val pathString = resultData.data?.path?.replace("/tree/primary:","/storage/emulated/0/")
                this.popup_window_folder.text = pathString
                val editor:SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putString("popFolder",pathString)
                editor.apply()
            }
        }
    }
    override fun onBackPressed() {

            //finishAffinity() // back
            // Implement back as home so as no to trigger oncreateview which would leak memory
        /*
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)*/

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        editor.commit()
        finish()

    }

}




