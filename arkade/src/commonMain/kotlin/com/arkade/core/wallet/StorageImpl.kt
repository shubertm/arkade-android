package com.arkade.core.wallet

import com.arkade.storage.db.DatabaseConstructor
import com.arkade.storage.db.entities.WalletEntity
import com.arkade.storage.db.initializeTestDb

internal class StorageImpl private constructor(
    private val isTest: Boolean = false,
) : Storage {
    private val db =
        if (!isTest) {
            DatabaseConstructor.initialize()
        } else {
            initializeTestDb()
        }
    private val walletDao = db.walletDao()

    override suspend fun loadWalletById(id: String): WalletEntity? = walletDao.load(id)

    override suspend fun loadWalletByFingerprint(fingerprint: String): WalletEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun loadWallets(): List<WalletEntity> = walletDao.loadAll()

    override suspend fun saveWallet(wallet: WalletEntity) = walletDao.save(wallet)

    override suspend fun deleteWallet(id: String) = walletDao.delete(id)

    override suspend fun updateWallet(wallet: WalletEntity) = walletDao.update(wallet)

    companion object {
        private var storage: Storage? = null

        fun get(isTest: Boolean = false): Storage {
            if (storage == null) {
                storage = StorageImpl(isTest)
            }
            return storage!!
        }
    }
}
