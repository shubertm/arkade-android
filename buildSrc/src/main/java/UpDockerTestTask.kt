import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class UpDockerTestTask: DefaultTask() {
    @get:Inject
    abstract val execOps : ExecOperations

    @TaskAction
    fun run() {
        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.quiet("Setting up docker containers")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        execOps.exec {
            workingDir = project.rootDir
            standardOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
            commandLine("docker", "compose", "-f", "docker-compose.yml", "up", "-d")
        }

        logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.quiet("✓ All docker containers ready")
        logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    }
}