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

/**
 * The `BoardingInput` class represents a `UTXO` that can be used to create a `VTXO`
 * during the boarding process.
 * [serverPubKey] is the Arkade operator's x-only public key
 * [ownerPubKey] is the Arkade user's x-only public key. This is the owner of the `UTXO`
 * [spendingInfo] is the information required to spend this `UTXO`
 * [address] is the address locking this `UTXO`
 * [exitDelay] (intervals) is the amount of time the owner waits for unilateral exit after transaction on chain confirmation
 */
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

    /**
     * @return the scriptpukey locking this `UTXO`
     */
    fun getScriptPubKey() = address.toScriptPubKey()

    /**
     * @param network is the [Network] where this `UTXO` exists and is valid
     * @param serverPubKey is the Arkade operator's x-only public key
     * @return an [ArkAddress] that locks a `VTXO` created from this `UTXO`
     */
    fun getArkAddress(
        network: Network,
        serverPubKey: XonlyPublicKey,
    ) = ArkAddress.create(
        network,
        serverPubKey.value.toByteArray(),
        spendingInfo.outputKey.value.toByteArray(),
    )

    /**
     * @param script is the script to use for spending this `UTXO`
     * @return the control block for the [script], required to spend using the [script]
     * @throws IllegalArgumentException if the provided script is not found in the [ScriptTree]
     */
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

    /**
     * @return the script and control block for spending this `UTXO` collaboratively encapsulated in [ScriptSpendingPath]
     */
    fun getForfeitSpendingInfo(): ScriptSpendingPath {
        val script = tapScripts[0]
        val controlBlock = getControlBlock(script)
        return ScriptSpendingPath(script, controlBlock)
    }

    /**
     * @return the script and control block for spending this `UTXO` unilaterally encapsulated in [ScriptSpendingPath]
     */
    fun getExitSpendingInfo(): ScriptSpendingPath {
        val script = tapScripts[1]
        val controlBlock = getControlBlock(script)
        return ScriptSpendingPath(script, controlBlock)
    }

    /**
     * @return the list of tap scripts that can be used to spend this `UTXO`
     */
    fun getTapScripts(): List<ByteArray> = tapScripts

    /**
     * @param now is the current system time
     * @param blockConfirmTime is the time at which this `UTXO` was confirmed on-chain
     * @return whether this `UTXO`'s unilateral [exitDelay] has expired or not
     */
    fun canBeClaimedUnilaterally(
        now: Duration,
        blockConfirmTime: Duration,
    ): Boolean {
        val exitDelaySeconds = multiplyExact(exitDelay, 512)
        val exitTime = blockConfirmTime + exitDelaySeconds.toDuration(DurationUnit.SECONDS)
        return now >= exitTime
    }

    companion object {
        /**
         * Creates a `BoardingInput`
         * @param severPubKey is the Arkade operator's x-only public key
         * @param ownerPubKey is the Arkade user's x-only public key. This is the owner of the `UTXO`
         * @param exitDelay (intervals) is the amount of time the owner waits for unilateral exit
         * after transaction on chain confirmation
         *
         * `intervals = (seconds / 512)`
         *
         * @param network is the [Network] where this `UTXO` exists and is valid
         * @return a `BoardingInput` that can be used to create a `VTXO`
         */
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
