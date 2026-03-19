package com.arkade.core.bitcoin

import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.OP_0
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
data class Address(
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
                when (witnessVersion) {
                    WitnessVersion.SEGWIT -> OP_0
                    WitnessVersion.TAPROOT -> OP_1
                },
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
            val scriptPubKeySize = scriptPubKey.size
            require(scriptPubKeySize == 34) {
                "Invalid scriptPubKey length, expected 34 bytes but got $scriptPubKeySize"
            }
            val witnessProgramPushByte = scriptPubKey[1]
            require(witnessProgramPushByte == 0x20.toByte()) {
                "Invalid witness program push length, expected 0x20 but got $witnessProgramPushByte"
            }
            val witnessVersion = WitnessVersion.fromByte(scriptPubKey[0])
            require(witnessVersion == WitnessVersion.SEGWIT || witnessVersion == WitnessVersion.TAPROOT) {
                "Unsupported witness version"
            }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Address

        if (hrp != other.hrp) return false
        if (witnessVersion != other.witnessVersion) return false
        if (!witnessProgram.contentEquals(other.witnessProgram)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hrp.hashCode()
        result = 31 * result + witnessVersion.hashCode()
        result = 31 * result + witnessProgram.contentHashCode()
        return result
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
