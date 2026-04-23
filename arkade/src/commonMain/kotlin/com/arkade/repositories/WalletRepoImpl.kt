package com.arkade.repositories

import com.arkade.core.wallet.Storage
import com.arkade.core.wallet.StorageImpl
import com.arkade.core.wallet.Wallet
import com.arkade.core.wallet.WalletImpl
import com.arkade.storage.db.entities.WalletEntity

internal class WalletRepoImpl(
    private val isTest: Boolean = false,
) : WalletRepo {
    private val storage: Storage = StorageImpl.get(isTest)

    override suspend fun saveWallet(wallet: Wallet) {
        val entity =
            WalletEntity(
                wallet.id,
                wallet.secret,
                wallet.destination,
                wallet.type,
                wallet.accountDescriptor,
                wallet.lastUsedIndex,
            )
        storage.saveWallet(entity)
    }

    override suspend fun loadWalletById(id: String): Wallet {
        val entity = storage.loadWalletById(id)
        return WalletImpl(this, entity.id, entity.secret, entity.destination, entity.type, entity.accountDescriptor, entity.lastUsedIndex)
    }

    override suspend fun loadWalletByFingerprint(fingerprint: String): Wallet {
        TODO("Not yet implemented")
    }

    override suspend fun loadWallets(): List<Wallet> =
        storage.loadWallets().map { entity ->
            WalletImpl(this, entity.id, entity.secret, entity.destination, entity.type, entity.accountDescriptor, entity.lastUsedIndex)
        }

    override suspend fun deleteWallet(id: String) = storage.deleteWallet(id)

    override suspend fun updateWallet(wallet: Wallet) {
        val entity =
            WalletEntity(
                wallet.id,
                wallet.secret,
                wallet.destination,
                wallet.type,
                wallet.accountDescriptor,
                wallet.lastUsedIndex,
            )
        storage.updateWallet(entity)
    }
}
