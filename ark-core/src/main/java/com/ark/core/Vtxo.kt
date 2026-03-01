package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Network
import com.ark.core.taproot.Parity
import com.ark.core.taproot.TaprootSpendingInfo
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class Vtxo(
    val serverPubKey: XonlyPublicKey,
    val ownerPubKey: XonlyPublicKey,
    val spendingInfo: TaprootSpendingInfo,
    val tapScripts: List<ByteArray>,
    val address: Address,
    val exitDelay: Long,
    val exitDelaySeconds: Long,
    val network: Network,
) {
    fun getScriptPubKey(): ByteArray = address.toScriptPubKey()

    fun getArkAddress(): ArkAddress =
        ArkAddress.create(
            network,
            serverPubKey.value.toByteArray(),
            spendingInfo.outputKey.value.toByteArray(),
        )

    fun getControlBlock(script: ByteArray): ByteArray {
        val spendingLeaf =
            spendingInfo.merkleScriptTree.findScript(ByteVector32(script))
                ?: throw IllegalArgumentException("Invalid leaf script")

        return Script.ControlBlock
            .build(
                spendingInfo.internalKey,
                spendingInfo.merkleScriptTree,
                spendingLeaf,
            ).toByteArray()
    }

    fun getForfeitSpendingPath(): ScriptSpendingPath {
        val forfeitScript = tapScripts[0]
        val controlBlock = getControlBlock(forfeitScript)
        return ScriptSpendingPath(forfeitScript, controlBlock)
    }

    fun getExitSpendingPath(): ScriptSpendingPath {
        val exitScript = tapScripts[1]
        val controlBlock = getControlBlock(exitScript)
        return ScriptSpendingPath(exitScript, controlBlock)
    }

    fun canBeClaimedUnilaterally(
        now: Duration,
        blockConfirmTime: Duration,
    ): Boolean {
        val exitTime = blockConfirmTime + exitDelaySeconds.toDuration(DurationUnit.SECONDS)
        return now > exitTime
    }

    companion object {
        fun build(
            serverPubKey: XonlyPublicKey,
            ownerPubKey: XonlyPublicKey,
            exitDelay: Long,
            network: Network,
        ): Vtxo {
            val forfeitScript = multisigScript(serverPubKey, ownerPubKey)
            val exitScript = csvSigScript(exitDelay, ownerPubKey)
            val tapScripts = listOf(forfeitScript, exitScript)

            return fromScripts(
                serverPubKey,
                ownerPubKey,
                tapScripts,
                exitDelay,
                network,
            )
        }

        fun fromScripts(
            serverPubKey: XonlyPublicKey,
            ownerPubKey: XonlyPublicKey,
            tapScripts: List<ByteArray>,
            exitDelay: Long,
            network: Network,
        ): Vtxo {
            require(tapScripts.size == 2) { "Expects exactly 2 tap scripts: forfeit and exit" }
            val unSpendablePubKey = XonlyPublicKey(ByteVector32.fromValidHex(UNSPENDABLE_PUBKEY))

            val (forfeitScript, exitScript) = tapScripts
            val forfeitLeaf = ScriptTree.Leaf(ByteVector(forfeitScript), 0)
            val exitLeaf = ScriptTree.Leaf(ByteVector(exitScript), 0)
            val scriptTree = ScriptTree.Branch(forfeitLeaf, exitLeaf)
            val merkleRoot = scriptTree.hash()

            val (outputKey, isOdd) = unSpendablePubKey.outputKey(merkleRoot)

            val spendingInfo =
                TaprootSpendingInfo(
                    unSpendablePubKey,
                    outputKey,
                    Parity.fromBooleanIsOdd(isOdd),
                    merkleRoot,
                    scriptTree,
                )

            val scriptPubKey =
                with(Script) {
                    write(pay2tr(unSpendablePubKey, merkleRoot))
                }
            val address = Address.fromScriptPubKey(scriptPubKey, network)

            return Vtxo(
                serverPubKey,
                ownerPubKey,
                spendingInfo,
                tapScripts,
                address,
                exitDelay,
                exitDelay * 512,
                network,
            )
        }
    }
}

data class ScriptSpendingPath(
    val script: ByteArray,
    val control: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScriptSpendingPath

        if (!script.contentEquals(other.script)) return false
        if (!control.contentEquals(other.control)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = script.contentHashCode()
        result = 31 * result + control.contentHashCode()
        return result
    }
}
