package com.example.minivpn

import android.net.VpnService
import android.content.Intent

class MinimalVpnService : VpnService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Builder()
        builder.setSession("MinimalVPN")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .setBlocking(true)

        try {
            builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }
}