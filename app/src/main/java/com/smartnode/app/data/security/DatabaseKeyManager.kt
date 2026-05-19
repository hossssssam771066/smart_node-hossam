package com.smartnode.app.data.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a random 256-bit SQLCipher passphrase on first launch, wraps it
 * with the Android-Keystore-bound AES-GCM master key, and persists only the
 * encrypted form in SharedPreferences.
 *
 * The raw passphrase only exists in memory during database open and is
 * therefore unreadable to anyone with raw filesystem access.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    private val context: Context,
    private val keystoreManager: KeystoreManager,
) {

    /** Returns the raw 32-byte passphrase used to open the SQLCipher database. */
    fun getOrCreatePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ivB64 = prefs.getString(KEY_IV, null)
        val ctB64 = prefs.getString(KEY_CT, null)

        if (ivB64 != null && ctB64 != null) {
            val payload = KeystoreManager.EncryptedPayload(
                iv = Base64.decode(ivB64, Base64.NO_WRAP),
                ciphertext = Base64.decode(ctB64, Base64.NO_WRAP),
            )
            return keystoreManager.decrypt(payload)
        }

        val passphrase = ByteArray(PASSPHRASE_LENGTH).also { SecureRandom().nextBytes(it) }
        val payload = keystoreManager.encrypt(passphrase)
        prefs.edit {
            putString(KEY_IV, Base64.encodeToString(payload.iv, Base64.NO_WRAP))
            putString(KEY_CT, Base64.encodeToString(payload.ciphertext, Base64.NO_WRAP))
        }
        return passphrase
    }

    companion object {
        private const val PREFS = "smart_node_security"
        private const val KEY_IV = "db_key_iv"
        private const val KEY_CT = "db_key_ct"
        private const val PASSPHRASE_LENGTH = 32
    }
}
