package lestelabs.antenna.ui.main.rest

import android.util.Log
import com.squareup.okhttp.ResponseBody
import lestelabs.antenna.ui.main.rest.models.Towers
import lestelabs.antenna.ui.main.scanner.DevicePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

fun findTower(openCellIdInterface: OpenCellIdInterface,pDevicePhone: DevicePhone,
              callback: (Coordenadas) -> Unit) {

    var coordenadas:Coordenadas = Coordenadas()



    openCellIdInterface.openCellIdResponse(pDevicePhone.mcc!!, pDevicePhone.mnc!!, pDevicePhone.cid!!, pDevicePhone.lac!!).enqueue(object:Callback<Towers> {
        override fun onResponse(call: Call<Towers>, response: Response<Towers>) {
            if(response.body()!!.result == 200) {
                try {
                    System.out.println(response.body())
                    coordenadas.lat = response.body()?.data?.lat
                    coordenadas.lon = response.body()?.data?.lon
                    Log.d(
                        "cfauli",
                        "Retrofit 200 " + response.body()!!.data.lat.toString() + ", " + response.body()!!.data.lon.toString()
                    )
                    callback(coordenadas)

                } catch (e: IOException) {
                    e.printStackTrace()
                    coordenadas.lat = -1000.0
                    coordenadas.lon = -1000.0
                    callback(coordenadas)
                }
            }
            else {
                coordenadas.lat = -1000.0
                coordenadas.lon = -1000.0
                callback(coordenadas)
            }
        }


        override fun onFailure(call: Call<Towers>, t: Throwable) {
            Log.e("cfauli", "RetrofitFailure")
            coordenadas.lat = -1000.0
            coordenadas.lon = -1000.0
            callback(coordenadas)

        }
    })
    }





