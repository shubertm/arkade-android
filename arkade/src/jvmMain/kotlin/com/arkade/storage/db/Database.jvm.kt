package com.arkade.storage.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual fun initializeTestDb(): Database =
    Room
        .inMemoryDatabaseBuilder<Database>(
            factory = { DatabaseConstructor.initialize() },
        ).setQueryCoroutineContext(Dispatchers.Unconfined)
        .setDriver(BundledSQLiteDriver())
        .build()
