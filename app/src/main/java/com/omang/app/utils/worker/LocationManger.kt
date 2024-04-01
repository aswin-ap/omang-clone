package com.omang.app.utils.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@SuppressLint("MissingPermission")
class LocationManger(context: Context) {
    private val fusedLocationProviderClient: FusedLocationProviderClient
    private val mLocationRequest: LocationRequest

    init {
        if (!context.hasLocationPermission())
            Toast.makeText(context, "Location permission is disabled", Toast.LENGTH_SHORT).show()

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled)
            Toast.makeText(context, "GPS is disabled", Toast.LENGTH_SHORT).show()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        mLocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10 * 60000
        )
            .setWaitForAccurateLocation(false).setMinUpdateIntervalMillis(5 * 60000)
            .setMaxUpdateDelayMillis(20 * 60000).build()

    }

    lateinit var locationCallback: LocationCallback

    suspend fun getLocationUpdates() = suspendCancellableCoroutine<LocationResult?> {
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                p0.isLocationAvailable
            }

            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                if (it.isActive) {
                    it.resume(p0)
                }
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}
