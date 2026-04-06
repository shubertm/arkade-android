import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class BuildDockerTestTask: DefaultTask() {

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun run() {
        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.quiet("Building docker containers")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        val result = execOps.exec {
            workingDir = project.rootDir
            standardOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
            commandLine("docker", "compose", "-f", "docker-compose.yml", "build", "--no-cache")
        }

        if (result.exitValue != 0) {
            throw Exception("Docker build failed with exit code: ${result.exitValue}")
        }

        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.quiet("✓ Finished building docker containers")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    }
}