
package com.tecsup.lab12_maps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Función para dibujar un corazón con coordenadas
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

    // Ubicación inicial: Arequipa
    val ArequipaLocation = LatLng(-16.4040102, -71.559611)

    // Ubicación de destino: Yura
    val YuraLocation = LatLng(-16.2520984, -71.6836503)

    // Configuración inicial de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            ArequipaLocation,
            12f
        )
    }

    // Controlar cuándo termina de cargar el mapa
    var mapaCargado by remember {
        mutableStateOf(false)
    }

    // Animación de la cámara hacia Yura
    LaunchedEffect(mapaCargado) {
        if (mapaCargado) {

            // Esperar 2 segundos mostrando Arequipa
            delay(2000)

            // Mover la cámara hacia Yura con zoom
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    YuraLocation,
                    16f
                ),
                durationMs = 3000
            )
        }
    }

    // Obtener contexto de la aplicación
    val context = LocalContext.current

    // Cargar imagen personalizada del osito
    val bitmap = remember(context) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.osito
        )
    }

    // Ajustar tamaño del osito a 150 x 250
    val bitmapEscalado = remember(bitmap) {
        Bitmap.createScaledBitmap(
            bitmap,
            150,
            250,
            true
        )
    }

    // Lista de ubicaciones del laboratorio
    val locations = listOf(
        LatLng(-16.433415, -71.5442652), // JLByR
        LatLng(-16.4205151, -71.4945209), // Paucarpata
        LatLng(-16.3524187, -71.5675994) // Zamacola
    )

    // Polígono del Mall Aventura
    val mallAventuraPolygon = listOf(
        LatLng(-16.432292, -71.509145),
        LatLng(-16.432757, -71.509626),
        LatLng(-16.433013, -71.509310),
        LatLng(-16.432566, -71.508853)
    )

    // Polígono del Parque Lambramani
    val parqueLambramaniPolygon = listOf(
        LatLng(-16.422704, -71.530830),
        LatLng(-16.422920, -71.531340),
        LatLng(-16.423264, -71.531110),
        LatLng(-16.423050, -71.530600)
    )

    // Polígono de la Plaza de Armas
    val plazaDeArmasPolygon = listOf(
        LatLng(-16.398866, -71.536961),
        LatLng(-16.398744, -71.536529),
        LatLng(-16.399178, -71.536289),
        LatLng(-16.399299, -71.536721)
    )

    // Puntos de ejemplo para las polilíneas
    val plazaDeArmas = LatLng(-16.399000, -71.536625)
    val parqueLambramani = LatLng(-16.422984, -71.530970)
    val mallAventura = LatLng(-16.432700, -71.509250)

    // Corazón 1: cerca de la Plaza de Armas
    val corazon1 = remember {
        crearCorazon(
            -16.4000,
            -71.5400,
            0.00003
        )
    }

    // Corazón 2: cerca del Parque Lambramani
    val corazon2 = remember {
        crearCorazon(
            -16.4250,
            -71.5340,
            0.00003
        )
    }

    // Mostrar el mapa
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,

            // Detectar cuando termina de cargar
            onMapLoaded = {
                mapaCargado = true
            }
        ) {

            // Marcador personalizado en Arequipa
            Marker(
                state = rememberMarkerState(
                    position = ArequipaLocation
                ),
                icon = BitmapDescriptorFactory.fromBitmap(
                    bitmapEscalado
                ),
                title = "Arequipa, Perú"
            )

            // Añadir varios marcadores
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(
                        position = location
                    ),
                    title = "Ubicación",
                    snippet = "Punto de interés"
                )
            }

            // Polígono de Plaza de Armas
            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polígono de Parque Lambramani
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polígono de Mall Aventura
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // Polilínea 1: Plaza de Armas a Lambramani
            Polyline(
                points = listOf(
                    plazaDeArmas,
                    parqueLambramani
                ),
                color = Color.Magenta,
                width = 8f
            )

            // Polilínea 2: Lambramani a Mall Aventura
            Polyline(
                points = listOf(
                    parqueLambramani,
                    mallAventura
                ),
                color = Color.Green,
                width = 8f
            )

            // Polilínea 3: Mall Aventura a Paucarpata
            Polyline(
                points = listOf(
                    mallAventura,
                    LatLng(-16.4205151, -71.4945209)
                ),
                color = Color.Cyan,
                width = 8f
            )

            // Corazón 1: rosado
            Polyline(
                points = corazon1,
                color = Color.Magenta,
                width = 12f
            )

            // Corazón 2: rojo
            Polyline(
                points = corazon2,
                color = Color.Red,
                width = 12f
            )
        }
    }
}
