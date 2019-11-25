package com.example.mediabender.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest


class NetworkConnectionHelper constructor(private var context: Context){

    companion object : SingletonHolder<NetworkConnectionHelper, Context>(::NetworkConnectionHelper)


     var isNetworkConnected: Boolean = false

    // Network Check
   init {
        try {
            val connectivityManager =
                this.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()

            connectivityManager.registerNetworkCallback(
                builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isNetworkConnected = true // Global Static Variable
                    }

                    override fun onLost(network: Network) {
                        isNetworkConnected = false // Global Static Variable
                    }
                }

            )
            isNetworkConnected = false
        } catch (e: Exception) {
            isNetworkConnected = false
        }
    }

}