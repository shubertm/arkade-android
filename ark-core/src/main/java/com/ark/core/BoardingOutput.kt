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

data class BoardingOutput(
    val severPubKey: XonlyPublicKey,
    val ownerPubKey: XonlyPublicKey,
    val spendingInfo: TaprootSpendingInfo,
    val address: Address,
    val exitDelay: Long,
) {
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
