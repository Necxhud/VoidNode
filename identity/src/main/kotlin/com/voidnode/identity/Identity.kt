package com.voidnode.identity

/** Назначение ключа. Метка входит в HKDF, поэтому ключи не зависят друг от друга. */
enum class KeyPurpose(val label: String) {
    DATABASE("voidnode/v1/database"),
    IDENTITY("voidnode/v1/identity"),
}

class Identity private constructor(private var master: ByteArray?) : AutoCloseable {

    fun key(purpose: KeyPurpose): ByteArray {
        val m = master ?: error("Identity закрыт")
        return Hkdf.derive(m, SALT, purpose.label.toByteArray(), 32)
    }

    override fun close() {
        master?.fill(0)
        master = null
    }

    companion object {
        private val SALT = "voidnode".toByteArray()

        fun fromPhrase(phrase: CharArray): Identity {
            require(Seed.isValid(phrase)) { "Неверная seed-фраза" }
            return Identity(Seed.master(phrase))
        }
    }
}
