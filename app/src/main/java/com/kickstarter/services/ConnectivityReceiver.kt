package com.kickstarter.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.kickstarter.BuildConfig
import timber.log.Timber

class ConnectivityReceiver : BroadcastReceiver() {

    companion object {
        @JvmStatic
        lateinit var connectivityReceiverListener: ConnectivityReceiverListener
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { // when given network becomes available
            super.onAvailable(network)
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected = true)
            if (BuildConfig.DEBUG) {
                Timber.d(" *************** $network has become Available")
            }
        }

        override fun onLost(network: Network) { // when given network loses connectivity or is disconnected
            super.onLost(network)
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected = false)
            if (BuildConfig.DEBUG) {
                Timber.d(" ************* $network has lost connection")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // - Check capabilities for WIFI or cellular connection
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unregister(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(networkCallback)
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean) {
            if (BuildConfig.DEBUG) {
                Timber.d("Network changed isConnected: $isConnected")
            }
        }
    }
}
