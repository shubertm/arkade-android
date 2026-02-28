package com.ark.core.taproot

import fr.acinq.bitcoin.ByteVector32
import fr.acinq.bitcoin.ScriptTree
import fr.acinq.bitcoin.XonlyPublicKey

/**
 * The `TaprootSpendingInfo` class carries the information required to spend the VTXO
 *
 * [internalKey] is the untweaked x-only public key for locking keypath spend
 *
 * **Note:** For Ark it is the [com.ark.core.UNSPENDABLE_PUBKEY] to make keypath unspendable
 *
 * [outputKey] is the tweaked x-only public key of [internalKey]
 *
 * [outputKeyParity] is the [Parity] of [outputKey]
 *
 * [merkleRoot] is the hash of the [ScriptTree]
 *
 * [merkleScriptTree] is the [ScriptTree] keeping the script spending paths
 */
data class TaprootSpendingInfo(
    val internalKey: XonlyPublicKey,
    val outputKey: XonlyPublicKey,
    val outputKeyParity: Parity,
    val merkleRoot: ByteVector32,
    val merkleScriptTree: ScriptTree,
)
