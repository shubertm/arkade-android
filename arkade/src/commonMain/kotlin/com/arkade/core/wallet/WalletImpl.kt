package com.arkade.core.wallet

import com.arkade.repositories.WalletRepo

class WalletImpl(
    override val repo: WalletRepo,
    override val id: String,
    override val secret: String,
    override val destination: String?,
    override val type: Wallet.Type,
    override val accountDescriptor: String,
    override var lastUsedIndex: Int,
) : Wallet {
    override suspend fun save() = repo.saveWallet(this)

    override suspend fun delete() = repo.deleteWallet(id)

    override suspend fun update() = repo.updateWallet(this)

    override suspend fun updateLastUsedIndex(index: Int) {
        require(index >= lastUsedIndex) { "Invalid last used index" }
        val oldLastUsedIndex = lastUsedIndex
        lastUsedIndex = index
        runCatching { update() }.onFailure { lastUsedIndex = oldLastUsedIndex }
    }
}
