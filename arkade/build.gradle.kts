import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
}

val currentOs: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()

kotlin {

    jvm()

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
                val targetDep = when  {
                    currentOs.isLinux -> libs.secp256k1.kmp.jni.jvm.linux
                    currentOs.isWindows -> libs.secp256k1.kmp.jni.jvm.windows
                    currentOs.isMacOsX -> libs.secp256k1.kmp.jni.jvm.macos
                    else -> error("Unsupported OS: $currentOs")
                }
                implementation(targetDep)
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