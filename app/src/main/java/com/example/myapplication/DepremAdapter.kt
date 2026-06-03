package com.example.myapplication

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemDepremBinding
import com.example.myapplication.network.DepremModel

class DepremAdapter(private var list: List<DepremModel>) : RecyclerView.Adapter<DepremAdapter.DepremViewHolder>() {

    class DepremViewHolder(val binding: ItemDepremBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepremViewHolder {
        val binding = ItemDepremBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DepremViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepremViewHolder, position: Int) {
        val deprem = list[position]

        // 1. İl ve İlçe Ayıklama
        val fullTitle = deprem.title
        val ilce = fullTitle.substringBefore(" (")
        val il = fullTitle.substringAfter("(").substringBefore(")")
        holder.binding.tvLocation.text = "$il - $ilce"

        // 2. Büyüklük ve Detaylar
        holder.binding.tvMag.text = deprem.mag.toString()
        holder.binding.tvDetails.text = "Tarih: ${deprem.date} | Derinlik: ${deprem.depth} km"

        // 3. Dinamik Renk (Büyüklüğe göre)
        val color = when {
            deprem.mag < 3.0 -> "#4CAF50" // Yeşil
            deprem.mag < 4.5 -> "#FF9800" // Turuncu
            else -> "#F44336"             // Kırmızı
        }
        val drawable = holder.binding.tvMag.background as GradientDrawable
        drawable.setColor(Color.parseColor(color))
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<DepremModel>) {
        this.list = newList
        notifyDataSetChanged()
    }
}