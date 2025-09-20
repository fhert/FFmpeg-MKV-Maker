package com.z.ffmpegmkvmaker

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.z.ffmpegmkvmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ConversionTask.ConversionListener, FFmpegCommandGenerator.DuplicadoListener, ArchivoAdapter.ArchivoListener, TareaAdapter.TareaListener {

    private lateinit var binding: ActivityMainBinding
    private val archivosSeleccionados = mutableListOf<ArchivoInfo>()
    private val tareas = mutableListOf<TareaInfo>()
    private var carpetaDestinoGlobal: String? = null
    private var pasoActual = 0
    private val configuracion = Configuracion()
    private var tareaActual: ConversionTask? = null
    private var archivoPendiente: ArchivoInfo? = null
    private var comandosPendientes: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerViews()
        configurarBotones()
        configurarTabLayout()
    }

    private fun configurarRecyclerViews() {
        binding.recyclerViewArchivos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewArchivos.adapter = ArchivoAdapter(archivosSeleccionados, this)

        binding.recyclerViewTareas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTareas.adapter = TareaAdapter(tareas, this)
    }

    private fun configurarBotones() {
        binding.botonAgregar.setOnClickListener { seleccionarArchivos() }
        binding.botonAgregarBluray.setOnClickListener { seleccionarCarpetaBluray() }
        binding.botonDestino.setOnClickListener { seleccionarCarpetaDestino() }
        binding.botonConvertir.setOnClickListener { iniciarConversion() }
    }

    private fun configurarTabLayout() {
        binding.tabLayoutPasos.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pasoActual = tab?.position ?: 0
                mostrarBotonesPasoActual()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private val seleccionadorArchivos = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            val nombre = obtenerNombreArchivo(uri)
            val tamano = obtenerTamanoArchivo(uri)
            archivosSeleccionados.add(ArchivoInfo(nombre, tamano, uri.toString()))
        }
        binding.recyclerViewArchivos.adapter?.notifyDataSetChanged()
        if (archivosSeleccionados.isNotEmpty()) {
            pasoActual = 1
            binding.tabLayoutPasos.addTab(binding.tabLayoutPasos.newTab().setText("Archivos"))
            mostrarBotonesPasoActual()
        }
    }

    private fun seleccionarArchivos() {
        seleccionadorArchivos.launch(arrayOf("*/*"))
    }

    private fun seleccionarCarpetaBluray() {
        // Implementar lógica para escanear carpeta Blu-ray
        Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show()
    }

    private val selectorCarpetaDestino = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            carpetaDestinoGlobal = uri.toString()
            Toast.makeText(this, "Carpeta de destino seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarCarpetaDestino() {
        selectorCarpetaDestino.launch(null)
    }

private fun iniciarConversion() {
    if (archivosSeleccionados.isEmpty()) {
        Toast.makeText(this, "No hay archivos seleccionados", Toast.LENGTH_SHORT).show()
        return
    }

    // Verificar que todos los pasos estén completados
    if (pasoActual < 6) {
        Toast.makeText(this, "Por favor completa todos los pasos de configuración", Toast.LENGTH_SHORT).show()
        return
    }

    // Crear tareas para cada archivo seleccionado
    for (archivo in archivosSeleccionados) {
        val tarea = TareaInfo(
            nombre = archivo.nombre,
            estado = Estado.PENDIENTE
        )
        tareas.add(tarea)
    }

    binding.recyclerViewTareas.adapter?.notifyDataSetChanged()

    // Iniciar la conversión del primer archivo
    convertirSiguienteArchivo()
}

private fun convertirSiguienteArchivo() {
    val tareaPendiente = tareas.find { it.estado == Estado.PENDIENTE }
    if (tareaPendiente != null) {
        val archivo = archivosSeleccionados.find { it.nombre == tareaPendiente.nombre }
        if (archivo != null) {
            tareaPendiente.estado = Estado.PROCESANDO
            binding.recyclerViewTareas.adapter?.notifyDataSetChanged()

            tareaActual = ConversionTask(
                context = this,
                archivo = archivo,
                configuracion = configuracion,
                carpetaDestino = carpetaDestinoGlobal,
                listener = this
            )
            tareaActual?.execute()
        }
    } else {
        // Todas las conversiones han finalizado
        Toast.makeText(this, "Todas las conversiones han finalizado", Toast.LENGTH_SHORT).show()
    }
}

    override fun onProgress(progreso: String) {
        runOnUiThread {
            Toast.makeText(this, progreso, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onComplete() {
        runOnUiThread {
            Toast.makeText(this, "Conversión completada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDuplicadoDetectado(nombreArchivo: String, carpetaDestino: String) {
        runOnUiThread {
            Toast.makeText(this, "Archivo duplicado detectado: $nombreArchivo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onArchivoSeleccionado(posicion: Int, seleccionado: Boolean) {
        archivosSeleccionados[posicion].seleccionado = seleccionado
    }

    override fun onOpcionesTareaPulsado(posicion: Int) {
        Toast.makeText(this, "Opciones de tarea: $posicion", Toast.LENGTH_SHORT).show()
    }

    override fun onTareaPulsadaLargo(posicion: Int) {
        Toast.makeText(this, "Tarea pulsada largo: $posicion", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarBotonesPasoActual() {
        binding.contenedorBotonesDinamicos.removeAllViews()
        
        when (pasoActual) {
            1 -> mostrarBotonesResolucion()
            2 -> mostrarBotonesRelacionAspecto()
            3 -> mostrarBotonesRangoDinamico()
            4 -> mostrarBotonesAudio()
            5 -> mostrarBotonesSubtitulos()
            6 -> mostrarBotonesAjustesAvanzados()
        }
    }

    private fun mostrarBotonesResolucion() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Selecciona la resolución del archivo original:"
        layout.addView(textView)
        
        val boton4K = Button(this)
        boton4K.text = "4K"
        boton4K.setOnClickListener {
            configuracion.resolucion = "4K"
            agregarTab("Resolución")
            pasoActual = 2
            mostrarBotonesPasoActual()
        }
        layout.addView(boton4K)
        
        val botonFHD = Button(this)
        botonFHD.text = "FHD (1080p)"
        botonFHD.setOnClickListener {
            configuracion.resolucion = "FHD"
            agregarTab("Resolución")
            pasoActual = 2
            mostrarBotonesPasoActual()
        }
        layout.addView(botonFHD)
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun mostrarBotonesRelacionAspecto() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Selecciona la relación de aspecto deseada:"
        layout.addView(textView)
        
        val relacionesAspecto = mapOf(
            "16∶9 / Sin Recorte" to "Dimensiones originales",
            "2.35∶1" to "1920×816p"
        )
        
        relacionesAspecto.forEach { (relacion, dimensiones) ->
            val boton = Button(this)
            boton.text = "$relacion\n$dimensiones"
            boton.setOnClickListener {
                configuracion.relacionAspecto = relacion
                agregarTab("Relación de aspecto")
                pasoActual = 3
                mostrarBotonesPasoActual()
            }
            layout.addView(boton)
        }
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun mostrarBotonesRangoDinamico() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Selecciona el rango dinámico del archivo original:"
        layout.addView(textView)
        
        val botonHDR = Button(this)
        botonHDR.text = "HDR"
        botonHDR.setOnClickListener {
            configuracion.rangoDinamico = "HDR"
            agregarTab("Rango dinámico")
            pasoActual = 4
            mostrarBotonesPasoActual()
        }
        layout.addView(botonHDR)
        
        val botonSDR = Button(this)
        botonSDR.text = "SDR"
        botonSDR.setOnClickListener {
            configuracion.rangoDinamico = "SDR"
            agregarTab("Rango dinámico")
            pasoActual = 4
            mostrarBotonesPasoActual()
        }
        layout.addView(botonSDR)
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun mostrarBotonesAudio() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Configuración de audio:"
        layout.addView(textView)
        
        val botonCopiar = Button(this)
        botonCopiar.text = "Copiar"
        botonCopiar.setOnClickListener {
            configuracion.audioOpcion = "Copiar"
            agregarTab("Audio")
            pasoActual = 5
            mostrarBotonesPasoActual()
        }
        layout.addView(botonCopiar)
        
        val botonPersonalizado = Button(this)
        botonPersonalizado.text = "Personalizado"
        botonPersonalizado.setOnClickListener {
            configuracion.audioOpcion = "Personalizado"
            agregarTab("Audio")
            pasoActual = 5
            mostrarBotonesPasoActual()
        }
        layout.addView(botonPersonalizado)
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun mostrarBotonesSubtitulos() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Configuración de subtítulos:"
        layout.addView(textView)
        
        val botonCopiar = Button(this)
        botonCopiar.text = "Copiar"
        botonCopiar.setOnClickListener {
            configuracion.subtitulosOpcion = "Copiar"
            agregarTab("Subtítulos")
            pasoActual = 6
            mostrarBotonesPasoActual()
        }
        layout.addView(botonCopiar)
        
        val botonPersonalizado = Button(this)
        botonPersonalizado.text = "Personalizado"
        botonPersonalizado.setOnClickListener {
            configuracion.subtitulosOpcion = "Personalizado"
            agregarTab("Subtítulos")
            pasoActual = 6
            mostrarBotonesPasoActual()
        }
        layout.addView(botonPersonalizado)
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun mostrarBotonesAjustesAvanzados() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val textView = android.widget.TextView(this)
        textView.text = "Ajustes avanzados:"
        layout.addView(textView)
        
        val botonAjustesAvanzados = Button(this)
        botonAjustesAvanzados.text = "Ajustes Avanzados"
        botonAjustesAvanzados.setOnClickListener {
            agregarTab("Ajustes avanzados")
            Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show()
        }
        layout.addView(botonAjustesAvanzados)
        
        binding.contenedorBotonesDinamicos.addView(layout)
    }

    private fun agregarTab(nombre: String) {
        binding.tabLayoutPasos.addTab(binding.tabLayoutPasos.newTab().setText(nombre))
        binding.tabLayoutPasos.selectTab(binding.tabLayoutPasos.getTabAt(binding.tabLayoutPasos.tabCount - 1))
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return "Desconocido"
    }

    private fun obtenerTamanoArchivo(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            return if (!it.isNull(sizeIndex)) it.getLong(sizeIndex) else -1
        }
        return -1
    }
}
