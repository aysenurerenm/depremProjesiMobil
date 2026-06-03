package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.AnasayfaBinding
import com.example.myapplication.network.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: AnasayfaBinding
    private lateinit var depremAdapter: DepremAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. View Binding ve Layout Kurulumu
        binding = AnasayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 2. RecyclerView Hazırlığı
        depremAdapter = DepremAdapter(listOf())
        binding.rvDepremler.layoutManager = LinearLayoutManager(this)
        binding.rvDepremler.adapter = depremAdapter

        // 3. Başlangıç Çağrıları
        depremleriGetir()
        konumIzniniKontrolEtVeBaslat()

        // 4. Sabit Buton Tıklamaları
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.card112.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:112")
            startActivity(intent)
        }
    }

    private fun konumIzniniKontrolEtVeBaslat() {
        // İstenecek izinler listesi (Konum izni zaten senin kodunda vardı)
        val izinlerListesi = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        // Eğer cihaz Android 13 (Tiramisu) veya daha üzeriyse listeye Bildirim İznini de ekle
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            izinlerListesi.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Listede izin verilmeyen herhangi bir şey var mı kontrol et
        val tumIzinlerOnayliMi = izinlerListesi.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!tumIzinlerOnayliMi) {
            // Eksik olan izinleri kullanıcıdan talep et (Hocanın önünde ilk açılışta kutucuklar açılır)
            ActivityCompat.requestPermissions(this, izinlerListesi.toTypedArray(), 100)
        } else {
            // İzinler zaten verilmişse direkt toplanma alanlarını getir
            toplanmaAlanlariniGetir()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            // En azından Konum izninin verilip verilmediğini kontrol ediyoruz
            val konumIzniIndeksi = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)

            if (konumIzniIndeksi != -1 && grantResults[konumIzniIndeksi] == PackageManager.PERMISSION_GRANTED) {
                // Kullanıcı izni verdiği an harita verilerini ve toplanma alanını yükle!
                Toast.makeText(this, "İzinler onaylandı, veriler yükleniyor...", Toast.LENGTH_SHORT).show()
                toplanmaAlanlariniGetir()
            } else {
                // Kullanıcı kritik olan konum iznini reddettiyse bilgilendir
                Toast.makeText(this, "Toplanma alanlarını görebilmek için konum izni vermelisiniz!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toplanmaAlanlariniGetir() {
        // 71. Satırı bu şekilde güncelle:
        val apiService = RetrofitClient.api

        apiService.getAlanlar().enqueue(object : Callback<List<ToplanmaAlani>> {
            override fun onResponse(call: Call<List<ToplanmaAlani>>, response: Response<List<ToplanmaAlani>>) {
                if (response.isSuccessful) {
                    val alanListesi = response.body()
                    if (!alanListesi.isNullOrEmpty()) {
                        // Cihazın konumunu al (Fused Location)
                        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    // Algoritmayı çalıştır: En yakın alanı ve mesafeyi al
                                    val result = enYakinAlaniBul(location.latitude, location.longitude, alanListesi)
                                    val alan = result.first
                                    val mesafeMetre = result.second

                                    alan?.let {
                                        // UI Güncelleme
                                        val mesafeKm = mesafeMetre / 1000
                                        binding.layoutToplanmaAlani.tvMesafe.text = String.format("Mesafe: %.2f km", mesafeKm)
                                        binding.layoutToplanmaAlani.tvAlanAdi.text = "En Yakın Güvenli Nokta"
                                        binding.layoutToplanmaAlani.tvAlanAdres.text = "Enlem: ${alan.enlem} / Boylam: ${alan.boylam}"

                                        binding.layoutToplanmaAlani.btnYolTarifi.setOnClickListener {
                                            yolTarifiAl(alan)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<ToplanmaAlani>>, t: Throwable) {
                Log.e("API_HATA", "Toplanma alanları çekilemedi: ${t.message}")
            }
        })
    }

    private fun depremleriGetir() {
        RetrofitClient.kandilliInstance.create(DepremApiService::class.java)
            .getSonDepremler(10).enqueue(object : Callback<DepremResponse> {
                override fun onResponse(call: Call<DepremResponse>, response: Response<DepremResponse>) {
                    if (response.isSuccessful) {
                        val sonOn = response.body()?.result?.take(10)
                        if (sonOn != null) {
                            depremAdapter.updateList(sonOn)
                        }
                    }
                }

                override fun onFailure(call: Call<DepremResponse>, t: Throwable) {
                    Log.e("API", "Kandilli hatası: ${t.message}")
                }
            })
    }

    // Hem alanı hem mesafeyi dönmek için Pair kullandık
    fun enYakinAlaniBul(userLat: Double, userLon: Double, alanListesi: List<ToplanmaAlani>): Pair<ToplanmaAlani?, Float> {
        var enYakinAlan: ToplanmaAlani? = null
        var enKisaMesafe = Float.MAX_VALUE

        val userLocation = android.location.Location("user").apply {
            latitude = userLat
            longitude = userLon
        }

        for (alan in alanListesi) {
            val alanLocation = android.location.Location("alan").apply {
                latitude = alan.enlem
                longitude = alan.boylam
            }

            val mesafe = userLocation.distanceTo(alanLocation)
            if (mesafe < enKisaMesafe) {
                enKisaMesafe = mesafe
                enYakinAlan = alan
            }
        }
        return Pair(enYakinAlan, enKisaMesafe)
    }

    fun yolTarifiAl(alan: ToplanmaAlani) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${alan.enlem},${alan.boylam}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}