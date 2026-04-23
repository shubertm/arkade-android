package com.arkade.storage.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arkade.core.wallet.Wallet

@Entity("wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val secret: String,
    val destination: String?,
    val type: Wallet.Type,
    val accountDescriptor: String,
    val lastUsedIndex: Int,
)
