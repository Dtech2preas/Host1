package com.example.sshvpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sshvpn.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vpnRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load AdMob banner
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // VPN connection button
        binding.connectButton.setOnClickListener {
            connectToVpn()
        }
    }

    private fun connectToVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, vpnRequestCode)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        startService(Intent(this, SshVpnService::class.java))
        binding.statusText.text = "Connecting..."
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == vpnRequestCode && resultCode == RESULT_OK) {
            startVpnService()
        }
    }
}
