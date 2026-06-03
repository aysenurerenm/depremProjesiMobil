package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DepoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DepoStokAdapter

    private lateinit var txtToplamKalem: TextView
    private lateinit var txtKritikStok: TextView
    private lateinit var txtAktifSevkiyat: TextView
    private lateinit var layoutKritikUyari: View

    // ÜRÜN EKLEME COMPONENTLERİ
    private lateinit var spinnerUrunler: Spinner
    private lateinit var etMiktar: EditText
    private lateinit var btnDepoyaEkle: Button

    // AÇILIR KAPANIR ALAN
    private lateinit var layoutHeaderDepo: LinearLayout
    private lateinit var layoutDepoContent: LinearLayout
    private lateinit var imgArrow: ImageView

    private var isExpanded = false

    // ÜRÜN LİSTESİ
    private var urunListesi = mutableListOf<UrunItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depo)

        // BİLDİRİM KANALINI OLUŞTUR (Kritik Stok Uyarıları İçin)
        bildirimKanaliniOlustur()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        // TEXTLER
        txtToplamKalem = findViewById(R.id.txtToplamKalem)
        txtKritikStok = findViewById(R.id.txtKritikStok)
        txtAktifSevkiyat = findViewById(R.id.txtAktifSevkiyat)
        layoutKritikUyari = findViewById(R.id.layoutKritikUyari)

        // ÜRÜN EKLEME COMPONENTLERİ
        spinnerUrunler = findViewById(R.id.spinnerUrunler)
        etMiktar = findViewById(R.id.etMiktar)
        btnDepoyaEkle = findViewById(R.id.btnDepoyaEkle)

        // AÇILIR KAPANIR
        layoutHeaderDepo = findViewById(R.id.layoutHeaderDepo)
        layoutDepoContent = findViewById(R.id.layoutDepoContent)
        imgArrow = findViewById(R.id.imgArrow)

        // RECYCLERVIEW
        recyclerView = findViewById(R.id.recyclerViewStok)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DepoStokAdapter(emptyList())
        recyclerView.adapter = adapter

        // LOGIN'DEN GELEN ID
        val kullaniciID = intent.getStringExtra("CITIZEN_ID")

        Log.d("API_ID", "Intent'ten gelen ID: $kullaniciID")

        if (kullaniciID!="0") {

            // 🔥 PROFESYONEL ARKA PLAN SERVİSİNİ BAŞLAT (Uygulama alta alınsa bile dinler)
            val serviceIntent = Intent(this, BildirimService::class.java)
            serviceIntent.putExtra("KULLANICI_ID", kullaniciID)
            startService(serviceIntent)

            // STOK VERİLERİ
            verileriGetir(kullaniciID)

            // ÜRÜNLERİ GETİR
            urunleriGetir()

            // AÇILIR / KAPANIR PANEL
            layoutHeaderDepo.setOnClickListener {
                isExpanded = !isExpanded

                if (isExpanded) {
                    layoutDepoContent.visibility = View.VISIBLE
                    imgArrow.rotation = 180f
                } else {
                    layoutDepoContent.visibility = View.GONE
                    imgArrow.rotation = 0f
                }
            }

            // ÜRÜN EKLEME BUTONU
            btnDepoyaEkle.setOnClickListener {
                val miktarText = etMiktar.text.toString()

                if (miktarText.isEmpty()) {
                    Toast.makeText(this, "Miktar giriniz", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (urunListesi.isEmpty()) {
                    Toast.makeText(this, "Ürün listesi yüklenemedi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val secilenPozisyon = spinnerUrunler.selectedItemPosition
                val secilenUrun = urunListesi[secilenPozisyon]

                depoUrunEkle(kullaniciID, secilenUrun.urunID, miktarText.toInt())
            }

            // ALAN İHTİYAÇLARI
            findViewById<Button>(R.id.btnAlanIhtiyaclari).setOnClickListener {
                val intentAlan = Intent(this, AlanIhtiyacActivity::class.java)
                intentAlan.putExtra("KULLANICI_ID", kullaniciID)
                startActivity(intentAlan)
            }

            // SEVKİYATLAR
            findViewById<Button>(R.id.btnSevkiyatlar).setOnClickListener {
                val intentSevkiyat = Intent(this, SevkiyatActivity::class.java)
                intentSevkiyat.putExtra("KULLANICI_ID", kullaniciID)
                startActivity(intentSevkiyat)
            }

            // TAMAMLANANLAR
            findViewById<Button>(R.id.btnTamamlananlar).setOnClickListener {
                val intentTamamlanan = Intent(this, TamamlananActivity::class.java)
                intentTamamlanan.putExtra("KULLANICI_ID", kullaniciID)
                startActivity(intentTamamlanan)
            }

        } else {
            Toast.makeText(this, "Oturum bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
        }
    }

    // STOK VERİLERİNİ GETİR
    private fun verileriGetir(depoId: String?) {
        val apiService = RetrofitClient.api

        apiService.getDepoStok(depoId).enqueue(object : Callback<DepoResponse> {
            override fun onResponse(call: Call<DepoResponse>, response: Response<DepoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        data.ozet?.let { ozet ->
                            txtToplamKalem.text = ozet.toplamKalem.toString()
                            txtKritikStok.text = ozet.kritikStok.toString()
                            txtAktifSevkiyat.text = ozet.aktifSevkiyat.toString()

                            if (ozet.kritikStok > 0) {
                                layoutKritikUyari.visibility = View.VISIBLE
                                // KRİTİK STOK VARSA BİLDİRİM GÖNDER (ID: 2)
                                bildirimGonder("Kritik Stok Uyarısı!", "Deponuzda ${ozet.kritikStok} kalem üründe kritik stok seviyesi bulunmaktadır.", 2)
                            } else {
                                layoutKritikUyari.visibility = View.GONE
                            }
                        }

                        val yeniUrunListesi = data.urunler ?: emptyList()
                        adapter = DepoStokAdapter(yeniUrunListesi)
                        recyclerView.adapter = adapter
                    }
                } else {
                    Toast.makeText(this@DepoActivity, "Sunucu hatası", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DepoResponse>, t: Throwable) {
                Toast.makeText(this@DepoActivity, "Hata: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("API_ERROR", t.message.toString())
            }
        })
    }

    // ÜRÜNLERİ GETİR
    private fun urunleriGetir() {
        RetrofitClient.api.getdepodakiurunler().enqueue(object : Callback<List<UrunItem>> {
            override fun onResponse(call: Call<List<UrunItem>>, response: Response<List<UrunItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    urunListesi = response.body()!!.toMutableList()
                    Log.d("URUNLER", urunListesi.toString())

                    val urunAdlari = urunListesi.map { it.urunAd }
                    val adapterSpinner = ArrayAdapter(
                        this@DepoActivity,
                        android.R.layout.simple_spinner_item,
                        urunAdlari
                    )
                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerUrunler.adapter = adapterSpinner
                } else {
                    Toast.makeText(this@DepoActivity, "Ürünler alınamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<UrunItem>>, t: Throwable) {
                Toast.makeText(this@DepoActivity, "Ürünler yüklenemedi", Toast.LENGTH_SHORT).show()
                Log.e("URUN_HATA", t.message.toString())
            }
        })
    }

    // DEPOYA ÜRÜN EKLE
    private fun depoUrunEkle(kullaniciID: String?, urunID: Int, miktar: Int) {
        RetrofitClient.api.depoUrunEkle(kullaniciID, urunID, miktar).enqueue(object : Callback<DepoUrunEkleResponse> {
            override fun onResponse(call: Call<DepoUrunEkleResponse>, response: Response<DepoUrunEkleResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DepoActivity, "Ürün başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    // INPUT TEMİZLE
                    etMiktar.text.clear()
                    // PANELİ KAPAT
                    layoutDepoContent.visibility = View.GONE
                    imgArrow.rotation = 0f
                    isExpanded = false
                    // LİSTEYİ YENİLE
                    verileriGetir(kullaniciID)
                } else {
                    Toast.makeText(this@DepoActivity, "Ekleme başarısız", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DepoUrunEkleResponse>, t: Throwable) {
                Toast.makeText(this@DepoActivity, "Hata: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("EKLE_HATA", t.message.toString())
            }
        })
    }

    // BİLDİRİM FONKSİYONLARI (Sadece Kritik Stok İçin Sayfada Kaldı)
    private fun bildirimKanaliniOlustur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kanal = NotificationChannel("depo_kanali", "Depo Bildirimleri", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(kanal)
        }
    }

    private fun bildirimGonder(baslik: String, mesaj: String, bildirimId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }
        val builder = NotificationCompat.Builder(this, "depo_kanali")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(bildirimId, builder.build())
    }
    override fun onDestroy() {
        super.onDestroy()
        // Sayfadan çıkıldığında servisi durdurur
        val serviceIntent = Intent(this, BildirimService::class.java)
        stopService(serviceIntent)
    }
}