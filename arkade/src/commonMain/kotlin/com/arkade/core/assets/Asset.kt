package com.arkade.core.assets

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class Asset(
    val id: String,
    val amount: BigDecimal,
)
