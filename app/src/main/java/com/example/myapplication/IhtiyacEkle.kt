package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityIhtiyacEkleBinding
import com.example.myapplication.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IhtiyacEkle : AppCompatActivity() {

    private lateinit var binding: ActivityIhtiyacEkleBinding
    private val apiService = RetrofitClient.api
    private var secilenUrunID: Int = -1
    private var gelenAlanID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIhtiyacEkleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Yönetici Panelinden gönderdiğimiz ALAN_ID'yi alıyoruz
        gelenAlanID = intent.getIntExtra("ALAN_ID", -1)

        // API'den ürünleri çek ve Dropdown'u doldur
        urunleriGetir()

        binding.btnIhtiyacKaydet.setOnClickListener {
            talebıGonder()
        }
    }

    private fun urunleriGetir() {
        apiService.getUrunler().enqueue(object : Callback<List<Urun>> {
            override fun onResponse(call: Call<List<Urun>>, response: Response<List<Urun>>) {
                if (response.isSuccessful) {
                    val urunListesi = response.body() ?: emptyList()

                    // Sadece ürün isimlerini içeren bir liste oluşturuyoruz
                    val urunAdlari = urunListesi.map { it.urunAd }

                    // Dropdown görünümü için basit bir layout kullanıyoruz
                    val adapter = ArrayAdapter(this@IhtiyacEkle, android.R.layout.simple_dropdown_item_1line, urunAdlari)
                    binding.autoCompleteUrunler.setAdapter(adapter)

                    // Kullanıcı listeden bir şey seçtiğinde ID'yi sakla
                    binding.autoCompleteUrunler.setOnItemClickListener { _, _, position, _ ->
                        // Seçilen isme göre listedeki doğru ürünü buluyoruz
                        val secilenUrunAd = urunAdlari[position]
                        val urun = urunListesi.find { it.urunAd == secilenUrunAd }
                        secilenUrunID = urun?.urunID ?: -1
                    }
                }
            }

            override fun onFailure(call: Call<List<Urun>>, t: Throwable) {
                Toast.makeText(this@IhtiyacEkle, "Ürünler yüklenemedi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun talebıGonder() {
        val miktarStr = binding.etMiktar.text.toString()

        if (secilenUrunID == -1 || miktarStr.isEmpty()) {
            Toast.makeText(this, "Lütfen ürün seçin ve miktar girin", Toast.LENGTH_SHORT).show()
            return
        }

        // IhtiyacTalebi modeline urunAdi parametresini boş olarak ekliyoruz (POST isteği için)
        val talep = IhtiyacTalebi(
            urunId = secilenUrunID,
            urunAdi = null,
            miktar = miktarStr.toInt(),
            durum = "Beklemede"
        )

        apiService.toplanmaAlaninaIhtiyacEkle(gelenAlanID, talep).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@IhtiyacEkle, "Talep başarıyla oluşturuldu", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@IhtiyacEkle, "Sunucu hatası: Talep oluşturulamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@IhtiyacEkle, "Bağlantı hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}