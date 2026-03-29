package com.arkade.network

import ark.v1.PendingTx
import ark.v1.SubmitTxResponse
import com.arkade.core.ArkServerInfo
import com.arkade.core.batches.BatchEvent
import com.arkade.core.txs.TxEvent
import com.arkade.intents.ArkIntent
import kotlinx.coroutines.flow.Flow

interface ArkadeClient {
    suspend fun getInfo(): ArkServerInfo

    suspend fun registerIntent(intent: ArkIntent): String

    suspend fun confirmIntentRegistration(intentId: String)

    suspend fun deleteIntent(intent: ArkIntent)

    suspend fun submitTransaction(
        signedArkTx: String,
        checkpointTxs: List<String>,
    ): SubmitTxResponse

    suspend fun finalizeTransaction(
        arkTxid: String,
        finalCheckpointTxs: List<String>,
    )

    suspend fun submitTreeNonces(
        batchId: String,
        pubkey: String,
        treeNonces: Map<String, String>,
    )

    suspend fun submitTreeSignatures(
        batchId: String,
        pubkey: String,
        treeSignatures: Map<String, String>,
    )

    suspend fun submitForfeitTxs(
        signedForfeitTxs: List<String>,
        signedCommitmentTx: String?,
    )

    suspend fun getPendingTxs(intent: ArkIntent): List<PendingTx>

    fun getBatchEventStream(): Flow<BatchEvent>

    fun getTransactionsStream(): Flow<TxEvent>
}
