
package com.tecsup.lab12_maps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Crear corazones mediante polilíneas
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

    // Estado de carga del mapa
    var mapaCargado by remember {
        mutableStateOf(false)
    }

    // NUEVO: Tipo de mapa seleccionado
    var tipoMapa by remember {
        mutableStateOf(MapType.NORMAL)
    }

    // NUEVO: Propiedades del mapa
    val propiedadesMapa = remember(tipoMapa) {
        MapProperties(
            mapType = tipoMapa
        )
    }

    // Animación de Arequipa hacia Yura
    LaunchedEffect(mapaCargado) {
        if (mapaCargado) {

            delay(2000)

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    YuraLocation,
                    16f
                ),
                durationMs = 3000
            )
        }
    }

    // Cargar imagen del osito
    val context = LocalContext.current

    val bitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.osito
        )
    }

    // Tamaño 150 x 250
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

    // Polígono Mall Aventura
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )

    // Polígono Parque Lambramani
    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )

    // Polígono Plaza de Armas
    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    // Puntos de interés
    val plazaDeArmas = LatLng(-16.399000, -71.536625)
    val parqueLambramani = LatLng(-16.422984, -71.530970)
    val mallAventura = LatLng(-16.432700, -71.509250)

    // Corazón rosado
    val corazon1 = remember {
        crearCorazon(
            -16.4000,
            -71.5400,
            0.00003
        )
    }

    // Corazón rojo
    val corazon2 = remember {
        crearCorazon(
            -16.4250,
            -71.5340,
            0.00003
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Mostrar Google Maps
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,

            // NUEVO: Aplicar tipo de mapa
            properties = propiedadesMapa,

            onMapLoaded = {
                mapaCargado = true
            }
        ) {

            // Marcador personalizado
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

            // Polígono Plaza de Armas
            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polígono Parque Lambramani
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polígono Mall Aventura
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polilínea Plaza de Armas - Lambramani
            Polyline(
                points = listOf(
                    plazaDeArmas,
                    parqueLambramani
                ),
                color = Color.Magenta,
                width = 8f
            )

            // Polilínea Lambramani - Mall Aventura
            Polyline(
                points = listOf(
                    parqueLambramani,
                    mallAventura
                ),
                color = Color.Green,
                width = 8f
            )

            // Polilínea Mall Aventura - Paucarpata
            Polyline(
                points = listOf(
                    mallAventura,
                    LatLng(-16.4205151, -71.4945209)
                ),
                color = Color.Cyan,
                width = 8f
            )

            // Corazón rosado
            Polyline(
                points = corazon1,
                color = Color.Magenta,
                width = 12f
            )

            // Corazón rojo
            Polyline(
                points = corazon2,
                color = Color.Red,
                width = 12f
            )
        }

        // NUEVO: Botones para cambiar el tipo de mapa
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 36.dp, start = 8.dp, end = 8.dp)
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
    }
}
