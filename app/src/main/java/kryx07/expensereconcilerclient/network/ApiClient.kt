package kryx07.expensereconcilerclient.network

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import kryx07.expensereconcilerclient.App
import kryx07.expensereconcilerclient.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(val context: Context) {

    companion object {
        private val BASE_URL = "http://192.168.43.154:8079/me/"
    }

    lateinit var service: TransactionApiService
        private set

    var gson: Gson
    val tokenAuthInterceptor: TokenAuthInterceptor


    init {
        App.appComponent.inject(this)
        gson = Gson()
        tokenAuthInterceptor = TokenAuthInterceptor(context)
        createRetrofit()
    }


    private fun createRetrofit() {
        val clientBuilder = OkHttpClient.Builder()

        // Add logging interceptor to see communication between app and server
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        // Add interceptors to OkHttpClient
        clientBuilder.addInterceptor(loggingInterceptor)
        clientBuilder.addInterceptor(tokenAuthInterceptor)
        // Set timeouts
        clientBuilder.addNetworkInterceptor(StethoInterceptor())
        clientBuilder.connectTimeout(1, TimeUnit.MINUTES)
        clientBuilder.writeTimeout(1, TimeUnit.MINUTES)
        clientBuilder.readTimeout(1, TimeUnit.MINUTES)

        // Create retrofit instance
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build()

        // Create service(interface) instance
        service = retrofit.create(TransactionApiService::class.java)
    }

}
