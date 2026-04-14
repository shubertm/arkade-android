package com.arkade.core.txs

sealed class ArkTransaction(
    override val txId: String,
    override val tx: String,
    open val signedCheckpointTxs: List<String>,
) : Transaction(txId, tx) {
    class Pending(
        override val txId: String,
        override val tx: String,
        override val signedCheckpointTxs: List<String>,
    ) : ArkTransaction(txId, tx, signedCheckpointTxs)

    class FullySigned(
        override val txId: String,
        override val tx: String,
        override val signedCheckpointTxs: List<String>,
    ) : ArkTransaction(txId, tx, signedCheckpointTxs)
}
