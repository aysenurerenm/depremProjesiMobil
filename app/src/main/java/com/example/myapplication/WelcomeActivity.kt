package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.CitizenBinding
import com.example.myapplication.network.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: CitizenBinding
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var isSafe = true
    private val handler = Handler(Looper.getMainLooper())
    private val uniqueWorkName = "HayatiDurumKontrolGorevi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = CitizenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvFamilyList.layoutManager = LinearLayoutManager(this)
        setupOSM()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        val citizenId = intent.getStringExtra("CITIZEN_ID")

        if (!citizenId.isNullOrEmpty()) {
            // Worker'ın SharedPref içerisinden çekebilmesi için ID kaydını garantiye alıyoruz
            val sharedPref = getSharedPreferences("UygulamaPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putString("kullaniciID", citizenId).apply()

            fetchCitizenData(citizenId)
            fetchFamilyData(citizenId)
            fetchAssemblyAreaData(citizenId)
        } else {
            Toast.makeText(this, "Kullanıcı ID bulunamadı!", Toast.LENGTH_SHORT).show()
        }

        binding.cardHelpRequest.setOnClickListener {
            processStatusAndLocationUpdate("DANGER")
        }

        binding.cardSafetyToggle.setOnClickListener {
            processStatusAndLocationUpdate("SAFE")
        }

        binding.btnYakinEkle.setOnClickListener {
            val tc = binding.etYakinTC.text.toString()
            if (tc.length == 11 && !citizenId.isNullOrEmpty()) {
                yeniYakinEkle(citizenId, tc)
            } else {
                Toast.makeText(this, "Lütfen 11 haneli geçerli bir TC girin", Toast.LENGTH_SHORT).show()
            }
        }

        // ✔️ DÜZELTME: Uzun basıldığında artık doğru işçi (DurumGuncellemeWorker) tetikleniyor
        binding.btnYakinEkle.setOnLongClickListener {
            val anlikZorunluIstek = OneTimeWorkRequestBuilder<DurumGuncellemeWorker>().build()
            WorkManager.getInstance(this).enqueue(anlikZorunluIstek)

            Toast.makeText(this, "Durum denetleme işçisi anlık tetiklendi! Logcat'i izleyin.", Toast.LENGTH_SHORT).show()
            true
        }

        // ✔️ DÜZELTME: Periyodik takip mekanizması tek bir kararlı yapıya bağlandı
        if (!citizenId.isNullOrEmpty()) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val durumIstegi = PeriodicWorkRequestBuilder<DurumGuncellemeWorker>(1, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.KEEP, // KEEP sayesinde süreç sıfırlanıp durmaz
                durumIstegi
            )
        }
    }

    private fun processStatusAndLocationUpdate(newStatus: String) {
        val citizenId = intent.getStringExtra("CITIZEN_ID") ?: return
        updateStatus(newStatus)

        val lastLocation = locationOverlay.myLocation
        if (lastLocation != null) {
            kullaniciKonumunuGuncelle(citizenId, newStatus, lastLocation.latitude, lastLocation.longitude)
        } else {
            Log.w("GPS", "Konum alınamadı.")
        }
    }

    private fun kullaniciKonumunuGuncelle(userId: String, durum: String, userLat: Double, userLon: Double) {
        val locationRequest = LocationUpdateRequest(durum = durum, enlem = userLat, boylam = userLon)
        RetrofitClient.api.updateLocation(userId, locationRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) Log.d("API_SUCCESS", "Konum güncellendi.")
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Konum hatası: ${t.message}")
            }
        })
    }

    private fun updateStatus(status: String) {
        val citizenId = intent.getStringExtra("CITIZEN_ID") ?: return
        RetrofitClient.api.update_status(citizenId, mapOf("status" to status))
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        isSafe = (status == "SAFE")
                        binding.txtStatus.text = if (isSafe) "Aktif durum: Güvende" else "Aktif durum: Tehlikede"
                        binding.txtStatus.setTextColor(if (isSafe) 0xFF2E7D32.toInt() else 0xFFE74C3C.toInt())

                        val mesaj = if (isSafe) "Durum 'GÜVENDE' olarak güncellendi" else "YARDIM TALEBİ İLETİLDİ!"
                        Toast.makeText(this@WelcomeActivity, mesaj, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@WelcomeActivity, "Bağlantı hatası!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchCitizenData(id: String) {
        RetrofitClient.api.get_citizen_details(id).enqueue(object : Callback<CitizenInfo> {
            override fun onResponse(call: Call<CitizenInfo>, response: Response<CitizenInfo>) {
                if (response.isSuccessful) response.body()?.let { updateUI(it) }
            }
            override fun onFailure(call: Call<CitizenInfo>, t: Throwable) {
                Log.e("API_HATA", "Bilgiler yüklenemedi")
            }
        })
    }

    private fun updateUI(data: CitizenInfo) {
        binding.apply {
            txtName.text = "${data.Ad} ${data.Soyad}"
            txtCitizenID.text = "T.C. No: ${data.tc}"
            txtStatus.text = "Aktif durum: ${data.kullaniciDurum}"
            txtLastUpdate.text = "Son güncelleme: ${data.durumGuncellemeZamani ?: "Bilinmiyor"}"
            isSafe = (data.kullaniciDurum == "SAFE")
        }
    }

    private fun fetchFamilyData(id: String) {
        RetrofitClient.api.get_citizen_family(id).enqueue(object : Callback<List<CitizenFamily>> {
            override fun onResponse(call: Call<List<CitizenFamily>>, response: Response<List<CitizenFamily>>) {
                if (response.isSuccessful) {
                    val familyList = response.body() ?: emptyList()
                    binding.rvFamilyList.adapter = FamilyAdapter(familyList)
                }
            }
            override fun onFailure(call: Call<List<CitizenFamily>>, t: Throwable) {
                Log.e("API_HATA", "Aile listesi hatası")
            }
        })
    }

    private fun setupOSM() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(15.0)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        binding.map.overlays.add(locationOverlay)
    }

    private fun fetchAssemblyAreaData(id: String) {
        RetrofitClient.api.getToplanmaAlanim(id).enqueue(object : Callback<AssemblyAreaResponse> {
            override fun onResponse(call: Call<AssemblyAreaResponse>, response: Response<AssemblyAreaResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { updateAssemblyAreaUI(it) }
                }
            }
            override fun onFailure(call: Call<AssemblyAreaResponse>, t: Throwable) {
                Log.e("API_HATA", "Toplanma alanı hatası")
            }
        })
    }

    private fun updateAssemblyAreaUI(response: AssemblyAreaResponse) {
        val alan = response.alanlar.firstOrNull()
        val yetkili = response.yetkililer.firstOrNull()

        if (alan != null) {
            val assemblyPoint = GeoPoint(alan.enlem, alan.boylam)
            binding.map.controller.setCenter(assemblyPoint)
            val marker = Marker(binding.map)
            marker.position = assemblyPoint
            marker.title = "Toplanma Alanı"
            binding.map.overlays.add(marker)
            binding.map.invalidate()
        }

        binding.layoutAssemblyArea.apply {
            root.findViewById<TextView>(R.id.txtAreaName)?.text = "Toplanma Alanı ID: ${alan?.alanId ?: ""}"
            root.findViewById<TextView>(R.id.txtAreaLocation)?.text = "Konum: ${alan?.enlem}, ${alan?.boylam}"
            root.findViewById<TextView>(R.id.txtOfficerName)?.text = "Yetkili: ${yetkili?.ad} ${yetkili?.soyad}"
            root.findViewById<TextView>(R.id.txtOfficerPhone)?.text = "Tel: ${yetkili?.telefon}"
        }
    }

    private fun yeniYakinEkle(citizenId: String, tcInput: String) {
        RetrofitClient.api.yakinEkle(citizenId, YakinEkleRequest(tcInput)).enqueue(object : Callback<YakinEkleResponse> {
            override fun onResponse(call: Call<YakinEkleResponse>, response: Response<YakinEkleResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@WelcomeActivity, "Yakın başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                    fetchFamilyData(citizenId)

                    // Yeni yakın eklendiğinde anlık durumu hemen çekmesi için OneTime tetikliyoruz
                    val anlikTetikleme = OneTimeWorkRequestBuilder<DurumGuncellemeWorker>().build()
                    WorkManager.getInstance(this@WelcomeActivity).enqueue(anlikTetikleme)
                } else {
                    Toast.makeText(this@WelcomeActivity, "Kullanıcı bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<YakinEkleResponse>, t: Throwable) {
                Log.e("API_HATA", "Bağlantı hatası")
            }
        })
    }

    override fun onResume() { super.onResume(); binding.map.onResume() }
    override fun onPause() { super.onPause(); binding.map.onPause() }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}