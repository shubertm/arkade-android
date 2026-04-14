package com.arkade.core.txs

/**
 * This is an interface of transaction events sent from Ark server,
 * any transaction event must implement this interface
 */
sealed interface TxEvent {
    class CommitmentEvent(
        val commitmentTx: Notification,
    ) : TxEvent

    class ArkEvent(
        val arkTx: Notification,
    ) : TxEvent

    class SweepEvent(
        val sweepTx: Notification,
    ) : TxEvent

    object HeartbeatEvent : TxEvent
}
