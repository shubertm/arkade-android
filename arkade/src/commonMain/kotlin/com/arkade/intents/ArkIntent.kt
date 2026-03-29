package com.arkade.intents

data class ArkIntent(
    val id: String,
    val txId: String,
    val walletId: String,
    val registerProofMessage: String,
    val registerProof: String,
)
