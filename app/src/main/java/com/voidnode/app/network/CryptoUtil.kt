package com.voidnode.app.network

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {

    private const val TRANSFORM = "AES/GCM/NoPadding"

    private fun key(passphrase: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256").digest(passphrase.toByteArray())
        return SecretKeySpec(digest, "AES")
    }

    fun encrypt(text: String, passphrase: String): String {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key(passphrase))
        val iv = cipher.iv
        val encrypted = cipher.doFinal(text.toByteArray())
        val result = iv + encrypted
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    fun decrypt(data: String, passphrase: String): String {
        val raw = Base64.decode(data, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, key(passphrase), GCMParameterSpec(128, raw, 0, 12))
        return String(cipher.doFinal(raw, 12, raw.size - 12))
    }
}
