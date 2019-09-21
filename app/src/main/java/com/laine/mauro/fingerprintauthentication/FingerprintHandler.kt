package com.laine.mauro.fingerprintauthentication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.support.v4.app.ActivityCompat
import android.widget.Toast

class FingerprintHandler constructor(private val context: Context) : FingerprintManager.AuthenticationCallback() {

    private lateinit var cancellationSignal: CancellationSignal


    fun startAuth(fingerprintManager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    //onAuthenticationError is called when a fatal error has occurred.
    // It provides the error code and error message as its parameters
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        Toast.makeText(context, "Authentication error\n" + errString, Toast.LENGTH_SHORT).show()
    }

    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of
    // the fingerprints registered on the device
    override fun onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional
    // information about the error
    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        Toast.makeText(context, "Authentication help\n" + helpString, Toast.LENGTH_SHORT).show();
    }

    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints
    // stored on the user’s device
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
    }
}