package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result // Result karmaşasını önlemek için kesin import
import com.example.myapplication.network.RetrofitClient

class YakinEklemeWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val sharedPref = context.getSharedPreferences("UygulamaPrefs", Context.MODE_PRIVATE)

        // =========================================================================
        // ✔️ DÜZELTME: T.C. yerine WelcomeActivity'den gelen kullaniciID (55 gibi) değerini okuyoruz
        // =========================================================================
        val aktifKullaniciId = sharedPref.getString("kullaniciID", null)

        // Logcat'te artık gönderilen temiz ID değerini görebilirsiniz
        Log.d("BİLDİRİM_WORKER", "Şu an sorgulanan Kullanıcı ID: $aktifKullaniciId")

        // Giriş yapmış geçerli bir kullanıcı ID yoksa arka planda boşuna istek atma, başarılı çık
        if (aktifKullaniciId.isNullOrEmpty()) return Result.success()

        return try {
            // ApiService üzerindeki getYakinlikBildirimleri artık Kullanıcı ID ile tetikleniyor
            val response = RetrofitClient.api.getYakinlikBildirimleri(aktifKullaniciId).execute()

            if (response.isSuccessful && response.body() != null) {
                val bildirimler = response.body()!!

                for (bildirim in bildirimler) {
                    val gonderenId = bildirim.id

                    val ad = bildirim.ekleyenAd ?: ""
                    val soyad = bildirim.ekleyenSoyad ?: ""
                    val fullIsim = "$ad $soyad".trim()

                    // =========================================================================
                    // 💡 SUNUM KOLAYLIĞI: Mükerrer bildirim kontrolünü devre dışı bıraktık.
                    // Böylece sunumda butona her uzun bastığında ekrana peş peşe canlı bildirim düşer.
                    // =========================================================================
                    val benzersizIntId = gonderenId.hashCode()
                    yakinlikBildirimiGoster(benzersizIntId, fullIsim)

                    /* Eski kilit mekanizması (İleride tek seferlik bildirim istersen açabilirsin):
                    val dahaOnceBildirimGittiMi = sharedPref.getBoolean("yakin_ekledi_$gonderenId", false)
                    if (!dahaOnceBildirimGittiMi) {
                        yakinlikBildirimiGoster(benzersizIntId, fullIsim)
                        sharedPref.edit().putBoolean("yakin_ekledi_$gonderenId", true).apply()
                    }
                    */
                    // =========================================================================
                }
            }
            Result.success()
        } catch (e: Throwable) {
            // HATA ÖNLEME: Protokol ayrıştırma (Parse Proto) veya ağ kopma hatalarını yakalar
            Log.e("BİLDİRİM_WORKER", "Ağ veya Ayrıştırma Hatası: ${e.message}")
            e.printStackTrace()
            Result.retry() // Bağlantı koptuysa Android daha sonra tekrar dener
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun yakinlikBildirimiGoster(id: Int, kimEklede: String) {
        val channelId = "yakinlik_ekleme_kanali_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Yakınlık Bildirimleri", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Yeni yakınlık bağlantısı bildirimleri"
                enableLights(true)
                enableVibration(true) // Android 13/14 Heads-up ekran tetikleyicisi
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC // Kilit ekranında göster
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentTitle("Yeni Yakınlık Bağlantısı")
            .setContentText("$kimEklede tarafından yakınlar listesine eklendiniz.")
            // ✔️ DÜZELTME: Android 14'ün "Heads-up" (tepeden sarkan anlık arayüz) mekanizmasını zorlamak için ayarlar
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Ses, titreşim ve ışık varsayılanlarını donanıma dikte eder
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(id, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}