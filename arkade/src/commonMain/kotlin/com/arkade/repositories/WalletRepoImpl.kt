package com.arkade.repositories

import com.arkade.core.wallet.Storage
import com.arkade.core.wallet.StorageImpl
import com.arkade.core.wallet.Wallet

internal class WalletRepoImpl(
    private val isTest: Boolean = false,
) : WalletRepo {
    private val storage: Storage = StorageImpl.get(isTest)

    override suspend fun saveWallet(wallet: Wallet) {
        storage.saveWallet(wallet.toRoomEntity())
    }

    override suspend fun loadWalletById(id: String): Wallet? {
        val entity = storage.loadWalletById(id)
        return entity?.toWallet(this)
    }

    override suspend fun loadWalletByFingerprint(fingerprint: String): Wallet? {
        TODO("Not yet implemented")
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
