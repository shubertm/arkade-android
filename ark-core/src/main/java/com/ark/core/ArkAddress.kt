package com.ark.core

import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.OP_1
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.OP_RETURN
import fr.acinq.bitcoin.Script

/**
 * The `ArkAddress` class represents a Bech32m Arkade address.
 *
 * [hrp] (Human Readable Parts) is the prefix for the address representing a network identifier
 *  - `ark` (mainnet)
 *  - `tark` (testnet)
 *
 * [version] is the version number for the address (1 byte)
 *
 * **Note:** currently supported version is `0`
 *
 * [serverPubKey] is the Arkade Service Provider's X-only public key (32 bytes)
 *
 * [vtxoTaprootPubKey] is the Taproot output key containing collaborative and exit script paths (32 bytes)
 *
 * @throws IllegalArgumentException
 * * if the address [version] provided is not supported
 * * if [serverPubKey] or [vtxoTaprootPubKey] provided are not exactly 32 bytes
 */
class ArkAddress(
    val hrp: String,
    val version: Int,
    val serverPubKey: ByteArray,
    val vtxoTaprootPubKey: ByteArray,
) {
    init {
        require(version == 0) { "Unsupported address version: $version" }
        val pubKeySize = serverPubKey.size
        val taprootKeySize = vtxoTaprootPubKey.size
        require(pubKeySize == 32) { "Invalid server public key length, expected 32 bytes, got $pubKeySize" }
        require(taprootKeySize == 32) {
            "Invalid vtxo taproot public key length, expected 32 bytes, got $taprootKeySize"
        }
    }

    /**
     * Generate an Ark address as a Bech32m `String`
     * */
    fun encode(): String {
        var bytes = ByteArray(1)
        bytes[0] = version.toByte()
        bytes = bytes + serverPubKey + vtxoTaprootPubKey
        val address = Bech32.encodeBytes(hrp, bytes, Bech32.Encoding.Bech32m)
        return address
    }

    /**
     * Creates a P2TR witness program from [vtxoTaprootPubKey]
     */
    fun toP2TRScriptPubkey(): ByteArray {
        val scriptPubkey =
            Script.write(
                listOf(OP_1, OP_PUSHDATA(vtxoTaprootPubKey)),
            )
        return scriptPubkey
    }

    /**
     * Creates an `OP_RETURN` from [vtxoTaprootPubKey]
     */
    fun toSubDustScriptPubkey(): ByteArray {
        val scriptPubkey =
            Script.write(
                listOf(OP_RETURN, OP_PUSHDATA(vtxoTaprootPubKey)),
            )
        return scriptPubkey
    }

    companion object {
        /**
         * Creates a new [ArkAddress] from a Bech32m `String`
         * @param address
         * @throws IllegalArgumentException
         * * if the address encoding is not Bech32m
         * * if the address payload is not exactly 65 bytes.
         * * if the address version is not supported
         */
        fun decode(address: String): ArkAddress {
            val (hrp, bytes, encoding) = Bech32.decodeBytes(address)
            require(encoding == Bech32.Encoding.Bech32m) { "Invalid Bech32 encoding: $encoding" }
            val bytesSize = bytes.size
            require(bytesSize == 65) { "Invalid payload length: $bytesSize" }
            val version = bytes[0].toInt()
            require(version == 0) { "Unsupported address version: $version" }

            val serverPubKey = bytes.copyOfRange(1, 33)
            val vtxoTaprootPubKey = bytes.copyOfRange(33, 65)
            return ArkAddress(
                hrp,
                version,
                serverPubKey,
                vtxoTaprootPubKey,
            )
        }
    }
}
