package com.arkade.core.bitcoin

enum class Network {
    MAINNET,
    TESTNET,
    SIGNET,
    REGTEST,
    ;

    companion object {
        fun fromString(network: String): Network =
            when (network) {
                "mainnet" -> MAINNET
                "testnet" -> TESTNET
                "signet" -> SIGNET
                "regtest" -> REGTEST
                else -> throw IllegalArgumentException("Invalid network: $network")
            }
    }
}
