package com.arkade.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class XOnlyPublicKeyTest {
    val unCompressedNums =
        "0450929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace8" +
            "03ac031d3c6863973926e049e637cb1b5f40a36dac28af1766968c30c2313f3a38904"
    val malformedUnCompressedNums =
        "50929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace8" +
            "03ac031d3c6863973926e049e637cb1b5f40a36dac28af1766968c30c2313f3a38904"
    val compressedNums = "0350929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace803ac0"
    val xOnlyNums = "50929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace803ac0"

    @Test
    fun should_decode_pub_keys_correctly() {
        val decodedUnCompressedNums = unCompressedNums.toXOnlyPubKey()
        assertEquals(UNSPENDABLE_PUBKEY, decodedUnCompressedNums.value.toHex())
        val decodedCompressedNums = compressedNums.toXOnlyPubKey()
        assertEquals(UNSPENDABLE_PUBKEY, decodedCompressedNums.value.toHex())
        val decodedXOnlyNums = xOnlyNums.toXOnlyPubKey()
        assertEquals(UNSPENDABLE_PUBKEY, decodedXOnlyNums.value.toHex())
    }

    @Test
    fun should_fail_on_malformed_pub_key() {
        assertFailsWith<IllegalArgumentException> {
            malformedUnCompressedNums.toXOnlyPubKey()
        }
    }
}
