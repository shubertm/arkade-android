package com.ark.core.bitcoin

import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.OP_1
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script

/**
 * The `Address` represents a [Bech32.Encoding.Bech32m] of an on-chain Bitcoin address
 *
 * [hrp] (Human Readable Parts) is the prefix for the address representing a network identifier
 * * `bc` (mainnet)
 * * `tb` (testnet, signet)
 * * `bcrt` (regtest)
 *
 * [witnessVersion] is the version of the witness program locking a UTXO to this address
 *
 * [witnessProgram] is the witness program for locking a UTXO to this address
 *
 * @throws IllegalArgumentException
 * * if the provided [witnessVersion] is not supported
 * * if the length of the provided witness program is not exactly 32 bytes
 */
class Address(
    val hrp: Hrp,
    val witnessVersion: WitnessVersion = WitnessVersion.TAPROOT,
    val witnessProgram: ByteArray,
) {
    init {
        require(witnessVersion == WitnessVersion.TAPROOT || witnessVersion == WitnessVersion.SEGWIT) {
            "Unsupported witness version"
        }
        val witnessProgramSize = witnessProgram.size
        require(witnessProgramSize == 32) { "Invalid witness program length, expected 32 bytes, but got $witnessProgramSize" }
    }

    /**
     * Generates a Bitcoin on-chain address as a [Bech32.Encoding.Bech32m] `String`
     */
    fun encode(): String = Bech32.encodeWitnessAddress(hrp.prefix, witnessVersion.toByte(), witnessProgram)

    /**
     * Creates a P2TR scriptpubkey from the [witnessProgram]
     */
    fun toScriptPubKey(): ByteArray {
        val asm =
            listOf(
                OP_1,
                OP_PUSHDATA(witnessProgram),
            )
        val scriptPubKey = Script.write(asm)
        return scriptPubKey
    }

    companion object {
        /**
         * Creates a new [Address] from a [Bech32.Encoding.Bech32m] `String`
         * @param address is a [Bech32.Encoding.Bech32m] `String`
         * @throws IllegalArgumentException
         * * if the witness program length not exactly 32 bytes
         * * if the witness version is no supported
         * * if the [Hrp] prefix is invalid
         */
        fun decode(address: String): Address {
            val (prefix, version, witnessProgram) = Bech32.decodeWitnessAddress(address)
            val bytesSize = witnessProgram.size
            require(bytesSize == 32) { "Invalid witness program length: $bytesSize" }
            require(
                version == WitnessVersion.TAPROOT.toByte() || version == WitnessVersion.SEGWIT.toByte(),
            ) { "Unsupported address version: $version" }

            val hrp = Hrp.fromString(prefix)
            return Address(
                hrp,
                WitnessVersion.fromByte(version),
                witnessProgram,
            )
        }

        /**
         * Creates a Bitcoin on-chain [Address] from a scriptpubkey
         * @param scriptPubKey is the script that can lock a UTXO to the [Address] it creates
         * @param network is the Bitcoin [Network] where the scriptpubkey and the [Address] are valid
         */
        fun fromScriptPubKey(
            scriptPubKey: ByteArray,
            network: Network,
        ): Address {
            val witnessVersionByte = scriptPubKey[0]
            val witnessProgram = scriptPubKey.copyOfRange(2, 34)
            val hrp = Hrp.fromNetwork(network)
            return Address(
                hrp,
                WitnessVersion.fromByte(witnessVersionByte),
                witnessProgram,
            )
        }
    }
}

enum class WitnessVersion {
    SEGWIT,
    TAPROOT,
    ;

    fun toByte(): Byte = ordinal.toByte()

    companion object {
        fun fromInt(version: Int): WitnessVersion {
            if (version == SEGWIT.ordinal) {
                return SEGWIT
            }
            if (version == TAPROOT.ordinal) {
                return TAPROOT
            }
            throw IllegalArgumentException("Unsupported witness version")
        }

        fun fromByte(version: Byte): WitnessVersion {
            var ver = version.toInt()
            // For Taproot raw witness version
            if (version == 0x51.toByte()) {
                ver = version - 0x50
            }
            return fromInt(ver)
        }
    }
}
