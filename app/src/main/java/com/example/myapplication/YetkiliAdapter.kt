package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.YetkiliDetay

class YetkiliAdapter(private val yetkiliListesi: List<YetkiliDetay>) :
    RecyclerView.Adapter<YetkiliAdapter.YetkiliViewHolder>() {

    class YetkiliViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAd: TextView = view.findViewById(R.id.txtYetkiliAd)
        val txtTel: TextView = view.findViewById(R.id.txtYetkiliTel)
        val btnAra: ImageButton = view.findViewById(R.id.btnAra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YetkiliViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_yetkili, parent, false)
        return YetkiliViewHolder(view)
    }

    override fun onBindViewHolder(holder: YetkiliViewHolder, position: Int) {
        val yetkili = yetkiliListesi[position]
        holder.txtAd.text = "${yetkili.ad} ${yetkili.soyad}"
        holder.txtTel.text = yetkili.telefon

        // Telefon butonuna basınca direkt arama ekranını açar
        holder.btnAra.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${yetkili.telefon}")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = yetkiliListesi.size
}