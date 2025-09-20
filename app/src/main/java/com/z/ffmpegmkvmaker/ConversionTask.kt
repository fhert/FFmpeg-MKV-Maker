package com.z.ffmpegmkvmaker

import android.content.Context
import android.os.AsyncTask
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

class ConversionTask(
    private val context: Context,
    private val archivo: ArchivoInfo,
    private val configuracion: Configuracion,
    private val carpetaDestino: String?,
    private val listener: ConversionListener
) : AsyncTask<Void, String, Boolean>() {

    interface ConversionListener {
        fun onProgress(progreso: String)
        fun onComplete()
        fun onError(error: String)
    }

    override fun doInBackground(vararg params: Void): Boolean {
        val generator = FFmpegCommandGenerator(context)
        val comandos = generator.generarComandos(
            archivo = archivo,
            configuracion = configuracion,
            carpetaDestino = carpetaDestino,
            listener = object : FFmpegCommandGenerator.DuplicadoListener {
                override fun onDuplicadoDetectado(nombreArchivo: String, carpetaDestino: String) {
                    // Manejar duplicados si es necesario
                    publishProgress("Archivo duplicado detectado: $nombreArchivo")
                }
            }
        )
        
        if (comandos.isNullOrEmpty()) {
            publishProgress("Error: No se pudieron generar los comandos")
            return false
        }
        
        for ((index, comando) in comandos.withIndex()) {
            val pase = if (index == 0) "primer pase" else "segundo pase"
            publishProgress("Ejecutando $pase...")
            publishProgress("Comando: $comando")
            
            val session = FFmpegKit.execute(comando)
            val returnCode = session.returnCode
            
            if (ReturnCode.isSuccess(returnCode)) {
                publishProgress("$pase completado correctamente")
                
                // Mostrar estadísticas de la conversión
                val logs = session.allLogsAsString
                if (logs.isNotEmpty()) {
                    publishProgress("Estadísticas: ${logs.take(200)}...") // Mostrar solo los primeros 200 caracteres
                }
            } else {
                val error = session.failStackTrace
                publishProgress("Error en $pase: $error")
                listener.onError(error)
                return false
            }
        }
        
        return true
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        listener.onProgress(values[0])
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (result) {
            listener.onComplete()
        }
    }
}
