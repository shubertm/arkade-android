import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class UnitTestTask: Test() {
    @get:Inject
    abstract val execOps: ExecOperations

    init {
        filter {
            excludeTestsMatching("com.arkade.e2e.*")
            includeTestsMatching("*")
        }
    }

    @TaskAction
    fun run() {
        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.quiet("Running all unit tests")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        val outputStream = ByteArrayOutputStream()
        val result = execOps.exec {
            workingDir = project.rootDir
            standardOutput = outputStream
            isIgnoreExitValue = true
            commandLine("./gradlew", ":arkade:cleanJvmTest", ":arkade:jvmTest", "--no-daemon")
        }

        if (result.exitValue != 0) {
            throw GradleException("Unit tests failed with exit code: ${result.exitValue}\n$outputStream")
        }

        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.quiet("✓ All unit tests passed")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    }
}