package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.myapplication.network.AlanYetkilisiDurumGuncelleme
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.*

class KullaniciDurumGuncelleActivity : AppCompatActivity() {

    // Görünüm bileşenlerini tanımla
    private lateinit var etTC: EditText
    private lateinit var etAciklama: EditText
    private lateinit var spinnerDurum: Spinner
    private lateinit var btnGuncelle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kullanicidurumguncelleme) // XML dosya adın

        setupViews()
        setupSpinner()

        btnGuncelle.setOnClickListener {
            val tc = etTC.text.toString().trim()
            val durum = spinnerDurum.selectedItem.toString()
            val aciklama = etAciklama.text.toString().trim()

            if (tc.length == 11) {
                guncellemeYap(tc, durum, aciklama)
            } else {
                etTC.error = "TC 11 hane olmalıdır!"
            }
        }
    }

    private fun setupViews() {
        etTC = findViewById(R.id.etTC)
        etAciklama = findViewById(R.id.etAciklama)
        spinnerDurum = findViewById(R.id.spinnerDurum)
        btnGuncelle = findViewById(R.id.btnGuncelle)
    }

    private fun setupSpinner() {
        val durumlar = arrayOf("SAFE","DANGER")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durumlar)
        spinnerDurum.adapter = adapter
    }

    private fun guncellemeYap(tc: String, durum: String, aciklama: String) {
        // Data sınıfını oluştur (Senin yazdığın sınıfla uyumlu olmalı)
        val istek =AlanYetkilisiDurumGuncelleme(tc, durum, aciklama)

        // LifecycleScope kullanmak Coroutine'i yönetmek için en güvenli yoldur
        lifecycleScope.launch {
            try {
                // API Çağrısı
                // Hatalı satırı şununla değiştir:
                val response = RetrofitClient.instance.durumGuncelle(istek)

                if (response.isSuccessful) {
                    Toast.makeText(this@KullaniciDurumGuncelleActivity, "Durum başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                    finish() // Başarılıysa sayfayı kapat
                } else {
                    // Django'dan gelen hata mesajını yakala (Örn: TC bulunamadı)
                    val hataMesaji = response.errorBody()?.string() ?: "Hata oluştu"
                    Toast.makeText(this@KullaniciDurumGuncelleActivity, hataMesaji, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // İnternet yoksa veya server kapalıysa buraya düşer
                Toast.makeText(this@KullaniciDurumGuncelleActivity, "Bağlantı Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}