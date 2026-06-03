package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Sevkiyat
import com.example.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SevkiyatActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    // private lateinit var progressBar: ProgressBar // Eğer layout'a eklersen bunu aktif et
    private var sevkiyatListesi: MutableList<Sevkiyat> = mutableListOf()
    private var adapter: SevkiyatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sevkiyat)

        // UI Bileşenlerini Hazırla
        setupRecyclerView()
        // progressBar = findViewById(R.id.progressBarSevkiyat) // Layout'ta varsa

        // Intent'ten gelen veri kontrolü
        val kullaniciId = intent.getStringExtra("KULLANICI_ID")

        if (kullaniciId != "0") {
            fetchSevkiyatData(kullaniciId)
        } else {
            showToast("Hata: Kullanıcı kimliği doğrulanamadı.")
            finish()
        }
    }

    private fun setupRecyclerView() {
        rv = findViewById(R.id.rvSevkiyat)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)

        // Adapter'ı boş bir liste ile bir kez başlatıyoruz
        adapter = SevkiyatAdapter(sevkiyatListesi)
        rv.adapter = adapter
    }

    private fun fetchSevkiyatData(userId: String?) {
        // progressBar.visibility = View.VISIBLE // Yükleme başlayınca göster
        Log.d("SevkiyatActivity", "Veri çekiliyor... Kullanıcı: $userId")

        RetrofitClient.api.getSevkiyatListesi(userId)
            .enqueue(object : Callback<List<Sevkiyat>> {

                override fun onResponse(
                    call: Call<List<Sevkiyat>>,
                    response: Response<List<Sevkiyat>>
                ) {
                    // progressBar.visibility = View.GONE // İşlem bitince gizle

                    if (response.isSuccessful) {
                        val yeniListe = response.body() ?: emptyList()

                        if (yeniListe.isEmpty()) {
                            showToast("Şu an aktif bir sevkiyat bulunmuyor.")
                        }

                        // Mevcut listeyi temizle ve yenisini ekle (Adapter'ı tekrar oluşturmaya gerek yok)
                        sevkiyatListesi.clear()
                        sevkiyatListesi.addAll(yeniListe)
                        adapter?.notifyDataSetChanged()

                        Log.i("SevkiyatActivity", "Başarılı: ${yeniListe.size} kayıt listelendi.")
                    } else {
                        handleError(response.code())
                    }
                }

                override fun onFailure(call: Call<List<Sevkiyat>>, t: Throwable) {
                    // progressBar.visibility = View.GONE
                    Log.e("SevkiyatActivity", "Ağ Hatası: ${t.message}")
                    showToast("Bağlantı hatası: Lütfen internetinizi kontrol edin.")
                }
            })
    }

    private fun handleError(code: Int) {
        val msg = "Sunucu Hatası ($code)"
        Log.e("SevkiyatActivity", msg)
        showToast(msg)
    }

    private fun showToast(message: String) {
        Toast.makeText(this@SevkiyatActivity, message, Toast.LENGTH_SHORT).show()
    }
}