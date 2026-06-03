package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityIhtiyacListeBinding
import com.example.myapplication.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IhtiyacListeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIhtiyacListeBinding
    private lateinit var adapter: IhtiyacAdapter
    private val apiService by lazy { RetrofitClient.api }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIhtiyacListeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val secilenAlanID = intent.getIntExtra("ALAN_ID", -1)
        val durumFiltresi = intent.getStringExtra("DURUM_FILTRESI") ?: "Beklemede"

        binding.tvListeBaslik.text = "$durumFiltresi Talepler"

        binding.root.post {
            if (secilenAlanID != -1) {
                fetchFilteredData(secilenAlanID, durumFiltresi)
            } else {
                Toast.makeText(this, "Hatalı Alan ID!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = IhtiyacAdapter(mutableListOf())
        binding.rvTumIhtiyaclar.layoutManager = LinearLayoutManager(this)
        binding.rvTumIhtiyaclar.setHasFixedSize(true)
        binding.rvTumIhtiyaclar.adapter = adapter
    }

    private fun fetchFilteredData(alanID: Int, filtre: String) {
        Log.d("FILTRE_TEST", "Aranan ID: $alanID, Aranan Filtre: '$filtre'")

        apiService.getAlanIhtiyaclari(alanID).enqueue(object : Callback<List<IhtiyacListesi>> {
            override fun onResponse(call: Call<List<IhtiyacListesi>>, response: Response<List<IhtiyacListesi>>) {
                if (response.isSuccessful) {
                    val tamListe = response.body() ?: emptyList()

                    // KRİTİK DÜZELTME: Gelen listeyi 'filtre' değişkenine göre süzüyoruz.
                    // trim() kullanarak metin başındaki/sonundaki boşlukları temizliyoruz.
                    val filtrelenmisListe = tamListe.filter {
                        it.durum.trim().equals(filtre.trim(), ignoreCase = true)
                    }

                    Log.d("API_TEST", "Toplam: ${tamListe.size}, Filtrelenmiş: ${filtrelenmisListe.size}")

                    // Sadece filtrelenmiş verileri gösteriyoruz
                    adapter.updateList(filtrelenmisListe)

                    if (filtrelenmisListe.isEmpty()) {
                        Toast.makeText(this@IhtiyacListeActivity, "$filtre durumunda talep yok.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<IhtiyacListesi>>, t: Throwable) {
                if (!isFinishing) {
                    Toast.makeText(this@IhtiyacListeActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}