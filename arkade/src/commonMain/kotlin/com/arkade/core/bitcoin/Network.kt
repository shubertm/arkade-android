package com.arkade.core.bitcoin

enum class Network {
    MAINNET,
    TESTNET,
    SIGNET,
    REGTEST,
    ;

    companion object {
        fun fromString(network: String): Network =
            when (network.lowercase()) {
                "mainnet" -> MAINNET
                "bitcoin" -> MAINNET
                "testnet" -> TESTNET
                "signet" -> SIGNET
                "mutinynet" -> SIGNET
                "regtest" -> REGTEST
                else -> throw IllegalArgumentException("Invalid network: $network")
            }
    }
}
