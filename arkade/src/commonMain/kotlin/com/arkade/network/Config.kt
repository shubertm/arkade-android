package com.arkade.network

enum class Config(
    val arkadeUrl: String,
    val arkadeWalletUrl: String? = null,
    val boltzUrl: String? = null,
    val explorerUrl: String? = null,
) {
    /**
     *  Mainnet configuration.
     */
    MAINNET(
        arkadeUrl = "https://arkade.computer",
        arkadeWalletUrl = "https://arkade.money",
        boltzUrl = "https://api.ark.boltz.exchange",
        explorerUrl = "https://arkade.space",
    ),

    /**
     *  Mutinynet (signet) configuration
     */
    MUTINYNET(
        arkadeUrl = "https://mutinynet.arkade.sh",
        arkadeWalletUrl = "https://mutinynet.arkade.money",
        boltzUrl = "https://api.boltz.mutinynet.arkade.sh/",
        explorerUrl = "https://explorer.mutinynet.arkade.sh",
    ),

    /**
     *  Local regtest configuration
     */
    REGTEST(
        arkadeUrl = "http://localhost:7070",
        arkadeWalletUrl = "http://localhost:3002",
        boltzUrl = "http://localhost:9069/",
        explorerUrl = "http://localhost:7080",
    ),
}
