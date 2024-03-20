package com.kickstarter.libs.keystore

import android.content.SharedPreferences
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.util.Base64
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.CodeVerifier
import timber.log.Timber
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class EncryptionEngine(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String = "",
    private val cipher: Cipher? = Cipher.getInstance("AES/CBC/PKCS5PADDING"), // TODO: communicate via interface not directly with Cipher
    private val keyStore: KeyStore? = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }, // TODO: communicate via interface not directly with Keystore
    private val keyGenerator: KeyGenerator? = KeyGenerator.getInstance(KEY_ALGORITHM_AES, "AndroidKeyStore") // TODO: communicate via interface not directly with KeyGenerator
) : StringPreferenceType {
    // - Overload to be able to use kotlin named parameters from JAVA code
    constructor(sharedPreferences: SharedPreferences, accessToken: String) : this(sharedPreferences = sharedPreferences, key = accessToken) {
        Timber.d("$this :Overloaded constructor")
    }
    override val isSet: Boolean
        get() = sharedPreferences.contains(key)

    override fun get(): String {
        return if (isSet) {
            val b64 = sharedPreferences.getString(key, defaultValue) ?: defaultValue
            val byteArray = decrypt(key, b64.toByteArray())
            return String(byteArray)
        } else ""
    }
    override fun set(value: String?) {
        value?.let {
            val encryptedData = encrypt(key, it)
            val b64 = Base64.encodeToString(encryptedData, CodeVerifier.PKCE_BASE64_ENCODE_SETTINGS)
            sharedPreferences.edit().putString(key, b64).apply()
        }
    }

    override fun delete() {
        sharedPreferences.edit().remove(key).apply()
    }

    private fun encrypt(keyAlias: String, strToEncrypt: String): ByteArray {
        val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
        val key = generateKey(keyAlias)
        cipher?.init(Cipher.ENCRYPT_MODE, key)
        return cipher?.doFinal(plainText) ?: "".toByteArray()
    }

    private fun decrypt(keyAlias: String, dataToDecrypt: ByteArray): ByteArray {
        val key = generateKey(keyAlias)
        cipher?.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(cipher.iv))
        return cipher?.doFinal(dataToDecrypt) ?: "".toByteArray()
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray()
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }
}
