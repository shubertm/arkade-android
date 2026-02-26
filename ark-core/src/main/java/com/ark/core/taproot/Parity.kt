package com.ark.core.taproot

/**
 * The `Parity` enum is a representation of whether an `XonlyPublicKey` is [Odd] or [Even]
 */
enum class Parity {
    Even,
    Odd,
    ;

    companion object {
        fun fromBooleanIsOdd(isOdd: Boolean): Parity {
            if (isOdd) {
                return Odd
            }
            return Even
        }

        fun fromBooleanIsEven(isEven: Boolean): Parity {
            if (isEven) {
                return Even
            }
            return Odd
        }
    }
}
