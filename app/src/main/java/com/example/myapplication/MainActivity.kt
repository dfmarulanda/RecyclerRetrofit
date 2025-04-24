package com.example.myapplication

import EstudianteAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.API.getClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * MainActivity: Pantalla principal de la aplicación que muestra una lista de estudiantes.
 * Esta clase demuestra:
 * 1. Uso de Retrofit para llamadas a API
 * 2. Implementación de RecyclerView
 * 3. Scroll infinito
 * 4. Manejo de errores
 * 5. Estados de carga
 * 6. Navegación a vista de detalle
 */
class MainActivity : AppCompatActivity() {

    // Componentes de UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstudianteAdapter
    private lateinit var emptyStateView: View
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar

    // Gestión de datos
    private var listaCompleta = mutableListOf<Estudiante>() // Lista completa de estudiantes
    private var currentPage = 1 // Número de página actual para paginación
    private var hasMorePages = true // Bandera para verificar si hay más páginas disponibles
    private var isLoading = false // Bandera para prevenir múltiples llamadas simultáneas a la API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar componentes de UI
        initializeViews()
        
        // Configurar RecyclerView con LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        // Inicializar adaptador con listener de clic para navegación
        adapter = EstudianteAdapter(listaCompleta) { estudiante ->
            // Navegar a la vista de detalle cuando se hace clic en un estudiante
            val intent = Intent(this, StudentDetailActivity::class.java).apply {
                putExtra("student", estudiante)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Agregar listener de scroll para scroll infinito
        setupInfiniteScrolling(layoutManager)

        // Cargar datos iniciales
        initializeStudentList()
        
        // Actualizar UI inicial
        updateUI()
    }

    /**
     * Inicializar todos los componentes de UI encontrando sus vistas
     */
    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewEstudiantes)
        emptyStateView = findViewById(R.id.emptyState)
        searchEditText = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Configurar scroll infinito agregando un listener de scroll al RecyclerView
     * Esto cargará automáticamente más elementos cuando el usuario llegue al final de la lista
     */
    private fun setupInfiniteScrolling(layoutManager: LinearLayoutManager) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Cargar más elementos cuando:
                // 1. No se está cargando actualmente
                // 2. Hay más páginas disponibles
                // 3. El usuario ha llegado al final
                // 4. Tenemos al menos 10 elementos (para prevenir carga prematura)
                if (!isLoading && hasMorePages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 10) {
                        loadMoreStudents()
                    }
                }
            }
        })
    }

    /**
     * Reiniciar el estado de paginación y cargar la primera página de estudiantes
     */
    private fun initializeStudentList() {
        currentPage = 1
        hasMorePages = true
        listaCompleta.clear()
        loadStudents(currentPage)
    }

    /**
     * Cargar estudiantes desde la API para una página específica
     * @param page El número de página a cargar
     */
    private fun loadStudents(page: Int) {
        if (isLoading) return // Prevenir múltiples llamadas simultáneas a la API
        
        isLoading = true
        showLoading(true)
        
        Log.d("MainActivity", "Cargando página $page")
        
        // Crear llamada a la API usando Retrofit
        val call = getClient().create(Routes::class.java).getUsers(page.toString())
        
        // Ejecutar llamada a la API de forma asíncrona
        call.enqueue(object : Callback<RequestEstudiantes> {
            override fun onResponse(
                call: Call<RequestEstudiantes>,
                response: Response<RequestEstudiantes>
            ) {
                isLoading = false
                showLoading(false)
                
                if (response.isSuccessful) {
                    val requestEstudiantes = response.body()
                    Log.d("MainActivity", "Respuesta exitosa. Cuerpo: $requestEstudiantes")
                    
                    if (requestEstudiantes != null && requestEstudiantes.results.isNotEmpty()) {
                        Log.d("MainActivity", "Datos recibidos: ${requestEstudiantes.results.size} estudiantes")
                        Log.d("MainActivity", "Primer estudiante: ${requestEstudiantes.results.first()}")
                        
                        // Actualizar estado de paginación basado en la respuesta de la API
                        hasMorePages = requestEstudiantes.next != null
                        Log.d("MainActivity", "¿Hay más páginas? $hasMorePages")
                        
                        if (page == 1) {
                            // Primera página: reemplazar toda la lista
                            listaCompleta.clear()
                            listaCompleta.addAll(requestEstudiantes.results)
                            adapter.updateList(listaCompleta)
                            Log.d("MainActivity", "Lista actualizada con ${listaCompleta.size} estudiantes")
                        } else {
                            // Páginas subsiguientes: agregar a la lista existente
                            val startPosition = listaCompleta.size
                            listaCompleta.addAll(requestEstudiantes.results)
                            adapter.notifyItemRangeInserted(startPosition, requestEstudiantes.results.size)
                            Log.d("MainActivity", "Agregados ${requestEstudiantes.results.size} estudiantes más")
                        }
                        
                        // Actualizar UI después de actualizar la lista
                        updateUI()
                    } else {
                        Log.w("MainActivity", "Respuesta vacía o sin estudiantes")
                        Toast.makeText(this@MainActivity, "No se encontraron estudiantes", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta: ${response.code()}")
                    Log.e("MainActivity", "Mensaje de error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "Error al cargar los datos: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RequestEstudiantes>, t: Throwable) {
                isLoading = false
                showLoading(false)
                Log.e("MainActivity", "Fallo en la llamada: ${t.message}", t)
                Toast.makeText(this@MainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Cargar la siguiente página de estudiantes
     */
    private fun loadMoreStudents() {
        currentPage++
        loadStudents(currentPage)
    }

    /**
     * Mostrar u ocultar el indicador de carga
     * @param show Si se debe mostrar el indicador de carga
     */
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Actualizar la UI basado en el estado actual de los datos
     */
    private fun updateUI() {
        if (listaCompleta.isEmpty()) {
            // Mostrar estado vacío cuando no hay datos disponibles
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            // Mostrar lista cuando hay datos disponibles
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }
}