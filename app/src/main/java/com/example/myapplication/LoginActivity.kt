package com.example.myapplication

import android.content.Context
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

        etTC = findViewById(R.id.etTC)
        etSifre = findViewById(R.id.etSifre)
        btnGiris = findViewById(R.id.btnGirisYap)

        btnGiris.setOnClickListener {
            val tc = etTC.text.toString().trim()
            val sifre = etSifre.text.toString().trim()

            if (tc.length == 11 && sifre.isNotEmpty()) {
                girişSorgula(tc, sifre)
            } else {
                Toast.makeText(
                    this,
                    "TC 11 haneli olmalı ve şifre boş bırakılmamalıdır.",
                    Toast.LENGTH_SHORT
                ).show()
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
                        val ad = body.ad ?: ""
                        val soyad = body.soyad ?: ""
                        val kullaniciID = body.kullaniciID
                        val rolID = body.rolID

                        Toast.makeText(
                            this@LoginActivity,
                            "Hoş geldin, $ad $soyad",
                            Toast.LENGTH_LONG
                        ).show()

                        // 1. Kimlik doğrulama kontrolü
                        if (kullaniciID != null) {
                            val temizId = kullaniciID.toString().trim()

                            // 2. SharedPreferences kaydı (Worker için hayati alan)
                            val sharedPref =
                                getSharedPreferences("UygulamaPrefs", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("kullaniciID", temizId).apply()

                            // 3. Intent yönlendirmesi
                            var intent: Intent? = null
                            if (rolID == 1) {
                                intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
                            } else if (rolID == 2) {
                                intent =
                                    Intent(this@LoginActivity, AlanYoneticisiActivity::class.java)
                            } else if (rolID == 3) {
                                intent = Intent(this@LoginActivity, DepoActivity::class.java)
                            } else if (rolID == 4) {
                                intent = Intent(this@LoginActivity, SürücüActivity::class.java)
                            }

                            if (intent != null) {
                                intent.putExtra("CITIZEN_ID", temizId)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Yetkisiz rol: $rolID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            Log.d("LOGIN", "Giriş Başarılı: ID=$temizId, Rol=$rolID")

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Kullanıcı ID sunucudan boş döndü!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // status == "success" değilse hatalı şifre/tc uyarısı
                        val msg = body?.message ?: "Giriş bilgileri hatalı"
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                } else {
                    // response.isSuccessful değilse (Örn: 400, 404, 500)
                    try {
                        // Sunucudan gelen hata JSON metnini alıyoruz (Örn: {"status":"error", "message":"Şifre Hatalı"})
                        val errorRawString = response.errorBody()?.string()

                        if (!errorRawString.isNullOrEmpty()) {
                            val jsonObject = org.json.JSONObject(errorRawString)
                            val sunucuHataMesaji =
                                jsonObject.optString("message", "Giriş başarısız.")

                            // Mobil ekranda doğrudan "Şifre Hatalı" veya "Kullanıcı Bulunamadı" yazar
                            Toast.makeText(this@LoginActivity, sunucuHataMesaji, Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Hata Kodu: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        // JSON ayrıştırılamazsa fallback olarak HTTP kodunu göster
                        Toast.makeText(
                            this@LoginActivity,
                            "Sunucu Hatası: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } // onResponse metodu kapanışı

            // Durum 3: İnternet Yok / Sunucu Çökük (Ağ Hatası)
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LOGIN", "Bağlantı Hatası: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Sunucuya bağlanılamadı. İnternetinizi kontrol edin.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}