package com.arkade.core.txs

import ark.v1.TxNotification

sealed interface TxEvent {
    class CommitmentEvent(
        val commitmentTx: TxNotification?,
    ) : TxEvent

    class ArkEvent(
        val arkTx: TxNotification?,
    ) : TxEvent

    class SweepEvent(
        val sweepTx: TxNotification?,
    ) : TxEvent

    object HeartbeatEvent : TxEvent
}
