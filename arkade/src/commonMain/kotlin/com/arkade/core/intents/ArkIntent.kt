package com.arkade.core.intents

import ark.v1.Intent

data class ArkIntent(
    val id: String,
    val txId: String,
    val walletId: String,
    val registerProofMessage: String,
    val registerProof: String,
) {
    internal fun toIntent(): Intent = Intent(registerProof, registerProofMessage)
}
