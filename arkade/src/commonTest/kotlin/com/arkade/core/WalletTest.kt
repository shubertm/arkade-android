package com.arkade.core

import com.arkade.core.wallet.Wallet
import com.arkade.network.ArkadeClient
import com.arkade.network.Config
import com.arkade.network.grpc.ArkadeClientImpl
import com.arkade.repositories.WalletRepo
import com.arkade.repositories.WalletRepoImpl
import com.arkade.storage.db.initializeTestDb
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

expect open class WalletTest()

class SingleKeyWalletTest : WalletTest() {
    private val testDb = initializeTestDb()
    private val client: ArkadeClient = ArkadeClientImpl(Config.MUTINYNET)

    @Test
    fun should_create_wallet_on_mutinynet_successfully() =
        runTest {
            val serverInfo = client.getInfo()
            val nsec = "nsec1wr49duqpjavggh78ewu9zlcuvw5huh6x5kqweqwnmjgw78kqqt6qsk0w9k"
            val wallet =
                Wallet.create(
                    nsec,
                    serverInfo = serverInfo,
                    testDb = testDb,
                )
            assertEquals(nsec, wallet.secret)
            assertEquals(Wallet.Type.SINGLE_KEY, wallet.type)

            wallet.save()

            val loadedWallet = Wallet.loadById(wallet.id, testDb)

            assertEquals(wallet.id, loadedWallet?.id!!)
            assertEquals(wallet.secret, loadedWallet.secret)
            assertEquals(wallet.destination, loadedWallet.destination)
            assertEquals(wallet.type, loadedWallet.type)
            assertEquals(wallet.accountDescriptor, loadedWallet.accountDescriptor)

            loadedWallet.delete()

            assertEquals(null, Wallet.loadById(wallet.id, testDb))
        }

    @Test
    fun should_load_more_wallets_on_mutinynet_successfully() =
        runTest {
            val nsecs =
                listOf(
                    "nsec1wr49duqpjavggh78ewu9zlcuvw5huh6x5kqweqwnmjgw78kqqt6qsk0w9k",
                    "nsec1msazr4ymx26cl83rl0wjulet9atuvlnlwyag9y6zz9rakvvh47rq99tupf",
                    "nsec1q390qprt2rl8urlfd2advh5s4rc3n4l8m0hpyrp8nd9f9tl3fkdqq9anc9",
                    "nsec1wzt73wjccrw4hm7wjpazp8vgcypvhu4egx3syzu6dgqz69kvewzs72kpx9",
                    "nsec1smd696h88hn2qje5ygzgx29n3u6dycvx2yh2lvgm2ey4q635manqnys59p",
                )
            val serverInfo = client.getInfo()
            val wallets = mutableListOf<Wallet>()
            for (nsec in nsecs) {
                val wallet = Wallet.create(nsec, serverInfo = serverInfo, testDb = testDb)
                wallets.add(wallet)
                wallet.save()
            }

            val repo: WalletRepo = WalletRepoImpl(testDb)
            repo.init()

            val loadedWallets = repo.loadWallets().filter { w -> w.type == Wallet.Type.SINGLE_KEY }

            assertEquals(wallets.size, loadedWallets.size)

            for (loadedWallet in loadedWallets) {
                val wallet = wallets.find { w -> w.id == loadedWallet.id }!!
                assertEquals(wallet.secret, loadedWallet.secret)
            }
        }
}

class HDWalletTest : WalletTest() {
    private val testDb = initializeTestDb()
    private val client: ArkadeClient = ArkadeClientImpl(Config.MUTINYNET)

    @Test
    fun should_create_wallet_on_mutinynet_successfully() =
        runTest {
            val serverInfo = client.getInfo()
            val secret = "secret"
            val wallet =
                Wallet.create(
                    secret,
                    serverInfo = serverInfo,
                    testDb = testDb,
                )
            assertEquals(secret, wallet.secret)
            assertEquals(Wallet.Type.HD, wallet.type)
            assertEquals(0, wallet.lastUsedIndex)

            wallet.save()

            val loadedWallet = Wallet.loadById(wallet.id, testDb)

            assertEquals(wallet.id, loadedWallet?.id!!)
            assertEquals(wallet.secret, loadedWallet.secret)
            assertEquals(wallet.destination, loadedWallet.destination)
            assertEquals(wallet.type, loadedWallet.type)
            assertEquals(wallet.accountDescriptor, loadedWallet.accountDescriptor)
            assertEquals(wallet.lastUsedIndex, loadedWallet.lastUsedIndex)

            loadedWallet.updateLastUsedIndex(1)

            val loadedWallet2 = Wallet.loadById(loadedWallet.id, testDb)

            assertEquals(1, loadedWallet2?.lastUsedIndex)

            loadedWallet.delete()

            assertEquals(null, Wallet.loadById(wallet.id, testDb))
        }

    @Test
    fun should_load_more_wallets_on_mutinynet_successfully() =
        runTest {
            val secrets =
                listOf(
                    "secret",
                    "secret1",
                    "secret2",
                    "secret3",
                    "secret4",
                )
            val serverInfo = client.getInfo()
            val wallets = mutableListOf<Wallet>()
            for (secret in secrets) {
                val wallet = Wallet.create(secret, serverInfo = serverInfo, testDb = testDb)
                wallets.add(wallet)
                wallet.save()
            }

            val repo: WalletRepo = WalletRepoImpl(testDb)
            repo.init()

            val loadedWallets = repo.loadWallets().filter { w -> w.type == Wallet.Type.HD }

            assertEquals(wallets.size, loadedWallets.size)

            for (loadedWallet in loadedWallets) {
                val wallet = wallets.find { w -> w.id == loadedWallet.id }!!
                assertEquals(wallet.secret, loadedWallet.secret)
            }
        }
}
