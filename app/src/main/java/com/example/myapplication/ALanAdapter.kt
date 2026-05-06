// AlanAdapter.kt
package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Alan

class AlanAdapter(private var alanListesi: List<Alan>) :
    RecyclerView.Adapter<AlanAdapter.AlanViewHolder>() {

    class AlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAd: TextView = view.findViewById(R.id.txtAlanAd)
        val txtId: TextView = view.findViewById(R.id.txtAlanId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alan, parent, false)
        return AlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlanViewHolder, position: Int) {
        val alan = alanListesi[position]
        holder.txtAd.text = alan.mahalleAdi
        holder.txtId.text = "Bölge ID: ${alan.id}"
    }

    override fun getItemCount() = alanListesi.size

    // Listeyi güncellemek için yardımcı fonksiyon
    fun guncelle(yeniListe: List<Alan>) {
        alanListesi = yeniListe
        notifyDataSetChanged()
    }
}