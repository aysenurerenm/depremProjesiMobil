package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.TamamlananYardimResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TamamlananActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    // Adapter'ı lateinit tanımlayıp onResponse içinde başlatıyoruz
    private var adapter: TamamlananAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tamamlanan)

        // UI Bileşenlerini Bağlama
        setupRecyclerView()

        // Intent'ten kullanıcı ID'sini al (Varsayılan değer -1)
        val kullaniciID = intent.getStringExtra("KULLANICI_ID")

        if (kullaniciID != "0") {
            verileriGetir(kullaniciID)
        } else {
            showError("Kullanıcı bilgisi alınamadı!")
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewTamamlanan)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Performans için: Listenin boyutu değişmiyorsa true yapabilirsin
        recyclerView.setHasFixedSize(true)
    }

    private fun verileriGetir(kullaniciID: String?) {
        Log.d("TamamlananActivity", "İstek gönderiliyor... KullanıcıID: $kullaniciID")

        RetrofitClient.api
            .getTamamlananYardimlar(kullaniciID)
            .enqueue(object : Callback<List<TamamlananYardimResponse>> {

                override fun onResponse(
                    call: Call<List<TamamlananYardimResponse>>,
                    response: Response<List<TamamlananYardimResponse>>
                ) {
                    if (response.isSuccessful) {
                        val liste = response.body() ?: emptyList()

                        if (liste.isEmpty()) {
                            showError("Henüz tamamlanmış bir yardımınız bulunmuyor.")
                        }

                        // Verileri Adapter'a gönderiyoruz
                        // Artık modelinde 'miktar' ve 'tamamlanmaZamani' olduğu için adapter bunları basacak
                        adapter = TamamlananAdapter(liste)
                        recyclerView.adapter = adapter

                        Log.d("TamamlananActivity", "Başarılı: ${liste.size} kayıt listelendi.")
                    } else {
                        val hataKodu = response.code()
                        Log.e("TamamlananActivity", "Sunucu Hatası: $hataKodu")
                        showError("Sunucu hatası oluştu (Hata: $hataKodu)")
                    }
                }

                override fun onFailure(call: Call<List<TamamlananYardimResponse>>, t: Throwable) {
                    Log.e("TamamlananActivity", "Bağlantı Hatası: ${t.message}")
                    showError("İnternet bağlantınızı kontrol edin.")
                }
            })
    }

    // Toast mesajlarını merkezi bir yerden yönetmek temizlik sağlar
    private fun showError(mesaj: String) {
        Toast.makeText(this@TamamlananActivity, mesaj, Toast.LENGTH_SHORT).show()
    }
}