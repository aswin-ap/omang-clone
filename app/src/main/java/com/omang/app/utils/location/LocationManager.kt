package com.omang.app.utils.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import timber.log.Timber

class LocationManager(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(10 * 60 * 1000) // 10 minutes in milliseconds
        .setFastestInterval(5 * 60 * 1000) // 5 minutes in milliseconds

    private val locationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            val location: Location? = locationResult.lastLocation
            location?.let {
                // Handle the location update
                Timber.tag("Location -> ").d("lat : ${it.latitude} long: ${it.longitude} alt: ${it.altitude} acc: ${it.accuracy}")
            }
        }
    }

    fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            // Handle security exception
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
