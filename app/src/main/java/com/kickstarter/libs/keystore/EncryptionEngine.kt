package com.kickstarter.libs.keystore

import android.content.SharedPreferences
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.extensions.decrypt
import com.kickstarter.libs.utils.extensions.encrypt
import timber.log.Timber
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

class EncryptionEngine(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String = ""
) : StringPreferenceType {

    var secretKey: String = "aesEncryptionKey"

    // - Overload to be able to use kotlin named parameters from JAVA code
    constructor(sharedPreferences: SharedPreferences, accessToken: String) : this(sharedPreferences = sharedPreferences, key = accessToken) {
        Timber.d("$this :Overloaded constructor")
    }

    override val isSet: Boolean
        get() = sharedPreferences.contains(key)

    override fun get(): String {
        return if (isSet) {
            val b64 = sharedPreferences.getString(key, defaultValue) ?: defaultValue
            return b64.decrypt(secretKey) ?: defaultValue
        } else ""
    }
    override fun set(value: String?) {
        value?.let {
            val encryptedData = value.encrypt(secretKey)
            sharedPreferences.edit().putString(key, encryptedData).apply()
        }
    }

    override fun delete() {
        sharedPreferences.edit().remove(key).apply()
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray()
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }
}
