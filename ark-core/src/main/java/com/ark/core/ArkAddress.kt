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
 * @throws IllegalStateException if the [serverPubKey] or [vtxoTaprootPubKey] provided are not exactly 32 bytes
 */
class ArkAddress(
    val hrp: String,
    val version: Int,
    val serverPubKey: ByteArray,
    val vtxoTaprootPubKey: ByteArray,
) {
    init {
        val pubKeySize = serverPubKey.size
        val taprootKeySize = vtxoTaprootPubKey.size
        if (pubKeySize != 32) {
            throw IllegalStateException("Invalid server public key length, expected 32 bytes, got $pubKeySize")
        }
        if (taprootKeySize != 32) {
            throw IllegalStateException("Invalid vtxo taproot public key length, expected 32 bytes, got $taprootKeySize")
        }
    }

    /**
     * Generate an Ark address as a Bech32m `String`
     * */
    fun encode(): String {
        var bytes = ByteArray(1)
        bytes[0] = this.version.toByte()
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
         * Creates a new `ArkAddress` from a Bech32m `String`
         * @param address
         */
        fun decode(address: String): ArkAddress {
            val (hrp, bytes, enc) = Bech32.decodeBytes(address)
            val version = bytes[0].toInt()
            val serverPubKey = bytes.copyOfRange(1, 33)
            val vtxoTaprootKey = bytes.copyOfRange(33, 65)
            return ArkAddress(
                hrp,
                version,
                serverPubKey,
                vtxoTaprootKey,
            )
        }
    }
}
