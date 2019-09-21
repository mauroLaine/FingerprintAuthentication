package com.laine.mauro.fingerprintauthentication

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private lateinit var cipher: Cipher
    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
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
        } else {
            try {
                generateKey()
            } catch (e: FingerprintException) {
                e.printStackTrace()
            }

            if (initCypher()) {
                cryptoObject = FingerprintManager.CryptoObject(cipher)
                val helper: FingerprintHandler = FingerprintHandler()
                helper.startAuth(fingerprintManager, cryptoObject)
            }
        }
    }

    private fun generateKey() {
        try {

            // Obtain a reference to the Keystore using the standard Android keystore container
            // identifier (“AndroidKeystore”)
            keyStore = KeyStore.getInstance("AndroidKeyStore")

            //Generate the key
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )

            //Initialize an empty KeyStore
            keyStore.load(null)

            //Initialize the KeyGenerator
            keyGenerator.init(
                //Specify the operation(s) this key can be used for
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    //Configure this key so that the user has to confirm their identity with a fingerprint
                    // each time they want to use it
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            //Generate the key//
            keyGenerator.generateKey()

        } catch (exc: Exception) {
            when (exc) {
                is KeyStoreException, is NoSuchAlgorithmException, is NoSuchProviderException,
                is InvalidAlgorithmParameterException, is CertificateException, is IOException -> {
                    exc.printStackTrace()
                    throw FingerprintException(exc)
                }
            }
        }
    }

    private fun initCypher(): Boolean {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (exc: Exception) {
            when (exc) {
                is NoSuchAlgorithmException, is NoSuchPaddingException -> {
                    throw RuntimeException("Failed to get Cipher", exc)
                }
            }
        }

        try {
            keyStore.load(null)
            val key: SecretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            //Return true if the cipher has been initialized successfully
            return true
        } catch (exc: Exception) {
            when (exc) {
                is KeyPermanentlyInvalidatedException -> {
                    return false
                }
                is KeyStoreException, is CertificateException, is UnrecoverableKeyException,
                is IOException, is NoSuchAlgorithmException, is InvalidKeyException -> {
                    throw RuntimeException("Failed to init Cipher", exc)
                }
            }
        }
        return false
    }

    private inner class FingerprintException(e: Exception) : Exception(e)

    companion object {
        private val KEY_NAME = "mauroLaine"

    }
}
