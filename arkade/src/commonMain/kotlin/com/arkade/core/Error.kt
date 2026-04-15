package com.arkade.core

import com.arkade.core.Error.SpentVtxo.message2

sealed class Error(
    val message: String,
) {
    object DuplicatedInput : Error("duplicated input")

    object SpentVtxo : Error("already spent")

    val SpentVtxo.message2: String
        get() = "VTXO_ALREADY_SPENT"

    object Unknown : Error("unknown")

    companion object {
        fun fromMessage(message: String?): Error =
            when {
                (message?.contains(DuplicatedInput.message) == true) -> {
                    DuplicatedInput
                }
                (message?.contains(SpentVtxo.message) == true || message?.contains(SpentVtxo.message2) == true) -> {
                    SpentVtxo
                }
                else -> Unknown
            }
    }
}
