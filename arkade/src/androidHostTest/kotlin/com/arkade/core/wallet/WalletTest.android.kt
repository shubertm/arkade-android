package com.arkade.core.wallet

import com.arkade.storage.db.initializeTestDb
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
actual abstract class WalletTest actual constructor() {
    actual val testDb = initializeTestDb()

    @AfterTest
    actual abstract fun cleanup()

    @Test
    actual abstract fun should_create_wallet_successfully()

    @Test
    actual abstract fun should_load_more_wallets_successfully()
}
