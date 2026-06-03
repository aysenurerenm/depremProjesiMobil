package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityYoneticiPanelBinding
import com.example.myapplication.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlanYoneticisiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYoneticiPanelBinding
    private val apiService: ApiService by lazy { RetrofitClient.api }
    private var secilenAlanID: Int = -1

    // IhtiyacListesi modelini kullanan adapterlar
    private lateinit var bekleyenAdapter: IhtiyacAdapter
    private lateinit var onaylananAdapter: IhtiyacAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoneticiPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()

        val citizenId = intent.getStringExtra("CITIZEN_ID")

        if (citizenId.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı bilgisi eksik!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Yetkili olunan alan bilgisini çek
        apiService.getYetkiliAlan(citizenId).enqueue(object : Callback<AlanResponse> {
            override fun onResponse(call: Call<AlanResponse>, response: Response<AlanResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    secilenAlanID = response.body()!!.alanId
                    fetchDashboardData()
                } else {
                    Toast.makeText(this@AlanYoneticisiActivity, "Yetkili alan bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AlanResponse>, t: Throwable) {
                Toast.makeText(this@AlanYoneticisiActivity, "Bağlantı hatası!", Toast.LENGTH_SHORT).show()
            }
        })

        // Buton Tıklama Dinleyicileri
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnYeniIhtiyacEkle.setOnClickListener {
            if (secilenAlanID != -1) {
                val intent = Intent(this, IhtiyacEkle::class.java)
                intent.putExtra("ALAN_ID", secilenAlanID)
                startActivity(intent)
            }
        }

        binding.tvTumBekleyenler.setOnClickListener {
            startListeActivity("Beklemede")
        }

        binding.tvTumOnaylananlar.setOnClickListener {
            startListeActivity("Onaylandı")
        }

        binding.btnTamamlananlaraGit.setOnClickListener {
            startListeActivity("Tamamlandı")
        }
        binding.btnYeniKullaniciEkle.setOnClickListener {
            // Kayıt sayfasına (KullaniciKayitActivity) yönlendir
            val intent = Intent(this, KullaniciKayitActivity::class.java)
            startActivity(intent)
        }
        binding.btnDurumGuncelle.setOnClickListener {
            // Durum güncelleme sayfasına yönlendir
            val intent = Intent(this, KullaniciDurumGuncelleActivity::class.java)

            // Eğer güncelleme yapılacak alanın ID'si gerekliyse bunu da gönderebilirsin
            if (secilenAlanID != -1) {
                intent.putExtra("ALAN_ID", secilenAlanID)
            }

            startActivity(intent)
        }
    }

    private fun startListeActivity(filtre: String) {
        val intent = Intent(this, IhtiyacListeActivity::class.java)
        intent.putExtra("ALAN_ID", secilenAlanID)
        intent.putExtra("DURUM_FILTRESI", filtre)
        startActivity(intent)
    }

    private fun setupRecyclerViews() {
        bekleyenAdapter = IhtiyacAdapter(mutableListOf())
        onaylananAdapter = IhtiyacAdapter(mutableListOf())

        binding.rvBekleyenler.layoutManager = LinearLayoutManager(this)
        binding.rvBekleyenler.adapter = bekleyenAdapter

        binding.rvOnaylananlar.layoutManager = LinearLayoutManager(this)
        binding.rvOnaylananlar.adapter = onaylananAdapter
    }

    private fun fetchDashboardData() {
        if (secilenAlanID == -1) return

        // IhtiyacListesi tipinde veriyi çek
        apiService.getAlanIhtiyaclari(secilenAlanID).enqueue(object : Callback<List<IhtiyacListesi>> {
            override fun onResponse(call: Call<List<IhtiyacListesi>>, response: Response<List<IhtiyacListesi>>) {
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()

                    // Duruma göre filtrele ve ilk 5 kaydı al
                    val bekleyenler = list.filter { it.durum.trim() == "Beklemede" }.take(5)
                    val onaylananlar = list.filter { it.durum.trim() == "Onaylandı" }.take(5)

                    bekleyenAdapter.updateList(bekleyenler)
                    onaylananAdapter.updateList(onaylananlar)
                }
            }
            override fun onFailure(call: Call<List<IhtiyacListesi>>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Hata")
            }
        })
    }
}