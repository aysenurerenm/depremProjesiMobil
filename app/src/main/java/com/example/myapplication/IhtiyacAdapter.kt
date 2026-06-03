package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.IhtiyacListesi

class IhtiyacAdapter(private var ihtiyacListesi: MutableList<IhtiyacListesi>) :
    RecyclerView.Adapter<IhtiyacAdapter.IhtiyacViewHolder>() {

    class IhtiyacViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUrunAd: TextView = view.findViewById(R.id.tvUrunAd)
        val tvMiktar: TextView = view.findViewById(R.id.tvMiktar)
        val tvDurum: TextView = view.findViewById(R.id.tvDurum)
        val tvTalepZamani: TextView = view.findViewById(R.id.tvTalepZamani)
        val tvOnayZamani: TextView = view.findViewById(R.id.tvOnayZamani)
        val tvTamamZamani: TextView = view.findViewById(R.id.tvTamamZamani)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IhtiyacViewHolder {
        // Takip kartı layout'unu inflate ediyoruz
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ihtiyac_takip, parent, false)
        return IhtiyacViewHolder(view)
    }

    private fun formatZaman(zaman: String?): String {
        if (zaman.isNullOrEmpty()) return "--:--"
        return try {
            zaman.replace("T", " ").substringBefore(".")
        } catch (e: Exception) {
            zaman ?: "--:--"
        }
    }

    override fun onBindViewHolder(holder: IhtiyacViewHolder, position: Int) {
        val ihtiyac = ihtiyacListesi[position]
        val durumMetni = ihtiyac.durum.trim()

        holder.tvUrunAd.text = ihtiyac.urunAdi ?: "Ürün (ID: ${ihtiyac.urunId})"
        holder.tvMiktar.text = "Miktar: ${ihtiyac.miktar}"
        holder.tvDurum.text = durumMetni

        holder.tvTalepZamani.text = "📝 Oluşturulma: ${formatZaman(ihtiyac.talepZamani)}"

        when (durumMetni) {
            "Beklemede" -> {
                holder.tvDurum.setBackgroundColor(Color.parseColor("#E65100"))
                holder.tvOnayZamani.visibility = View.GONE
                holder.tvTamamZamani.visibility = View.GONE
            }
            "Onaylandı" -> {
                holder.tvDurum.setBackgroundColor(Color.parseColor("#2E7D32"))
                holder.tvOnayZamani.visibility = View.VISIBLE
                holder.tvOnayZamani.text = "✅ Onay: ${formatZaman(ihtiyac.onaylanmaZamani)}"
                holder.tvTamamZamani.visibility = View.GONE
            }
            "Tamamlandı" -> {
                holder.tvDurum.setBackgroundColor(Color.parseColor("#455A64"))
                holder.tvOnayZamani.visibility = View.VISIBLE
                holder.tvOnayZamani.text = "✅ Onay: ${formatZaman(ihtiyac.onaylanmaZamani)}"
                holder.tvTamamZamani.visibility = View.VISIBLE
                holder.tvTamamZamani.text = "🏁 Tamamlanma: ${formatZaman(ihtiyac.tamamlanmaZamani)}"
            }
            else -> {
                holder.tvDurum.setBackgroundColor(Color.GRAY)
                holder.tvOnayZamani.visibility = View.GONE
                holder.tvTamamZamani.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = ihtiyacListesi.size

    fun updateList(newList: List<IhtiyacListesi>) {
        ihtiyacListesi.clear()
        ihtiyacListesi.addAll(newList)
        notifyDataSetChanged()
    }
}