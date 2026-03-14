package com.ark.core.bitcoin

import java.math.BigDecimal

/**
 * The `Coin` class represents the value and [Unit] of a [Utxo],
 * the [Unit] can be [Unit.BTC] or [Unit.SATOSHI]
 */
data class Coin(
    val unit: Unit,
    val amount: BigDecimal,
) {
    /**
     * Convert this [Coin] from [Unit.BTC] to [Unit.SATOSHI]
     */
    fun toSatoshi(): Coin {
        if (unit == Unit.SATOSHI) return this
        val sats = amount * Unit.BASE
        return Coin(Unit.SATOSHI, sats)
    }

    /**
     * Convert this [Coin] from [Unit.SATOSHI] to [Unit.BTC]
     */
    fun toBTC(): Coin {
        if (unit == Unit.BTC) return this
        val btc = amount / Unit.BASE
        return Coin(Unit.BTC, btc)
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
        fun fromBTC(btc: Float): Coin = Coin(Unit.BTC, btc.toBigDecimal())
    }
}
