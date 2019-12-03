package com.example.mediabender.helpers

import android.content.Context
import com.example.mediabender.R
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * This class is a wrapper for the java encryption package. The key must be defined in the config file first.
 */
class EncryptionHelper constructor(private var context: Context) {

    companion object : SingletonHolder<EncryptionHelper, Context>(::EncryptionHelper)

    private val secretKeySpec: SecretKeySpec
    private val cipher: Cipher

    init{

        val pswdIterations = 65536
        val keySize = 128
        val saltBytes = byteArrayOf(0, 1, 2, 3, 4, 5, 6)

        val byteKey = context.getString(R.string.enc_key).toCharArray()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec = PBEKeySpec(byteKey, saltBytes, pswdIterations, keySize)
        val secretKey = factory.generateSecret(spec)

        secretKeySpec = SecretKeySpec(secretKey.getEncoded(), "AES")

        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    }

    fun encrypt(message: String?): String?{
        return message?.let {
            cipher.doFinal(it.toByteArray(Charset.forName("UTF-8"))).toString(Charset.forName("UTF-8"))
        }
    }

}