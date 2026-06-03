package com.example.myapplication.network



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.40.162.199:8000/api/"
    private const val KANDILLI_URL = "https://api.orhanaydogdu.com.tr/deprem/kandilli/"

    // Bu zaten doğru, bunu kullanabilirsin
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // HATA BURADAYDI: Tipini ApiService yapıyoruz ve sonuna .create ekliyoruz
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) // Bu satır hayati!
    }

    // Kandilli için de ayrı bir servis arayüzün varsa (örn: DepremApiService)
    // onu da sonuna .create(DepremApiService::class.java) ekleyerek döndürmelisin
    val kandilliInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(KANDILLI_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}