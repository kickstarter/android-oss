package com.kickstarter.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.kickstarter.KSApplication

class ConnectivityReceiver() : BroadcastReceiver() {

    companion object {

        @JvmStatic
        lateinit var connectivityReceiverListener: ConnectivityReceiverListener

        fun isConnected(): Boolean {
            val cm = KSApplication.instance
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

    }

    override fun onReceive(context: Context, arg1: Intent) {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        connectivityReceiverListener.onNetworkConnectionChanged(isConnected)
    }

    fun isConnected(): Boolean {
        val cm = KSApplication.instance
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }


    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

}