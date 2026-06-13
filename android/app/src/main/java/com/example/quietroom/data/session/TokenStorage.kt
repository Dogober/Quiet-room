package com.example.quietroom.data.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object TokenStorage {

    private const val PREFS_NAME = "quiet_room_secure_prefs"
    private const val LEGACY_PREFS_NAME = "quiet_room_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_IV = "jwt_token_iv"
    private const val KEY_ALIAS = "quiet_room_token_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    fun saveToken(
        context: Context,
        token: String
    ) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateSecretKey()
        )

        val encryptedToken = cipher.doFinal(
            token.toByteArray(StandardCharsets.UTF_8)
        )

        preferences(context)
            .edit()
            .putString(
                KEY_TOKEN,
                Base64.encodeToString(
                    encryptedToken,
                    Base64.NO_WRAP
                )
            )
            .putString(
                KEY_IV,
                Base64.encodeToString(
                    cipher.iv,
                    Base64.NO_WRAP
                )
            )
            .apply()
    }

    fun getToken(context: Context): String? {
        val preferences = preferences(context)
        val encryptedToken = preferences.getString(
            KEY_TOKEN,
            null
        ) ?: return null
        val iv = preferences.getString(
            KEY_IV,
            null
        ) ?: return null

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(
                    128,
                    Base64.decode(iv, Base64.NO_WRAP)
                )
            )

            val decryptedToken = cipher.doFinal(
                Base64.decode(
                    encryptedToken,
                    Base64.NO_WRAP
                )
            )
            String(
                decryptedToken,
                StandardCharsets.UTF_8
            )
        }.getOrElse {
            clearToken(context)
            null
        }
    }

    fun clearToken(context: Context) {
        preferences(context)
            .edit()
            .remove(KEY_TOKEN)
            .remove(KEY_IV)
            .apply()

        context.getSharedPreferences(
            LEGACY_PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(
            ANDROID_KEY_STORE
        ).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(
            KEY_ALIAS,
            null
        ) as? SecretKey

        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )
        return keyGenerator.generateKey()
    }
}
