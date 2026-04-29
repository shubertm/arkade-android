package com.arkade.di

import com.arkade.core.wallet.Storage
import com.arkade.core.wallet.StorageImpl
import com.arkade.repositories.WalletRepo
import com.arkade.repositories.WalletRepoImpl
import org.koin.dsl.module

val storageModule =
    module {
        single<Storage> { StorageImpl(get()) }
    }

val repoModule =
    module {
        single<WalletRepo> { WalletRepoImpl(get()) }
    }
