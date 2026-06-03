package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemFamilyBinding
import com.example.myapplication.network.CitizenFamily

class FamilyAdapter(private val familyList: List<CitizenFamily>) :
    RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder>() {

    class FamilyViewHolder(val binding: ItemFamilyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val binding = ItemFamilyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FamilyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        val member = familyList[position]

        holder.binding.apply {
            // İsim ve soyismi birleştirirken trim() kullanarak gereksiz boşlukları önleyebiliriz
            val fullName = "${member.yakinAd ?: ""} ${member.yakinSoyad ?: ""}".trim()
            tvFamilyMemberName.text = if (fullName.isEmpty()) "Bilinmeyen Üye" else fullName

            // Durum kontrolü
            val durum = member.yakinDurum ?: "Durum belirtilmedi"
            tvFamilyMemberStatus.text = durum

            // HATA DÜZELTMESİ: Burada aciklama zaten null değilse atanıyor,
            // ancak alt satırda tekrar null kontrolü yapılmış. Şöyle daha temiz:
            tvFamilyMemberExplain.text = member.yakinAciklama.takeUnless { it.isNullOrBlank() } ?: "Açıklama belirtilmedi"
            tvFamilyMemberTime.text = member.yakinSonGuncelleme.takeUnless { it.isNullOrBlank() }
            // Renk değişimi mantığı
            // 'setBackgroundResource' yerine 'setBackgroundColor' ve ContextCompat kullanmak daha garantidir
            val context = root.context
            val indicatorColor = if (durum.contains("Güvende", ignoreCase = true)) {
                android.R.color.holo_green_dark
            } else {
                android.R.color.holo_red_dark
            }

            viewStatusIndicator.setBackgroundResource(indicatorColor)
        }
    }


    override fun getItemCount() = familyList.size
}