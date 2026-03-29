package com.arkade.core.txs

import ark.v1.Heartbeat
import ark.v1.TxNotification

class TxEvent(
    val commitmentTx: TxNotification?,
    val arkTx: TxNotification?,
    val sweepTx: TxNotification?,
    val heartbeat: Heartbeat?,
)
