package lestelabs.antenna.ui.main.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pop_up_settings.*
import lestelabs.antenna.R


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
        this.seekBarDistance.max = 100

        textSeek = getString(R.string.textSampleDistance) + ": " + this.seekBarDistance.progress + "m"
        this.popup_window_text_distance.text = textSeek

        this.popup_window_folder.text = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()


        popup_window_button.setOnClickListener {
            val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.commit()
            finish()
        }
    }

    private fun readInitialConfiguration() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("sharedpreferences", Context.MODE_PRIVATE)
        this.seekBarDistance.progress = sharedPreferences.getInt("num_dist_samples",getString(R.string.minDistSample).toInt())
        this.seekBarTime.progress  = sharedPreferences.getInt("num_time_samples",getString(R.string.minTimeSample).toInt())
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


}



