package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.YukDetay

class YukAdapter(private val yukListesi: List<YukDetay>) :
    RecyclerView.Adapter<YukAdapter.YukViewHolder>() {

    class YukViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUrunAd: TextView = view.findViewById(R.id.txtYukUrunAd)
        val txtMiktar: TextView = view.findViewById(R.id.txtYukMiktar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YukViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_surucu_yuk, parent, false)
        return YukViewHolder(view)
    }

    override fun onBindViewHolder(holder: YukViewHolder, position: Int) {
        val yuk = yukListesi[position]
        holder.txtUrunAd.text = yuk.urunAd
        holder.txtMiktar.text = "Miktar: ${yuk.miktar} Adet"
    }

    override fun getItemCount() = yukListesi.size
}