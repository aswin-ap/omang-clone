package com.omang.app.utils.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.widget.Toast
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Singleton

/**
Custom Internet Connectivity Observer class 🥶
 * @author Aswin
 */
@Singleton
class NetworkConnection(val context: Context) {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var isCallbackRegistered = false

    fun observeNetworkConnection(): Flow<Boolean> = callbackFlow {
        val networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onAvailable(network: Network) {
                trySend(true)
            }
        }

        try {
            async {
                connectivityManager.registerDefaultNetworkCallback(networkConnectionCallback)
                trySend(hasInternet())
            }.await()
        }catch (e : Exception){
            e.printStackTrace()
        }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkConnectionCallback)
        }
    }

    /*  fun observeNetworkConnection(): Flow<Boolean> = callbackFlow {
          val networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
              override fun onLost(network: Network) {
                  trySend(false)
              }

              override fun onAvailable(network: Network) {
                  trySend(true)
              }
          }

          if (!isCallbackRegistered) {
              isCallbackRegistered = try {
                  connectivityManager.registerDefaultNetworkCallback(networkConnectionCallback)
                  true
              } catch (e: Exception) {
                  // TODO Handle registration failure
                  false
          //                e.printStackTrace()
              }
          }

          trySend(isConnected())

          awaitClose {
              if (isCallbackRegistered) {
                  if (networkConnectionCallback != null) {
                      try {
                          connectivityManager.unregisterNetworkCallback(networkConnectionCallback)

                      }catch (e : Exception){
                          e.printStackTrace()
                      }
                  }
                  isCallbackRegistered = false
              }
          }
      }*/

    fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    fun hasInternet(): Boolean {
        val nw = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(nw) ?: return false

        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                    socket.close()
                    true
                } catch (e: IOException) {
                    e.printStackTrace()
                    false
                }
    }
}