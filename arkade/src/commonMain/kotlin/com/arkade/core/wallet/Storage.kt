package com.arkade.core.wallet

import com.arkade.storage.db.entities.WalletEntity

interface Storage {
    suspend fun loadWalletById(id: String): WalletEntity

    suspend fun loadWalletByFingerprint(fingerprint: String): WalletEntity

    suspend fun loadWallets(): List<WalletEntity>

    suspend fun saveWallet(wallet: WalletEntity)

    suspend fun deleteWallet(id: String)

    suspend fun updateWallet(wallet: WalletEntity)
}
