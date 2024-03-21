package com.kickstarter.libs.preferences

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Build
import com.kickstarter.libs.MockSharedPreferences
import com.kickstarter.libs.keystore.EncryptionEngine
import com.kickstarter.libs.keystore.KSKeyStore
import com.kickstarter.mock.MockFeatureFlagClient
import org.junit.Test
import java.security.Key
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

class EncryptionEngineTest : KSRobolectricTestCase() {

    lateinit var build: Build
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        build = requireNotNull(environment().build())
        context = application()
    }
    @Test
    fun testEncryptDecrypt() {

        val mockKSKeyStore = object : KSKeyStore {
            override var ksKeyStore: KeyStore? = null

            override fun getSecretKey(keyAlias: String): Key? {
                val key = "aesEncryptionKey"
                return SecretKeySpec(key.toByteArray(), "AES")
            }
        }
        val mockffClient = MockFeatureFlagClient()
        val engine = EncryptionEngine(
            sharedPreferences = MockSharedPreferences(),
            "Alias",
            context,
            mockffClient,
        )

        engine.ksKeyStore = mockKSKeyStore

        val textForEncryption = "This my text that will be encrypted!"

        engine.set(textForEncryption)
        val decrypted = engine.get()

        assertEquals(textForEncryption, decrypted)
    }
}
