package com.arkade.core.batches

import kotlin.time.Duration

sealed interface BatchEvent {
    class BatchStartedEvent(
        val id: String,
        val batchExpiry: Duration,
        val intentIdHashes: List<String>,
    ) : BatchEvent

    class BatchFinalizationEvent(
        val id: String,
        val commitmentTx: String,
    ) : BatchEvent

    class BatchFinalizedEvent(
        val id: String,
        val commitmentTxId: String,
    ) : BatchEvent

    class BatchFailedEvent(
        val id: String,
        val reason: String,
    ) : BatchEvent

    class TreeSigningStartedEvent(
        val id: String,
        val coSigners: List<String>,
        val unsignedCommitmentTx: String,
    ) : BatchEvent

    class TreeNoncesAggregatedEvent(
        val id: String,
        val treeNonces: Map<String, String>,
    ) : BatchEvent

    class TreeTxEvent(
        val id: String,
        val batchIndex: Int,
        val tx: String,
        val txId: String,
        val children: Map<Int, String>,
        val topic: List<String>,
    ) : BatchEvent

    class TreeSignatureEvent(
        val id: String,
        val batchIndex: Int,
        val txId: String,
        topic: List<String>,
    ) : BatchEvent

    class TreeNoncesEvent(
        val id: String,
        val txId: String,
        val topic: List<String>,
        val treeNonces: Map<String, String>,
    ) : BatchEvent

    class Heartbeat : BatchEvent

    class StreamStartedEvent(
        val id: String,
    ) : BatchEvent
}
