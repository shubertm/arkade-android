package com.arkade.core.script

import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.OP_CHECKSIG
import fr.acinq.bitcoin.OP_CHECKSIGVERIFY
import fr.acinq.bitcoin.OP_PUSHDATA
import fr.acinq.bitcoin.Script
import fr.acinq.bitcoin.ScriptElt
import fr.acinq.bitcoin.XonlyPublicKey

class NofNMultisigTapScript(
    private val owners: List<XonlyPublicKey>,
) : ArkTapScript {
    override fun buildScript(): ByteArray {
        val asm = mutableListOf<ScriptElt>()
        owners.forEach { owner ->
            asm.add(OP_PUSHDATA(owner))
            asm.add(OP_CHECKSIGVERIFY)
        }
        return Script.write(asm)
    }

    companion object {
        fun parse(script: ByteArray): NofNMultisigTapScript {
            val owners = hashSetOf<XonlyPublicKey>()
            val asm = Script.parse(script)
            var lastOp = asm.last()
            var index = asm.lastIndex

            while (lastOp != OP_CHECKSIG) {
                lastOp = asm[index]
                val push = asm[index - 1] as OP_PUSHDATA
                if (lastOp == OP_CHECKSIGVERIFY && push.isPush()) {
                    val pubKey = XonlyPublicKey(ByteVector32(push.data))
                    owners.add(pubKey)
                }
                index -= 2
            }
            return NofNMultisigTapScript(owners.toList())
        }
    }
}
