package com.arkade.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Messages exchanged by Ark server and clients

// Types
@Serializable
data class OutPoint(
    val txid: String,
    val vout: String,
)

@Serializable
data class Input(
    val outpoint: OutPoint,
    val taprootTree: TapScripts,
)

@Serializable
data class VtxoData(
    val outpoint: OutPoint,
    val amount: ULong,
    val script: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("expired_at")
    val expiredAt: Long,
    @SerialName("commitment_txids")
    val commitmentTxIds: List<String>,
    @SerialName("is_preconfirmed")
    val isPreconfirmed: Boolean,
    @SerialName("is_swept")
    val isSwept: Boolean,
    @SerialName("is_unrolled")
    val isUnrolled: Boolean,
    @SerialName("is_spent")
    val isSpent: Boolean,
    @SerialName("spent_by")
    val spentBy: String,
    @SerialName("settled_by")
    val settledBy: String,
    @SerialName("ark_txid")
    val arkTxId: String,
)

@Serializable
data class TxData(
    val txid: String,
    val tx: String,
)

@Serializable
data class TxNotification(
    val txid: String,
    val tx: String,
    @SerialName("spent_vtxos")
    val spentVtxos: List<VtxoData>,
    @SerialName("spendable_vtxos")
    val spendableVtxos: List<VtxoData>,
    @SerialName("checkpoint_txs")
    val checkpointTxs: Map<String, TxData>,
)

@Serializable
data class TapScripts(
    val scripts: List<String>,
)

@Serializable
data class BIP322Signature(
    val signature: String,
    val message: String,
)

@Serializable
data class MarketHour(
    @SerialName("next_start_time")
    val nextStartTime: Long,
    @SerialName("next_end_time")
    val nextEndTime: Long,
    val period: Long,
    @SerialName("round_interval")
    val roundInterval: Long,
)

// Events
@Serializable
data class BatchStartedEvent(
    val id: String,
    @SerialName("intent_id_hashes")
    val intentIdHashes: List<String>,
    @SerialName("batch_expiry")
    val batchExpiry: Long,
)

@Serializable
data class BatchFinalizationEvent(
    val id: String,
    @SerialName("commitment_tx")
    val commitmentTx: String,
)

@Serializable
data class BatchFinalizedEvent(
    val id: String,
    @SerialName("commitment_tx_id")
    val commitmentTxId: String,
)

@Serializable
data class BatchFailedEvent(
    val id: String,
    val reason: String,
)

@Serializable
data class TreeSigningStartedEvent(
    val id: String,
    @SerialName("cosigners_pubkeys")
    val cosignersPubKeys: List<String>,
    @SerialName("unsigned_commitment_tx")
    val unsignedCommitmentTx: String,
)

@Serializable
data class TreeNoncesAggregatedEvent(
    val id: String,
    @SerialName("tree_nonces")
    val treeNonces: String,
)

@Serializable
data class TreeTxEvent(
    val id: String,
    val topic: List<String>,
    @SerialName("batch_index")
    val batchIndex: Int,
    val txid: String,
    val tx: String,
    val children: Map<UInt, String>,
)

@Serializable
data class TreeSignatureEvent(
    val id: String,
    val topic: List<String>,
    @SerialName("batch_index")
    val batchIndex: Int,
    val txid: String,
    val signature: String,
)
