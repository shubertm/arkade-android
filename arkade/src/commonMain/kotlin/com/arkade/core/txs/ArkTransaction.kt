package com.arkade.core.txs

/**
 * A base Ark off-chain transaction
 * @param txId is the transaction id
 * @param tx is the serialized transaction data
 * @param signedCheckpointTxs is the list of signed checkpoint transactions
 */
sealed class ArkTransaction(
    txId: String,
    tx: String,
    val signedCheckpointTxs: List<String>,
) : Transaction(txId, tx) {
    class Pending(
        txId: String,
        tx: String,
        signedCheckpointTxs: List<String>,
    ) : ArkTransaction(txId, tx, signedCheckpointTxs)

    class FullySigned(
        txId: String,
        tx: String,
        signedCheckpointTxs: List<String>,
    ) : ArkTransaction(txId, tx, signedCheckpointTxs)
}
