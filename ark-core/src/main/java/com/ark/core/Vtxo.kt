package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Network
import com.ark.core.taproot.Parity
import com.ark.core.taproot.TaprootSpendingInfo
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Crypto
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey

data class Vtxo(
    val serverPubKey: XonlyPublicKey,
    val ownerPubKey: XonlyPublicKey,
    val spendInfo: TaprootSpendingInfo,
    val tapScripts: List<ByteArray>,
    val address: Address,
    val exitDelay: Long,
    val exitDelaySeconds: Long,
    val network: Network,
) {
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

            val (outputKey, isOdd) = unSpendablePubKey.outputKey(Crypto.TaprootTweak.ScriptPathTweak(merkleRoot))

            val spendInfo =
                TaprootSpendingInfo(
                    unSpendablePubKey,
                    outputKey,
                    Parity.fromBooleanIsOdd(isOdd),
                    merkleRoot,
                    scriptTree,
                )

            val scriptPubKey =
                with(Script) {
                    write(pay2tr(unSpendablePubKey, Crypto.TaprootTweak.KeyPathTweak))
                }
            val address = Address.fromScriptPubKey(scriptPubKey, network)

            return Vtxo(
                serverPubKey,
                ownerPubKey,
                spendInfo,
                tapScripts,
                address,
                exitDelay,
                exitDelay * 512,
                network,
            )
        }
    }
}
