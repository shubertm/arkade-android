import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByName

abstract class UnitTestTask: Test() {
    init {
        val jvmTestTask = project.tasks.getByName<Test>("jvmTest")
        jvmTestTask.excludeE2ETests()

        val androidTestTask = project.tasks.getByName<Test>("testAndroidHostTest")
        androidTestTask.excludeE2ETests()

        dependsOn(jvmTestTask)
        dependsOn(androidTestTask)

        doFirst {
            logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.quiet("Running all unit tests...")
            logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }

        doLast {
            logger.quiet("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.quiet("✓ All unit tests passed")
            logger.quiet("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        }
    }

    fun Test.excludeE2ETests() {
        filter {
            excludeTestsMatching("com.arkade.e2e.*")
            includeTestsMatching("*")
        }
    }
}