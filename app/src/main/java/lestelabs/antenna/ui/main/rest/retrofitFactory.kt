package lestelabs.antenna.ui.main.rest


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


fun  retrofitFactory(): OpenCellIdInterface {



    val retrofit = Retrofit.Builder()
        .baseUrl(APIConstants.API_URL)

        // add other factories here, if needed.

        //.addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(OpenCellIdInterface::class.java)

}