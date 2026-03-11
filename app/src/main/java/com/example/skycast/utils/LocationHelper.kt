package com.example.skycast.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (Location?) -> Unit
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { cachedLocation: Location? ->
            if (cachedLocation != null) {
                onLocationReceived(cachedLocation)
            } else {
                // Fallback to fetch current location using balanced power block to prevent hanging
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                }.addOnFailureListener {
                    onLocationReceived(null)
                }
            }
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    }
}