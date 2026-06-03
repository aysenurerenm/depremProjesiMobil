package com.example.myapplication.network
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

// API'nin en dış katmanı
data class DepremResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("result") val result: List<DepremModel>
)

// Her bir deprem verisi
data class DepremModel(
    @SerializedName("title") val title: String,        // Örn: "SEFERIHISAR (IZMIR)"
    @SerializedName("mag") val mag: Double,           // Büyüklük (4.2)
    @SerializedName("date") val date: String,         // Tarih (2026.05.07)
    @SerializedName("depth") val depth: Double,       // Derinlik (5.0)
    @SerializedName("location_properties") val locationProperties: LocationProperties?,
    @SerializedName("geojson") val geojson: GeoJson
)

// İl ve İlçe detayları (Eğer API direkt ayırmışsa buradan alabilirsin)
data class LocationProperties(
    @SerializedName("epi_center") val epiCenter: EpiCenter?
)

data class EpiCenter(
    @SerializedName("name") val name: String?         // İlçe ismi
)

// Harita entegrasyonu için koordinatlar
data class GeoJson(
    @SerializedName("coordinates") val coordinates: List<Double> // [Boylam, Enlem]
)
// network/DepremApiService.kt
interface DepremApiService {
    // API genellikle limit parametresini destekler
    @GET("live")
    fun getSonDepremler(
        @Query("limit") limit: Int = 10
    ): Call<DepremResponse>
}