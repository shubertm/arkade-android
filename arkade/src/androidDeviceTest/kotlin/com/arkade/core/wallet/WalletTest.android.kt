package com.arkade.core.wallet

import com.arkade.storage.db.Database
import com.arkade.storage.db.initializeTestDb
import kotlin.test.AfterTest
import kotlin.test.Test

actual abstract class WalletTest actual constructor() : com.arkade.Test() {
    actual val testDb: Database = initializeTestDb()

    /**
     * Executes after each test to perform test-specific cleanup.
     *
     * Implementations must release resources and restore any shared state created or modified by the test.
     */
    @AfterTest
    actual abstract fun cleanup()

    /**
     * Verifies that creating a wallet succeeds and the created wallet is persisted and retrievable.
     */
    @Test
    actual abstract fun should_create_wallet_successfully()

    /**
     * Verifies that loading additional wallets succeeds and yields the expected results.
     */
    @Test
    actual abstract fun should_load_more_wallets_successfully()
}
