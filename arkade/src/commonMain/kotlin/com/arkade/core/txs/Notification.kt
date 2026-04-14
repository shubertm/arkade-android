package com.arkade.core.txs

import com.arkade.core.Vtxo
import fr.acinq.bitcoin.OutPoint

data class Notification(
    val txId: String,
    val tx: String,
    val checkpointTxs: Map<String, Transaction>,
    val spentVtxos: List<Vtxo.Data>,
    val spendableVtxos: List<Vtxo.Data>,
    val sweptVtxos: List<OutPoint>,
)
