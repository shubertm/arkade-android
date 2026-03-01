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

/**
 * The `Vtxo` class represents a `VTXO (Virtual Transaction Output)`, an Ark abstraction of the
 * Bitcoin `UTXO (Unspent Transaction Output)`
 *
 * [serverPubKey] is the Arkade operator's x-only public key
 *
 * [ownerPubKey] is the Arkade user's x-only public key. This is the owner of the `VTXO`
 *
 * [spendingInfo] is the information required to spend this `VTXO`
 *
 * [tapScripts] is the list of scripts that can be used to spend this `VTXO`
 *
 * [address] is the address locking this `VTXO`
 *
 * [exitDelay] (intervals) is the amount of time the owner waits for unilateral exit after transaction on chain confirmation
 *
 * `intervals = (seconds / 512)`
 *
 * [exitDelaySeconds] is the [exitDelay] in seconds
 *
 * [network] is the [Network] where this `VTXO` exists and is valid
 */
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
    /**
     * @return the scriptpukey locking this `VTXO`
     */
    fun getScriptPubKey(): ByteArray = address.toScriptPubKey()

    /**
     * @return an [ArkAddress] that locks this `VTXO`
     */
    fun getArkAddress(): ArkAddress =
        ArkAddress.create(
            network,
            serverPubKey.value.toByteArray(),
            spendingInfo.outputKey.value.toByteArray(),
        )

    /**
     * @param script is the script to use for spending this `VTXO`
     * @return the control block for the [script], required to spend using the [script]
     * @throws IllegalArgumentException if the provided script is not found in the [ScriptTree]
     */
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

    /**
     * @return the script and control block for spending this `UTXO` collaboratively encapsulated in [ScriptSpendingPath]
     */
    fun getForfeitSpendingPath(): ScriptSpendingPath {
        val forfeitScript = tapScripts[0]
        val controlBlock = getControlBlock(forfeitScript)
        return ScriptSpendingPath(forfeitScript, controlBlock)
    }

    /**
     * @return the script and control block for spending this `UTXO` unilaterally encapsulated in [ScriptSpendingPath]
     */
    fun getExitSpendingPath(): ScriptSpendingPath {
        val exitScript = tapScripts[1]
        val controlBlock = getControlBlock(exitScript)
        return ScriptSpendingPath(exitScript, controlBlock)
    }

    /**
     * @param now is the current system time
     * @param blockConfirmTime is the time at which this `VTXO` was confirmed on-chain
     * @return whether this `VTXO`'s unilateral [exitDelay] has expired or not
     */
    fun canBeClaimedUnilaterally(
        now: Duration,
        blockConfirmTime: Duration,
    ): Boolean {
        val exitTime = blockConfirmTime + exitDelaySeconds.toDuration(DurationUnit.SECONDS)
        return now > exitTime
    }

    companion object {
        /**
         * Creates a `VTXO`
         * @param serverPubKey is the Arkade operator's x-only public key
         * @param ownerPubKey is the Arkade user's x-only public key. This is the owner of the `VTXO`
         * @param exitDelay (intervals) is the amount of time the owner waits for unilateral exit
         * after transaction on chain confirmation
         *
         * `intervals = (seconds / 512)`
         *
         * @param network is the [Network] where this `VTXO` exists and is valid
         */
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


        /**
         * Creates a `VTXO` from custom scripts
         * @param serverPubKey is the Arkade operator's x-only public key
         * @param ownerPubKey is the Arkade user's x-only public key. This is the owner of the `VTXO`
         * @param tapScripts is the list of scripts that can be used to spend this `VTXO`
         * @param exitDelay (intervals) is the amount of time the owner waits for unilateral exit
         * after transaction on chain confirmation
         *
         * `intervals = (seconds / 512)`
         *
         * @param network is the [Network] where this `VTXO` exists and is valid
         */
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

/**
 * This is an encapsulation of a script to use for spending a `VTXO` and it's control block
 */
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
