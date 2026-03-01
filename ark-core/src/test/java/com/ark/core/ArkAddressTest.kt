package com.ark.core

import fr.acinq.bitcoin.Bech32
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Test decoding and encoding Ark addresses, generating Taproot ScriptPubKey and SubDust ScriptPubKey
 */
class ArkAddressTest {
    @Test
    fun round_trip() {
        val address =
            "tark1qqellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt846qj6x"

        val decoded = ArkAddress.decode(address)
        val hrp = decoded.hrp
        assertEquals("tark", hrp.prefix)

        val version = decoded.version
        assertEquals(0, version)

        val serverPubKey = decoded.serverPubKey.toHexString()
        assertEquals(
            "33ffb3dee353b1a9ebe4ced64b946238d0a4ac364f275d771da6ad2445d07ae0",
            serverPubKey,
        )

        val vtxoTaprootKey = decoded.vtxoTaprootPubKey.toHexString()
        assertEquals(
            "25a43cecfa0e1b1a4f72d64ad15f4cfa7a84d0723e8511c969aa543638ea9967",
            vtxoTaprootKey,
        )

        val encoded = decoded.encode()

        assertEquals(address, encoded)

        assertEquals(
            "512025a43cecfa0e1b1a4f72d64ad15f4cfa7a84d0723e8511c969aa543638ea9967",
            decoded.toP2TRScriptPubkey().toHexString(),
        )
        assertEquals(
            "6a2025a43cecfa0e1b1a4f72d64ad15f4cfa7a84d0723e8511c969aa543638ea9967",
            decoded.toSubDustScriptPubkey().toHexString(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun fail_decoding_on_unsupported_address_version() {
        val address =
            "tark1qyellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt82zz7h8"
        ArkAddress.decode(address)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fail_decoding_on_invalid_bech32_encoding() {
        val address =
            "tark1qqellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt8qxs7ly"
        ArkAddress.decode(address)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fail_decoding_on_invalid_payload_length() {
        val address =
            "tark1qqellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt8ykjrem86pcd35nmj6e9dzh6vlfagf5rj8" +
                "6z3rjtf4f2rvw82n9ns8c73mu"
        ArkAddress.decode(address)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fail_decoding_on_invalid_server_pubkey_size() {
        val address =
            "tark1qqellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt8ykjrem86pcd35nmj6e9dzh6vlfagf5rj8" +
                "6z3rjtf4f2rvw82n9ns8c73mu"
        val (hrp, bytes, encoding) = Bech32.decodeBytes(address)
        ArkAddress(
            ArkHrp.fromString(hrp),
            bytes[0].toInt(),
            bytes.copyOfRange(1, 49),
            bytes.copyOfRange(49, 97),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun fail_decoding_on_invalid_vtxo_taproot_pubkey_size() {
        val address =
            "tark1qqellv77udfmr20tun8dvju5vgudpf9vxe8jwhthrkn26fz96pawqfdy8nk05rs" +
                "mrf8h94j26905e7n6sng8y059z8ykn2j5xcuw4xt8ykjrem86pcd35nmj6e9dzh6vlfagf5rj8" +
                "6z3rjtf4f2rvw82n9ns8c73mu"
        val (hrp, bytes, encoding) = Bech32.decodeBytes(address)
        ArkAddress(
            ArkHrp.fromString(hrp),
            bytes[0].toInt(),
            bytes.copyOfRange(1, 33),
            bytes.copyOfRange(33, 97),
        )
    }
}
