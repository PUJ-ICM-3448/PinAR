package com.example.pinar.ui.screens.map.util

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object DirectionsHelper {

    fun getApiKey(context: Context): String {
        return try {
            @Suppress("DEPRECATION")
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun fetchRoute(
        context: Context,
        origin: LatLng,
        destination: LatLng
    ): Result<List<LatLng>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey(context)
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Falta MAPS_API_KEY en la configuracion local")
                )
            }
            val routeUrl = buildString {
                append("https://maps.googleapis.com/maps/api/directions/json")
                append("?origin=${origin.latitude},${origin.longitude}")
                append("&destination=${destination.latitude},${destination.longitude}")
                append("&key=$apiKey")
            }
            val response = URL(routeUrl).openConnection().let { conn ->
                conn as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.requestMethod = "GET"
                conn.inputStream.bufferedReader().use { it.readText() }
                    .also { conn.disconnect() }
            }
            val json = JSONObject(response)
            val status = json.optString("status")
            if (status != "OK") {
                return@withContext Result.failure(
                    IllegalStateException("No hay ruta disponible: $status")
                )
            }
            val encodedPolyline = json
                .getJSONArray("routes")
                .getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")
            Result.success(decodePolyline(encodedPolyline))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        var latitude = 0
        var longitude = 0

        while (index < encoded.length) {
            var shift = 0
            var result = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            latitude += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            longitude += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            polyline.add(LatLng(latitude.toDouble() / 1E5, longitude.toDouble() / 1E5))
        }
        return polyline
    }
}
