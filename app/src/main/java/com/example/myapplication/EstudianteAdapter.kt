import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Estudiante
import com.example.myapplication.R

class EstudianteAdapter(
    private var listaEstudiantes: MutableList<Estudiante>,
    private val onItemClick: (Estudiante) -> Unit
) : RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder>() {

    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvCarrera: TextView = itemView.findViewById(R.id.tvMajor)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvBio)
        val ivEstudiante: ImageView = itemView.findViewById(R.id.ivStudentPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = listaEstudiantes[position]
        
        holder.tvNombre.text = estudiante.name
        holder.tvCarrera.text = "Height: ${estudiante.height} cm"
        holder.tvDescripcion.text = "Gender: ${estudiante.gender}"

        holder.itemView.setOnClickListener {
            onItemClick(estudiante)
        }
    }

    override fun getItemCount(): Int = listaEstudiantes.size

    fun updateList(newList: List<Estudiante>) {
        listaEstudiantes.clear()
        listaEstudiantes.addAll(newList)
        notifyDataSetChanged()
    }
}