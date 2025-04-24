# Aplicación de Lista de Estudiantes - Guía Detallada

## 📱 ¿Qué es esta aplicación?

Esta es una aplicación Android que muestra una lista de estudiantes (personajes de Star Wars) obtenidos de una API pública. La aplicación demuestra conceptos fundamentales de desarrollo Android como:

- Llamadas a API REST
- Mostrar datos en una lista
- Navegación entre pantallas
- Manejo de datos y estados

## 🛠️ Requisitos Previos

Antes de comenzar, necesitas:

1. Android Studio instalado
2. Conocimientos básicos de:
   - Kotlin (sintaxis básica)
   - XML (para layouts)
   - Programación orientada a objetos

## 📋 Paso 1: Crear el Proyecto

1. Abre Android Studio
2. Click en "New Project"
3. Selecciona "Empty Activity"
4. Configura:
   - Name: "MyApplication"
   - Package name: "com.example.myapplication"
   - Language: Kotlin
   - Minimum SDK: API 24 (Android 7.0)

## 📦 Paso 2: Agregar Dependencias

En el archivo `app/build.gradle.kts`, agrega estas dependencias:

```kotlin
dependencies {
    // Retrofit - Para hacer llamadas a internet
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // RecyclerView - Para mostrar la lista
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // CardView - Para el diseño de las tarjetas
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // Material Design - Para un diseño moderno
    implementation 'com.google.android.material:material:1.11.0'
}
```

## 📝 Paso 3: Crear las Clases de Datos

### 3.1 Clase Estudiante
Esta clase representa a un estudiante con sus propiedades:

```kotlin
@Parcelize
data class Estudiante(
    val name: String,        // Nombre del estudiante
    val height: String,      // Altura
    val gender: String,      // Género
    val hair_color: String,  // Color de pelo
    val birth_year: String   // Año de nacimiento
) : Parcelable
```

Explicación:
- `@Parcelize`: Permite pasar el objeto entre pantallas
- `data class`: Tipo especial de clase en Kotlin para guardar datos
- `Parcelable`: Interfaz necesaria para pasar objetos entre actividades

### 3.2 Clase RequestEstudiantes
Esta clase representa la respuesta de la API:

```kotlin
data class RequestEstudiantes(
    val count: Int,              // Total de estudiantes
    val next: String?,           // URL de la siguiente página
    val previous: String?,       // URL de la página anterior
    val listStudents: List<Estudiante> // Lista de estudiantes
)
```

## 🌐 Paso 4: Configurar Retrofit

### 4.1 Crear la Interfaz Routes
Esta interfaz define las llamadas a la API:

```kotlin
interface Routes {
    @GET("people/")  // Endpoint de la API
    fun getUsers(@Query("page") page: String): Call<RequestEstudiantes>
}
```

Explicación:
- `@GET`: Indica que es una petición GET
- `@Query`: Agrega parámetros a la URL
- `Call`: Tipo de retorno que maneja la respuesta

### 4.2 Configurar el Cliente Retrofit
Crea un objeto para configurar Retrofit:

```kotlin
object API {
    private const val BASE_URL = "https://swapi.dev/api/"
    
    fun getClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

## 📱 Paso 5: Crear los Layouts

### 5.1 Layout Principal (activity_main.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- Título -->
    <TextView
        android:id="@+id/tvTitle"
        android:text="Estudiantes"/>
    
    <!-- Barra de búsqueda -->
    <EditText
        android:id="@+id/etSearch"
        android:hint="Buscar estudiantes..."/>
    
    <!-- Lista de estudiantes -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewEstudiantes"/>
    
    <!-- Indicador de carga -->
    <ProgressBar
        android:id="@+id/progressBar"/>
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 5.2 Layout del Item (item_student.xml)
```xml
<androidx.cardview.widget.CardView>
    <LinearLayout>
        <TextView android:id="@+id/tvName"/>
        <TextView android:id="@+id/tvHeight"/>
        <TextView android:id="@+id/tvGender"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

## 🔄 Paso 6: Implementar el Adaptador

El adaptador maneja cómo se muestran los datos en el RecyclerView:

```kotlin
class EstudianteAdapter(
    private var estudiantes: List<Estudiante>,
    private val onItemClick: (Estudiante) -> Unit
) : RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder>() {
    
    // ViewHolder: Mantiene las referencias a las vistas
    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvHeight: TextView = itemView.findViewById(R.id.tvHeight)
        val tvGender: TextView = itemView.findViewById(R.id.tvGender)
    }
    
    // Crea nuevos ViewHolders
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return EstudianteViewHolder(view)
    }
    
    // Vincula los datos con las vistas
    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = estudiantes[position]
        holder.tvName.text = estudiante.name
        holder.tvHeight.text = "Altura: ${estudiante.height}"
        holder.tvGender.text = "Género: ${estudiante.gender}"
        
        holder.itemView.setOnClickListener {
            onItemClick(estudiante)
        }
    }
    
    // Retorna el número de elementos
    override fun getItemCount() = estudiantes.size
    
    // Actualiza la lista
    fun updateList(newList: List<Estudiante>) {
        estudiantes = newList
        notifyDataSetChanged()
    }
}
```

## 🚀 Paso 7: Implementar la Actividad Principal

### 7.1 Variables y Inicialización
```kotlin
class MainActivity : AppCompatActivity() {
    // Variables para la UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstudianteAdapter
    private lateinit var progressBar: ProgressBar
    
    // Variables para los datos
    private var listaCompleta = mutableListOf<Estudiante>()
    private var currentPage = 1
    private var hasMorePages = true
    private var isLoading = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar vistas
        initializeViews()
        
        // Configurar RecyclerView
        setupRecyclerView()
        
        // Cargar datos iniciales
        loadInitialData()
    }
}
```

### 7.2 Cargar Datos
```kotlin
private fun loadStudents(page: Int) {
    if (isLoading) return
    
    isLoading = true
    showLoading(true)
    
    val call = getClient().create(Routes::class.java).getUsers(page.toString())
    call.enqueue(object : Callback<RequestEstudiantes> {
        override fun onResponse(call: Call<RequestEstudiantes>, response: Response<RequestEstudiantes>) {
            isLoading = false
            showLoading(false)
            
            if (response.isSuccessful) {
                val requestEstudiantes = response.body()
                if (requestEstudiantes != null && requestEstudiantes.listStudents.isNotEmpty()) {
                    // Actualizar lista
                    if (page == 1) {
                        listaCompleta.clear()
                        listaCompleta.addAll(requestEstudiantes.listStudents)
                        adapter.updateList(listaCompleta)
                    } else {
                        val startPosition = listaCompleta.size
                        listaCompleta.addAll(requestEstudiantes.listStudents)
                        adapter.notifyItemRangeInserted(startPosition, requestEstudiantes.listStudents.size)
                    }
                    
                    // Actualizar estado de paginación
                    hasMorePages = requestEstudiantes.next != null
                }
            }
        }
        
        override fun onFailure(call: Call<RequestEstudiantes>, t: Throwable) {
            isLoading = false
            showLoading(false)
            Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}
```

## 🔍 Paso 8: Implementar Scroll Infinito

```kotlin
private fun setupInfiniteScrolling(layoutManager: LinearLayoutManager) {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            
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
```

## 🎯 Paso 9: Implementar la Vista de Detalle

### 9.1 Layout de Detalle (activity_student_detail.xml)
```xml
<ScrollView>
    <LinearLayout>
        <TextView android:id="@+id/tvName"/>
        <TextView android:id="@+id/tvHeight"/>
        <TextView android:id="@+id/tvGender"/>
        <TextView android:id="@+id/tvHairColor"/>
        <TextView android:id="@+id/tvBirthYear"/>
    </LinearLayout>
</ScrollView>
```

### 9.2 Actividad de Detalle
```kotlin
class StudentDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)
        
        val estudiante = intent.getParcelableExtra<Estudiante>("student")
        
        if (estudiante != null) {
            // Mostrar datos en la UI
            findViewById<TextView>(R.id.tvName).text = estudiante.name
            findViewById<TextView>(R.id.tvHeight).text = "Altura: ${estudiante.height}"
            findViewById<TextView>(R.id.tvGender).text = "Género: ${estudiante.gender}"
            findViewById<TextView>(R.id.tvHairColor).text = "Color de pelo: ${estudiante.hair_color}"
            findViewById<TextView>(R.id.tvBirthYear).text = "Año de nacimiento: ${estudiante.birth_year}"
        }
    }
}
```

## 🔧 Solución de Problemas Comunes

### 1. La lista no muestra datos
- Verifica que la API esté respondiendo
- Revisa los logs para ver la respuesta
- Asegúrate de que los nombres de las propiedades coincidan

### 2. Error de conexión
- Verifica los permisos de internet en el AndroidManifest.xml
- Comprueba la URL de la API
- Verifica la conexión a internet

### 3. La aplicación se cierra
- Revisa los logs de error
- Verifica que todas las vistas estén inicializadas
- Comprueba los null checks

## 📚 Recursos Adicionales

- [Documentación de Android](https://developer.android.com/docs)
- [Guía de Retrofit](https://square.github.io/retrofit/)
- [Tutorial de RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
- [Material Design](https://material.io/develop/android)

## 🎓 Próximos Pasos

1. Implementar búsqueda
2. Agregar filtros
3. Implementar caché
4. Agregar animaciones
5. Implementar modo offline 