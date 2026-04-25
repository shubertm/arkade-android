import org.gradle.api.tasks.testing.Test

abstract class UnitTestTask: Test() {
    init {
        filter {
            excludeTestsMatching("com.arkade.e2e.*")
            includeTestsMatching("*")
        }

        failFast = true

        testLogging.showStandardStreams = true

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
}