package com.arkade.core.txs

/**
 * A base Bitcoin transaction
 * @param txId is the transaction id
 * @param tx is the serialized transaction data
 */
open class Transaction(
    val txId: String,
    val tx: String,
)
