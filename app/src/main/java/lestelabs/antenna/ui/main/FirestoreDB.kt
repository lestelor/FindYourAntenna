package lestelabs.antenna.ui.main

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDB {

    fun getFromFirestore(collection: String, document: String, field: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        var output:String? = null


        db.collection(collection).document(document)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    output = task.result?.data!![field].toString()
                    Log.d("cfauli", "SpeedTest output " + output)
                    callback(output)
                } else {
                    Log.d("cfauli", "SpeedTest Error getting Firebase SpeedTestFiles ", task.exception)
                    callback(output)
                }
            }


    }
}