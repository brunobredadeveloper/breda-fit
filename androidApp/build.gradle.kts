plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "dev.breda.fit"
    compileSdk = 37
    defaultConfig { applicationId = "dev.breda.fit"; minSdk = 26; targetSdk = 37; versionCode = 5; versionName = "0.5.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { compose = true; resValues = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    buildTypes {
        debug { signingConfig = signingConfigs.getByName("debug"); applicationIdSuffix = ".debug"; versionNameSuffix = "-debug"; resValue("string", "app_name", "Breda Fit · avaliação") }
        create("preview") { initWith(getByName("debug")); applicationIdSuffix = ".preview05"; versionNameSuffix = "-preview"; resValue("string", "app_name", "Breda Fit Avatares 0.5"); matchingFallbacks += listOf("debug") }
        release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
    }
}
dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
