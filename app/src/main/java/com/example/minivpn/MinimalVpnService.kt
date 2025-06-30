package com.example.sshvpn

import android.net.VpnService
import android.content.Intent
import android.util.Log
import com.jcraft.jsch.*

class SshVpnService : VpnService() {
    private var sshThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Set up VPN tunnel
        val builder = Builder()
        builder.setSession("SSHVPN")
            .addAddress("10.8.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)

        try {
            builder.establish()
            Log.d("SSHVPN", "VPN interface established")

            // Start SSH tunnel
            startSshTunnel()
        } catch (e: Exception) {
            Log.e("SSHVPN", "VPN setup failed", e)
        }

        return START_STICKY
    }

    private fun startSshTunnel() {
        sshThread = Thread {
            try {
                val jsch = JSch()
                val session = jsch.getSession(
                    "YOUR_SSH_USERNAME",   // Replace with your SSH username
                    "YOUR_SSH_HOST",       // Replace with server host (e.g., server.fastssh.com)
                    YOUR_SSH_PORT          // Replace with port number (22, 443, etc.)
                )
                
                session.setPassword("YOUR_SSH_PASSWORD") // Replace with your password
                session.setConfig("StrictHostKeyChecking", "no")
                
                // 30-second keepalive to prevent disconnections
                session.setConfig("ServerAliveInterval", "30")
                
                session.connect()
                Log.d("SSHVPN", "SSH connection established")
                
                // Create SOCKS5 proxy on local port 1080
                session.setPortForwardingL(1080, "127.0.0.1", 1080)
                Log.d("SSHVPN", "SOCKS5 proxy started on 127.0.0.1:1080")
                
                // Keep thread alive
                while (!Thread.interrupted()) {
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                Log.e("SSHVPN", "SSH tunnel failed", e)
            }
        }
        
        sshThread?.start()
    }

    override fun onDestroy() {
        sshThread?.interrupt()
        super.onDestroy()
        Log.d("SSHVPN", "Service destroyed")
    }
}