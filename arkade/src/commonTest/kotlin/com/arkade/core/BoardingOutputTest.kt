package com.arkade.core

import com.arkade.core.bitcoin.Address
import com.arkade.core.bitcoin.Network
import com.arkade.core.taproot.Parity
import fr.acinq.bitcoin.ByteVector
import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.Crypto
import fr.acinq.bitcoin.PublicKey
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class BoardingOutputTest {
    @Test
    fun mainnet_should_succeed_on_creating_boarding_output() {
        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val boardingOutput =
            BoardingOutput.create(
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

        assertEquals(serverPubKey, boardingOutput.serverPubKey)
        assertEquals(ownerPubKey, boardingOutput.ownerPubKey)
        assertEquals(144, boardingOutput.exitDelay)
        assertEquals(address, boardingOutput.address)

        assertEquals(scriptPubKey.toHexString(), boardingOutput.getScriptPubKey().toHexString())

        val (forfeit, exit) = boardingOutput.getTapScripts()
        assertEquals(forfeitScript.toHexString(), forfeit.toHexString())
        assertEquals(exitScript.toHexString(), exit.toHexString())

        assertEquals(address.hrp.prefix, boardingOutput.address.hrp.prefix)
        assertEquals(address.witnessVersion, boardingOutput.address.witnessVersion)
        assertEquals(address.witnessProgram.toHexString(), boardingOutput.address.witnessProgram.toHexString())
        assertEquals(address.encode(), boardingOutput.address.encode())

        val (outputKey, isOdd) = unSpendablePubKey.outputKey(merkleRoot)
        val parity = Parity.fromBooleanIsOdd(isOdd)
        assertEquals(unSpendablePubKey, boardingOutput.spendingInfo.internalKey)
        assertEquals(outputKey, boardingOutput.spendingInfo.outputKey)
        assertEquals(parity, boardingOutput.spendingInfo.outputKeyParity)
        assertEquals(merkleRoot, boardingOutput.spendingInfo.merkleRoot)
        assertEquals(scriptTree, boardingOutput.spendingInfo.merkleScriptTree)

        val arkAddress = boardingOutput.getArkAddress(Network.MAINNET, serverPubKey)
        assertEquals(ArkHrp.MAINNET, arkAddress.hrp)
        assertEquals(serverPubKey.value.toHex(), arkAddress.serverPubKey.toHexString())
        assertEquals(outputKey.value.toHex(), arkAddress.vtxoTaprootPubKey.toHexString())
        assertEquals(scriptPubKey.toHexString(), arkAddress.toP2TRScriptPubkey().toHexString())
    }

    @Test
    fun testnet_should_succeed_on_creating_boarding_output() {
        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val boardingOutput =
            BoardingOutput.create(
                serverPubKey,
                ownerPubKey,
                144L,
                Network.TESTNET,
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
        val address = Address.fromScriptPubKey(scriptPubKey, Network.TESTNET)

        assertEquals(serverPubKey, boardingOutput.serverPubKey)
        assertEquals(ownerPubKey, boardingOutput.ownerPubKey)
        assertEquals(144, boardingOutput.exitDelay)
        assertEquals(address, boardingOutput.address)

        assertEquals(scriptPubKey.toHexString(), boardingOutput.getScriptPubKey().toHexString())

        val (forfeit, exit) = boardingOutput.getTapScripts()
        assertEquals(forfeitScript.toHexString(), forfeit.toHexString())
        assertEquals(exitScript.toHexString(), exit.toHexString())

        assertEquals(address.hrp.prefix, boardingOutput.address.hrp.prefix)
        assertEquals(address.witnessVersion, boardingOutput.address.witnessVersion)
        assertEquals(address.witnessProgram.toHexString(), boardingOutput.address.witnessProgram.toHexString())
        assertEquals(address.encode(), boardingOutput.address.encode())

        val (outputKey, isOdd) = unSpendablePubKey.outputKey(merkleRoot)
        val parity = Parity.fromBooleanIsOdd(isOdd)
        assertEquals(unSpendablePubKey, boardingOutput.spendingInfo.internalKey)
        assertEquals(outputKey, boardingOutput.spendingInfo.outputKey)
        assertEquals(parity, boardingOutput.spendingInfo.outputKeyParity)
        assertEquals(merkleRoot, boardingOutput.spendingInfo.merkleRoot)
        assertEquals(scriptTree, boardingOutput.spendingInfo.merkleScriptTree)

        val arkAddress = boardingOutput.getArkAddress(Network.TESTNET, serverPubKey)
        assertEquals(ArkHrp.TESTNET, arkAddress.hrp)
        assertEquals(serverPubKey.value.toHex(), arkAddress.serverPubKey.toHexString())
        assertEquals(outputKey.value.toHex(), arkAddress.vtxoTaprootPubKey.toHexString())
        assertEquals(scriptPubKey.toHexString(), arkAddress.toP2TRScriptPubkey().toHexString())
    }

    @Test
    fun regtest_should_succeed_on_creating_boarding_output() {
        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val boardingOutput =
            BoardingOutput.create(
                serverPubKey,
                ownerPubKey,
                144L,
                Network.REGTEST,
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
        val address = Address.fromScriptPubKey(scriptPubKey, Network.REGTEST)

        assertEquals(serverPubKey, boardingOutput.serverPubKey)
        assertEquals(ownerPubKey, boardingOutput.ownerPubKey)
        assertEquals(144, boardingOutput.exitDelay)
        assertEquals(address, boardingOutput.address)

        assertEquals(scriptPubKey.toHexString(), boardingOutput.getScriptPubKey().toHexString())

        val (forfeit, exit) = boardingOutput.getTapScripts()
        assertEquals(forfeitScript.toHexString(), forfeit.toHexString())
        assertEquals(exitScript.toHexString(), exit.toHexString())

        assertEquals(address.hrp.prefix, boardingOutput.address.hrp.prefix)
        assertEquals(address.witnessVersion, boardingOutput.address.witnessVersion)
        assertEquals(address.witnessProgram.toHexString(), boardingOutput.address.witnessProgram.toHexString())
        assertEquals(address.encode(), boardingOutput.address.encode())

        val (outputKey, isOdd) = unSpendablePubKey.outputKey(merkleRoot)
        val parity = Parity.fromBooleanIsOdd(isOdd)
        assertEquals(unSpendablePubKey, boardingOutput.spendingInfo.internalKey)
        assertEquals(outputKey, boardingOutput.spendingInfo.outputKey)
        assertEquals(parity, boardingOutput.spendingInfo.outputKeyParity)
        assertEquals(merkleRoot, boardingOutput.spendingInfo.merkleRoot)
        assertEquals(scriptTree, boardingOutput.spendingInfo.merkleScriptTree)

        val arkAddress = boardingOutput.getArkAddress(Network.REGTEST, serverPubKey)
        assertEquals(ArkHrp.TESTNET, arkAddress.hrp)
        assertEquals(serverPubKey.value.toHex(), arkAddress.serverPubKey.toHexString())
        assertEquals(outputKey.value.toHex(), arkAddress.vtxoTaprootPubKey.toHexString())
        assertEquals(scriptPubKey.toHexString(), arkAddress.toP2TRScriptPubkey().toHexString())
    }
}
