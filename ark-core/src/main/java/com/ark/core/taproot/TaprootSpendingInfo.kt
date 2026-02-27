package com.ark.core.taproot

import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey

class TaprootSpendingInfo(
    val internalKey: XonlyPublicKey,
    val outputKey: XonlyPublicKey,
    val outputKeyParity: Parity,
    val merkleRoot: ByteVector32,
    val merkleScriptTree: ScriptTree,
)
