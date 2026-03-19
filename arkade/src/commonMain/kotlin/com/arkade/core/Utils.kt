package com.arkade.core

import com.arkade.core.bitcoin.Coin
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import fr.acinq.bitcoin.OutPoint

/**
 * The unspendable x-only public key, nobody knows the private key. Any funds locked to this public key cannot be spent,
 * aka `NUMS`. In Taproot, it is used to lock key path spending and force script path spending.
 */
const val UNSPENDABLE_PUBKEY = "50929b74c1a04954b78b4b6035e97a5e078a5a0f28ec96d547bfee9ace803ac0"

fun Long.multiplyExact(other: Long): Long {
    val result = this * other
    if (result !in Long.MIN_VALUE .. Long.MAX_VALUE) {
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