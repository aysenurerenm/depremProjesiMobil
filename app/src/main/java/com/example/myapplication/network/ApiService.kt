package com.example.myapplication.network

import android.R
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
data class LoginRequest(
    val tc: String,
    val sifre: String
)

data class LoginResponse(
    val status: String,
    val kullaniciID: Int?,
    val ad: String?,
    val soyad: String?,
    val rolID: Int?,
    val message: String?

)

data class Alan(
    @SerializedName("alanID")
    val id: Int,

    @SerializedName("mahalleAdı") // Django'daki char field ismiyle aynı olmalı
    val mahalleAdi: String,

    @SerializedName("enlem")
    val enlem: Double?,

    @SerializedName("boylam")
    val boylam: Double?
)
// ApiService.kt
interface ApiService {
    @POST("login/")
    fun loginControl(@Body loginData: Map<String, String>): Call<LoginResponse>

    @GET("alanlar/") // Toplanma alanlarını getiren mevcut fonksiyonunuz
    fun getAlanlar(): Call<List<Alan>>
}

// Yanıtı karşılayacak basit bir model
