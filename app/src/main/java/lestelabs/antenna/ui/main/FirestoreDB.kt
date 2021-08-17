package lestelabs.antenna.ui.main

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab3.*
import lestelabs.antenna.ui.main.data.Server
import lestelabs.antenna.ui.main.data.Site

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

    fun loadServersFromFirestore(callback: (MutableList<Server>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        var servers : MutableList<Server> = mutableListOf()
        // Internet connection is available, get remote data
        db.collection("servers")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val server: Server = Server()
                    server.id = document.id.toInt()
                    server.name  = document.data["name"].toString()
                    server.server  = document.data["server"].toString()
                    server.dlURL  = document.data["dlURL"].toString()
                    server.ulURL  = document.data["ulURL"].toString()
                    server.pingURL  = document.data["pingURL"].toString()
                    server.getIpURL  = document.data["getIpURL"].toString()
                    server.sponsorName  = document.data["sponsorName"].toString()
                    server.sponsorURL  = document.data["sponsorURL"].toString()
                    servers.add(server)
                }
                Log.d("TAB1", "Found servers in Firestore " + servers.count())
                callback(servers)
            }
            .addOnFailureListener { exception ->
                callback(servers)
                Log.e("TAB1", "Error getting servers: ", exception)
            }
    }
}