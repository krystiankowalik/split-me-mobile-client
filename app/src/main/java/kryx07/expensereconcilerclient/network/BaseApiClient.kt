package kryx07.expensereconcilerclient.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import kryx07.expensereconcilerclient.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.LocalDate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class BaseApiClient<out T>(open val baseUrl: String,
                                serviceClass: Class<T>
) {


    private val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate(json.asString) })
            .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
                val `object` = JsonObject()
                `object`.addProperty("date", src.toString("yyyy-MM-dd"))
                `object`.getAsJsonPrimitive("date")
            })
            .create()


    private fun retrofit(): Retrofit {
        val clientBuilder = OkHttpClient.Builder()

        // Add logging interceptor to see communication between app and server
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        // Add interceptors to OkHttpClient
        clientBuilder.addInterceptor(loggingInterceptor)
        //clientBuilder.followSslRedirects(true)
        // Set timeouts
        clientBuilder.addNetworkInterceptor(StethoInterceptor())
        clientBuilder.connectTimeout(1, TimeUnit.MINUTES)
        clientBuilder.writeTimeout(1, TimeUnit.MINUTES)
        clientBuilder.readTimeout(1, TimeUnit.MINUTES)


        // Create retrofit instance
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build()


    }


    val service = retrofit().create(serviceClass)

}