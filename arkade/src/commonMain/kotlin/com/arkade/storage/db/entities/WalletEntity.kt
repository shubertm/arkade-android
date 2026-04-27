package com.arkade.storage.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arkade.core.wallet.Wallet
import com.arkade.core.wallet.WalletImpl
import com.arkade.repositories.WalletRepo

@Entity("wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val secret: String,
    val destination: String?,
    val type: Wallet.Type,
    val accountDescriptor: String,
    val lastUsedIndex: Int,
) {
    /**
     * Constructs a [Wallet] instance from this entity using the provided repository.
     *
     * @param repo Repository used by the created [Wallet] for persistence and operations.
     * @return A [Wallet] initialized with this entity's `id`, `secret`, `destination`, `type`,
     * `accountDescriptor`, and `lastUsedIndex`.
     */
    fun toWallet(repo: WalletRepo): Wallet = WalletImpl(repo, id, secret, destination, type, accountDescriptor, lastUsedIndex)
}
