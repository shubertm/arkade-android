package com.arkade.core.txs

sealed interface TxEvent {
    class CommitmentEvent(
        val commitmentTx: Notification,
    ) : TxEvent

    class ArkEvent(
        val arkTx: Notification?,
    ) : TxEvent

    class SweepEvent(
        val sweepTx: Notification?,
    ) : TxEvent

    object HeartbeatEvent : TxEvent
}
