package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Network
import com.ark.core.taproot.Parity
import com.ark.core.taproot.TaprootSpendingInfo
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.PublicKey
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey
import java.lang.Math.multiplyExact
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class BoardingOutput(
    val serverPubKey: XonlyPublicKey,
    val ownerPubKey: XonlyPublicKey,
    val spendingInfo: TaprootSpendingInfo,
    val address: Address,
    val exitDelay: Long,
) {
    private val tapScripts: List<ByteArray>

    init {
        val forfeitScript = multisigScript(serverPubKey, ownerPubKey)
        val exitScript = csvSigScript(exitDelay, ownerPubKey)
        tapScripts = listOf(forfeitScript, exitScript)
    }

    fun getScriptPubKey() = address.toScriptPubKey()

    fun getArkAddress(
        network: Network,
        serverPubKey: XonlyPublicKey,
    ) = ArkAddress.create(
        network,
        serverPubKey.value.toByteArray(),
        spendingInfo.outputKey.value.toByteArray(),
    )

    fun getControlBlock(script: ByteArray): ByteArray {
        val spendingLeaf =
            spendingInfo.merkleScriptTree.findScript((ByteVector(script)))
                ?: throw IllegalArgumentException("Invalid leaf script")

        return Script.ControlBlock
            .build(
                spendingInfo.internalKey,
                spendingInfo.merkleScriptTree,
                spendingLeaf,
            ).toByteArray()
    }

    fun getForfeitSpendingInfo(): ScriptSpendingPath {
        val script = tapScripts[0]
        val controlBlock = getControlBlock(script)
        return ScriptSpendingPath(script, controlBlock)
    }

    fun getExitSpendingInfo(): ScriptSpendingPath {
        val script = tapScripts[1]
        val controlBlock = getControlBlock(script)
        return ScriptSpendingPath(script, controlBlock)
    }

    fun getTapScripts(): List<ByteArray> = tapScripts

    fun canBeClaimedUnilaterally(
        now: Duration,
        blockConfirmTime: Duration,
    ): Boolean {
        val exitDelaySeconds = multiplyExact(exitDelay, 512)
        val exitTime = blockConfirmTime + exitDelaySeconds.toDuration(DurationUnit.SECONDS)
        return now >= exitTime
    }

    companion object {
        fun create(
            severPubKey: XonlyPublicKey,
            ownerPubKey: XonlyPublicKey,
            exitDelay: Long,
            network: Network,
        ): BoardingOutput {
            val multisigScript = multisigScript(severPubKey, ownerPubKey)
            val csvScript = csvSigScript(exitDelay, ownerPubKey)

            val merkleTree =
                ScriptTree.Branch(
                    ScriptTree.Leaf(ByteVector(multisigScript), 0),
                    ScriptTree.Leaf(ByteVector(csvScript), 0),
                )
            val merkleRoot = merkleTree.hash()

            val internalKey = PublicKey.fromHex(UNSPENDABLE_PUBKEY).xOnly()
            val (outputKey, isOdd) = internalKey.outputKey(merkleTree)
            val spendingInfo =
                TaprootSpendingInfo(
                    internalKey,
                    outputKey,
                    Parity.fromBooleanIsOdd(isOdd),
                    merkleRoot,
                    merkleTree,
                )

            val scriptPubKey =
                with(Script) {
                    write(pay2tr(internalKey, merkleRoot))
                }
            val address = Address.fromScriptPubKey(scriptPubKey, network)

            return BoardingOutput(
                severPubKey,
                ownerPubKey,
                spendingInfo,
                address,
                exitDelay,
            )
        }
    }
}
