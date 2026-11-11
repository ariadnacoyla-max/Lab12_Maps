
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
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay

@Composable
fun MapScreen() {

    // Ubicación inicial: Arequipa, Perú
    val ArequipaLocation = LatLng(-16.4040102, -71.559611)

    // Ubicación de destino: Yura
    val YuraLocation = LatLng(-16.2520984, -71.6836503)

    // Configuración inicial de la cámara en Arequipa
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

    // Animación de cámara hacia Yura
    LaunchedEffect(mapaCargado) {
        if (mapaCargado) {

            // Esperar 2 segundos mostrando Arequipa
            delay(2000)

            // Mover la cámara hacia Yura y aumentar el zoom
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    YuraLocation,
                    15f
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

    // Mostrar mapa
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,

            // Detectar cuando Google Maps termina de cargar
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
        }
    }
}
