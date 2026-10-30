package com.voidnode.identity

import kotlin.test.*

private fun ByteArray.hex() = joinToString("") { "%02x".format(it) }

private const val PHRASE12 =
    "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

class HkdfTest {
    @Test
    fun rfc5869Case1() {
        val ikm = ByteArray(22) { 0x0b }
        val salt = ByteArray(13) { it.toByte() }
        val info = ByteArray(10) { (0xf0 + it).toByte() }
        assertEquals(
            "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865",
            Hkdf.derive(ikm, salt, info, 42).hex()
        )
    }
}

class SeedTest {
    @Test
    fun bip39Vector() {
        val seed = Seed.master(PHRASE12.toCharArray(), "TREZOR".toCharArray())
        assertEquals(
            "c55257c360c07c72029aebc1b53c05ed0362ada38ead3e3e9efa3708e53495531f09a6987599d18264c1e1c92f2cf141630c7a3c4ab7c81b2f001698e7463b04",
            seed.hex()
        )
    }

    @Test
    fun badChecksumRejected() {
        assertFalse(Seed.isValid(PHRASE12.replace("about", "abandon").toCharArray()))
    }

    @Test
    fun generated24WordsValidAndUnique() {
        val a = Seed.generate()
        assertEquals(24, String(a).split(" ").size)
        assertTrue(Seed.isValid(a))
        assertNotEquals(String(a), String(Seed.generate()))
    }
}

class IdentityTest {
    @Test
    fun sameSeedSameKeys() {
        val a = Identity.fromPhrase(PHRASE12.toCharArray()).key(KeyPurpose.DATABASE)
        val b = Identity.fromPhrase(PHRASE12.toCharArray()).key(KeyPurpose.DATABASE)
        assertContentEquals(a, b)
        assertEquals(32, a.size)
    }

    @Test
    fun purposesAreIndependent() {
        val id = Identity.fromPhrase(PHRASE12.toCharArray())
        assertFalse(id.key(KeyPurpose.DATABASE).contentEquals(id.key(KeyPurpose.IDENTITY)))
    }

    @Test
    fun wrongPhraseRejected() {
        assertFailsWith<IllegalArgumentException> {
            Identity.fromPhrase(PHRASE12.replace("about", "abandon").toCharArray())
        }
    }

    @Test
    fun closedIdentityRefusesKeys() {
        val id = Identity.fromPhrase(PHRASE12.toCharArray())
        id.close()
        assertFailsWith<IllegalStateException> { id.key(KeyPurpose.DATABASE) }
    }
}
