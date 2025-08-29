package com.z.ffmpegmkvmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TareaAdapter(
    private val tareas: List<TareaInfo>,
    private val listener: TareaListener
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    interface TareaListener {
        fun onOpcionesTareaPulsado(posicion: Int)
        fun onTareaPulsadaLargo(posicion: Int)
    }

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numeroTextView: TextView = itemView.findViewById(R.id.textViewNumeroTarea)
        val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombreTarea)
        val detallesTextView: TextView = itemView.findViewById(R.id.textViewDetallesTarea)
        val estadoTextView: TextView = itemView.findViewById(R.id.textViewEstadoTarea)
        val opcionesButton: ImageButton = itemView.findViewById(R.id.botonOpcionesTarea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        
        holder.numeroTextView.text = "${position + 1}."
        holder.nombreTextView.text = tarea.nombre
        holder.detallesTextView.text = "Resolución: 1080p SDR | Bitrate: 12M | Audio: Passthrough | Subs: Passthrough"
        
        when (tarea.estado) {
            Estado.PENDIENTE -> {
                holder.estadoTextView.text = "Pendiente"
                holder.estadoTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
            }
            Estado.PROCESANDO -> {
                holder.estadoTextView.text = "Procesando..."
                holder.estadoTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light))
            }
            Estado.COMPLETADO -> {
                holder.estadoTextView.text = "Completado"
                holder.estadoTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light))
            }
            Estado.ERROR -> {
                holder.estadoTextView.text = "Error"
                holder.estadoTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light))
            }
        }
        
        holder.opcionesButton.setOnClickListener {
            listener.onOpcionesTareaPulsado(position)
        }
        
        holder.itemView.setOnLongClickListener {
            listener.onTareaPulsadaLargo(position)
            true
        }
    }

    override fun getItemCount() = tareas.size
}
