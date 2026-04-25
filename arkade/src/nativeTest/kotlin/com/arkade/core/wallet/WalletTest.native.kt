package com.arkade.core.wallet

import com.arkade.storage.db.Database
import com.arkade.storage.db.initializeTestDb
import kotlin.test.AfterTest
import kotlin.test.Test

actual open class WalletTest actual constructor() {
    actual val testDb: Database = initializeTestDb()

    @AfterTest
    actual open fun cleanup() {}

    @Test
    actual open fun should_create_wallet_successfully() {}

    @Test
    actual open fun should_load_more_wallets_successfully() {}
}
