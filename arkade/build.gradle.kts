import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.devtools.ksp.gradle.KspAATask
import org.gradle.kotlin.dsl.commonTest
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.square.wire)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
}

val currentOs: String = System.getProperty("os.name").lowercase()

wire {
    kotlin {}
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    androidLibrary {
        namespace = "com.arkade"
        compileSdk {
            version = release(36) { minorApiLevel = 1 }
        }
        minSdk = 24

        withHostTestBuilder {}

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "libKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.bitcoin.kmp)
                implementation(libs.secp256k1.kmp)
                implementation(libs.bignum)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.square.wire.runtime)
                implementation(libs.square.wire.grpc.client)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.room.sqlite.wrapper)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        jvmTest {
            dependencies {
                val targetDep =
                    when {
                        currentOs.contains("linux") -> libs.secp256k1.kmp.jni.jvm.linux
                        currentOs.contains("windows") -> libs.secp256k1.kmp.jni.jvm.windows
                        currentOs.contains("mac") || currentOs.contains("darwin") -> libs.secp256k1.kmp.jni.jvm.macos
                        else -> error("Unsupported OS: $currentOs")
                    }
                implementation(targetDep)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.secp256k1.kmp.jni.jvm)
                implementation(libs.androidx.test.core)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.junit)
            }
        }
    }
}

dependencies {
    add("kspJvm", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

tasks.register<SetupTestTask>("testSetup") {
    dependsOn("testUpDocker")
}

tasks.register<UpDockerTestTask>("testUpDocker")

tasks.register<DownDockerTestTask>("testDownDocker")

tasks.register<E2ETestTask>("testE2EDocker") {
    dependsOn("testSetup")
    finalizedBy("testDownDocker")
}

tasks.register<BuildDockerTestTask>("buildDocker")

tasks.register<UnitTestTask>("testUnit") {
    val test =
        project.extensions
            .getByType(SourceSetContainer::class.java)
            .getByName("jvmTest")
    testClassesDirs = test.output.classesDirs
    classpath = test.runtimeClasspath
}

tasks.getByName("ktlintCheck") {
    mustRunAfter(tasks.withType<KspAATask>())
}

// tasks.androidPreBuild.dependsOn("ktlintCheck")
// tasks.getByName("compileKotlinJvm").dependsOn("ktlintCheck")
