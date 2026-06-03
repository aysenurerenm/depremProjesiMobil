package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.AlandakullanicikaydiBinding

import com.example.myapplication.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KullaniciKayitActivity : AppCompatActivity() {

    private lateinit var binding: AlandakullanicikaydiBinding
    private val apiService by lazy { RetrofitClient.api }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AlandakullanicikaydiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Durum Seçeneklerini Hazırla (Dropdown/Spinner)
        setupDurumSpinner()

        // 2. Kaydet Butonu Tıklama Olayı
        binding.btnKullaniciKaydet.setOnClickListener {
            kullaniciyiSistemeKaydet()
        }
    }

    private fun setupDurumSpinner() {
        // Kullanıcının göreceği metinler
        val durumlar = arrayOf("SAFE (Güvendeyim)", "DANGER (Yardım Lazım)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, durumlar)
        binding.autoCompleteDurum.setAdapter(adapter)
    }

    private fun kullaniciyiSistemeKaydet() {
        val ad = binding.etKullaniciAd.text.toString().trim()
        val soyad = binding.etKullaniciSoyad.text.toString().trim()
        val tc = binding.etKullaniciTC.text.toString().trim()
        val aciklama = binding.etKullaniciAciklama.text.toString().trim()
        val secilenMetin = binding.autoCompleteDurum.text.toString()

        // Validasyonlar
        if (ad.isEmpty() || soyad.isEmpty()) {
            Toast.makeText(this, "Lütfen ad ve soyad giriniz", Toast.LENGTH_SHORT).show()
            return
        }

        if (tc.length != 11) {
            Toast.makeText(this, "TC Kimlik No 11 haneli olmalıdır", Toast.LENGTH_SHORT).show()
            return
        }

        // Arayüzdeki seçimi veritabanı formatına (SAFE/DANGER) dönüştür
        val veritabaniDurumu = if (secilenMetin.contains("DANGER")) "DANGER" else "SAFE"

        // Data class nesnesini oluştur
        val yeniKayit = SahaKullaniciKayit(
            ad = ad,
            soyad = soyad,
            tc = tc,
            durum = veritabaniDurumu,
            aciklama = aciklama
        )

        // API İsteği Gönder
        apiService.sahaKullaniciKaydi(yeniKayit).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val basariMesaji = response.body()?.message ?: "Kayıt başarıyla oluşturuldu."
                    Toast.makeText(this@KullaniciKayitActivity, basariMesaji, Toast.LENGTH_LONG).show()

                    // İşlem başarılıysa dashboard'a geri dön
                    finish()
                } else {
                    // Genellikle TC mükerrer olduğunda buraya düşer
                    Toast.makeText(this@KullaniciKayitActivity, "Hata: Kayıt oluşturulamadı (TC zaten var olabilir).", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@KullaniciKayitActivity, "Bağlantı hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}