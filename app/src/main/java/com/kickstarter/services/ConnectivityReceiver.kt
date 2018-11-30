package com.kickstarter.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.kickstarter.BuildConfig
import timber.log.Timber

class ConnectivityReceiver : BroadcastReceiver() {

    companion object {
        @JvmStatic
        lateinit var connectivityReceiverListener: ConnectivityReceiverListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        connectivityReceiverListener.onNetworkConnectionChanged(isConnected)

        if (BuildConfig.DEBUG) {
            Timber.d("$isConnected Network changed")
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }
}
