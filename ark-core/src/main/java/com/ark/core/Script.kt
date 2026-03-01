package com.ark.core

import fr.acinq.bitcoin.OP_CHECKSEQUENCEVERIFY
import fr.acinq.bitcoin.OP_CHECKSIG
import fr.acinq.bitcoin.OP_CHECKSIGVERIFY
import fr.acinq.bitcoin.OP_DROP
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.XonlyPublicKey

/**
 * @param serverPubKey is the Arkade operator's x-only public key
 * @param ownerPubKey is the `VTXO` owner's x-only public key
 * @return a multisig script for collaborative exit
 */
fun multisigScript(
    serverPubKey: XonlyPublicKey,
    ownerPubKey: XonlyPublicKey,
): ByteArray {
    val asm =
        listOf(
            OP_PUSHDATA(serverPubKey),
            OP_CHECKSIGVERIFY,
            OP_PUSHDATA(ownerPubKey),
            OP_CHECKSIG,
        )
    return Script.write(asm)
}

/**
 * @param lockTime is the wait time for the exit after on-chain confirmation
 * @param ownerPubKey is the x-only public key for the `VTXO` owner
 * @return a `CSV` script for unilateral exit
 */
fun csvSigScript(
    lockTime: Long,
    ownerPubKey: XonlyPublicKey,
): ByteArray {
    require(lockTime in 0..0xFFFFL) { "Invalid lock time" }
    val asm =
        listOf(
            OP_PUSHDATA(Script.encodeNumber(lockTime)),
            OP_CHECKSEQUENCEVERIFY,
            OP_DROP,
            OP_PUSHDATA(ownerPubKey),
            OP_CHECKSIG,
        )
    return Script.write(asm)
}
