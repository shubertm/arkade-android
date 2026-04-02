import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class UpDockerTestTask: DefaultTask() {
    @get:Inject
    abstract val execOps : ExecOperations

    @TaskAction
    fun run() {
        execOps.exec {
            workingDir = project.rootDir
            commandLine("docker", "compose", "-f", "docker-compose.yml", "up", "-d")
        }
    }
}