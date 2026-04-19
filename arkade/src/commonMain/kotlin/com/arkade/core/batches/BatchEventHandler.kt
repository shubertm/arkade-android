package com.arkade.core.batches

interface BatchEventHandler {
    fun onBatchStarted()

    fun onBatchFinalized()

    fun onBatchFinalization()

    fun onBatchFailed()

    fun onTreeSigningStarted()

    fun onTreeNoncesAggregated()

    fun onTreeTx()

    fun onTreeSignature()

    fun onTreeNonces()
}
