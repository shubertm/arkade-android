package com.arkade.core.bitcoin

import fr.acinq.bitcoin.OutPoint

/**
 * The `Utxo` class represents the on-chain UTXO locked to the provided [Address]
 *
 * @property outpoint is the reference of this UTXO on-chain
 * @property amount is the amount of value this UTXO has
 * @property blockConfirmationTime is the Unix timestamp (in seconds) when this UTXO was first confirmed on-chain
 * @property isSpent is whether this UTXO is already spent or not
 * @property address is the on-chain [Address] locking this UTXO
 */
data class Utxo(
    val outpoint: OutPoint,
    val amount: Long,
    val blockConfirmationTime: Long,
    val isSpent: Boolean = false,
    val address: Address? = null,
)
