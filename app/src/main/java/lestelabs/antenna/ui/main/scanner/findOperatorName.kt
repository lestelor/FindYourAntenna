package lestelabs.antenna.ui.main.scanner

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab3.*
import kotlinx.coroutines.launch



fun findOperatorName(pDevice:DevicePhone?, callback: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val celltower = db.collection("mccmnc")
    var operatorName = ""

    celltower.whereEqualTo("MCC", pDevice?.mcc.toString()).whereEqualTo("MNC", pDevice?.mnc.toString())
        .get()
        .addOnSuccessListener { documents ->
            if (documents != null && documents.size() > 0) {
                val document = documents.first()
                operatorName = document.data["Network"].toString()
                Log.d("cfauli", "findOperatorName " + operatorName )
                callback(operatorName)
            }
        }
        .addOnFailureListener { exception ->
            Log.d("cfauli", "Error getting operator: ", exception)
            callback(operatorName)
        }

}

