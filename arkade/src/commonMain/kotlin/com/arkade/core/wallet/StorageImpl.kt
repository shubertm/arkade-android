package com.arkade.core.wallet

import com.arkade.storage.db.Database
import com.arkade.storage.db.DatabaseConstructor
import com.arkade.storage.db.entities.WalletEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class StorageImpl private constructor(
    testDb: Database? = null,
) : Storage {
    private val db = testDb ?: DatabaseConstructor.initialize()
    private val walletDao = db.walletDao()

    override suspend fun loadWalletById(id: String): WalletEntity? = walletDao.load(id)

    override suspend fun loadWallets(): List<WalletEntity> = walletDao.loadAll()

    override suspend fun saveWallet(wallet: WalletEntity) = walletDao.save(wallet)

    override suspend fun deleteWallet(id: String) = walletDao.delete(id)

    override suspend fun updateWallet(wallet: WalletEntity) = walletDao.update(wallet)

    companion object {
        private var storage: Storage? = null
        private var testStorage: Storage? = null
        private val mutex = Mutex()

        suspend fun get(testDb: Database? = null): Storage =
            mutex.withLock {
                if (testDb != null) {
                    testStorage ?: StorageImpl(testDb).also { testStorage = it }
                } else {
                    storage ?: StorageImpl().also { storage = it }
                }
            }
    }
}
