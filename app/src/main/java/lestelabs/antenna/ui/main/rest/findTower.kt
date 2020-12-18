package lestelabs.antenna.ui.main.rest

import android.util.Log
import lestelabs.antenna.ui.main.AppRater
import lestelabs.antenna.ui.main.crashlytics.Crashlytics
import lestelabs.antenna.ui.main.rest.models.Towers
import lestelabs.antenna.ui.main.scanner.DevicePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

fun findTower(
    openCellIdInterface: OpenCellIdInterface, pDevicePhone: DevicePhone?,
    callback: (Coordenadas) -> Unit
) {

    var coordenadas: Coordenadas = Coordenadas()
    coordenadas.lat = -1000.0
    coordenadas.lon = -1000.0

    val tabName = "findTower"
    var crashlyticsKeyAnt = ""

    if (pDevicePhone?.mcc == null || pDevicePhone.mnc == null || pDevicePhone.cid ==null || pDevicePhone.lac == null ) {
        // Control point for Crashlitycs
        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
        callback(coordenadas)
    } else {
        openCellIdInterface.openCellIdResponse(pDevicePhone.mcc!!, pDevicePhone.mnc!!, pDevicePhone.cid!!, pDevicePhone.lac!!).enqueue(object : Callback<Towers> {
            override fun onResponse(call: Call<Towers>, response: Response<Towers>) {
                if (response.body()?.result == 200) {
                    try {
                        //System.out.println(response.body())
                        coordenadas.lat = response.body()?.data?.lat?: -1000.0
                        coordenadas.lon = response.body()?.data?.lon?: -1000.0
                        Log.d("cfauli", "Retrofit 200 ")
                        callback(coordenadas)

                    } catch (e: IOException) {
                        // Control point for Crashlitycs
                        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                        e.printStackTrace()
                        callback(coordenadas)
                    } catch (np: NullPointerException) {
                        // Control point for Crashlitycs
                        crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                        println(np);
                        callback(coordenadas)
                    }
                } else {
                    // Control point for Crashlitycs
                    crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                    callback(coordenadas)
                }
            }


            override fun onFailure(call: Call<Towers>, t: Throwable) {
                // Control point for Crashlitycs
                crashlyticsKeyAnt = Crashlytics.controlPointCrashlytics(tabName, Thread.currentThread().stackTrace, crashlyticsKeyAnt)
                Log.e("cfauli", "RetrofitFailure")
                callback(coordenadas)

            }
        })
    }

}





