package lestelabs.antenna.ui.main.rest

import android.util.Log
import lestelabs.antenna.ui.main.rest.models.Towers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

fun findTower (openCellIdInterface:OpenCellIdInterface, callback: (Coordenadas) -> Unit) {

    var salir:Boolean = false
    var coordenadas:Coordenadas = Coordenadas()

    openCellIdInterface.openCellIdResponse(214,3,12834060, 2426).enqueue( object :
        Callback<Towers> {
        override fun onResponse(call: Call<Towers>, response: Response<Towers>) {
            Log.d("cfauli","Onresponse 1 " + response.body()?.result)
            try {
                System.out.println(response.body())
                Log.d("cfauli","Onresponse 2 " + response.body()?.data?.lat + ", " +  response.body()?.data?.lon )
                coordenadas.lat = response.body()?.data?.lat
                coordenadas.lon = response.body()?.data?.lon
                callback(coordenadas)

            } catch (e: IOException) {
                e.printStackTrace()
                coordenadas.lat = -1000.0
                coordenadas.lon = -1000.0

            }
        }

        override fun onFailure(call: Call<Towers>, t: Throwable) {
            Log.e("cfauli","RetrofitFailure")
            coordenadas.lat = -1000.0
            coordenadas.lon = -1000.0

        }
    })


}