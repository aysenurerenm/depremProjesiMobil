package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.TamamlananYardim

class TamamlananSurucuAdapter(
    private val liste: List<TamamlananYardim>
) : RecyclerView.Adapter<TamamlananSurucuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtDepoAd: TextView = view.findViewById(R.id.txtDepoAd)
        val txtAlanAd: TextView = view.findViewById(R.id.txtAlanAd)
        val txtUrunAd: TextView = view.findViewById(R.id.txtUrunAd)
        val txtArac: TextView = view.findViewById(R.id.txtArac)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tamamlanansurucu, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = liste[position]

        holder.txtDepoAd.text = "Depo: ${item.depoAd}"
        holder.txtAlanAd.text = "Alan: ${item.alanAd}"
        holder.txtUrunAd.text = "Ürün: ${item.urunAd}"
        holder.txtArac.text = "Araç ID: ${item.aracID}"
    }

    override fun getItemCount(): Int = liste.size
}