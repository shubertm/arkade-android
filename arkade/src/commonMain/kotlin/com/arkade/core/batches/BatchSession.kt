package com.arkade.core.batches

import com.arkade.core.csvSigScript
import com.arkade.network.ArkadeClient

class BatchSession(
    private val client: ArkadeClient,
) : BatchEventHandler {
    suspend fun init() {
        val serverInfo = client.getInfo()
        val sweepTapScript = csvSigScript(serverInfo.sessionDuration.inWholeSeconds, serverInfo.forfeitPubKey)
    }

    override fun onBatchStarted() {
        TODO("Not yet implemented")
    }

    override fun onBatchFinalized() {
        TODO("Not yet implemented")
    }

    override fun onBatchFinalization() {
        TODO("Not yet implemented")
    }

    override fun onBatchFailed() {
        TODO("Not yet implemented")
    }

    override fun onTreeSigningStarted() {
        TODO("Not yet implemented")
    }

    override fun onTreeNoncesAggregated() {
        TODO("Not yet implemented")
    }

    override fun onTreeTx() {
        TODO("Not yet implemented")
    }

    override fun onTreeSignature() {
        TODO("Not yet implemented")
    }

    override fun onTreeNonces() {
        TODO("Not yet implemented")
    }
}
