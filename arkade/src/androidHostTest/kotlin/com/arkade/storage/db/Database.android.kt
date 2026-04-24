package com.arkade.storage.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual fun initializeTestDb(): Database {
    val context =
        androidx.test.core.app.ApplicationProvider
            .getApplicationContext<Context>()
    return Room
        .inMemoryDatabaseBuilder(
            context,
            Database::class.java,
        ).setQueryCoroutineContext(Dispatchers.Unconfined)
        .setDriver(BundledSQLiteDriver())
        .build()
}
