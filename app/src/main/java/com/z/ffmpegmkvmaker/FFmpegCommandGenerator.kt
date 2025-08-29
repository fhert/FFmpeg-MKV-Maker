package com.z.ffmpegmkvmaker

import android.content.Context

class FFmpegCommandGenerator(private val context: Context) {

    fun generarComandos(
        archivo: ArchivoInfo,
        configuracion: Configuracion,
        carpetaDestino: String?,
        listener: DuplicadoListener
    ): List<String>? {
        // Implementación simplificada
        return listOf("ffmpeg -i input.mkv output.mkv")
    }

    interface DuplicadoListener {
        fun onDuplicadoDetectado(nombreArchivo: String, carpetaDestino: String)
    }
}
