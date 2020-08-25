package lestelabs.antenna.ui.main.menu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_pop_up_settings.*
import lestelabs.antenna.R
import lestelabs.antenna.ui.main.Constants


class PopUpSettings : AppCompatActivity(), OnSeekBarChangeListener {




    @SuppressLint("ApplySharedPref")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_settings)

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()


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

            editor.putBoolean("chkBoxTowers",isChecked)
            editor.apply()
        }
        val chkBxSamples = findViewById<View>(R.id.pop_up_checkBox_samples) as CheckBox
        chkBxSamples.setOnCheckedChangeListener { buttonView, isChecked ->

            editor.putBoolean("chkBoxSamples",isChecked)
            editor.apply()
        }

        val threMobBlack = findViewById<View>(R.id.threMobBlack) as EditText
        threMobBlack.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_mob_black", threMobBlack.text.toString())
            editor.apply()

        }
        val threMobRed = findViewById<View>(R.id.threMobRed) as EditText
        threMobRed.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_mob_red", threMobRed.text.toString())
            editor.apply()

        }
        val threMobYellow = findViewById<View>(R.id.threMobYellow) as EditText
        threMobYellow.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_mob_yellow", threMobYellow.text.toString())
            editor.apply()

        }


        val threWifiBlack = findViewById<View>(R.id.threWifiBlack) as EditText
        threWifiBlack.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_wifi_black", threWifiBlack.text.toString())
            editor.apply()

        }
        val threWifiRed = findViewById<View>(R.id.threWifiRed) as EditText
        threWifiRed.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_wifi_red", threWifiRed.text.toString())
            editor.apply()

        }
        val threWifiYellow = findViewById<View>(R.id.threWifiYellow) as EditText
        threWifiYellow.doOnTextChanged { text, start, before, count ->

            editor.putString("thres_wifi_yellow", threWifiYellow.text.toString())
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

        val threMobBlack = findViewById<View>(R.id.threMobBlack) as TextView
        val threMobRed = findViewById<View>(R.id.threMobRed) as TextView
        val threMobYellow = findViewById<View>(R.id.threMobYellow) as TextView
        val threMobGreen = findViewById<View>(R.id.threMobGreen) as TextView
        val threWifiBlack = findViewById<View>(R.id.threWifiBlack) as TextView
        val threWifiRed = findViewById<View>(R.id.threWifiRed) as TextView
        val threWifiYellow = findViewById<View>(R.id.threWifiYellow) as TextView
        val threWifiGreen = findViewById<View>(R.id.threWifiGreen) as TextView

        threMobBlack.text = sharedPreferences.getString("thres_mob_black",Constants.MINMOBILESIGNALBLACK)
        threMobRed.text = sharedPreferences.getString("thres_mob_red",Constants.MINMOBILESIGNALRED)
        threMobYellow.text = sharedPreferences.getString("thres_mob_yellow",Constants.MINMOBILESIGNALYELLOW)
        threMobGreen.text = Constants.MINMOBILESIGNALGREEN

        threWifiBlack.text = sharedPreferences.getString("thres_wifi_black",Constants.MINWIFISIGNALBLACK)
        threWifiRed.text = sharedPreferences.getString("thres_wifi_red",Constants.MINWIFISIGNALRED)
        threWifiYellow.text = sharedPreferences.getString("thres_wifi_yellow",Constants.MINWIFISIGNALYELLOW)
        threWifiGreen.text = Constants.MINWIFISIGNALGREEN
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




