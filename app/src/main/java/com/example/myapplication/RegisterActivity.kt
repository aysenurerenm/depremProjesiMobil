package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.RegisterBinding
import com.example.myapplication.network.ApiService
import com.example.myapplication.network.ApiResponse
import com.example.myapplication.network.KullaniciKayit
import com.example.myapplication.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnKaydet.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val ad = binding.etAd.text.toString().trim()
        val soyad = binding.etSoyad.text.toString().trim()
        val tc = binding.etTC.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val telefon = binding.etTelefon.text.toString().trim()
        val sifre = binding.etSifre.text.toString()
        val sifreTekrar = binding.etSifreTekrar.text.toString()
        val yasStr = binding.etYas.text.toString().trim()

        // Validasyonlar
        if (ad.isEmpty()) { binding.etAd.error = "Ad alanı boş bırakılamaz"; return }
        if (tc.length != 11) { binding.etTC.error = "TC 11 haneli olmalıdır"; return }
        if (!email.contains("@")) { binding.etEmail.error = "Geçerli bir e-posta giriniz"; return }
        if (sifre != sifreTekrar) { binding.etSifreTekrar.error = "Şifreler eşleşmiyor"; return }

        val kayitVerisi = KullaniciKayit(
            ad = ad, soyad = soyad, tc = tc,
            yas = yasStr.toIntOrNull() ?: 0,
            cinsiyet = binding.rbErkek.isChecked,
            telefon = telefon, email = email, sifre = sifre
        )

        // 71. Satırı bu şekilde güncelle:
        val apiService = RetrofitClient.api

        apiService.registerUser(kayitVerisi).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Hoş geldin!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    try {
                        // Django'dan gelen {"error": "..."} formatını okuyoruz
                        val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                        val mesaj = errorResponse.error ?: "Bu bilgiler zaten kayıtlı."
                        Toast.makeText(this@RegisterActivity, mesaj, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Hata: Bilgiler zaten mevcut.", Toast.LENGTH_SHORT).show()
                    }
                }
            } // onResponse bitti

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}