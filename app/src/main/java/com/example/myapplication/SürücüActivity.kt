package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SurucuResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SürücüActivity : AppCompatActivity() {

    private lateinit var txtRotaBilgi: TextView
    private lateinit var btnSevkiyataBasla: Button
    private lateinit var btnTamamla: Button
    private lateinit var btnTamamlananGor: Button

    private lateinit var rvAlanYetkilileri: RecyclerView
    private lateinit var rvDepoYetkilileri: RecyclerView
    private lateinit var rvYukler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surucu)

        initViews()

        val kullaniciId = intent.getStringExtra("CITIZEN_ID")

        if (kullaniciId != "0") {

            surucuPaneliniYukle(kullaniciId)

            btnTamamla.setOnClickListener {
                sevkiyatTamamlaIstegi(kullaniciId)
            }

            btnTamamlananGor.setOnClickListener {
                val intent = Intent(this, TamamlananSurucuActivity::class.java)
                intent.putExtra("kullanici_id", kullaniciId)
                startActivity(intent)
            }

        } else {
            Toast.makeText(this, "Hata: Kullanıcı ID bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {

        txtRotaBilgi = findViewById(R.id.txtRotaKod)
        btnSevkiyataBasla = findViewById(R.id.btnSevkiyataBasla)
        btnTamamla = findViewById(R.id.btnTamamla)
        btnTamamlananGor = findViewById(R.id.btnTamamlananGor)

        rvAlanYetkilileri = findViewById(R.id.rvAlanYetkilileri)
        rvDepoYetkilileri = findViewById(R.id.rvDepoYetkilileri)
        rvYukler = findViewById(R.id.rvYukler)

        rvAlanYetkilileri.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvDepoYetkilileri.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvYukler.layoutManager = LinearLayoutManager(this)
    }

    private fun surucuPaneliniYukle(id: String?) {

        RetrofitClient.api.getSurucuPanelVerileri(id)
            .enqueue(object : Callback<SurucuResponse> {

                override fun onResponse(
                    call: Call<SurucuResponse>,
                    response: Response<SurucuResponse>
                ) {

                    if (response.isSuccessful) {

                        response.body()?.let { data ->

                            txtRotaBilgi.text =
                                "Plaka: ${data.aracBilgi?.plaka}\n${data.rota?.kalkis} -> ${data.rota?.varis}"

                            rvAlanYetkilileri.adapter =
                                YetkiliAdapter(data.yetkililer ?: emptyList())

                            rvDepoYetkilileri.adapter =
                                YetkiliAdapter(data.depo_yetkilileri ?: emptyList())

                            rvYukler.adapter =
                                YukAdapter(data.yukler ?: emptyList())

                            // ✅ SEVKİYATA BAŞLA BUTONU
                            btnSevkiyataBasla.setOnClickListener {

                                sevkiyatBaslatIstegi(
                                    kullaniciId = id,
                                    startLat = data.rota?.startLat,
                                    startLng = data.rota?.startLng,
                                    endLat = data.rota?.endLat,
                                    endLng = data.rota?.endLng
                                )
                            }

                            // ✅ DURUMA GÖRE BUTON KONTROLÜ
                            if (data.aracBilgi?.durum == "SEVKIYATTA") {
                                btnSevkiyataBasla.isEnabled = false
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<SurucuResponse>, t: Throwable) {
                    Log.e("API_ERROR", t.message ?: "Hata")
                }
            })
    }

    // 🚀 SEVKİYAT BAŞLAT + NAVİGASYON
    // 🚀 SEVKİYAT BAŞLAT + NAVİGASYON
    // 🚀 SEVKİYAT BAŞLAT + NAVİGASYON (A'dan B'ye Rota)
    // 🚀 SEVKİYAT BAŞLAT + NAVİGASYON (A'dan B'ye Rota)
    private fun sevkiyatBaslatIstegi(
        kullaniciId: String?,
        startLat: Double?,
        startLng: Double?,
        endLat: Double?,
        endLng: Double?
    ) {
        Log.d("BUTON_TEST", "İstek atılıyor... Koordinatlar: $startLat, $startLng -> $endLat, $endLng")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.sevkiyatBaslat(kullaniciId)

                if (response.isSuccessful) {
                    Toast.makeText(this@SürücüActivity, "Sevkiyat Başlatıldı", Toast.LENGTH_SHORT).show()
                    btnSevkiyataBasla.isEnabled = false

                    // 📍 RESMİ GOOGLE MAPS YOL TARİFİ (DIRECTIONS) FORMATI
                    if (startLat != null && startLng != null && endLat != null && endLng != null) {

                        // Doğrudan Kalkış ve Varış noktalarını içeren resmi format
                        val url = "https://www.google.com/maps/dir/?api=1&origin=$startLat,$startLng&destination=$endLat,$endLng&travelmode=driving"
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(packageManager) != null) {
                            startActivity(mapIntent)
                        } else {
                            // Google Maps uygulaması yoksa tarayıcıda aç
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    } else {
                        Toast.makeText(this@SürücüActivity, "Rota koordinatları eksik!", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@SürücüActivity, "Hata: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BUTON_TEST", "Çökme Hatası: ${e.message}")
                Toast.makeText(this@SürücüActivity, "Bağlantı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sevkiyatTamamlaIstegi(id: String?) {

        RetrofitClient.api.sevkiyatTamamla(id)
            .enqueue(object : Callback<Map<String, String>> {

                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@SürücüActivity,
                            "Sevkiyat Tamamlandı!",
                            Toast.LENGTH_LONG
                        ).show()


                        val intent = Intent(this@SürücüActivity, SürücüActivity::class.java)


                        intent.putExtra("KULLANICI_ID", id)

                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()

                    } else {

                        Toast.makeText(
                            this@SürücüActivity,
                            "Hata oluştu! (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {

                    Toast.makeText(
                        this@SürücüActivity,
                        "İnternet bağlantınızı kontrol edin!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}