
package com.tecsup.lab12_maps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Función para crear corazones mediante polilíneas
fun crearCorazon(
    latitud: Double,
    longitud: Double,
    escala: Double
): List<LatLng> {
    return (0..100).map { i ->
        val t = 2 * PI * i / 100

        val x = 16 * sin(t) * sin(t) * sin(t)

        val y = 13 * cos(t) -
                5 * cos(2 * t) -
                2 * cos(3 * t) -
                cos(4 * t)

        LatLng(
            latitud + y * escala,
            longitud + x * escala
        )
    }
}

@Composable
fun MapScreen() {

    val context = LocalContext.current

    // Ubicaciones principales
    val ArequipaLocation = LatLng(-16.4040102, -71.559611)
    val YuraLocation = LatLng(-16.2520984, -71.6836503)

    // Configuración de cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            ArequipaLocation,
            12f
        )
    }

    var mapaCargado by remember {
        mutableStateOf(false)
    }

    // Tipo de mapa seleccionado
    var tipoMapa by remember {
        mutableStateOf(MapType.NORMAL)
    }

    val propiedadesMapa = remember(tipoMapa) {
        MapProperties(mapType = tipoMapa)
    }

    // NUEVO: Cliente de ubicación
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // NUEVO: Ubicación actual del usuario
    var ubicacionActual by remember {
        mutableStateOf<LatLng?>(null)
    }

    // NUEVO: Estado de la búsqueda
    var buscandoUbicacion by remember {
        mutableStateOf(false)
    }

    var mensajeUbicacion by remember {
        mutableStateOf("")
    }

    // NUEVO: Solicitar ubicación actual
    fun obtenerUbicacion() {

        val permisoPreciso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoPreciso && !permisoAproximado) {
            mensajeUbicacion = "Permiso de ubicación no concedido"
            return
        }

        buscandoUbicacion = true
        mensajeUbicacion = "Buscando ubicación..."

        val prioridad = if (permisoPreciso) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val solicitud = LocationRequest.Builder(
            prioridad,
            1000L
        )
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation

                if (location != null) {
                    ubicacionActual = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    mensajeUbicacion = "Ubicación encontrada"
                } else {
                    mensajeUbicacion = "No se pudo obtener la ubicación"
                }

                buscandoUbicacion = false
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                solicitud,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener {
                buscandoUbicacion = false
                mensajeUbicacion = "Error al obtener ubicación"
            }
        } catch (e: SecurityException) {
            buscandoUbicacion = false
            mensajeUbicacion = "Permiso de ubicación no disponible"
        }
    }

    // NUEVO: Solicitar permisos al usuario
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->

        val autorizado =
            permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (autorizado) {
            obtenerUbicacion()
        } else {
            mensajeUbicacion = "Debes permitir el acceso a la ubicación"
        }
    }

    // NUEVO: Mover cámara a ubicación encontrada
    LaunchedEffect(ubicacionActual) {
        ubicacionActual?.let { posicion ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    posicion,
                    17f
                ),
                durationMs = 2000
            )
        }
    }

    // Animación anterior: Arequipa hacia Yura
    LaunchedEffect(mapaCargado) {
        if (mapaCargado) {
            delay(2000)

            // Evitar que la animación de Yura interrumpa
            // una búsqueda de ubicación
            if (ubicacionActual == null && !buscandoUbicacion) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        YuraLocation,
                        16f
                    ),
                    durationMs = 3000
                )
            }
        }
    }

    // Cargar imagen del osito
    val bitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.osito
        )
    }

    val bitmapEscalado = remember(bitmap) {
        Bitmap.createScaledBitmap(
            bitmap,
            150,
            250,
            true
        )
    }

    // Marcadores del laboratorio
    val locations = listOf(
        LatLng(-16.433415, -71.5442652),
        LatLng(-16.4205151, -71.4945209),
        LatLng(-16.3524187, -71.5675994)
    )

    // Polígonos
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )

    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )

    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    val plazaDeArmas = LatLng(-16.399000, -71.536625)
    val parqueLambramani = LatLng(-16.422984, -71.530970)
    val mallAventura = LatLng(-16.432700, -71.509250)

    // Corazones personalizados
    val corazon1 = remember {
        crearCorazon(-16.4000, -71.5400, 0.00003)
    }

    val corazon2 = remember {
        crearCorazon(-16.4250, -71.5340, 0.00003)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = propiedadesMapa,
            onMapLoaded = {
                mapaCargado = true
            }
        ) {

            // Marcador del osito
            Marker(
                state = rememberMarkerState(
                    position = ArequipaLocation
                ),
                icon = BitmapDescriptorFactory.fromBitmap(
                    bitmapEscalado
                ),
                title = "Arequipa, Perú"
            )

            // Marcadores adicionales
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(
                        position = location
                    ),
                    title = "Ubicación",
                    snippet = "Punto de interés"
                )
            }

            // NUEVO: Marcador de ubicación actual
            ubicacionActual?.let { posicion ->
                Marker(
                    state = rememberMarkerState(
                        position = posicion
                    ),
                    title = "Mi ubicación actual",
                    snippet = "Ubicación obtenida mediante GPS"
                )
            }

            // Polígonos
            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polilíneas
            Polyline(
                points = listOf(
                    plazaDeArmas,
                    parqueLambramani
                ),
                color = Color.Magenta,
                width = 8f
            )

            Polyline(
                points = listOf(
                    parqueLambramani,
                    mallAventura
                ),
                color = Color.Green,
                width = 8f
            )

            Polyline(
                points = listOf(
                    mallAventura,
                    LatLng(-16.4205151, -71.4945209)
                ),
                color = Color.Cyan,
                width = 8f
            )

            // Corazones
            Polyline(
                points = corazon1,
                color = Color.Magenta,
                width = 12f
            )

            Polyline(
                points = corazon2,
                color = Color.Red,
                width = 12f
            )
        }

        // Botones para cambiar tipo de mapa
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    top = 36.dp,
                    start = 8.dp,
                    end = 8.dp
                )
                .background(
                    Color.White.copy(alpha = 0.95f),
                    RoundedCornerShape(16.dp)
                )
                .horizontalScroll(rememberScrollState())
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            val opciones = listOf(
                "Normal" to MapType.NORMAL,
                "Satélite" to MapType.SATELLITE,
                "Híbrido" to MapType.HYBRID,
                "Terreno" to MapType.TERRAIN
            )

            opciones.forEach { (nombre, tipo) ->

                Button(
                    onClick = {
                        tipoMapa = tipo
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoMapa == tipo)
                            Color(0xFF7B2CBF)
                        else
                            Color(0xFF242424)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                ) {
                    Text(
                        text = nombre,
                        color = Color.White
                    )
                }
            }
        }

        // NUEVO: Botón Mi ubicación
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (mensajeUbicacion.isNotEmpty()) {
                Text(
                    text = mensajeUbicacion,
                    color = Color.Black,
                    modifier = Modifier
                        .background(
                            Color.White,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {

                    val permisoPreciso =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    val permisoAproximado =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (permisoPreciso || permisoAproximado) {
                        obtenerUbicacion()
                    } else {
                        permisoLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                enabled = !buscandoUbicacion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B2CBF)
                )
            ) {
                Text(
                    text = if (buscandoUbicacion)
                        "Buscando ubicación..."
                    else
                        "📍 Mi ubicación"
                )
            }
        }
    }
}
