package com.arkade.core

import com.arkade.core.bitcoin.Address
import com.arkade.core.bitcoin.Hrp
import com.arkade.core.bitcoin.Network
import com.arkade.core.bitcoin.WitnessVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OnChainTaprootAddressTest {
    val mainnetAddress = "bc1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjst0yf78"
    val testnetAddress = "tb1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjsu8jxyg"
    val regtestAddress = "bcrt1pjhe7sjma3rnkdfelsrj5l6g58z9ysclkrjxgy0duxcp9k6r9atjs37cq3j"
    val witnessProgram = "95f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5"
    val scriptPubKey = "512095f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5".hexToByteArray()
    val malformedScriptPubKey = "511495f3e84b7d88e766a73f80e54fe914388a4863f61c8c823dbc36025b6865eae5".hexToByteArray()

    @Test
    fun mainnet_round_trip() {
        val network = Network.MAINNET

        val decoded = Address.decode(mainnetAddress)

        assertEquals("bc", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bc", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(mainnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun mainnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(mainnetAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.MAINNET,
                WitnessVersion.TAPROOT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun mainnet_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.MAINNET)
        }
    }

    @Test
    fun testnet_round_trip() {
        val network = Network.TESTNET

        val decoded = Address.decode(testnetAddress)

        assertEquals("tb", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("tb", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(testnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun testnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(testnetAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.TESTNETS,
                WitnessVersion.TAPROOT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun testnet_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.TESTNET)
        }
    }

    @Test
    fun regtest_round_trip() {
        val network = Network.REGTEST

        val decoded = Address.decode(regtestAddress)

        assertEquals("bcrt", decoded.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bcrt", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.TAPROOT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(regtestAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun regtest_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(regtestAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.REGTEST,
                WitnessVersion.TAPROOT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun regtest_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.TESTNET)
        }
    }

    @Test
    fun fail_on_unsupported_witness_version() {
        assertFailsWith<IllegalArgumentException> {
            WitnessVersion.fromInt(3)
        }
    }
}

class OnChainSegwitAddressTest {
    val mainnetAddress = "bc1q6jktr35gzw37e7gh96fkm5w8qp256f8qh5uxdg"
    val testnetAddress = "tb1q6jktr35gzw37e7gh96fkm5w8qp256f8qaj84km"
    val regtestAddress = "bcrt1q6jktr35gzw37e7gh96fkm5w8qp256f8qlm7cpj"
    val witnessProgram = "d4acb1c68813a3ecf9172e936dd1c700554d24e0"
    val scriptPubKey = "0014d4acb1c68813a3ecf9172e936dd1c700554d24e0".hexToByteArray()
    val malformedScriptPubKey = "0020d4acb1c68813a3ecf9172e936dd1c700554d24e0".hexToByteArray()

    @Test
    fun mainnet_round_trip() {
        val network = Network.MAINNET

        val decoded = Address.decode(mainnetAddress)

        assertEquals("bc", decoded.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bc", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(mainnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun mainnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(mainnetAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.MAINNET,
                WitnessVersion.SEGWIT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun mainnet_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.MAINNET)
        }
    }

    @Test
    fun testnet_round_trip() {
        val network = Network.TESTNET

        val decoded = Address.decode(testnetAddress)

        assertEquals("tb", decoded.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("tb", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(testnetAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun testnet_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(testnetAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.TESTNETS,
                WitnessVersion.SEGWIT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun testnet_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.TESTNET)
        }
    }

    @Test
    fun regtest_round_trip() {
        val network = Network.REGTEST

        val decoded = Address.decode(regtestAddress)

        assertEquals("bcrt", decoded.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, decoded.witnessVersion)
        assertEquals(witnessProgram, decoded.witnessProgram.toHexString())

        val fromScriptPubKey = Address.fromScriptPubKey(scriptPubKey, network)
        assertEquals("bcrt", fromScriptPubKey.hrp.prefix)
        assertEquals(WitnessVersion.SEGWIT, fromScriptPubKey.witnessVersion)
        assertEquals(witnessProgram, fromScriptPubKey.witnessProgram.toHexString())

        val encoded = decoded.encode()
        assertEquals(regtestAddress, encoded)

        assertEquals(scriptPubKey.toHexString(), decoded.toScriptPubKey().toHexString())
    }

    @Test
    fun regtest_fail_creating_address_on_invalid_witness_program_length() {
        val decoded = Address.decode(regtestAddress)
        assertFailsWith<IllegalArgumentException> {
            Address(
                Hrp.REGTEST,
                WitnessVersion.SEGWIT,
                decoded.witnessProgram.copyOfRange(3, 16),
            )
        }
    }

    @Test
    fun regtest_fail_creating_address_on_malformed_script_pubkey() {
        assertFailsWith<IllegalArgumentException> {
            Address.fromScriptPubKey(malformedScriptPubKey, Network.REGTEST)
        }
    }

    @Test
    fun fail_on_unsupported_witness_version() {
        assertFailsWith<IllegalArgumentException> {
            WitnessVersion.fromInt(3)
        }
    }
}
