package com.z.ffmpegmkvmaker

data class ArchivoInfo(
    val nombre: String,
    val tamano: Long,
    val uri: String,
    var seleccionado: Boolean = false
)
