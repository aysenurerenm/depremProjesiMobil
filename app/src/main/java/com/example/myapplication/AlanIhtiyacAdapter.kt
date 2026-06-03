package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.AlanIhtiyac
import com.example.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlanIhtiyacAdapter(private val liste: List<AlanIhtiyac>) :
    RecyclerView.Adapter<AlanIhtiyacAdapter.IhtiyacViewHolder>() {

    class IhtiyacViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAlanAdi: TextView = view.findViewById(R.id.txtAlanAdi)
        val txtSevkiyatDurumu: TextView = view.findViewById(R.id.txtSevkiyatDurumu)
        val txtUrunBilgi: TextView = view.findViewById(R.id.txtUrunBilgi)
        val txtMiktarBilgi: TextView = view.findViewById(R.id.txtMiktarBilgi)
        val txtZamanBilgi: TextView = view.findViewById(R.id.txtZamanBilgi) // XML'deki yeni ID
        val btnOnayla: Button = view.findViewById(R.id.btnOnayla)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IhtiyacViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alan_ihtiyac, parent, false)
        return IhtiyacViewHolder(view)
    }

    override fun onBindViewHolder(holder: IhtiyacViewHolder, position: Int) {
        val item = liste[position]

        // --- Veri Bağlama ---
        holder.txtAlanAdi.text = item.alanAd
        holder.txtUrunBilgi.text = "Ürün: ${item.urunAd ?: "Bilinmiyor"}"
        holder.txtMiktarBilgi.text = "${item.miktar} Adet"
        holder.txtSevkiyatDurumu.text = item.durum

        // --- Zaman Bilgisi ve Görsel Durum ---
        val durumTemiz = item.durum.lowercase().trim()

        when (durumTemiz) {
            "onaylandı" -> {
                holder.txtSevkiyatDurumu.setBackgroundColor(Color.parseColor("#10B981")) // Yeşil
                holder.btnOnayla.isEnabled = false
                holder.btnOnayla.text = "Onaylandı"
                holder.btnOnayla.alpha = 0.5f

                // Onay zamanını göster
                holder.txtZamanBilgi.text = "Onay: ${formatSadeTarih(item.onaylanmaZamani)}"
            }
            "bekliyor", "beklemede" -> {
                holder.txtSevkiyatDurumu.setBackgroundColor(Color.parseColor("#F59E0B")) // Turuncu
                holder.btnOnayla.isEnabled = true
                holder.btnOnayla.text = "Talebi Onayla"
                holder.btnOnayla.alpha = 1.0f

                // Talep zamanını göster
                holder.txtZamanBilgi.text = "Talep: ${formatSadeTarih(item.talepZamani)}"
            }
            else -> {
                holder.txtSevkiyatDurumu.setBackgroundColor(Color.parseColor("#3B82F6")) // Mavi
                holder.btnOnayla.isEnabled = true
                holder.txtZamanBilgi.text = "Zaman: ${formatSadeTarih(item.talepZamani)}"
            }
        }

        // --- Onay Butonu İşlemi ---
        holder.btnOnayla.setOnClickListener {
            holder.btnOnayla.isEnabled = false // Çift tıklamayı önle

            RetrofitClient.api.ihtiyacOnayla(item.ihtiyacID)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(holder.itemView.context, "Talep onaylandı", Toast.LENGTH_SHORT).show()

                            // Arayüzü anlık güncelle
                            holder.txtSevkiyatDurumu.text = "onaylandı"
                            holder.txtSevkiyatDurumu.setBackgroundColor(Color.parseColor("#10B981"))
                            holder.btnOnayla.isEnabled = false
                            holder.btnOnayla.text = "Onaylandı"
                            holder.btnOnayla.alpha = 0.5f
                        } else {
                            holder.btnOnayla.isEnabled = true
                            Toast.makeText(holder.itemView.context, "Hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        holder.btnOnayla.isEnabled = true
                        Toast.makeText(holder.itemView.context, "Bağlantı kesildi", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // Tarih formatını güzelleştiren yardımcı fonksiyon
    private fun formatSadeTarih(hamTarih: String?): String {
        if (hamTarih == null || hamTarih == "null" || hamTarih.isEmpty()) return "Belirtilmedi"

        return try {
            // Gelen veri: "2026-05-05 21:24:41.26143+"
            // 1. Tarih kısmını al (ilk 10 karakter: 2026-05-05)
            val tarihHam = hamTarih.substring(0, 10)
            val tarihDizi = tarihHam.split("-")
            val formatliTarih = "${tarihDizi[2]}.${tarihDizi[1]}.${tarihDizi[0]}" // 05.05.2026

            // 2. Saat kısmını al (11. karakterden başla, 5 karakter al: 21:24)
            val saatKismi = hamTarih.substring(11, 16)

            "$formatliTarih $saatKismi"
        } catch (e: Exception) {
            // Eğer format beklenmedik bir şekilde gelirse uygulamayı çökertme, ham veriyi göster
            hamTarih
        }
    }

    override fun getItemCount() = liste.size
}