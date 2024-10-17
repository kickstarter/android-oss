package com.kickstarter.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.RuntimeException

class ConnectivityReceiver(
    private val connectivityReceiverListener: ConnectivityReceiverListener,
    private val context: Context
) : BroadcastReceiver(), DefaultLifecycleObserver {
    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    private val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { // when given network becomes available
            super.onAvailable(network)
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected = true)
        }

        override fun onLost(network: Network) { // when given network loses connectivity or is disconnected
            super.onLost(network)
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected = false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: once all migration to RXJava is completed, get rid of the BroadcastReceiver receiver
        // and simple register the lifecycle observer on the target activity
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // - Check capabilities for WIFI or cellular connection
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            cm.registerNetworkCallback(networkRequest, networkCallback)
        } catch (exception: RuntimeException) {
            FirebaseCrashlytics.getInstance().setCustomKey(this.javaClass.name, exception.message ?: "")
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    private fun unregister(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(networkCallback)
        } catch (exception: IllegalArgumentException) {
            FirebaseCrashlytics.getInstance().setCustomKey(this.javaClass.name, exception.message ?: "")
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        context.registerReceiver(this, filter)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        this.unregister(context)
        context.unregisterReceiver(this)
    }
}
