package com.omang.app.utils.signal

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NetworkSignalObserver(
    val context: Context
) : SignalObserver {

    private val telephonyManager = context.getSystemService(
        TelephonyManager::class.java
    ) as TelephonyManager

    /**
     * We are using different methods to fetch the signal status on API level 31 and 29.
     * Flow emits the returns the calculated signal strength and emits to observer.
     */
    override fun observe(): Flow<SignalObserver.Status> {
        return callbackFlow {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                val myCallback =
                    object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                            val strength = getSignalStrength(signalStrength.level)
                            // Timber.d("Signal strength is $strength")
                            launch {
                                send(strength)
                            }
                        }
                    }

                telephonyManager.registerTelephonyCallback(
                    context.mainExecutor,
                    myCallback
                )

                awaitClose {
                    telephonyManager.unregisterTelephonyCallback(myCallback)
                }
            } else {

                val phoneStateListenerCallback = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                        super.onSignalStrengthsChanged(signalStrength)
                        val strength = getSignalStrength(signalStrength!!.level)
                        launch {
                            send(strength)
                        }
                    }
                }

                telephonyManager.listen(
                    phoneStateListenerCallback,
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                )

                awaitClose {
                    telephonyManager.listen(
                        phoneStateListenerCallback,
                        PhoneStateListener.LISTEN_NONE
                    )
                }
            }
        }
    }

    //Calculates the signal strength
    fun getSignalStrength(gsmSignalStrength: Int): SignalObserver.Status {
        return when (gsmSignalStrength) {
            0 -> SignalObserver.Status.NOT_AVAILABLE
            1 -> SignalObserver.Status.WEAK
            2 -> SignalObserver.Status.AVERAGE
            3 -> SignalObserver.Status.GOOD
            4 -> SignalObserver.Status.EXCELLENT
            else -> SignalObserver.Status.NOT_AVAILABLE
        }
    }
}