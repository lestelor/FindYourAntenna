package lestelabs.antenna.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import lestelabs.antenna.R
import java.util.*

object AppRater {
    private const val APP_TITLE = "Network Test" // App Name
    private const val APP_PNAME = "lestelabs.antenna" // Package Name
    private const val SECS_UNTIL_PROMPT = 30 //Min number of days
    private const val LAUNCHES_UNTIL_PROMPT = 0 //Min number of launches



    fun app_launched(mContext: Context) {
        val prefs = mContext.getSharedPreferences("apprater", 0)
        if (prefs.getBoolean("dontshowagain", false)) {
            return
        }
        val editor = prefs.edit()

        // Increment launch counter
        val launch_count = prefs.getLong("launch_count", 0) + 1
        editor.putLong("launch_count", launch_count)

        // Get date of first launch
        var date_firstLaunch = prefs.getLong("date_firstlaunch", 0)
        if (date_firstLaunch == 0L) {
            date_firstLaunch = System.currentTimeMillis()
            editor.putLong("date_firstlaunch", date_firstLaunch)
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                SECS_UNTIL_PROMPT * 1000
            ) {
                showRateDialog(mContext, editor)
            }
        }
        editor.apply()
        editor.commit()
    }

    fun showRateDialog(mContext: Context, editor: SharedPreferences.Editor?) {
        val appRaterResult: MutableMap<String, Any?> = HashMap()
        val dateNow = Date()
        appRaterResult["date"] = DateFormat.format("yyyy/MM/dd HH:mm:ss", dateNow)
        var documentId = DateFormat.format("yyyyMMdd",dateNow).toString() + " " +  DateFormat.format("HHmmss",dateNow).toString()
        val db = FirebaseFirestore.getInstance()

        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(mContext.getString(R.string.Rate) + " " + APP_TITLE)
        builder.setMessage(mContext.getString(R.string.IfYouEnjoy) + " " + APP_TITLE + mContext.getString(R.string.ThaksForSupport))
        builder.setNegativeButton(mContext.getString(R.string.NoThanks)) { dialog, which ->
            documentId = "No;" + documentId
            appRaterResult["voteCasted"] = "No"
            // Add a new document with a generated ID
            db.collection("AppRater").document(documentId)
                .set(appRaterResult)
                .addOnSuccessListener {
                    Log.d("cfauli", "DocumentSnapshot added with ID: " + documentId)
                }
                .addOnFailureListener {
                    Log.d("cfauli", "Error adding document: " + it)
                }
        }
        builder.setPositiveButton("OK") { dialog, which ->
            documentId = "OK;" + documentId
            appRaterResult["voteCasted"] = "OK"
            // Add a new document with a generated ID
            db.collection("AppRater").document(documentId)
                .set(appRaterResult)
                .addOnSuccessListener {
                    Log.d("cfauli", "DocumentSnapshot added with ID: " + documentId)
                }
                .addOnFailureListener {
                    Log.d("cfauli", "Error adding document: " + it)
                }

            val uri = Uri.parse("https://play.google.com/store/apps/details?id=" + APP_PNAME)
            mContext.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        builder.setNeutralButton(mContext.getString(R.string.RemindMeLater)) { dialog, which ->
            documentId = "Remind;" + documentId
            appRaterResult["voteCasted"] = "Remind"
            // Add a new document with a generated ID
            db.collection("AppRater").document(documentId)
                .set(appRaterResult)
                .addOnSuccessListener {
                    Log.d("cfauli", "DocumentSnapshot added with ID: " + documentId)
                }
                .addOnFailureListener {
                    Log.d("cfauli", "Error adding document: " + it)
                }
            if (editor != null) {
                editor.putBoolean("dontshowagain", true)
                editor.commit()
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

        /*val dialog = Dialog(mContext)
        dialog.setTitle(mContext.getString(R.string.Rate) + " " + APP_TITLE)
        val ll = LinearLayout(mContext)
        ll.orientation = LinearLayout.VERTICAL
        val tv = TextView(mContext)
        tv.text = mContext.getString(R.string.IfYouEnjoy) + APP_TITLE + mContext.getString(R.string.ThaksForSupport)
        tv.width = 240
        tv.setPadding(4, 0, 4, 10)
        ll.addView(tv)
        val b1 = Button(mContext)
        b1.text = mContext.getString(R.string.Rate)
        b1.setOnClickListener {
            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)))
            dialog.dismiss()
        }
        ll.addView(b1)
        val b2 = Button(mContext)
        b2.text = mContext.getString(R.string.RemindMeLater)
        b2.setOnClickListener { dialog.dismiss() }
        ll.addView(b2)
        val b3 = Button(mContext)
        b3.text = mContext.getString(R.string.NoThanks)
        b3.setOnClickListener {
            if (editor != null) {
                editor.putBoolean("dontshowagain", true)
                editor.commit()
            }
            dialog.dismiss()
        }
        ll.addView(b3)
        dialog.setContentView(ll)
        dialog.show()*/
    }
}