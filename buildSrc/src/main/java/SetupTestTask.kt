import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.inject.Inject
import kotlin.system.exitProcess

abstract class SetupTestTask: DefaultTask() {

    @get:Inject
    abstract val execOps : ExecOperations

    @Internal
    val arkdExec = listOf("docker", "exec", "arkd")

    suspend fun setupArkServer() {
        try {
            logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.quiet("Setting up Ark server")
            logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            val walletStatus = checkWalletStatus()
            if (walletStatus.first && walletStatus.second && walletStatus.third) {
                logger.quiet(" ✔ Wallet ready and synced")
            } else {
                if (!walletStatus.first) {
                    logger.quiet("Creating Ark wallet...")

                    execOps.exec {
                        val arkdExec = arkdExec.toMutableList()
                        arkdExec.addAll(listOf("arkd", "wallet", "create", "--password", "secret"))
                        commandLine(arkdExec)
                        standardOutput = ByteArrayOutputStream()
                        isIgnoreExitValue = true
                    }
                    logger.quiet("✔ Wallet created")
                }
                if (!walletStatus.second) {
                    logger.quiet("\nUnlocking Ark wallet...")
                    execOps.exec {
                        val arkdExec = arkdExec.toMutableList()
                        arkdExec.addAll(listOf("arkd", "wallet", "unlock", "--password", "secret"))
                        commandLine(arkdExec)
                        standardOutput = ByteArrayOutputStream()
                    }
                    logger.quiet("✔ Wallet unlocked")
                }
                waitForWalletReadiness()
            }

            val serverInfo = waitForArkServer()

            val serverInfoJson = Json.parseToJsonElement(serverInfo)
            if (serverInfoJson is JsonObject) {
                logger.quiet("\nark Server Public Key: ${serverInfoJson["signerPubkey"]}")
            }

            logger.quiet("\nFunding Ark wallet...")
            val outputStream = ByteArrayOutputStream()
            execOps.exec {
                val arkdExec = arkdExec.toMutableList()
                arkdExec.addAll(listOf("arkd", "wallet", "address"))
                commandLine(arkdExec)
                standardOutput = outputStream
                isIgnoreExitValue = true
            }

            val arkAddress = outputStream.toString().trim()

            logger.quiet("  Address: $arkAddress")

            repeat(10) {
                execOps.exec {
                    commandLine("nigiri", "faucet", arkAddress)
                    isIgnoreExitValue = true
                    standardOutput = ByteArrayOutputStream()
                }
            }

            logger.quiet("  ✔ Wallet funded with 10 BTC")

            // Waiting for transactions to be confirmed
            delay(5000)

            logger.quiet("\nInitializing Ark client")
            execOps.exec {
                val arkdExec = arkdExec.toMutableList()
                arkdExec.addAll(
                    listOf(
                        "ark",
                        "init",
                        "--server-url",
                        "http://localhost:7070",
                        "--explorer",
                        "http://localhost:3000",
                        "--password",
                        "secret"
                    )
                )
                commandLine(arkdExec)
                standardOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }

            logger.quiet("  ✔ Client initialized")

            logger.quiet("\nCreating and redeeming notes")

            val noteResult = execOps.exec {
                val arkdExec = arkdExec.toMutableList()
                arkdExec.addAll(listOf("arkd", "note", "--amount", "2000000"))
                outputStream.reset()
                standardOutput = outputStream
                isIgnoreExitValue = true
                commandLine(arkdExec)
            }

            if (noteResult.exitValue != 0) {
                throw Exception("Failed to create note")
            }


            val note = outputStream.toString().trim()

            if (note.isEmpty()) {
                throw Exception("Failed to create note: empty note")
            }

            val redeemResult = execOps.exec {
                val arkdExec = arkdExec.toMutableList()
                standardOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
                arkdExec.addAll(listOf("ark", "redeem-notes", "-n", note, "--password", "secret"))
                commandLine(arkdExec)
            }

            if (redeemResult.exitValue != 0) {
                throw Exception("Failed to redeem note")
            }

            logger.quiet("  ✔ Notes redeemed")

            logger.quiet("\n✔ Ark server and client setup complete")
        } catch(e: Exception) {
            logger.quiet("\n❌ Ark server and client setup failed")
            throw e
        }
    }

    suspend fun checkWalletStatus(maxRetries: Int = 30, retryDelay: Long = 2000): Triple<Boolean, Boolean, Boolean> {
        for (i in 0 .. maxRetries) {
            val outputStream = ByteArrayOutputStream()
            val result = execOps.exec {
                val arkdExec = arkdExec.toMutableList()
                arkdExec.addAll(listOf("arkd", "wallet", "status"))
                commandLine(arkdExec)
                standardOutput = outputStream
                isIgnoreExitValue = true
            }

            val status = outputStream.toString().trim()

            if (result.exitValue == 0 && status.isNotEmpty()) {
                val isInitialized = status.contains("initialized: true")
                val isUnlocked = status.contains("unlocked: true")
                val isSynced = status.contains("synced: true")
                return Triple(isInitialized, isUnlocked, isSynced)
            }
            if (i < maxRetries) {
                delay(retryDelay)
            }
        }
        throw IllegalStateException("Wallet status check failed after $maxRetries retries")
    }

    suspend fun waitForWalletReadiness(maxRetries: Int = 30, retryDelay: Long = 2000): Boolean {
        logger.quiet("\nWaiting for wallet to be ready and synced...")
        for (i in 0 .. maxRetries) {
            val status = try {
                checkWalletStatus(0)
            } catch (_: Exception) { null }

            if (status?.first == true && status.second && status.third) {
                logger.quiet(" ✔ Wallet ready and synced")
                return true
            }
            if (i < maxRetries) {
                logger.quiet("Waiting... (${i + 1}/$maxRetries)")
                delay(retryDelay)
            }
        }
        throw Exception("wallet failed to be ready after maximum retries")
    }

    suspend fun waitForArkServer(maxRetries: Int = 30, retryDelay: Long = 2000): String {
        val client = HttpClient.newHttpClient()

        logger.quiet("\nWaiting for ark server to be ready...")
        for (i in 0 .. maxRetries) {
            try {
                val response = client.sendRequest("http://localhost:7070/v1/info")
                val responseBody = response.body().trim()

                if (response.statusCode() == 200 && responseBody.isNotEmpty() && !responseBody.contains("server not ready")) {
                    logger.quiet(" ✔ Server ready")
                    return responseBody
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw e
            }
            catch (e: CancellationException) {
                throw e
            }
            catch (_: Exception) {
                // Ignore any exceptions and retry
            }

            if (i < maxRetries) {
                logger.quiet("Waiting... (${i + 1}/$maxRetries)")
                delay(retryDelay)
            }
        }
        throw Exception("ark server failed to be ready after maximum retries")
    }

    suspend fun setupFulmine() {
        try {
            logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.quiet("Setting up Fulmine")
            logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            val client = HttpClient.newHttpClient()

            logger.quiet("Creating Fulmine wallet...")
            val requestBody = """
                {
                    "private_key": "5b9902c1098cc0f4c7e91066ef3227e292d994a50ebc33961ac6daa656fd242e",
                    "password": "password",
                    "server_url": "http://arkd:7070"
                }
                """.trimIndent()

            val response = client.sendRequest(
                "http://localhost:7001/api/v1/wallet/create",
                requestBody
            )

            val walletAlreadyInitialized = response.body().contains("wallet already initialized")

            if (walletAlreadyInitialized) {
                logger.quiet("  ✔ Wallet already initialized")
                delay(5000)
            } else {
                if (response.statusCode() == 200) {
                    logger.quiet("  ✔ Wallet created")
                    delay(5000)
                } else {
                    throw Exception("Fulmine failed to create wallet: ${response.body()}")
                }
            }

            logger.quiet("\nUnlocking Fulmine wallet...")

            val unlockRequestBody = "{ \"password\": \"password\" }"

            val unlockResponse = client.sendRequest(
                "http://localhost:7001/api/v1/wallet/unlock",
                unlockRequestBody
            )

            if (unlockResponse.statusCode() == 200) {
                logger.quiet("  ✔ Wallet unlocked")
                delay(2000)
            } else {
                throw Exception("Fulmine failed to unlock wallet: ${unlockResponse.body()}")
            }

            logger.quiet("\nGetting Fulmine address...")

            val fulmineAddressResponse = client.sendRequest("http://localhost:7001/api/v1/address")
            val fulmineAddressJsonResponse = Json.parseToJsonElement(fulmineAddressResponse.body())
            val fulmineAddress = fulmineAddressJsonResponse.jsonObject["address"]
                ?.jsonPrimitive?.content
                ?.split("?")[0]?.split(":")[1]
                ?: throw IllegalStateException("Fulmine address missing or invalid: ${fulmineAddressResponse.body()}")

            logger.quiet("  Address: $fulmineAddress")

            logger.quiet("\nFunding Fulmine address...")
            client.faucet(fulmineAddress, 1)

            logger.quiet("\nSettling funds in Fulmine...")
            val settleResponse = client.sendRequest("http://localhost:7001/api/v1/settle")
            if (settleResponse.statusCode() == 200) {
                logger.quiet("  ✔ Funds settled")
            } else {
                throw Exception("Fulmine failed to settle funds: ${settleResponse.body()}")
            }

            logger.quiet("\n✔ Fulmine setup complete")
        } catch(e: Exception) {
            logger.quiet("\n❌ Fulmine setup failed: $e")
            throw e
        }
    }

    suspend fun HttpClient.faucet(address: String, amount: Long, maxRetries: Int = 10, retryDelay: Long = 1000): String {
        val initialCountResponse = sendRequest("http://localhost:3000/address/$address")
        if (initialCountResponse.statusCode() == 200) {
            val initialCount = Json.parseToJsonElement(initialCountResponse.body())
                .jsonObject["chain_stats"]
                ?.jsonObject["tx_count"]?.toString()?.toIntOrNull() ?: 0

            val outputStream = ByteArrayOutputStream()
            execOps.exec {
                standardOutput = outputStream
                isIgnoreExitValue = true
                commandLine("nigiri", "faucet", address, amount.toString())
            }
            val txId = outputStream.toString().split(":")[1].trim()
            logger.quiet("  Transaction ID: $txId")

            repeat(maxRetries) { i ->
                delay(retryDelay)
                try {
                    val newCountResponse = sendRequest("http://localhost:3000/address/$address")
                    if (newCountResponse.statusCode() == 200) {
                        val newCount = Json.parseToJsonElement(newCountResponse.body())
                            .jsonObject["chain_stats"]
                            ?.jsonObject["tx_count"]?.toString()?.toIntOrNull() ?: 0

                        if (newCount > initialCount) {
                            logger.quiet("  ✔ Confirmed")
                            return txId
                        }
                    } else {
                        throw Exception("Failed to get new faucet count: ${newCountResponse.body()}")
                    }
                } catch (_: Exception) { /* Ignore and retry */
                }
                if (i < maxRetries) {
                    logger.quiet("  Waiting for confirmation ($i/$maxRetries)...")
                }
            }
            throw Exception("Timed out waiting for faucet transaction to confirm.")
        } else {
            throw Exception("Failed to get initial faucet count: ${initialCountResponse.body()}")
        }
    }

    suspend fun HttpClient.sendRequest(uri: String, body: String = ""): HttpResponse<String> {
        val requestBuilder = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .uri(URI.create(uri))
        if (body.isNotEmpty()) {
            val requestBody = HttpRequest.BodyPublishers.ofString(body)
            requestBuilder.POST(requestBody)
        }
        val request = requestBuilder.build()
        val response = withContext(Dispatchers.IO) {
            send(request, HttpResponse.BodyHandlers.ofString())
        }
        return response
    }

    @TaskAction
    fun run() {
        runBlocking(Dispatchers.IO) {
            try {
                setupArkServer()
                setupFulmine()
                logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                logger.quiet("✓ Regtest setup completed successfully")
                logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            } catch(e: Exception) {
                logger.quiet("\n❌ Regtest setup failed")
                logger.debug(e.message, e)
                exitProcess(1)
            }
        }
    }
}