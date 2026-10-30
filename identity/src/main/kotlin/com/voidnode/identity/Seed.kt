package com.voidnode.identity

import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import cash.z.ecc.android.bip39.toSeed

/** BIP-39: 24 слова <-> 64-байтный мастер-ключ. Фразу нигде не логируем. */
object Seed {
    private const val LANG = "en"

    fun generate(): CharArray =
        MnemonicCode(WordCount.COUNT_24, languageCode = LANG).use { it.chars.copyOf() }

    fun isValid(phrase: CharArray): Boolean =
        try {
            MnemonicCode(phrase.copyOf(), languageCode = LANG).use { it.validate() }
            true
        } catch (e: Exception) {
            false
        }

    fun master(phrase: CharArray, passphrase: CharArray = CharArray(0)): ByteArray =
        MnemonicCode(phrase.copyOf(), languageCode = LANG).use { it.toSeed(passphrase) }
}
