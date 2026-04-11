package com.arkade.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.PublicKey
import fr.acinq.bitcoin.XonlyPublicKey

/**
 * The unspendable x-only public key, nobody knows the private key. Any funds locked to this public key cannot be spent,
 * aka `NUMS`. In Taproot, it is used to lock key path spending and force script path spending.
 */
const val UNSPENDABLE_PUBKEY = "50929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace803ac0"

fun Long.multiplyExact(other: Long): Long {
    if (this == 0L || other == 0L) return 0L
    val result = this * other
    if (result / other != this || (this == Long.MIN_VALUE && other == -1L) || (other == Long.MIN_VALUE && this == -1L)) {
        throw ArithmeticException("Long overflow")
    }
    return result
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigDecimal): BigDecimal {
    var sum: BigDecimal = 0.toBigDecimal()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Converts a hex string to an [XonlyPublicKey] public key
 * The hex string can be an uncompressed, compressed or x-only public key
 * @return the [XonlyPublicKey] public key
 */
fun String.toXOnlyPubKey(): XonlyPublicKey {
    val bytes = hexToByteArray()
    val bytesSize = bytes.size
    if (bytesSize == 32) {
        return XonlyPublicKey(ByteVector32(bytes))
    }
    if (bytesSize == 33 || bytesSize == 65) {
        return PublicKey.parse(bytes).xOnly()
    }
    throw IllegalArgumentException("Invalid public key: $this")
}
