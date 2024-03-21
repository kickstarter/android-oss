package com.kickstarter.libs.keystore

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.extensions.decrypt
import com.kickstarter.libs.utils.extensions.encrypt
import com.kickstarter.libs.utils.extensions.isKSApplication
import timber.log.Timber
import java.security.Key
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

interface KSKeyStore {
    var ksKeyStore: KeyStore?
    fun getSecretKey(keyAlias: String): Key? {
        return ksKeyStore?.getKey(keyAlias, null)
    }

    fun generateSecretKey(keyAlias: String) {
        ksKeyStore?.let {
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

            it.setEntry(keyAlias, entry, protectionParameter)
        }
    }
}
class EncryptionEngine(
    private val sharedPreferences: SharedPreferences,
    private val keyAlias: String,
    private val defaultValue: String = "",
    private val context: Context,
    private val featureFlagClient: FeatureFlagClientType
) : StringPreferenceType {

    // - Avoid instantiating KeyStore on test Applications
    var ksKeyStore = object : KSKeyStore {
        override var ksKeyStore: KeyStore? =
            if (context.isKSApplication()) KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            else null
    }

    // - Overload to be able to use kotlin named parameters from JAVA code
    constructor(
        sharedPreferences: SharedPreferences,
        accessToken: String,
        context: Context,
        featureFlagClient: FeatureFlagClientType
    ) : this(sharedPreferences = sharedPreferences, keyAlias = accessToken, context = context, featureFlagClient = featureFlagClient) {
        Timber.d("$this :Overloaded constructor")

        ksKeyStore.generateSecretKey(keyAlias = keyAlias)
    }

    override val isSet: Boolean
        get() = sharedPreferences.contains(keyAlias)

    override fun get(): String {
        return if (isSet) {
            if (featureFlagClient.getBoolean(FlagKey.ANDROID_ENCRYPT)) {
                val b64 = sharedPreferences.getString(keyAlias, defaultValue) ?: defaultValue
                val secretKey = ksKeyStore.getSecretKey(keyAlias)
                return b64.decrypt(secretKey) ?: defaultValue
            } else sharedPreferences.getString(keyAlias, defaultValue) ?: defaultValue
        } else ""
    }
    override fun set(value: String?) {
        value?.let {
            if (featureFlagClient.getBoolean(FlagKey = FlagKey.ANDROID_ENCRYPT)) {
                val secretKey = ksKeyStore.getSecretKey(keyAlias)
                val encryptedData = value.encrypt(secretKey = secretKey)
                sharedPreferences.edit().putString(keyAlias, encryptedData).apply()
            } else sharedPreferences.edit().putString(keyAlias, value).apply()
        }
    }
    override fun delete() {
        sharedPreferences.edit().remove(keyAlias).apply()
    }
}
