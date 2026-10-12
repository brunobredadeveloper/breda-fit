plugins { kotlin("multiplatform"); kotlin("plugin.serialization"); id("com.android.kotlin.multiplatform.library") }
kotlin {
    android { namespace = "dev.breda.fit.shared"; compileSdk = 37; minSdk = 26; withHostTest {} }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    jvmToolchain(17)
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        commonTest.dependencies { implementation(kotlin("test")); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2") }
    }
}
