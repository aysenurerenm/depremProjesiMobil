package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.TamamlananYardimResponse

class TamamlananAdapter(
    private val liste: List<TamamlananYardimResponse>
) : RecyclerView.Adapter<TamamlananAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // XML'deki ID'lerle koddaki tanımlamaların birebir aynı olması şarttır
        val txtUrunAd: TextView = view.findViewById(R.id.txtUrunAd)
        val txtAlan: TextView = view.findViewById(R.id.txtAlan)
        val txtMiktar: TextView = view.findViewById(R.id.txtMiktar)
        val txtZamanBilgi: TextView = view.findViewById(R.id.txtZamanBilgi)
        val txtDurum: TextView = view.findViewById(R.id.txtDurum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Layout isminin doğruluğundan emin ol: item_tamamlanan.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tamamlanan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = liste.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = liste[position]

        holder.txtAlan.text = item.alanAd ?: "Belirtilmedi"
        holder.txtUrunAd.text = "Ürün: ${item.urunAd ?: "Bilinmiyor"}"

        // yukMiktar değil, miktar kullanıyoruz (API'ye göre)
        holder.txtMiktar.text = "${item.miktar} Adet"

        holder.txtDurum.text = item.durum ?: "Tamamlandı"

        // Tarih formatlama (Gönderdiğin JSON'da T harfi var, substring bunu çözer)
        holder.txtZamanBilgi.text = "Tamamlanma: ${formatSadeTarih(item.tamamlanmaZamani)}"
    }
    private fun formatSadeTarih(hamTarih: String?): String {
        if (hamTarih.isNullOrEmpty() || hamTarih == "null" || hamTarih.length < 16) return "Belirtilmedi"
        return try {
            val tarihHam = hamTarih.substring(0, 10)
            val parcalar = tarihHam.split("-")
            val duzgunTarih = "${parcalar[2]}.${parcalar[1]}.${parcalar[0]}"
            val saatKismi = hamTarih.substring(11, 16)
            "$duzgunTarih $saatKismi"
        } catch (e: Exception) {
            "Tarih Hatası"
        }
    }
}