package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.Urun
import com.example.myapplication.network.DepoUrun

class DepoStokAdapter(private val urunListesi: List<DepoUrun>) :
    RecyclerView.Adapter<DepoStokAdapter.UrunViewHolder>() {

    class UrunViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUrunAd: TextView = view.findViewById(R.id.txtUrunAd)
        val txtMiktar: TextView = view.findViewById(R.id.txtMiktar)
        val txtBirim: TextView = view.findViewById(R.id.txtBirim)
        val imgDurumOk: ImageView = view.findViewById(R.id.imgDurumOk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stok, parent, false)
        return UrunViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrunViewHolder, position: Int) {
        val urun = urunListesi[position]

        holder.txtUrunAd.text = urun.urunAd
        holder.txtMiktar.text = urun.miktar.toString()
        holder.txtBirim.text = urun.birim

        if (urun.kritik) {
            // Kritik stok → kırmızı
            holder.txtMiktar.setTextColor(Color.RED)
            holder.imgDurumOk.setImageResource(android.R.drawable.arrow_down_float)
        } else {
            // Normal → yeşil
            holder.txtMiktar.setTextColor(Color.parseColor("#10B981"))
            holder.imgDurumOk.setImageResource(android.R.drawable.arrow_up_float)
        }
    }

    override fun getItemCount(): Int = urunListesi.size
}