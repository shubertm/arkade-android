package com.ark.core.bitcoin

import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.OP_1
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script

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

    fun encode(): String = Bech32.encodeWitnessAddress(hrp.prefix, witnessVersion.toByte(), witnessProgram)

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
        fun decode(address: String): Address {
            val (prefix, version, witnessProgram) = Bech32.decodeWitnessAddress(address)
            val hrp = Hrp.fromString(prefix)
            return Address(
                hrp,
                WitnessVersion.fromByte(version),
                witnessProgram,
            )
        }

        fun fromScriptPubKey(
            script: ByteArray,
            network: Network,
        ): Address {
            val witnessVersionByte = script[0]
            val witnessProgram = script.copyOfRange(2, 34)
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
