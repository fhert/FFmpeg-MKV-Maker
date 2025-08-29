package com.z.ffmpegmkvmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchivoAdapter(
    private val archivos: List<ArchivoInfo>,
    private val listener: ArchivoListener
) : RecyclerView.Adapter<ArchivoAdapter.ArchivoViewHolder>() {

    interface ArchivoListener {
        fun onArchivoSeleccionado(posicion: Int, seleccionado: Boolean)
    }

    class ArchivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxArchivo)
        val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombreArchivo)
        val detallesTextView: TextView = itemView.findViewById(R.id.textViewDetallesArchivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archivo, parent, false)
        return ArchivoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArchivoViewHolder, position: Int) {
        val archivo = archivos[position]
        
        holder.nombreTextView.text = archivo.nombre
        holder.detallesTextView.text = "Tamaño: ${formatearTamano(archivo.tamano)}"
        
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = archivo.seleccionado
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            archivo.seleccionado = isChecked
            listener.onArchivoSeleccionado(position, isChecked)
        }
    }

    override fun getItemCount() = archivos.size

    private fun formatearTamano(tamano: Long): String {
        if (tamano < 1024) return "$tamano B"
        val kb = tamano / 1024.0
        if (kb < 1024) return String.format("%.2f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }
}
