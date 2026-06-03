package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Sevkiyat

class SevkiyatAdapter(private val sevkiyatListesi: List<Sevkiyat>) :
    RecyclerView.Adapter<SevkiyatAdapter.SevkiyatViewHolder>() {

    class SevkiyatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSurucu: TextView = itemView.findViewById(R.id.txtSurucu)
        val txtPlaka: TextView = itemView.findViewById(R.id.txtPlaka)
        val txtArac: TextView = itemView.findViewById(R.id.txtArac)
        val txtAlan: TextView = itemView.findViewById(R.id.txtAlan)
        val txtDurum: TextView = itemView.findViewById(R.id.txtDurum)
        // 🔥 Yeni eklenen alanlar (XML'de tanımladığın ID'ler ile aynı olmalı)
        val txtMiktar: TextView = itemView.findViewById(R.id.txtMiktar)
        val txtOnayZamani: TextView = itemView.findViewById(R.id.txtOnayZamani)
        val txtSevkiyatZamani: TextView = itemView.findViewById(R.id.txtSevkiyatZamani)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SevkiyatViewHolder {
        // ⚠️ DÜZELTME: Burası activity_sevkiyat değil, item_sevkiyat (kart tasarımı) olmalı
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sevkiyat, parent, false)
        return SevkiyatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SevkiyatViewHolder, position: Int) {
        val sevkiyat = sevkiyatListesi[position]

        holder.txtSurucu.text = "${sevkiyat.surucuAd} ${sevkiyat.surucuSoyad}"
        holder.txtPlaka.text = "Plaka: ${sevkiyat.plaka}"
        holder.txtArac.text = "Araç: ${sevkiyat.aracTip}"
        holder.txtAlan.text = "Hedef: ${sevkiyat.alanAd}"
        holder.txtDurum.text = "Durum: ${sevkiyat.durum}"

        // 🔥 Yeni verileri ekrana basıyoruz
        holder.txtMiktar.text = "${sevkiyat.miktar} Adet"
        holder.txtOnayZamani.text = "Onay: ${formatSadeTarih(sevkiyat.onaylanmaZamani)}"
        holder.txtSevkiyatZamani.text = "Yola Çıkış: ${formatSadeTarih(sevkiyat.sevkiyatZamani)}"
    }

    override fun getItemCount(): Int = sevkiyatListesi.size

    // Zamanı daha okunaklı gösteren yardımcı fonksiyon
    private fun formatSadeTarih(hamTarih: String?): String {
        if (hamTarih.isNullOrEmpty() || hamTarih == "null") return "---"
        return try {
            // Eğer backend'den "2026-05-09 21:49" geliyorsa direkt döndür veya parçala
            hamTarih
        } catch (e: Exception) {
            "---"
        }
    }
}