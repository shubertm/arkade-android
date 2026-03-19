package com.arkade.core.bitcoin

enum class Hrp(
    val prefix: String,
) {
    MAINNET("bc"),
    TESTNETS("tb"),
    REGTEST("bcrt"),
    ;

    companion object {
        fun fromNetwork(network: Network): Hrp =
            when (network) {
                Network.MAINNET -> MAINNET
                Network.TESTNET -> TESTNETS
                Network.SIGNET -> TESTNETS
                Network.REGTEST -> REGTEST
            }

        fun fromString(prefix: String): Hrp =
            when (prefix) {
                MAINNET.prefix -> MAINNET
                TESTNETS.prefix -> TESTNETS
                REGTEST.prefix -> REGTEST
                else -> {
                    throw IllegalArgumentException("Invalid address prefix")
                }
            }
    }
}
