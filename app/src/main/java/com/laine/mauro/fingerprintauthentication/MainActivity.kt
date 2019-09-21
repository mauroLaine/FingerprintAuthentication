package com.laine.mauro.fingerprintauthentication

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var fingerprintManager: FingerprintManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        if (!fingerprintManager.isHardwareDetected) {
            information_text.text = getString(R.string.not_supported)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            information_text.text = getString(R.string.enable_permission)
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            information_text.text = getString(R.string.register_fingerprint)
        }

        if (!keyguardManager.isKeyguardSecure) {
            information_text.text = getString(R.string.enable_lockscreen)
        }

    }
}
