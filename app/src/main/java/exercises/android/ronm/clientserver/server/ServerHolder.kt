package exercises.android.ronm.clientserver.server

import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://hujipostpc2019.pythonanywhere.com" // base url always without last slash '/'

object ServerHolder {

    var serverInterface: ServerInterface

    init {
        val client = OkHttpClient()
        val retrofitCreator =
            retrofit2.Retrofit.Builder().client(client).baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(client).build()
        serverInterface = retrofitCreator.create(ServerInterface::class.java)
    }




}