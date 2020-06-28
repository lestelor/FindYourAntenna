package lestelabs.antenna.ui.main.menu

import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pop_up_settings.*
import lestelabs.antenna.R


class PopUpSettings : AppCompatActivity(), OnSeekBarChangeListener {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up_settings)

        this.seekBarDistance!!.setOnSeekBarChangeListener(this)
        this.seekBarTime!!.setOnSeekBarChangeListener(this)

        seekBarTime.min= 1
        seekBarTime.max = 60
        seekBarTime.progress = 10
        seekBarDistance.min= 5
        seekBarDistance.max = 100
        seekBarTime.progress = 10

        popup_window_button.setOnClickListener {
            finish()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar!!.id) {
            R.id.seekBarDistance -> {
                val text = getString(R.string.textSampleDistance) + ": " + progress.toString() + "m"
                this.popup_window_text_distance.text = text
            }
            R.id.seekBarTime -> {
                val text = getString(R.string.textSampleDistance) + progress.toString() + "s"
                this.popup_window_text_time.text = text
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

}



