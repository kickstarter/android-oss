package com.kickstarter.libs.keystore

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.extensions.decrypt
import com.kickstarter.libs.utils.extensions.encrypt
import com.kickstarter.libs.utils.extensions.isKSApplication
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncryptionEngine(
    private val sharedPreferences: SharedPreferences,
    private val keyAlias: String,
    private val defaultValue: String = "",
    private var keyStore: KeyStore? = null
) : StringPreferenceType {

    // - Overload to be able to use kotlin named parameters from JAVA code
    constructor(
        sharedPreferences: SharedPreferences,
        accessToken: String,
        context: Context
    ) : this(sharedPreferences = sharedPreferences, keyAlias = accessToken) {
        Timber.d("$this :Overloaded constructor")

        // - Avoid instantiating KeyStore on test Applications
        if (context.isKSApplication()) keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (getSecretKey(keyAlias = keyAlias) == null)
            generateSecretKey(keyAlias = keyAlias)
    }

    override val isSet: Boolean
        get() = sharedPreferences.contains(keyAlias)

    override fun get(): String {
        return if (isSet) {
            val b64 = sharedPreferences.getString(keyAlias, defaultValue) ?: defaultValue
            val secretKey = getSecretKey(keyAlias)
            return b64.decrypt(secretKey) ?: defaultValue
        } else ""
    }
    override fun set(value: String?) {
        value?.let {
            val secretKey = getSecretKey(keyAlias)
            val encryptedData = value.encrypt(secretKey = secretKey)
            sharedPreferences.edit().putString(keyAlias, encryptedData).apply()
        }
    }

    override fun delete() {
        sharedPreferences.edit().remove(keyAlias).apply()
    }
    private fun generateSecretKey(keyAlias: String) {

        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()

        // - Set duration/rotation for the key on the keyStorage
//        val start: Calendar = Calendar.getInstance()
//        val end: Calendar = Calendar.getInstance()
//        end.add(Calendar.YEAR, 2)

        val entry = KeyStore.SecretKeyEntry(secretKey)
        val protectionParameter =
            KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//                .setKeyValidityStart(start.time)
//                .setKeyValidityEnd(end.time)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()

        keyStore?.setEntry(keyAlias, entry, protectionParameter)
    }

    private fun getSecretKey(keyAlias: String) = keyStore?.getKey(keyAlias, null)
}
