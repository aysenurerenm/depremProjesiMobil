package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // Hata çözümü için kritik import

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge desteği eklemek istersen burada enableEdgeToEdge() kullanabilirsin
        setContentView(R.layout.activity_main)

        // 1. XML'deki ID ile uyumlu türü (CardView) belirleyerek bileşeni bağla
        val cardVatandas = findViewById<CardView>(R.id.cardVatandas)

        // 2. Tıklama olayını tanımla
        cardVatandas.setOnClickListener {
            // Vatandaş girişine tıklandığında LoginActivity'ye yönlendir
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}