package lestelabs.antenna.ui.main.rest



import com.squareup.okhttp.ResponseBody
import lestelabs.antenna.ui.main.rest.models.Towers
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenCellIdInterface {

    @GET("/geolocation/cell?v=1.1&data=open")
    fun openCellIdResponse(
        @Query("mcc") mcc: Int,
        @Query("mnc") mnc: Int,
        @Query("cellid") cellid: Int,
        @Query("lac") lac: Int): Call<Towers>

}