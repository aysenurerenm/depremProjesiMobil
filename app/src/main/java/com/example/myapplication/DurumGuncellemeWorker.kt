package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.network.RetrofitClient
import retrofit2.Response

class DurumGuncellemeWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val channelId = "izole_acil_durum_kanali_v3"

    override fun doWork(): Result {
        Log.d("DURUM_WORKER", "1. Arka plan servisi tetiklendi.")

        val sharedPref = applicationContext.getSharedPreferences("UygulamaPrefs", Context.MODE_PRIVATE)
        val aktifKullaniciId = sharedPref.getString("kullaniciID", null)

        if (aktifKullaniciId == null) {
            Log.e("DURUM_WORKER", "2. HATA: SharedPreferences içinde kullaniciID bulunamadı!")
            return Result.failure()
        }
        Log.d("DURUM_WORKER", "3. Aktif Kullanıcı ID ile istek atılıyor: $aktifKullaniciId")

        // Kanalı döngüden ÖNCE bir kez oluşturarak işletim sisteminin kilitlenmesini önlüyoruz
        createNotificationChannelOnce()

        try {
            val response = RetrofitClient.api.getDurumGuncellemeleri(aktifKullaniciId).execute()

            if (response.isSuccessful && response.body() != null) {
                val bildirimListesi = response.body()!!
                Log.d("DURUM_WORKER", "4. Django'dan gelen veri sayısı: ${bildirimListesi.size}")

                for (bildirim in bildirimListesi) {
                    Log.d("DURUM_WORKER", "5. Bildirim basılıyor: ${bildirim.ad} - ${bildirim.durum}")
                    acilDurumBildirimiBas(
                        bildirim.id.hashCode(),
                        "🚨 AFET VE ACİL DURUM UYARISI",
                        "${bildirim.ad} ${bildirim.soyad} şu an ${bildirim.durum}!",
                        bildirim.durum == "Tehlikede"
                    )
                }
                return Result.success()
            } else {
                Log.e("DURUM_WORKER", "HATA: Sunucu kodu: ${response.code()}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e("DURUM_WORKER", "KRİTİK AĞ/BAĞLANTI HATASI: ${e.message}", e)
            return Result.retry()
        }
    }

    private fun createNotificationChannelOnce() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Eğer kanal sistemde zaten varsa tekrar oluşturup OS kuyruğunu yormuyoruz
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Hayati Durum Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Yakınlarınızın hayati durum değişiklikleri"
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun acilDurumBildirimiBas(id: Int, baslik: String, mesaj: String, isTehlike: Boolean) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(if (isTehlike) android.R.drawable.stat_sys_warning else android.R.drawable.checkbox_on_background)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            NotificationManagerCompat.from(applicationContext).notify(id, builder.build())
        } catch (e: SecurityException) {
            Log.e("DURUM_WORKER", "Bildirim izni telefon tarafından engellendi: ${e.message}")
        }
    }
}