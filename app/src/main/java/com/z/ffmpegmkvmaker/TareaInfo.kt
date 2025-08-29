package com.z.ffmpegmkvmaker

data class TareaInfo(
    val nombre: String,
    val estado: Estado,
    val mensajeError: String? = null
)
