package lestelabs.antenna.ui.main.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenCellIdInterface {


    @GET("/cell")
    fun openCellIdResponse(@Query("key") mcc: String="9abd6e867e1cf9"): Call<ResponseBody?>?
}