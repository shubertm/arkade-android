package com.arkade.core.script

import fr.acinq.bitcoin.OP_CHECKLOCKTIMEVERIFY
import fr.acinq.bitcoin.OP_DROP
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script

class LockTimeTapScript(
    private val lockTime: Long,
) : ArkTapScript {
    override fun buildScript(): ByteArray {
        val asm =
            listOf(
                OP_PUSHDATA(Script.encodeNumber(lockTime)),
                OP_CHECKLOCKTIMEVERIFY,
                OP_DROP,
            )
        return Script.write(asm)
    }
}
