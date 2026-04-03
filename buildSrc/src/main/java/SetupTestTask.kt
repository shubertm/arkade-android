import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toDuration

abstract class SetupTestTask: DefaultTask() {

    @get:Inject
    abstract val execOps : ExecOperations

    @Internal
    val arkdExec = listOf("docker", "exec", "arkd")
    suspend fun setupArkServer() {
        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.quiet("Setting up Ark server")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        val walletStatus = checkWalletStatus()
        if (walletStatus.first && walletStatus.second && walletStatus.third) {
            logger.quiet(" ✔ Wallet ready and synced")
        } else {
            if (!walletStatus.first) {
                logger.quiet("Creating ark wallet...")

                execOps.exec {
                    val arkdExec = arkdExec.toMutableList()
                    arkdExec.addAll(listOf("arkd", "wallet", "create", "--password", "secret"))
                    commandLine(arkdExec)
                    standardOutput = ByteArrayOutputStream()
                }
                logger.quiet("✔ Wallet created")
            }
            if (!walletStatus.second) {
                logger.quiet("\nUnlocking ark wallet...")
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
            delay(retryDelay)
        }
        return Triple(false, false, false)
    }

    suspend fun waitForWalletReadiness(maxRetries: Int = 30, retryDelay: Long = 2000): Boolean {
        logger.quiet("\nWaiting for wallet to be ready and synced...")
        for (i in 0 .. maxRetries) {
            val status = checkWalletStatus(0)
            if (status.first && status.second && status.third) {
                logger.quiet(" ✔ Wallet ready and synced")
                return true
            }
            if (i < maxRetries - 1) {
                logger.quiet("Waiting... (${i + 1}/$maxRetries)")
                delay(retryDelay)
            }
        }
        throw Exception("wallet failed to be ready after maximum retries")
    }

    suspend fun waitForArkServer(maxRetries: Int = 30, retryDelay: Long = 2000): String {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(2))
            .uri(
                URI.create("http://localhost:7070/v1/info")
            ).build()

        logger.quiet("\nWaiting for ark server to be ready...")
        for (i in 0 .. maxRetries) {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.send(request, HttpResponse.BodyHandlers.ofString())
                }
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

            if (i < maxRetries - 1) {
                logger.quiet("Waiting... (${i + 1}/$maxRetries)")
                delay(retryDelay)
            }
        }
        throw Exception("ark server failed to be ready after maximum retries")
    }

    @TaskAction
    fun run() {
        runBlocking(Dispatchers.IO) {
            setupArkServer()
        }
    }
}