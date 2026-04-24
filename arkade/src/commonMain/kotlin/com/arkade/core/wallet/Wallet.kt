package com.arkade.core.wallet

import com.arkade.core.ArkAddress
import com.arkade.core.ArkServerInfo
import com.arkade.core.bitcoin.Network
import com.arkade.repositories.WalletRepo
import com.arkade.repositories.WalletRepoImpl
import com.arkade.storage.db.Database
import com.arkade.storage.db.entities.WalletEntity
import fr.acinq.bitcoin.Bech32
import fr.acinq.bitcoin.DeterministicWallet
import fr.acinq.bitcoin.KeyPath
import fr.acinq.bitcoin.PrivateKey

interface Wallet {
    val id: String
    val secret: String
    val destination: String?
    val type: Type
    val accountDescriptor: String
    val lastUsedIndex: Int

    val repo: WalletRepo

    suspend fun save()

    suspend fun delete()

    suspend fun update()

    suspend fun updateLastUsedIndex(index: Int)

    fun toRoomEntity(): WalletEntity = WalletEntity(id, secret, destination, type, accountDescriptor, lastUsedIndex)

    enum class Type {
        HD,
        SINGLE_KEY,
    }

    companion object {
        private const val NSEC_HRP = "nsec"

        fun create(
            secret: String,
            destination: String? = null,
            serverInfo: ArkServerInfo,
            testDb: Database? = null,
        ): Wallet {
            if (destination != null) {
                validateDestination(destination, serverInfo)
            }

            val repo: WalletRepo = WalletRepoImpl(testDb)

            return if (secret.startsWith(NSEC_HRP, true)) {
                createNSecWallet(secret, destination, repo)
            } else {
                createHDWallet(secret, destination, serverInfo, repo)
            }
        }

        suspend fun loadById(
            id: String,
            testDb: Database? = null,
        ): Wallet? {
            val repo: WalletRepo = WalletRepoImpl(testDb)
            return repo.loadWalletById(id)
        }

        fun getOutputDescriptorFromNSec(nsec: String): String {
            val privateKey = getPrivateKeyFromNSec(nsec)
            return "tr(${privateKey.publicKey().toHex()})"
        }

        private fun createNSecWallet(
            nsec: String,
            destination: String?,
            repo: WalletRepo,
        ): Wallet {
            val outputDescriptor = getOutputDescriptorFromNSec(nsec)
            return WalletImpl(
                repo,
                outputDescriptor,
                nsec,
                destination,
                Type.SINGLE_KEY,
                outputDescriptor,
                0,
            )
        }

        private fun createHDWallet(
            mnemonic: String,
            destination: String?,
            serverInfo: ArkServerInfo,
            repo: WalletRepo,
        ): Wallet {
            val masterKey = DeterministicWallet.generate(mnemonic.encodeToByteArray())

            fun encodePubKeyByNetwork(
                pubKey: DeterministicWallet.ExtendedPublicKey,
                network: Network,
            ): String =
                when (network) {
                    Network.MAINNET -> pubKey.encode(DeterministicWallet.xpub)
                    else -> pubKey.encode(true)
                }
            val fingerprint = encodePubKeyByNetwork(masterKey.extendedPublicKey, serverInfo.network)
            val coinType =
                when (serverInfo.network) {
                    Network.MAINNET -> 0
                    else -> 1
                }
            val accountKeyPath = KeyPath("m/86'/$coinType'/0'")
            val accountPrivateKey = masterKey.derivePrivateKey(accountKeyPath)
            val accountPublicKey = encodePubKeyByNetwork(accountPrivateKey.extendedPublicKey, serverInfo.network)
            val accountDescriptor = "tr([$fingerprint/86'/$coinType'/0']$accountPublicKey/0/*)"

            return WalletImpl(
                repo,
                accountDescriptor,
                mnemonic,
                destination,
                Type.HD,
                accountDescriptor,
                0,
            )
        }

        private fun validateDestination(
            address: String,
            serverInfo: ArkServerInfo,
        ) {
            val arkAddress = ArkAddress.decode(address)
            if (!serverInfo.signerPubKey.value
                    .toByteArray()
                    .contentEquals(arkAddress.serverPubKey)
            ) {
                throw IllegalArgumentException("Invalid destination server key")
            }
        }

        private fun getPrivateKeyFromNSec(nsec: String): PrivateKey {
            val (_, bytes, _) = Bech32.decodeBytes(nsec)
            return PrivateKey.fromHex(bytes.toHexString())
        }
    }
}
