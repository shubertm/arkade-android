package com.ark.core.bitcoin

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The `Coin` class represents the value and [Unit] of a [Utxo],
 * the [Unit] can be [Unit.BTC] or [Unit.SATOSHI]
 */
data class Coin(
    val unit: Unit,
    val amount: BigDecimal,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
        when (unit) {
            Unit.BTC -> require(amount.scale() <= 8) { "BTC amount must have at most 8 decimal places" }
            Unit.SATOSHI -> require(amount.scale() <= 0) { "Satoshi amount must have no decimal places" }
        }
    }

    /**
     * Convert this [Coin] from [Unit.BTC] to [Unit.SATOSHI]
     */
    fun toSatoshi(): Coin {
        if (unit == Unit.SATOSHI) return this
        val sats = amount.multiply(Unit.BASE).setScale(0, RoundingMode.UNNECESSARY)
        return Coin(Unit.SATOSHI, sats)
    }

    /**
     * Convert this [Coin] from [Unit.SATOSHI] to [Unit.BTC]
     */
    fun toBTC(): Coin {
        if (unit == Unit.BTC) return this
        val btc = amount.divide(Unit.BASE, 8, RoundingMode.UNNECESSARY)
        return Coin(Unit.BTC, btc)
    }

    override fun equals(other: Any?): Boolean = other is Coin && unit == other.unit && amount.compareTo(other.amount) == 0

    override fun hashCode(): Int {
        var result = unit.hashCode()
        result = 31 * result + amount.stripTrailingZeros().hashCode()
        return result
    }

    /**
     * The `Unit` enum class represents the units in which a [Utxo] value can be presented in.
     */
    enum class Unit {
        BTC,
        SATOSHI,
        ;

        companion object {
            internal val BASE = BigDecimal(100_000_000L)
        }
    }

    companion object {
        /**
         * @param satoshi is the amount of money in [Unit.SATOSHI]
         * @return [Coin] in [Unit.SATOSHI]
         */
        fun fromSatoshi(satoshi: Long): Coin = Coin(Unit.SATOSHI, satoshi.toBigDecimal())

        /**
         * @param btc is the amount of money in [Unit.BTC]
         * @return [Coin] in [Unit.BTC]
         */
        fun fromBTC(btc: BigDecimal): Coin = Coin(Unit.BTC, btc)
    }
}
