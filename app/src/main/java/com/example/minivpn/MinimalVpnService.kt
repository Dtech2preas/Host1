package com.example.readyvpn

import android.net.VpnService
import android.content.Intent
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config

class VpnService : VpnService(), Tunnel {
    private val wgConfig = Config.Builder()
        .setPrivateKey("<YOUR_PRIVATE_KEY>")
        .addPeer(
            Config.Peer.Builder()
                .setPublicKey("<SERVER_PUBLIC_KEY>")
                .addAllowedIp("0.0.0.0/0")
                .setEndpoint("<SERVER_IP>:51820")
                .build()
        )
        .build()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Builder()
        builder.setSession("ReadyVPN")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .setBlocking(true)
            .establish()

        // WireGuard connection
        com.wireguard.android.backend.Backend.getDefault().apply {
            setState(this@VpnService, Tunnel.State.UP, wgConfig)
        }

        return START_STICKY
    }

    override fun getName() = "ReadyVPN"
    override fun onDestroy() {
        com.wireguard.android.backend.Backend.getDefault().setState(this, Tunnel.State.DOWN)
        super.onDestroy()
    }
}