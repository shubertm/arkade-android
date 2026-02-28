package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Network
import com.ark.core.taproot.Parity
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Crypto
import fr.acinq.bitcoin.PublicKey
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey
import junit.framework.TestCase.assertEquals
import org.junit.Test

class VtxoTest {
    @Test
    fun mainnet_should_succeed_on_building_vtxo() {
        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val vtxo =
            Vtxo.build(
                serverPubKey,
                ownerPubKey,
                144L,
                Network.MAINNET,
            )
        val forfeitScript = multisigScript(serverPubKey, ownerPubKey)
        val exitScript = csvSigScript(144, ownerPubKey)
        assertEquals(68, forfeitScript.size)
        assertEquals(39, exitScript.size)

        val forfeitLeaf = ScriptTree.Leaf(ByteVector(forfeitScript), 0)
        val exitLeaf = ScriptTree.Leaf(ByteVector(exitScript), 0)
        val scriptTree = ScriptTree.Branch(forfeitLeaf, exitLeaf)
        val merkleRoot = scriptTree.hash()

        val unSpendablePubKey = XonlyPublicKey(ByteVector32.fromValidHex(UNSPENDABLE_PUBKEY))
        val scriptPubKey =
            with(Script) {
                write(pay2tr(unSpendablePubKey, Crypto.TaprootTweak.ScriptPathTweak(merkleRoot)))
            }
        val address = Address.fromScriptPubKey(scriptPubKey, Network.MAINNET)

        assertEquals(serverPubKey, vtxo.serverPubKey)
        assertEquals(ownerPubKey, vtxo.ownerPubKey)
        assertEquals(vtxo.network, Network.MAINNET)
        assertEquals(144, vtxo.exitDelay)
        assertEquals(144L * 512, vtxo.exitDelaySeconds)
        assertEquals(2, vtxo.tapScripts.size)

        val (forfeit, exit) = vtxo.tapScripts
        assertEquals(forfeitScript.toHexString(), forfeit.toHexString())
        assertEquals(exitScript.toHexString(), exit.toHexString())

        assertEquals(address.hrp.prefix, vtxo.address.hrp.prefix)
        assertEquals(address.witnessVersion, vtxo.address.witnessVersion)
        assertEquals(address.witnessProgram.toHexString(), vtxo.address.witnessProgram.toHexString())
        assertEquals(address.encode(), vtxo.address.encode())

        val (outputKey, isOdd) = unSpendablePubKey.outputKey(merkleRoot)
        val parity = Parity.fromBooleanIsOdd(isOdd)
        assertEquals(unSpendablePubKey, vtxo.spendingInfo.internalKey)
        assertEquals(outputKey, vtxo.spendingInfo.outputKey)
        assertEquals(parity, vtxo.spendingInfo.outputKeyParity)
        assertEquals(merkleRoot, vtxo.spendingInfo.merkleRoot)
        assertEquals(scriptTree, vtxo.spendingInfo.merkleScriptTree)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mainnet_should_fail_on_invalid_number_of_tap_scripts() {
        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val forfeitScript = multisigScript(serverPubKey, ownerPubKey)
        assertEquals(68, forfeitScript.size)

        Vtxo.fromScripts(
            serverPubKey,
            ownerPubKey,
            listOf(forfeitScript),
            144L,
            Network.MAINNET,
        )
    }
}
