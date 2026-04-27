package com.arkade.core.wallet

import androidx.annotation.VisibleForTesting
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

    /**
     * Retrieve a wallet entity by its identifier.
     *
     * @param id The wallet's unique identifier.
     * @return The matching WalletEntity if found, `null` if no wallet exists with the given id.
     */
    override suspend fun loadWalletById(id: String): WalletEntity? = walletDao.load(id)

    /**
     * Retrieves all persisted wallet entities.
     *
     * @return A list of `WalletEntity` objects representing all stored wallets; an empty list if
     * no wallets exist.
     */
    override suspend fun loadWallets(): List<WalletEntity> = walletDao.loadAll()

    /**
     * Persists the given wallet entity to the underlying storage.
     *
     * @param wallet The wallet entity to save.
     */
    override suspend fun saveWallet(wallet: WalletEntity) = walletDao.save(wallet)

    /**
     * Deletes the wallet with the given identifier from persistent storage.
     *
     * @param id The wallet's identifier to remove.
     */
    override suspend fun deleteWallet(id: String) = walletDao.delete(id)

    /**
     * Updates an existing wallet record in the persistent store.
     *
     * @param wallet The wallet entity containing updated fields; its `id` is used to identify
     * which record to update.
     */
    override suspend fun updateWallet(wallet: WalletEntity) = walletDao.update(wallet)

    internal fun close() {
        db.close()
    }

    companion object {
        private var storage: Storage? = null
        private var testStorage: Storage? = null
        private val mutex = Mutex()

        /**
         * Obtain the shared Storage singleton, optionally backed by the provided test Database.
         *
         * If `testDb` is non-null, returns a cached test Storage created with that Database;
         * otherwise returns a cached production Storage. Initialization is performed once and the
         * instance is reused for subsequent calls.
         *
         * @param testDb Optional test Database to create or retrieve a test-backed Storage
         * instance.
         * @return The singleton Storage; a test-backed instance when `testDb` is provided,
         * otherwise the production instance.
         */
        suspend fun get(testDb: Database? = null): Storage =
            mutex.withLock {
                if (testDb != null) {
                    testStorage ?: StorageImpl(testDb).also { testStorage = it }
                } else {
                    storage ?: StorageImpl().also { storage = it }
                }
            }

        /**
         * Clears the cached test `Storage` instance.
         *
         * After calling this, the next `get(testDb = ...)` call will create a new test-backed
         * `Storage`.
         */
        @VisibleForTesting
        fun reset() {
            (testStorage as StorageImpl).close()
            testStorage = null
        }
    }
}
