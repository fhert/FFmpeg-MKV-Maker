package com.z.ffmpegmkvmaker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

class FFmpegCommandGenerator(private val context: Context) {

    fun generarComandos(
        archivo: ArchivoInfo,
        configuracion: Configuracion,
        carpetaDestino: String?,
        listener: DuplicadoListener
    ): List<String>? {
        val uri = Uri.parse(archivo.uri)
        val nombreArchivo = obtenerNombreArchivo(uri)
        val rutaArchivo = obtenerRutaArchivo(uri)
        val carpetaLogs = File(rutaArchivo).parent + "/logs"
        val carpetaDestinoFinal = carpetaDestino ?: "${context.getExternalFilesDir("Videos")}/Convertidos"
        val nombreSinExtension = nombreArchivo.substringBeforeLast('.')

        // Crear carpetas si no existen
        File(carpetaLogs).mkdirs()
        File(carpetaDestinoFinal).mkdirs()

        val comandos = mutableListOf<String>()

        // Generar comando para el primer pase
        val comandoPase1 = generarComandoPase1(
            rutaArchivo,
            carpetaLogs,
            nombreSinExtension,
            configuracion
        )
        comandos.add(comandoPase1)

        // Generar comando para el segundo pase
        val comandoPase2 = generarComandoPase2(
            rutaArchivo,
            carpetaLogs,
            carpetaDestinoFinal,
            nombreSinExtension,
            configuracion
        )
        comandos.add(comandoPase2)

        return comandos
    }

    private fun generarComandoPase1(
        rutaArchivo: String,
        carpetaLogs: String,
        nombreSinExtension: String,
        configuracion: Configuracion
    ): String {
        val comando = StringBuilder()
        
        comando.append("ffmpeg -ss 00:00:00.000 -to 10:00:00.000 -i \"$rutaArchivo\" ")
        comando.append("-pass 1 -passlogfile \"$carpetaLogs/${nombreSinExtension}_log\" ")
        
        // Añadir filtros de video según la configuración
        comando.append(generarFiltrosVideo(configuracion))
        
        comando.append("-map 0:v -map 0:a? -map 0:s? ")
        comando.append("-c:v libx264 -profile:v high -level 4.1 -preset medium -b:v 12M ")
        comando.append("-x264-params \"cabac=1:ref=1:deblock=-1,-1:analyse=0x3:me=hex:subme=5:psy=0:mixed_ref=0:trellis=0:fast_pskip=0:threads=4:bframes=2:b_pyramid=0:weightb=0:weightp=0:keyint=24:keyint_min=13:mbtree=0\" ")
        comando.append("-c:a copy -c:s copy -f null NUL")
        
        return comando.toString()
    }

    private fun generarComandoPase2(
        rutaArchivo: String,
        carpetaLogs: String,
        carpetaDestino: String,
        nombreSinExtension: String,
        configuracion: Configuracion
    ): String {
        val comando = StringBuilder()
        
        comando.append("ffmpeg -ss 00:00:00.000 -to 10:00:00.000 -i \"$rutaArchivo\" ")
        comando.append("-pass 2 -passlogfile \"$carpetaLogs/${nombreSinExtension}_log\" ")
        
        // Añadir filtros de video según la configuración
        comando.append(generarFiltrosVideo(configuracion))
        
        comando.append("-map 0:v -map 0:a? -map 0:s? ")
        comando.append("-c:v libx264 -profile:v high -level 4.1 -preset medium -b:v 12M ")
        comando.append("-x264-params \"cabac=1:ref=1:deblock=-1,-1:analyse=0x3:me=hex:subme=5:psy=0:mixed_ref=0:trellis=0:fast_pskip=0:threads=4:bframes=2:b_pyramid=0:weightb=0:weightp=0:keyint=24:keyint_min=13:mbtree=0\" ")
        comando.append("-c:a copy -c:s copy \"$carpetaDestino/${nombreSinExtension}.mkv\"")
        
        return comando.toString()
    }

    private fun generarFiltrosVideo(configuracion: Configuracion): String {
        val filtros = StringBuilder()
        filtros.append("-vf \"")
        
        // Añadir crop según la relación de aspecto
        when (configuracion.relacionAspecto) {
            "4∶3" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw-480:ih:240:0,")
                } else { // 4K
                    filtros.append("crop=iw-960:ih:480:0,")
                }
            }
            "3∶2" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw-300:ih:150:0,")
                } else { // 4K
                    filtros.append("crop=iw-600:ih:300:0,")
                }
            }
            "5∶3" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw-120:ih:60:0,")
                } else { // 4K
                    filtros.append("crop=iw-240:ih:120:0,")
                }
            }
            "1.85∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-44:0:22,")
                } else { // 4K
                    filtros.append("crop=iw:ih-88:0:44,")
                }
            }
            "1.9:1 (IMAX)" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-70:0:35,")
                } else { // 4K
                    filtros.append("crop=iw:ih-140:0:70,")
                }
            }
            "2.0∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-120:0:60,")
                } else { // 4K
                    filtros.append("crop=iw:ih-240:0:120,")
                }
            }
            "2.1∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-166:0:83,")
                } else { // 4K
                    filtros.append("crop=iw:ih-332:0:166,")
                }
            }
            "2.2∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-208:0:104,")
                } else { // 4K
                    filtros.append("crop=iw:ih-416:0:208,")
                }
            }
            "2.35∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-264:0:132,")
                } else { // 4K
                    filtros.append("crop=iw:ih-528:0:264,")
                }
            }
            "2.37∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-270:0:135,")
                } else { // 4K
                    filtros.append("crop=iw:ih-540:0:270,")
                }
            }
            "2.40∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-280:0:140,")
                } else { // 4K
                    filtros.append("crop=iw:ih-560:0:280,")
                }
            }
            "2.75∶1" -> {
                if (configuracion.resolucion == "FHD") {
                    filtros.append("crop=iw:ih-382:0:191,")
                } else { // 4K
                    filtros.append("crop=iw:ih-764:0:382,")
                }
            }
        }
        
        // Añadir scale según la resolución
        if (configuracion.resolucion == "4K") {
            filtros.append("scale=iw/2:ih/2,")
        }
        
        // Añadir tonemap según el rango dinámico
        if (configuracion.rangoDinamico == "HDR") {
            filtros.append("zscale=t=linear:npl=100,format=gbrpf32le,zscale=p=bt709,tonemap=hable:desat=0,zscale=t=bt709:m=bt709:r=tv,")
        }
        
        filtros.append("format=yuv420p\" ")
        
        return filtros.toString()
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return "Desconocido"
    }

    private fun obtenerRutaArchivo(uri: Uri): String {
        // En una implementación real, necesitaríamos copiar el archivo a un almacenamiento accesible
        // Por ahora, devolvemos la URI como cadena
        return uri.toString()
    }

    interface DuplicadoListener {
        fun onDuplicadoDetectado(nombreArchivo: String, carpetaDestino: String)
    }
}
