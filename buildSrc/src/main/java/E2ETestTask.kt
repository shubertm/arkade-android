import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class E2ETestTask: DefaultTask() {

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun run() {
        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.quiet("Running all e2e tests")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        val outputStream = ByteArrayOutputStream()

        val result = execOps.exec {
            workingDir = project.rootDir
            standardOutput = outputStream
            isIgnoreExitValue = true
            commandLine("./gradlew", "cleanJvmTest", "jvmTest", "--tests", "com.arkade.e2e.*", "--no-daemon")
        }

        if (result.exitValue != 0) {
            throw GradleException("E2E tests failed with exit code: ${result.exitValue}\n$outputStream")
        }

        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.quiet("✓ All e2e tests passed")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    }
}