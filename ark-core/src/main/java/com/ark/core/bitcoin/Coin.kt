package com.ark.core.bitcoin

/**
 * The `Coin` class represents the value and [Unit] of a [Utxo],
 * the [Unit] can be [Unit.BTC] or [Unit.SATOSHI]
 */
data class Coin(
    val unit: Unit,
    val amount: Float,
) {
    /**
     * Convert this [Coin] from [Unit.BTC] to [Unit.SATOSHI]
     */
    fun toSatoshi(): Coin {
        val sats = amount * Unit.BASE
        return Coin(Unit.SATOSHI, sats)
    }

    /**
     * Convert this [Coin] from [Unit.SATOSHI] to [Unit.BTC]
     */
    fun toBTC(): Coin {
        val btc = amount / 100_000_000L
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
            internal const val BASE = 100_000_000L
        }
    }

    companion object {
        /**
         * @param satoshi is the amount of money in [Unit.SATOSHI]
         * @return [Coin] in [Unit.SATOSHI]
         */
        fun fromSatoshi(satoshi: Long): Coin = Coin(Unit.SATOSHI, satoshi.toFloat())

        /**
         * @param btc is the amount of money in [Unit.BTC]
         * @return [Coin] in [Unit.BTC]
         */
        fun fromBTC(btc: Float): Coin = Coin(Unit.BTC, btc)
    }
}
