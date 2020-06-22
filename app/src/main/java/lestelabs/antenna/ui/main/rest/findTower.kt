package lestelabs.antenna.ui.main.rest

import android.util.Log
import com.squareup.okhttp.ResponseBody
import lestelabs.antenna.ui.main.rest.models.Towers
import lestelabs.antenna.ui.main.scanner.DevicePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

fun findTower(openCellIdInterface: OpenCellIdInterface,mcc:Int,mnc:Int, cellid: Int, lac: Int, neighbourCells: MutableList<DevicePhone>,
              callback: (Coordenadas) -> Unit):List<DevicePhone> {

    var coordenadas:Coordenadas = Coordenadas()
    val totalCellId = mcc.toString() + mnc.toString() + lac.toString() + cellid.toString()
    var devicePhone: DevicePhone? = neighbourCells.find {it.totalCellId == totalCellId }
    val neighbourCells2 = neighbourCells
    if (devicePhone != null) {
        coordenadas.lat = devicePhone.totalCellIdLat
        coordenadas.lon = devicePhone.totalCellIdLon
        callback(coordenadas)
        return neighbourCells2
    } else {

        openCellIdInterface.openCellIdResponse(mcc, mnc, cellid, lac).enqueue(object:Callback<Towers> {
            override fun onResponse(call: Call<Towers>, response: Response<Towers>) {
                if(response.body()!!.result == 200) {
                    try {
                        System.out.println(response.body())
                        coordenadas.lat = response.body()?.data?.lat
                        coordenadas.lon = response.body()?.data?.lon
                        Log.d(
                            "cfauli",
                            "Onresponse 2 " + response.body()
                        )
                        devicePhone = DevicePhone()
                        devicePhone?.totalCellId =
                            mcc.toString() + mnc.toString() + lac.toString() + cellid.toString()
                        devicePhone?.totalCellIdLat = coordenadas.lat
                        devicePhone?.totalCellIdLon = coordenadas.lon
                        neighbourCells2.add(devicePhone!!)
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
        return neighbourCells2
    }

}



