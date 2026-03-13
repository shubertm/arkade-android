package com.ark.core.bitcoin

import fr.acinq.bitcoin.OutPoint

data class Utxo(
    val outpoint: OutPoint,
    val amount: Long,
    val blockConfirmationTime: Long,
    val isSpent: Boolean = false,
    val address: Address? = null,
)
