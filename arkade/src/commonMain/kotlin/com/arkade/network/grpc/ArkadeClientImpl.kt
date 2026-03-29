package com.arkade.network.grpc

import ark.v1.ConfirmRegistrationRequest
import ark.v1.DeleteIntentRequest
import ark.v1.FinalizeTxRequest
import ark.v1.GetEventStreamRequest
import ark.v1.GetInfoRequest
import ark.v1.GetPendingTxRequest
import ark.v1.GetTransactionsStreamRequest
import ark.v1.GrpcArkServiceClient
import ark.v1.Intent
import ark.v1.PendingTx
import ark.v1.RegisterIntentRequest
import ark.v1.SubmitSignedForfeitTxsRequest
import ark.v1.SubmitTreeNoncesRequest
import ark.v1.SubmitTreeSignaturesRequest
import ark.v1.SubmitTxResponse
import com.arkade.core.ArkServerInfo
import com.arkade.core.LockedVTXOException
import com.arkade.core.SpentVTXOException
import com.arkade.core.batches.BatchEvent
import com.arkade.core.bitcoin.Address
import com.arkade.core.bitcoin.Network
import com.arkade.core.txs.TxEvent
import com.arkade.intents.ArkIntent
import com.arkade.network.ArkadeClient
import com.arkade.network.Config
import com.squareup.wire.GrpcClient
import com.squareup.wire.bidirectionalStream
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.XonlyPublicKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.time.Duration.Companion.seconds

class ArkServiceClientImpl(
    netConfig: Config,
) : ArkadeClient {
    private val grpcClient = gRPCClient(netConfig.arkadeUrl)
    private val arkadeServiceClient = GrpcArkServiceClient(grpcClient)

    override suspend fun getInfo(): ArkServerInfo {
        val infoResponse = arkadeServiceClient.GetInfo().execute(GetInfoRequest())
        return ArkServerInfo(
            infoResponse.version,
            XonlyPublicKey(ByteVector32.fromValidHex(infoResponse.signer_pubkey)),
            XonlyPublicKey(ByteVector32.fromValidHex(infoResponse.forfeit_pubkey)),
            Address.decode(infoResponse.forfeit_address),
            infoResponse.checkpoint_tapscript,
            Network.fromString(infoResponse.network),
            infoResponse.session_duration.seconds,
            infoResponse.unilateral_exit_delay.seconds,
            infoResponse.boarding_exit_delay.seconds,
            infoResponse.utxo_min_amount,
            infoResponse.utxo_max_amount,
            infoResponse.vtxo_min_amount,
            infoResponse.vtxo_max_amount,
            infoResponse.dust,
            infoResponse.fees,
            infoResponse.scheduled_session,
            infoResponse.deprecated_signers,
            infoResponse.service_status,
            infoResponse.digest,
            infoResponse.max_tx_weight,
            infoResponse.max_op_return_outputs,
        )
    }

    override suspend fun registerIntent(intent: ArkIntent): String {
        try {
            val intent = Intent(intent.registerProof, intent.registerProofMessage)
            val intentRequest = RegisterIntentRequest(intent)
            val intentResponse =
                arkadeServiceClient
                    .RegisterIntent()
                    .execute(intentRequest)
            return intentResponse.intent_id
        } catch (_: CancellationException) {
            return ""
        } catch (e: Exception) {
            val msg = e.message
            when {
                (msg?.contains("duplicated input") == true) -> {
                    throw LockedVTXOException("VTXO is already locked by another intent")
                }
                (msg?.contains("already spent") == true || e.message?.contains("VTXO_ALREADY_SPENT") == true) -> {
                    throw SpentVTXOException("VTXO input was already spent in a batch: ${e.message}")
                }
                else -> throw e
            }
        }
    }

    override suspend fun confirmIntentRegistration(intentId: String) {
        arkadeServiceClient.ConfirmRegistration().execute(
            ConfirmRegistrationRequest(intentId),
        )
    }

    override suspend fun deleteIntent(intent: ArkIntent) {
        arkadeServiceClient.DeleteIntent().execute(
            DeleteIntentRequest(Intent(intent.registerProof, intent.registerProofMessage)),
        )
    }

    override suspend fun submitTransaction(
        signedArkTx: String,
        checkpointTxs: List<String>,
    ): SubmitTxResponse {
        val txRequest = ark.v1.SubmitTxRequest(signedArkTx, checkpointTxs)
        return arkadeServiceClient.SubmitTx().execute(txRequest)
    }

    override suspend fun finalizeTransaction(
        arkTxid: String,
        finalCheckpointTxs: List<String>,
    ) {
        val request = FinalizeTxRequest(arkTxid, finalCheckpointTxs)
        arkadeServiceClient.FinalizeTx().execute(request)
    }

    override suspend fun submitTreeNonces(
        batchId: String,
        pubkey: String,
        treeNonces: Map<String, String>,
    ) {
        val request = SubmitTreeNoncesRequest(batchId, pubkey, treeNonces)
        arkadeServiceClient.SubmitTreeNonces().execute(request)
    }

    override suspend fun submitTreeSignatures(
        batchId: String,
        pubkey: String,
        treeSignatures: Map<String, String>,
    ) {
        val request = SubmitTreeSignaturesRequest(batchId, pubkey, treeSignatures)
        arkadeServiceClient.SubmitTreeSignatures().execute(request)
    }

    override suspend fun submitForfeitTxs(
        signedForfeitTxs: List<String>,
        signedCommitmentTx: String?,
    ) {
        var request = SubmitSignedForfeitTxsRequest(signedForfeitTxs)
        if (signedCommitmentTx != null) {
            request = request.copy(signed_commitment_tx = signedCommitmentTx)
        }
        arkadeServiceClient.SubmitSignedForfeitTxs().execute(request)
    }

    override fun getBatchEventStream(): Flow<BatchEvent> {
        val scope = CoroutineScope(Dispatchers.IO)
        val request = GetEventStreamRequest()
        return channelFlow {
            val receiveChannel =
                arkadeServiceClient.GetEventStream().bidirectionalStream(
                    scope,
                ) { sendChannel, _ ->
                    sendChannel.send(request)
                }
            receiveChannel.consumeEach { response ->
                when (response) {
                    response.batch_started -> {
                        send(
                            BatchEvent.BatchStartedEvent(
                                response.batch_started.id,
                                response.batch_started.batch_expiry.seconds,
                                response.batch_started.intent_id_hashes,
                            ),
                        )
                    }
                    response.batch_finalization -> {
                        send(
                            BatchEvent.BatchFinalizationEvent(
                                response.batch_finalization.commitment_tx,
                                response.batch_finalization.id,
                            ),
                        )
                    }
                    response.batch_finalized -> {
                        send(
                            BatchEvent.BatchFinalizedEvent(
                                response.batch_finalized.commitment_txid,
                                response.batch_finalized.id,
                            ),
                        )
                    }
                    response.batch_failed -> {
                        send(
                            BatchEvent.BatchFailedEvent(
                                response.batch_failed.id,
                                response.batch_failed.reason,
                            ),
                        )
                    }
                    response.tree_signing_started -> {
                        send(
                            BatchEvent.TreeSigningStartedEvent(
                                response.tree_signing_started.id,
                                response.tree_signing_started.cosigners_pubkeys,
                                response.tree_signing_started.unsigned_commitment_tx,
                            ),
                        )
                    }
                    response.tree_nonces_aggregated -> {
                        send(
                            BatchEvent.TreeNoncesAggregatedEvent(
                                response.tree_nonces_aggregated.id,
                                response.tree_nonces_aggregated.tree_nonces,
                            ),
                        )
                    }
                    response.tree_tx -> {
                        send(
                            BatchEvent.TreeTxEvent(
                                response.tree_tx.id,
                                response.tree_tx.batch_index,
                                response.tree_tx.tx,
                                response.tree_tx.txid,
                                response.tree_tx.children,
                                response.tree_tx.topic,
                            ),
                        )
                    }
                    response.tree_signature -> {
                        send(
                            BatchEvent.TreeSignatureEvent(
                                response.tree_signature.id,
                                response.tree_signature.batch_index,
                                response.tree_signature.txid,
                                response.tree_signature.topic,
                            ),
                        )
                    }
                    response.tree_nonces -> {
                        send(
                            BatchEvent.TreeNoncesEvent(
                                response.tree_nonces.id,
                                response.tree_nonces.txid,
                                response.tree_nonces.topic,
                                response.tree_nonces.nonces,
                            ),
                        )
                    }
                    response.heartbeat -> {
                        send(BatchEvent.Heartbeat())
                    }
                    response.stream_started -> {
                        send(BatchEvent.StreamStartedEvent(response.stream_started.id))
                    }
                }
            }
        }
    }

    override fun getTransactionsStream(): Flow<TxEvent> {
        val scope = CoroutineScope(Dispatchers.IO)
        val request = GetTransactionsStreamRequest()
        return channelFlow {
            val receiveChannel =
                arkadeServiceClient.GetTransactionsStream().bidirectionalStream(scope) { sendChannel, _ ->
                    sendChannel.send(request)
                }
            receiveChannel.consumeEach { response ->
                send(
                    TxEvent(
                        response.commitment_tx,
                        response.ark_tx,
                        response.sweep_tx,
                        response.heartbeat,
                    ),
                )
            }
        }
    }

    override suspend fun getPendingTxs(intent: ArkIntent): List<PendingTx> {
        val request = GetPendingTxRequest(Intent(intent.registerProof, intent.registerProofMessage))
        val response = arkadeServiceClient.GetPendingTx().execute(request)
        return response.pending_txs
    }
}

expect fun gRPCClient(baseUrl: String): GrpcClient
