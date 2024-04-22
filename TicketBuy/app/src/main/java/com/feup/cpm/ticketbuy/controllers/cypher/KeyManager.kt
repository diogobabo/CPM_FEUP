package com.feup.cpm.ticketbuy.controllers.cypher

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.KeyStore.LoadStoreParameter
import java.security.interfaces.RSAPublicKey
import java.util.Calendar
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal


object KeyManager {

    private const val KEY_ALIAS = "TicketBuyKeyPair"
    private const val KEY_SIZE = 512
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALGO = "RSA"
    private const val SIGN_ALGO = "SHA256WithRSA"
    private const val ENC_ALGO = "RSA/NONE/PKCS1Padding"
    private const val SerialNr = 1234567890L

    private var keyPair: KeyPair? = null
    fun generateAndStoreKeys() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setKeySize(KEY_SIZE)
                .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)   // allowed digests for encryption and for signature
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)    // allowed padding schema for encryption
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)      // allowed padding schema for signature
                .setCertificateSubject(X500Principal("CN=" + KEY_ALIAS))
                .setCertificateSerialNumber(BigInteger.valueOf(SerialNr))
                .setCertificateNotBefore(GregorianCalendar().time)
                .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
                .build()
            KeyPairGenerator.getInstance(KEY_ALGO, ANDROID_KEYSTORE).apply {
                initialize(spec)
                generateKeyPair()   // the generated keys are stored in the Android Keystore
            }

            keyPair = getKeyPair()
        }
        else {
            keyPair = getKeyPair()
        }
    }

    private fun getKeyPair(): KeyPair? {
        // Load the key pair from the Android Keystore
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
        return if (publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }
    // Function to get the public key
    fun getPublicKey(): String? {
        val publicKey = this.keyPair?.public as? RSAPublicKey ?: return null
        val encodedPublicKey = publicKey.encoded
        return Base64.encodeToString(encodedPublicKey, Base64.DEFAULT)
    }

    // Function to sing data
    fun singData(data: ByteArray): ByteArray? {
        val signature = java.security.Signature.getInstance(SIGN_ALGO)
        if (keyPair == null) {
            return null
        }
        signature.initSign(keyPair?.private)
        signature.update(data)
        return signature.sign()
    }
}
