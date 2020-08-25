package lestelabs.antenna.ui.main.rest

/*
Check this site, it's very well explained and simple: https://www.mylnikov.org/archives/1059
Here's an example:
.MCC: 268
.MNC: 06
.LAC: 8280
.CELL ID: 5616
API LINK: https://api.mylnikov.org/geolocation/cell?v=1.1&data=open&mcc=268&mnc=06&lac=8280&cellid=5616
*/

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