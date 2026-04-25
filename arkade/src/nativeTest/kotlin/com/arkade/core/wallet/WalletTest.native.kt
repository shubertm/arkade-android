package com.arkade.core.wallet

import com.arkade.storage.db.Database
import com.arkade.storage.db.initializeTestDb
import kotlin.test.AfterTest
import kotlin.test.Test

actual abstract class WalletTest actual constructor() {
    actual val testDb: Database = initializeTestDb()

    @AfterTest
    actual abstract fun cleanup()

    @Test
    actual abstract fun should_create_wallet_successfully()

    @Test
    actual abstract fun should_load_more_wallets_successfully()
}
