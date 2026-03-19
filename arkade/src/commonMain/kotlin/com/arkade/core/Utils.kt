package com.arkade.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

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
