import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.square.wire)
}

val currentOs: String = System.getProperty("os.name").lowercase()

wire {
    kotlin {}
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
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
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
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }
    }
}

tasks.register<SetupTestTask>("testSetup") {
    dependsOn("testUpDocker")
    finalizedBy("testDownDocker")
}

tasks.register<UpDockerTestTask>("testUpDocker")

tasks.register<DownDockerTestTask>("testDownDocker")

tasks.androidPreBuild.dependsOn("ktlintCheck")
tasks.getByName("compileKotlinJvm").dependsOn("ktlintCheck")
