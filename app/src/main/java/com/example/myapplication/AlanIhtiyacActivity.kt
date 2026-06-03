package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.AlanIhtiyac
import com.example.myapplication.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlanIhtiyacActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alan_ihtiyac)

        rv = findViewById(R.id.rvAlanIhtiyaclari)
        rv.layoutManager = LinearLayoutManager(this)

        // 1. ADIM: LoginActivity'den gönderdiğimiz ID'yi alıyoruz
        // Eğer DepoActivity'den buraya geçerken putExtra yapmadıysan SharedPreferences kullanmalısın
        val kullaniciID = intent.getStringExtra("KULLANICI_ID")

        if (kullaniciID != "0") {
            verileriGetir(kullaniciID)
            Log.d("API_ID","ID BUUUNDU: $kullaniciID")
        } else {
            // Alternatif: SharedPreferences'tan dene (Eğer intent boş gelirse)
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val savedID = sharedPref.getString("CITIZEN_ID","w")

            if (savedID != "0") {
                verileriGetir(savedID)
            } else {
                Toast.makeText(this, "Kullanıcı oturum hatası!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verileriGetir(id: String?) {
        // API Interface'inde Path parametresi beklediği için id'yi gönderiyoruz
        RetrofitClient.api.getdepoAlanIhtiyaclari(id).enqueue(object : Callback<List<AlanIhtiyac>> {
            override fun onResponse(call: Call<List<AlanIhtiyac>>, response: Response<List<AlanIhtiyac>>) {
                if (response.isSuccessful && response.body() != null) {
                    val liste = response.body()!!
                    if (liste.isEmpty()) {
                        Toast.makeText(this@AlanIhtiyacActivity, "Bölgenize ait ihtiyaç kaydı yok", Toast.LENGTH_LONG).show()
                    } else {
                        rv.adapter = AlanIhtiyacAdapter(liste)
                        Log.d("API_BASARI", "Gelen veri sayısı: ${liste.size}")
                    }
                } else {
                    Toast.makeText(this@AlanIhtiyacActivity, "Hata: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<AlanIhtiyac>>, t: Throwable) {
                Log.e("API_HATA", "Mesaj: ${t.message}")
                Toast.makeText(this@AlanIhtiyacActivity, "Bağlantı Hatası!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}