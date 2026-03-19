package com.lawsphere.app.core.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

class CourtModeHandler(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    // Supreme Court Coordinates
    private val targetLat = 28.6143
    private val targetLon = 77.2415

    @SuppressLint("MissingPermission")
    fun startRealCheck(onResult: (Boolean, Float) -> Unit) {
        try {
            client.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val results = FloatArray(1)
                    Location.distanceBetween(it.latitude, it.longitude, targetLat, targetLon, results)
                    val distance = results[0]
                    onResult(distance < 500, distance)
                }
            }
        } catch (e: SecurityException) {}
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationFound: (Location?) -> Unit) {
        client.lastLocation.addOnSuccessListener { location ->
            onLocationFound(location)
        }
    }
}