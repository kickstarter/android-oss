package com.kickstarter.libs.keystore

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import com.kickstarter.libs.preferences.StringPreferenceType
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionEngine(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String = "",
    private val cipher: Cipher? = Cipher.getInstance("AES/GCM/NoPadding"), // TODO: communicate via interface not directly with Cipher
    private val keyStore: KeyStore? = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }, // TODO: communicate via interface not directly with Keystore
    private val keyGenerator: KeyGenerator? = KeyGenerator.getInstance(KEY_ALGORITHM_AES, "AndroidKeyStore") // TODO: communicate via interface not directly with KeyGenerator
) : StringPreferenceType {
    // - Overload to be able to use kotlin named parameters from JAVA code
    constructor(sharedPreferences: SharedPreferences, accessToken: String) : this(sharedPreferences = sharedPreferences, key = accessToken) {
        Timber.d("$this :Overloaded constructor")
    }

    private val charset by lazy {
        charset("UTF-8")
    }
    override val isSet: Boolean
        get() = sharedPreferences.contains(key)

    override fun get(): String {
        val encryptedValue = sharedPreferences.getString(key, defaultValue) ?: defaultValue
        val decryptedValue = if (encryptedValue.isNotEmpty()) {
             decryptData(keyAlias = key, encryptedValue.toByteArray())
        } else defaultValue
        return decryptedValue ?: defaultValue
    }
    override fun set(value: String?) {
        value?.let {
            val encryptedData = encryptData(keyAlias = key, it)
            sharedPreferences.edit().putString(key, encryptedData.toString()).apply()
        }
    }

    override fun delete() {
        sharedPreferences.edit().remove(key).apply()
    }

    private fun encryptData(keyAlias: String, text: String): ByteArray? {
        cipher?.init(Cipher.ENCRYPT_MODE, generateSecretKey(keyAlias))
        return cipher?.doFinal(text.toByteArray(charset))
    }

    private fun decryptData(keyAlias: String, encryptedData: ByteArray): String? {
        cipher?.init(Cipher.DECRYPT_MODE, getSecretKey(keyAlias), GCMParameterSpec(128, cipher.iv))
        return cipher?.doFinal(encryptedData)?.toString(charset)
    }

    private fun generateSecretKey(keyAlias: String): SecretKey? {
        return keyGenerator?.apply {
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }?.generateKey()
    }

    private fun getSecretKey(keyAlias: String) =
        (keyStore?.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
}
