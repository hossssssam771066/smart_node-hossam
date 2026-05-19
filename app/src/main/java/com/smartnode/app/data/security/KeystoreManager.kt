package com.smartnode.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages an AES-256-GCM master key inside the hardware-backed Android Keystore.
 *
 * The key is generated on first use, never leaves the secure element, and is
 * used to encrypt / decrypt the SQLCipher database passphrase before it is
 * persisted on disk.
 */
@Singleton
class KeystoreManager @Inject constructor() {

    fun encrypt(plain: ByteArray): EncryptedPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val iv = cipher.iv.copyOf()
        val ciphertext = cipher.doFinal(plain)
        return EncryptedPayload(iv = iv, ciphertext = ciphertext)
    }

    fun decrypt(payload: EncryptedPayload): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(GCM_TAG_BITS, payload.iv),
            )
        }
        return cipher.doFinal(payload.ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val gen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()
        gen.init(spec)
        return gen.generateKey()
    }

    data class EncryptedPayload(val iv: ByteArray, val ciphertext: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EncryptedPayload) return false
            return iv.contentEquals(other.iv) &&
                ciphertext.contentEquals(other.ciphertext)
        }

        override fun hashCode(): Int =
            31 * iv.contentHashCode() + ciphertext.contentHashCode()
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "smart_node_master_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_BITS = 256
        private const val GCM_TAG_BITS = 128
    }
}
