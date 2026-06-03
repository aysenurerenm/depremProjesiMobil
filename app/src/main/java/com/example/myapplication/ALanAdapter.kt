import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.network.AlanYetkilisi
import com.example.myapplication.network.ToplanmaAlani

class AlanAdapter(
    private val alanlar: List<ToplanmaAlani>,
    private val yetkililer: List<AlanYetkilisi>
) : RecyclerView.Adapter<AlanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtAreaName: TextView = view.findViewById(R.id.txtAreaName)
        val txtAreaLocation: TextView = view.findViewById(R.id.txtAreaLocation)
        val txtOfficerName: TextView = view.findViewById(R.id.txtOfficerName)
        val txtOfficerPhone: TextView = view.findViewById(R.id.txtOfficerPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Senin son paylaştığın XML dosyasının adı neyse R.layout. kısmına onu yaz (örn: item_toplanma_alani)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assembly_area, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alan = alanlar.getOrNull(position)
        val yetkili = yetkililer.getOrNull(position)

        // Verileri XML'deki ilgili yerlere set ediyoruz
        holder.txtAreaName.text = "Toplanma Alanı: ${alan?.alanId?: ""}"
        holder.txtAreaLocation.text = "Konum: ${alan?.enlem ?: "0.0"}, ${alan?.boylam ?: "0.0"}"

        holder.txtOfficerName.text = "Ad Soyad: ${yetkili?.ad ?: "--"} ${yetkili?.soyad ?: ""}"
        holder.txtOfficerPhone.text = "Telefon: ${yetkili?.telefon ?: "--"}"
    }

    override fun getItemCount(): Int = alanlar.size
}