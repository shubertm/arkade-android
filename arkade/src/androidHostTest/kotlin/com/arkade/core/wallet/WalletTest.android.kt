package com.arkade.core.wallet

import com.arkade.storage.db.initializeTestDb
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
actual open class WalletTest actual constructor() {
    actual val testDb = initializeTestDb()

    @AfterTest
    actual open fun cleanup() {}

    @Test
    actual open fun should_create_wallet_successfully() {}

    @Test
    actual open fun should_load_more_wallets_successfully() {}
}
