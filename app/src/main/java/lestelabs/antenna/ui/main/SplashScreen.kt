package lestelabs.antenna.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {

    // Welcome screen. The duration is set as an illustrative example on how to create a delay.
    // This activity is defined in the Manifest as the first activity to be launched
    // In the styles.xml is defined @style/AppTheme.SplashScreen setting as background the UOC picture

    private val DURACION_SPLASH = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, DURACION_SPLASH.toLong())
    }
}
