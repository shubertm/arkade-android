package com.arkade.core.wallet

import com.arkade.storage.db.initializeTestDb
import kotlin.test.AfterTest
import kotlin.test.Test

actual abstract class WalletTest actual constructor() : com.arkade.Test() {
    actual val testDb = initializeTestDb()

    /**
     * Cleans up resources and resets state after each test.
     *
     * Implementations should release any resources, clear test-specific data, and restore global
     * or shared state to ensure tests are isolated.
     */
    @AfterTest
    actual abstract fun cleanup()

    /**
     * Verifies that a wallet can be created successfully and stored in the test database.
     *
     * Implementations should assert that the created wallet meets expected properties and is
     * persisted, so it can be retrieved by subsequent operations.
     */
    @Test
    actual abstract fun should_create_wallet_successfully()

    /**
     * Verifies that additional wallets can be loaded successfully.
     *
     * Implementations must assert that requesting more wallet entries returns the expected results
     * and updates any pagination or in-memory state accordingly.
     */
    @Test
    actual abstract fun should_load_more_wallets_successfully()
}
