package com.arkade.core

sealed class Error(
    val message: String,
) {
    object DuplicatedInput : Error("duplicated input")

    object SpentVtxo : Error("already spent") {
        const val MESSAGE2: String = "VTXO_ALREADY_SPENT"
    }

    object Unknown : Error("unknown")

    companion object {
        fun fromMessage(message: String?): Error =
            when {
                (message?.contains(DuplicatedInput.message) == true) -> {
                    DuplicatedInput
                }
                (message?.contains(SpentVtxo.message) == true || message?.contains(SpentVtxo.MESSAGE2) == true) -> {
                    SpentVtxo
                }
                else -> Unknown
            }
    }
}
