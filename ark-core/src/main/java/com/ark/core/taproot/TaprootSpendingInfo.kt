package com.ark.core.taproot

import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey

class TaprootSpendingInfo(
    internalKey: XonlyPublicKey,
    merkleRoot: ByteVector32,
    outputKey: XonlyPublicKey,
    parity: Parity,
    merkleScriptTree: ScriptTree,
)
