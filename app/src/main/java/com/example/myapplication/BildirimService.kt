package com.example.myapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.network.AlanIhtiyac
import com.example.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BildirimService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val kontrolSuresi: Long = 10000
    private var kullaniciID: String? ="0"

    private val talepKontrolGorevi = object : Runnable {
        override fun run() {
            Log.d("BİLDİRİM_TEST", "10 saniye doldu, API'ye soruluyor... (ID: $kullaniciID)")
            if (kullaniciID != "0") {
                yeniTalepleriSorgula(kullaniciID)
            }
            handler.postDelayed(this, kontrolSuresi)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        kullaniciID = intent?.getStringExtra("KULLANICI_ID")
        Log.d("BİLDİRİM_TEST", "Servis Başlatıldı! Gelen Kullanıcı ID: $kullaniciID")

        if (kullaniciID != "0") {
            bildirimKanaliniOlustur()
            handler.post(talepKontrolGorevi)
        } else {
            Log.e("BİLDİRİM_TEST", "HATA: Kullanıcı ID -1 geldi, servis çalışmayacak!")
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BİLDİRİM_TEST", "Servis Durduruldu!")
        handler.removeCallbacks(talepKontrolGorevi)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun yeniTalepleriSorgula(id: String?) {
        RetrofitClient.api.getdepoAlanIhtiyaclari(id).enqueue(object : Callback<List<AlanIhtiyac>> {
            override fun onResponse(call: Call<List<AlanIhtiyac>>, response: Response<List<AlanIhtiyac>>) {
                if (response.isSuccessful) {
                    val gelenListe = response.body() ?: emptyList()
                    val yeniSayi = gelenListe.size

                    // 🔥 PROJE İSTERİ: YEREL CİHAZDA VERİ DEPOLAMA (SharedPreferences)
                    val sharedPref = getSharedPreferences("DepoHafizasi", Context.MODE_PRIVATE)

                    // Telefona kaydedilmiş son sayıyı al (Eğer ilk kez açılıyorsa -1 döner)
                    val eskiSayi = sharedPref.getInt("KAYITLI_TALEP_SAYISI", -1)

                    Log.d("BİLDİRİM_TEST", "API Başarılı! Gelen Kayıt Sayısı: $yeniSayi | Telefona Kayıtlı Eski Sayı: $eskiSayi")

                    // Eğer telefonda kayıtlı bir sayı varsa ve yeni gelen sayı bundan büyükse BİLDİRİM AT!
                    if (eskiSayi != -1 && yeniSayi > eskiSayi) {
                        val fark = yeniSayi - eskiSayi
                        Log.d("BİLDİRİM_TEST", "YENİ TALEP TESPİT EDİLDİ! Bildirim gönderiliyor...")
                        bildirimGonder("🚨 Yeni Talep Geldi!", "Toplanma alanından $fark yeni ihtiyaç (Bekliyor) eklendi.", 3)
                    }

                    // İşlem bitince, yeni sayıyı uygulamayı kapatsan bile silinmemesi için telefona KAYDET
                    sharedPref.edit().putInt("KAYITLI_TALEP_SAYISI", yeniSayi).apply()

                } else {
                    Log.e("BİLDİRİM_TEST", "API Hata Kodu Döndü: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<AlanIhtiyac>>, t: Throwable) {
                Log.e("BİLDİRİM_TEST", "API'ye Bağlanılamadı! Hata: ${t.message}")
            }
        })
    }

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
                Log.e("BİLDİRİM_TEST", "BİLDİRİM GÖNDERİLEMEDİ: Kullanıcı izin vermemiş!")
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
        Log.d("BİLDİRİM_TEST", "Bildirim başarıyla telefona itildi!")
    }
}