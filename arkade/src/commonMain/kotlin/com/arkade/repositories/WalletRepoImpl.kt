package com.arkade.repositories

import com.arkade.core.wallet.Storage
import com.arkade.core.wallet.StorageImpl
import com.arkade.core.wallet.Wallet
import com.arkade.storage.db.Database

internal class WalletRepoImpl(
    private val testDb: Database? = null,
) : WalletRepo {
    private lateinit var storage: Storage

    override suspend fun init() {
        storage = StorageImpl.get(testDb)
    }

    override suspend fun saveWallet(wallet: Wallet) {
        storage.saveWallet(wallet.toRoomEntity())
    }

    override suspend fun loadWalletById(id: String): Wallet? {
        val entity = storage.loadWalletById(id)
        return entity?.toWallet(this)
    }

    override suspend fun loadWallets(): List<Wallet> =
        storage.loadWallets().map { entity ->
            entity.toWallet(this)
        }

    override suspend fun deleteWallet(id: String) = storage.deleteWallet(id)

    override suspend fun updateWallet(wallet: Wallet) {
        storage.updateWallet(wallet.toRoomEntity())
    }
}
