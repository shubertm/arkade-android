package com.arkade.core

import fr.acinq.bitcoin.PublicKey
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ScriptTest {
    @Test
    fun scripts_should_be_generated_correctly() {
        val csvScript = "029000b2752015fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8ac"
        val multisigScript =
            "20a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622" +
                "cad2015fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8ac"

        val serverPubKey = PublicKey.fromHex("03a19310a999207dbd9a03d20f649e37c7a578a07d75e6fa19aa3f33fc6b15622c").xOnly()
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()

        val csvScriptGenerated = csvSigScript(0x90, ownerPubKey)
        val multisigScriptGenerated = multisigScript(serverPubKey, ownerPubKey)

        assertEquals(39, csvScriptGenerated.size)
        assertEquals(68, multisigScriptGenerated.size)
        assertEquals(csvScript, csvScriptGenerated.toHexString())
        assertEquals(multisigScript, multisigScriptGenerated.toHexString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun csv_script_generation_should_fail_on_invalid_lock_time_above_range() {
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()
        csvSigScript(0xFFFFE, ownerPubKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun csv_script_generation_should_fail_on_invalid_lock_time_below_range() {
        val ownerPubKey = PublicKey.fromHex("0315fbe13a8cf7e4d0c81b0caf4040f37666933d97080abb04f908964bb14588a8").xOnly()
        csvSigScript(-0x01, ownerPubKey)
    }
}
