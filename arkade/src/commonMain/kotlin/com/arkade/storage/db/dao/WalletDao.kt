package com.arkade.storage.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arkade.storage.db.entities.WalletEntity

@Dao
interface WalletDao {
    @Insert
    suspend fun save(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun load(id: String): WalletEntity

    @Query("SELECT * FROM wallets")
    suspend fun loadAll(): List<WalletEntity>

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun delete(id: String)

    @Update
    suspend fun update(wallet: WalletEntity)
}
