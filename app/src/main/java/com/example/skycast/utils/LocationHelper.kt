package com.example.skycast.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

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
                // Fallback: fetch current location but race it against a 3000ms timeout.
                // We launch a coroutine scope to handle the timeout.
                val cancellationTokenSource = CancellationTokenSource()
                var hasReturned = false
                
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    val locationTask = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cancellationTokenSource.token
                    )
                    
                    try {
                        kotlinx.coroutines.withTimeout(3000L) {
                            // Wait for the task to complete within the 3s window
                            locationTask.addOnSuccessListener { location: Location? ->
                                if (!hasReturned) {
                                    hasReturned = true
                                    onLocationReceived(location)
                                }
                            }.addOnFailureListener {
                                if (!hasReturned) {
                                    hasReturned = true
                                    onLocationReceived(null)
                                }
                            }
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        // Timeout hit: cancel the GPS request and return null immediately.
                        cancellationTokenSource.cancel()
                        if (!hasReturned) {
                            hasReturned = true
                            onLocationReceived(null)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    }
}