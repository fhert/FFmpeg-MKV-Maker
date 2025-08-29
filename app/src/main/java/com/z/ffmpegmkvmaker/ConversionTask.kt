package com.z.ffmpegmkvmaker

import android.content.Context
import android.os.AsyncTask

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
        // Simulación de conversión
        publishProgress("Iniciando conversión...")
        Thread.sleep(1000)
        publishProgress("Procesando video...")
        Thread.sleep(2000)
        publishProgress("Finalizando conversión...")
        Thread.sleep(1000)
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
        } else {
            listener.onError("Error en la conversión")
        }
    }
}
