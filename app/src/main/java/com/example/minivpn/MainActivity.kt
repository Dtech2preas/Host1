package com.example.readyvpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.readyvpn.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // AdMob
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // VPN Connection
        binding.connectButton.setOnClickListener {
            if (VpnService.prepare(this) == null) {
                startService(Intent(this, VpnService::class.java))
                binding.statusText.text = "Connected"
            } else {
                startActivityForResult(Intent(this, VpnService::class.java), 0)
            }
        }
    }
}