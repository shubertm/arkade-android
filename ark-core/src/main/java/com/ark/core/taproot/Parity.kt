package com.ark.core.taproot

/**
 * The `Parity` enum is a representation of whether an `XonlyPublicKey` is [Odd] or [Even]
 */
enum class Parity {
    Even,
    Odd,
    ;

    companion object {
        /**
         * Determines a [Parity] using the provided `Boolean`
         * Returns [Odd] when the Boolean is true else [Even]
         */
        fun fromBooleanIsOdd(isOdd: Boolean): Parity {
            if (isOdd) {
                return Odd
            }
            return Even
        }

        /**
         * Determines a [Parity] using the provided `Boolean`
         * Returns [Even] when the Boolean is true else [Odd]
         */
        fun fromBooleanIsEven(isEven: Boolean): Parity {
            if (isEven) {
                return Even
            }
            return Odd
        }
    }
}
