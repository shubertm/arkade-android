package com.arkade.repositories

import com.arkade.core.wallet.Wallet

interface WalletRepo {
    suspend fun saveWallet(wallet: Wallet)

    suspend fun loadWalletById(id: String): Wallet?

    suspend fun loadWallets(): List<Wallet>

    suspend fun deleteWallet(id: String)

    suspend fun updateWallet(wallet: Wallet)
}
