package com.example.renewed


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginAPI {



    @POST("/r/random.json")
    fun getTop(): Call<String>
  //  https://www.reddit.com/api/v1/authorize?client_id=CLIENT_ID&response_type=TYPE&
  //  state=RANDOM_STRING&redirect_uri=URI&duration=DURATION&scope=SCOPE_STRING
    companion object {

        //private const val BASE_URL = "https://www.reddit.com/api/v1/authorize?"
       private const val BASE_URL = "https://www.reddit.com"
        fun create(): LoginAPI {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)

                .build()
                .create(LoginAPI::class.java)
        }
    }
}