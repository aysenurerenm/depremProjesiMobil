package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.network.LoginResponse
import com.example.myapplication.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etTC: TextInputEditText
    private lateinit var etSifre: TextInputEditText
    private lateinit var btnGiris: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Bileşenleri XML ile bağlama
        etTC = findViewById(R.id.etTC)
        etSifre = findViewById(R.id.etSifre)
        btnGiris = findViewById(R.id.btnGirisYap)

        btnGiris.setOnClickListener {
            val tc = etTC.text.toString()
            val sifre = etSifre.text.toString()

            if (tc.length == 11 && sifre.isNotEmpty()) {
                girişSorgula(tc, sifre)
            } else {
                Toast.makeText(this, "TC 11 haneli olmalı ve şifre boş bırakılmamalıdır.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun girişSorgula(tc: String, sifre: String) {
        val loginData = mapOf("tc" to tc, "sifre" to sifre)

        RetrofitClient.api.loginControl(loginData).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        // 1. Hoş geldin mesajını göster
                        val ad = body.ad ?: ""
                        val soyad = body.soyad ?: ""
                        Toast.makeText(
                            this@LoginActivity,
                            "Hoş geldin, $ad $soyad",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d("LOGIN", "Giriş Başarılı: $ad")

                        // 2. Ana sayfaya geçiş yap
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("KULLANICI_ADI", ad)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            body?.message ?: "Giriş bilgileri hatalı",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Sunucu hatası: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LOGIN", "Bağlantı Hatası: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "İnternet bağlantınızı kontrol edin",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }}
