package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

// --- DATA CLASSES ---
data class ApiResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("detail") val detail: String? = null // Bazı DRF hataları 'detail' anahtarıyla gelir
)
data class LocationUpdateRequest(
    val durum: String, // "güvendeyim" veya "yardım lazım"
    val enlem: Double,
    val boylam: Double
)
data class LoginRequest(
    val tc: String,
    val sifre: String
)

data class LoginResponse(
    val status: String,
    val message: String?,
    val ad: String?,
    val soyad: String?,
    val rolID: Int?,
    val kullaniciID: String?
)

data class CitizenInfo(
    @SerializedName("Ad") val Ad: String?,
    @SerializedName("Soyad") val Soyad: String?,
    @SerializedName("kullaniciID") val kullaniciID: String?,
    @SerializedName("kullaniciDurum") val kullaniciDurum: String?,
    @SerializedName("lastUpdate") val lastUpdate: String?,
    @SerializedName("familyCount") val familyCount: Int?,
    @SerializedName("safeFamilyCount") val safeFamilyCount: Int?,
    @SerializedName("rolID") val rolID: Int?,
    @SerializedName("enlem") val enlem: Double?,
    @SerializedName("boylam") val boylam:Double?,
    @SerializedName("durumGuncellemeZamani") val durumGuncellemeZamani:String?,
    @SerializedName("TC") val tc: String,


)
data class KullaniciKayit(
    @SerializedName("kullaniciID") val kullaniciID: Int? = null,
    @SerializedName("Ad") val ad: String,
    @SerializedName("Soyad") val soyad: String,
    @SerializedName("TC") val tc: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefon") val telefon: String,
    @SerializedName("yas") val yas: Int?,
    @SerializedName("cinsiyet") val cinsiyet: Boolean,
    @SerializedName("Sifre") val sifre: String? = null, // Kayıt esnasında gönderilir, okurken boş döner
    //@SerializedName("rolID") val rolID: Int? = null,
    @SerializedName("kullaniciDurum") val durum: String? = "bilinmiyor"
)
data class Alan(
    @SerializedName("alanID") val id: Int,
    @SerializedName("mahalleAdı") val mahalleAdi: String,
    @SerializedName("enlem") val enlem: Double?,
    @SerializedName("boylam") val boylam: Double?
)
data class CitizenFamily(
    @SerializedName("yakinID") val yakinID: Int?,
    @SerializedName("yakin_ad") val yakinAd: String?,
    @SerializedName("yakin_soyad") val yakinSoyad: String?,
    @SerializedName("yakin_durum") val yakinDurum: String?,
    @SerializedName("yakin_telefon") val yakinTelefon: String?,
    @SerializedName("yakin_aciklama") val yakinAciklama: String?,
    @SerializedName("yakin_durum_guncelleme_zamani") val yakinSonGuncelleme: String?
)
data class AssemblyAreaResponse(
    val alanlar: List<ToplanmaAlani>,
    val yetkililer: List<AlanYetkilisi>
)
data class AlanYetkilisi(
    @SerializedName("kullaniciID") val kullaniciId: Int,
    @SerializedName("ad") val ad: String,
    @SerializedName("soyad") val soyad: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefon") val telefon: String,
    @SerializedName("alanID") val alanId: Int,

)

data class ToplanmaAlani(
    @SerializedName("alanID") val alanId: Int,

    @SerializedName("enlem") val enlem: Double,
    @SerializedName("boylam") val boylam: Double
)
data class YakinEkleRequest(
    @SerializedName("yakin_tc") val yakinTc: String
)

// Sunucudan dönecek başarı yanıtı (Response)
data class YakinEkleResponse(
    @SerializedName("message") val message: String,
    @SerializedName("eklenen_yakin") val eklenenYakin: EklenenYakinInfo?
)
data class EklenenYakinInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("ad") val ad: String,
    @SerializedName("soyad") val soyad: String,
    @SerializedName("durum") val durum: String
)
// Eksik olan ana yanıt modeli

data class IhtiyacListesi(
    @SerializedName("urunID")
    val urunId: Int,
    @SerializedName("urun_adi") val urunAdi: String?,

    @SerializedName("ihtiyacmiktar")
    val miktar: Int,

    @SerializedName("ihtiyacdurum")
    val durum: String = "Beklemede",

    @SerializedName("talepZamanı")
    val talepZamani: String? = null, // Örn: "2024-05-20T14:30:00Z"
    @SerializedName("onaylanmaZamanı")
    val onaylanmaZamani: String? = null, // Örn: "2024-05-20T14:30:00Z"
    @SerializedName("tamamlanmaZamanı")
    val tamamlanmaZamani: String?


)
data class IhtiyacTalebi(
    @SerializedName("urunID")
    val urunId: Int,
    @SerializedName("urun_adi") val urunAdi: String?,

    @SerializedName("ihtiyacmiktar")
    val miktar: Int,

    @SerializedName("ihtiyacdurum")
    val durum: String = "Beklemede",

)


data class AlanYetkilisiDurumGuncelleme(
    @SerializedName("TC")
    val tc: String,

    @SerializedName("kullaniciDurum")
    val kullaniciDurum: String,

    @SerializedName("aciklama")
    val aciklama: String? = null
)


data class SahaKullaniciKayit(
    @SerializedName("Ad") val ad: String,
    @SerializedName("Soyad") val soyad: String,
    @SerializedName("TC") val tc: String,
    @SerializedName("kullaniciDurum") val durum: String,
    @SerializedName("aciklama") val aciklama: String?
    // roID, enlem, boylam gibi sayısal alanları buradan çıkarın veya
    // göndermek zorundaysanız String yerine Int/Double yapın.
)
// --- INTERFACE ---
data class AlanIhtiyac(
    @SerializedName("ihtiyacID") val ihtiyacID: Int,
    @SerializedName("alanID") val alanID: Int,

    // Backend'de eklediğimiz "urunAd" anahtarı ile birebir aynı olmalı
    @SerializedName("urunAd") val urunAd: String?,

    @SerializedName("miktar") val miktar: Int,

    // Backend'de "toplanmaAlani" olarak gönderiyoruz
    @SerializedName("toplanmaAlani") val alanAd: String,

    @SerializedName("durum") val durum: String,

    // Zaman bilgilerini de ekleyelim (Opsiyonel ama iyi olur)
    @SerializedName("talepZamani") val talepZamani: String?,
    @SerializedName("onaylanmaZamani") val onaylanmaZamani: String?,
    @SerializedName("tamamlanmaZamani") val tamamlanmaZamani: String?
)

data class AlanResponse(
    @SerializedName("alanID") val alanId: Int
)
data class Urun(
    @SerializedName("urunID")
    val urunID: Int,

    @SerializedName("urunAd")
    val urunAd: String,

    @SerializedName("urunHacim")
    val urunHacim: Int,

    @SerializedName("Aciliyet")
    val aciliyet: String? = null
)
data class DurumBildirimResponse(
    @SerializedName("id") val id: String,
    @SerializedName("ad") val ad: String,
    @SerializedName("soyad") val soyad: String,
    @SerializedName("durum") val durum: String,
    @SerializedName("mesaj") val mesaj: String
)
// ApiService.kt dosyanın içindeki modeli bununla güncelleyin:
data class YakinlikBildirimiResponse(
    @SerializedName("id") val id: String, // Int yerine String yaptık
    @SerializedName("ekleyenAd") val ekleyenAd: String?,
    @SerializedName("ekleyenSoyad") val ekleyenSoyad: String?
)
data class DurumGuncellemeResponse(
    // Django'dan gelen "id" alanını Kotlin'deki String id alanına eşler
    @SerializedName("id")
    val id: String,

    // Yakının adı (Kullanici modelindeki 'Ad' alanı)
    @SerializedName("ad")
    val ad: String?,

    // Yakının soyadı (Kullanici modelindeki 'Soyad' alanı)
    @SerializedName("soyad")
    val soyad: String?,

    // Django tarafının hazırladığı "Tehlikede" veya "Güvende" metni
    @SerializedName("durum")
    val durum: String?
)
data class DepoUrun(
    @SerializedName("urunAd") val urunAd: String,
    @SerializedName("miktar") val miktar: Int,
    @SerializedName("birim") val birim: String,
    @SerializedName("kritik") val kritik: Boolean
)
data class DepoOzet(
    val toplamKalem: Int,
    val kritikStok: Int,
    @SerializedName("aktifSevkiyat")
    val aktifSevkiyat: Int,
    @SerializedName("Ad") val depoAdi: String?
)

data class DepoResponse(
    val status: String,
    val ozet: DepoOzet?,
    val urunler: List<DepoUrun>?
)


data class Sevkiyat(
    @SerializedName("surucuAd") val surucuAd: String,
    @SerializedName("surucuSoyad") val surucuSoyad: String,
    @SerializedName("plaka") val plaka: String,
    @SerializedName("aracTip") val aracTip: String,
    @SerializedName("alanAd") val alanAd: String,
    @SerializedName("depoAd") val depoAd: String,
    @SerializedName("durum") val durum: String,
    @SerializedName("miktar") val miktar: Int,
    @SerializedName("onaylanmaZamani") val onaylanmaZamani: String?,
    @SerializedName("sevkiyatZamani") val sevkiyatZamani: String?
)
data class TamamlananYardimResponse(
    @SerializedName("ihtiyacID") val ihtiyacID: Int,
    @SerializedName("alanAd") val alanAd: String?,
    @SerializedName("urunAd") val urunAd: String?,
    @SerializedName("miktar") val miktar: Int, // API'den "miktar" geliyor
    @SerializedName("durum") val durum: String?,
    @SerializedName("tamamlanmaZamani") val tamamlanmaZamani: String?
)
// network/ApiService.kt içine ekle veya güncelle

data class SurucuResponse(
    val status: String,
    val aracBilgi: AracBilgi?,
    val rota: RotaBilgi?,
    val yukler: List<YukDetay>?,
    val yetkililer: List<YetkiliDetay>?,
    val depo_yetkilileri: List<YetkiliDetay>?
)

data class AracBilgi(
    val plaka: String,
    val durum: String
)



data class RotaBilgi(
    val kalkis: String,
    val varis: String,

    @SerializedName("start_lat")
    val startLat: Double?,

    @SerializedName("start_lng")
    val startLng: Double?,

    @SerializedName("end_lat")
    val endLat: Double?,

    @SerializedName("end_lng")
    val endLng: Double?
)

data class YukDetay(
    val urunAd: String,
    val miktar: Int
)

data class YetkiliDetay(
    val ad: String,
    val soyad: String,
    val telefon: String,
    val email: String?
)
data class TamamlananYardim(
    val ulasimID: Int,
    val depoID: Int,
    val depoAd: String,
    val alanID: Int,
    val alanAd: String,
    val aracID: Int,
    val urunID: Int,
    val urunAd: String,
    val yukID: Int,
    val durum: String,
    val yukMiktar: Int
)
data class UrunItem(
    @SerializedName("urunID")
    val urunID: Int,

    @SerializedName("urunAd")
    val urunAd: String
)

data class DepoUrunEkleResponse(
    val status: String,
    val message: String
)
data class SevkiyatResponse(
    val message: String,
    val durum: String,
    val sevkiyatZamani: String
)
interface ApiService {
    @POST("login/")
    fun loginControl(@Body loginData: Map<String, String>): Call<LoginResponse>

    // Birisi beni yakını olarak eklediyse o bildirimleri getiren servis
    @GET("yeni_bildirimler/{id}/")
    fun getYakinlikBildirimleri(@Path("id") id: String): Call<List<YakinlikBildirimiResponse>>

    @GET("citizen/{id}/")
    fun get_citizen_details(@Path("id") id: String): Call<CitizenInfo>

    @GET("family/{id}/")
    fun get_citizen_family(@Path("id") id: String): Call<List<CitizenFamily>>

    @POST("update-status/{id}/")
    fun update_status(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Call<Map<String, String>>

    @GET("toplanma-alanim/{id}/")
    fun getToplanmaAlanim(
        @Path("id") id: String
    ): Call<AssemblyAreaResponse>
    @GET("alanlar") // Örnek endpoint
    fun getAlanlar(): Call<List<ToplanmaAlani>>
    @POST("register/") // Django'daki urls.py'da tanımladığın path
    fun registerUser(@Body kullanici: KullaniciKayit): Call<ApiResponse>

    @POST("update-location/{id}/")
    fun updateLocation(
        @Path("id") id: String,
        @Body locationData: LocationUpdateRequest
    ): Call<ApiResponse>
    @POST("yakin-ekle/{id}/")
    fun yakinEkle(
        @Path("id") id: String, // İstek yapan kullanıcının ID'si
        @Body body: YakinEkleRequest
    ): Call<YakinEkleResponse>

    @GET("urunler/")
    fun getUrunler(): Call<List<Urun>>

    // 2. Dashboard verilerini çek (Hata aldığın yer burasıydı)
    // URL'deki {id} ile @Path("id") isminin birebir aynı olması gerekir.
    @GET("alan-ihtiyaclari/{id}/")
    fun getAlanIhtiyaclari(@Path("id") alanId: Int): Call<List<IhtiyacListesi>>

    // 3. İhtiyaç Ekle (Burada senin kodunda {id} yazıp @Path("pk") demiştin, düzelttim)
    @POST("toplanma-alanina-ihtiyac-ekle/{id}/")
    fun toplanmaAlaninaIhtiyacEkle(
        @Path("id") alanId: Int,
        @Body talep: IhtiyacTalebi
    ): Call<ApiResponse>
    // ApiService.kt içine ekle
    @GET("get-yetkili-alan/{citizenId}/")
    fun getYetkiliAlan(@Path("citizenId") citizenId: String): Call<AlanResponse>

    @POST("sahaKullaniciKayit/") // Django'daki yeni endpoint adınız
    fun sahaKullaniciKaydi(@Body kullanici: SahaKullaniciKayit): Call<ApiResponse>

    @POST("alan-yetkilis-durum-guncelle/")
    suspend fun durumGuncelle(@Body guncelleme: AlanYetkilisiDurumGuncelleme): Response<ApiResponse>

    @GET("getDurumGuncellemeleri/{pk}/")
    fun getDurumGuncellemeleri(
        @Path("pk") pk: String
    ): Call<List<DurumBildirimResponse>>








    @GET("depo-stok/{kullanici_id}/")
    fun getDepoStok(@Path("kullanici_id") kullaniciID: String?): Call<DepoResponse>

    @GET("depoalan-ihtiyaclari/{kullanici_id}/")
    fun getdepoAlanIhtiyaclari(@Path("kullanici_id") kullaniciID: String?): Call<List<AlanIhtiyac>>

    @POST("ihtiyac-onayla/{id}/")
    fun ihtiyacOnayla(
        @Path("id") id: Int
    ): Call<Map<String, String>>
    @GET("depo-sevkiyat/{kullanici_id}/")
    fun getSevkiyatListesi(
        @Path("kullanici_id") kullaniciId: String?
    ): Call<List<Sevkiyat>>
    @GET("tamamlanan-yardimlar/{kullanici_id}/")
    fun getTamamlananYardimlar(
        @Path("kullanici_id") kullaniciId: String?
    ): Call<List<TamamlananYardimResponse>>
    @GET("depodakiurunler/")
    fun getdepodakiurunler(): Call<List<UrunItem>>

    @FormUrlEncoded
    @POST("depo-urun-ekle/")
    fun depoUrunEkle(

        @Field("kullaniciID")
        kullaniciID: String?,

        @Field("urunID")
        urunID: Int,

        @Field("miktar")
        miktar: Int

    ): Call<DepoUrunEkleResponse>
    @GET("surucu-panel/{kullanici_id}/")
    fun getSurucuPanelVerileri(@Path("kullanici_id") kullaniciId: String?): Call<SurucuResponse>
    @POST("sevkiyat-baslat/{kullanici_id}/")
    suspend fun sevkiyatBaslat(@Path("kullanici_id") id: String?): Response<SevkiyatResponse>
    @POST("sevkiyat-tamamla/{id}/")
    fun sevkiyatTamamla(@Path("id") id: String?): Call<Map<String, String>>

    @GET("tamamlanan-sevkiyatlar/{kullanici_id}/")
    fun getTamamlananSurucuYardimlar(@Path("kullanici_id") kullaniciId: String?): Call<List<TamamlananYardim>>

// AlanResponse içinde alanID dönmeli
}