package lestelabs.antenna.ui.main.rest


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


fun  retrofitFactory(): OpenCellIdInterface {



    val retrofit = Retrofit.Builder()
        .baseUrl(APIConstants.API_URL)

       //.addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(OpenCellIdInterface::class.java)

}