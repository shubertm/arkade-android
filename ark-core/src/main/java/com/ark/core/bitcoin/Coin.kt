package com.ark.core.bitcoin

data class Coin(
    val unit: Unit,
    val amount: Float,
) {
    fun toSatoshi(): Coin {
        val sats = amount * Unit.BASE
        return Coin(Unit.SATOSHI, sats)
    }

    fun toBTC(): Coin {
        val btc = amount / 100_000_000L
        return Coin(Unit.BTC, btc)
    }

    enum class Unit {
        BTC,
        SATOSHI,
        ;

        companion object {
            internal const val BASE = 100_000_000L
        }
    }

    companion object {
        fun fromSatoshi(satoshi: Long): Coin = Coin(Unit.SATOSHI, satoshi.toFloat())

        fun fromBTC(btc: Float): Coin = Coin(Unit.BTC, btc)
    }
}
