package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.TamamlananYardim
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TamamlananSurucuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tamamlanansurucu)

        recyclerView = findViewById(R.id.recyclerViewTamamlanan)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val kullaniciId = intent.getStringExtra("kullanici_id")

        Log.e("DEBUG_ID", "Gelen Kullanici ID: $kullaniciId")

        if (kullaniciId != "0") {
            getTamamlananlar(kullaniciId)
        } else {
            Toast.makeText(this, "Kullanıcı ID bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTamamlananlar(kullaniciId: String?) {

        Log.e("API_CALL", "Request gidiyor... ID: $kullaniciId")

        RetrofitClient.api.getTamamlananSurucuYardimlar(kullaniciId)
            .enqueue(object : Callback<List<TamamlananYardim>> {

                override fun onResponse(
                    call: Call<List<TamamlananYardim>>,
                    response: Response<List<TamamlananYardim>>
                ) {

                    Log.e("API_CODE", "Response code: ${response.code()}")

                    Log.e("API_SUCCESS", "Success: ${response.isSuccessful}")

                    Log.e("API_RAW_BODY", "Body: ${response.body()}")

                    if (response.isSuccessful) {

                        val liste = response.body() ?: emptyList()

                        Log.e("LIST_SIZE", "Liste size: ${liste.size}")

                        for (item in liste) {
                            Log.e("ITEM", item.toString())
                        }

                        recyclerView.adapter = TamamlananSurucuAdapter(liste)

                    } else {

                        Log.e("API_ERROR_BODY",
                            response.errorBody()?.string() ?: "Boş error body"
                        )

                        Toast.makeText(
                            this@TamamlananSurucuActivity,
                            "Veri alınamadı (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<TamamlananYardim>>, t: Throwable) {

                    Log.e("API_FAILURE", "Hata: ${t.message}")
                    Log.e("API_FAILURE_STACK", Log.getStackTraceString(t))

                    Toast.makeText(
                        this@TamamlananSurucuActivity,
                        "İnternet hatası",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}