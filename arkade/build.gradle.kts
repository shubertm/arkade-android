import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.ktlint.gradle)
}

val currentOs: org.gradle.internal.os.OperatingSystem =
    org.gradle.internal.os.OperatingSystem
        .current()

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
        minSdk = 26

        withHostTestBuilder {
        }

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
                        currentOs.isLinux -> libs.secp256k1.kmp.jni.jvm.linux
                        currentOs.isWindows -> libs.secp256k1.kmp.jni.jvm.windows
                        currentOs.isMacOsX -> libs.secp256k1.kmp.jni.jvm.macos
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

tasks.androidPreBuild.dependsOn("ktlintCheck")
tasks.getByName("compileKotlinJvm").dependsOn("ktlintCheck")
tasks.getByName("ktlintCheck").dependsOn("ktlintFormat")
