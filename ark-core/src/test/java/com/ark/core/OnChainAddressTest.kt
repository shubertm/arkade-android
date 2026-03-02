package com.ark.core

import com.ark.core.bitcoin.Address
import com.ark.core.bitcoin.Hrp
import com.ark.core.bitcoin.Network
import com.ark.core.bitcoin.WitnessVersion
import junit.framework.TestCase.assertEquals
import org.junit.Test

class OnChainAddressTest {
    val mainnetAddress = "bc1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjst0yf78"
    val testnetAddress = "tb1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjsu8jxyg"
    val regtestAddress = "bcrt1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjs37cq3j"

    @Test
    fun mainnet_round_trip() {
        val network = Network.MAINNET
        val scriptPubKey = "512095f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5".hexToByteArray()

        val decoded = Address.decode(mainnetAddress)

        assertEquals("bc", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bc", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(mainnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun mainnet_fail_creating_address_on_unsupported_witness_version() {
        val decoded = Address.decode(mainnetAddress)
        Address(
            Hrp.MAINNET,
            WitnessVersion.fromInt(3),
            decoded.witnessProgram,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun mainnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(mainnetAddress)
        Address(
            Hrp.MAINNET,
            WitnessVersion.TAPROOT,
            decoded.witnessProgram.copyOfRange(3, 16),
        )
    }

    @Test
    fun testnet_round_trip() {
        val network = Network.TESTNET
        val scriptPubKey = "512095f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5".hexToByteArray()

        val decoded = Address.decode(testnetAddress)

        assertEquals("tb", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("tb", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(testnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testnet_fail_creating_address_on_unsupported_witness_version() {
        val decoded = Address.decode(testnetAddress)
        Address(
            Hrp.TESTNETS,
            WitnessVersion.fromInt(3),
            decoded.witnessProgram,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(testnetAddress)
        Address(
            Hrp.TESTNETS,
            WitnessVersion.TAPROOT,
            decoded.witnessProgram.copyOfRange(3, 16),
        )
    }

    @Test
    fun regtest_round_trip() {
        val network = Network.REGTEST
        val scriptPubKey = "512095f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5".hexToByteArray()

        val decoded = Address.decode(regtestAddress)

        assertEquals("bcrt", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bcrt", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals("95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5", fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(regtestAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun regtest_fail_creating_address_on_unsupported_witness_version() {
        val decoded = Address.decode(regtestAddress)
        Address(
            Hrp.REGTEST,
            WitnessVersion.fromInt(3),
            decoded.witnessProgram,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun regtest_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(regtestAddress)
        Address(
            Hrp.REGTEST,
            WitnessVersion.TAPROOT,
            decoded.witnessProgram.copyOfRange(3, 16),
        )
    }
}
