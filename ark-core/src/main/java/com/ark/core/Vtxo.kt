package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Network
import com.ark.core.taproot.Parity
import com.ark.core.taproot.TaprootSpendingInfo
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Crypto
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey

class Vtxo(
    serverPubKey: XonlyPublicKey,
    ownerPubKey: XonlyPublicKey,
    spendInfo: TaprootSpendingInfo,
    tapScripts: List<ByteArray>,
    address: Address,
    exitDelay: Long,
    exitDelaySeconds: Long,
    network: Network,
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
            val unSpendablePubKey = XonlyPublicKey(ByteVector32.fromValidHex(UNSPENDABLE_PUBKEY))

            val forfeitLeaf = ScriptTree.Leaf(ByteVector(tapScripts[0]), 0)
            val exitLeaf = ScriptTree.Leaf(ByteVector(tapScripts[1]), 0)
            val scriptTree = ScriptTree.Branch(forfeitLeaf, exitLeaf)

            val (outputKey, isOdd) = unSpendablePubKey.outputKey(Crypto.TaprootTweak.KeyPathTweak)

            val spendInfo =
                TaprootSpendingInfo(
                    unSpendablePubKey,
                    scriptTree.hash(),
                    outputKey,
                    Parity.fromBooleanIsOdd(isOdd),
                    scriptTree,
                )

            val address = Address()
            val network = Network()

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
