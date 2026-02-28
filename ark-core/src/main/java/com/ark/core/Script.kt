package com.ark.core

import fr.acinq.bitcoin.OP_CHECKSEQUENCEVERIFY
import fr.acinq.bitcoin.OP_CHECKSIG
import fr.acinq.bitcoin.OP_CHECKSIGVERIFY
import fr.acinq.bitcoin.OP_DROP
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.XonlyPublicKey

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
