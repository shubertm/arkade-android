package com.arkade.network.grpc

import com.arkade.network.ArkadeClient
import com.arkade.network.Config

class ArkServiceClientTest {
    val arkadeClient: ArkadeClient = ArkadeClientImpl(Config.MAINNET)

    /*@Test
    fun can_fetch_server_info_from_mainnet() =
        runTest {
            val serverInfo = arkadeClient.getInfo()
            assertEquals(Network.MAINNET, serverInfo.network)
        }
     */
}
