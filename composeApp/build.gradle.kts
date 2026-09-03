import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.ExperimentalComposeLibrary
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun projectPropertyOrLocal(name: String, defaultValue: String): String =
    providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: defaultValue

fun projectPropertyLocalOrEnv(name: String): String? =
    providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: providers.environmentVariable(name).orNull

fun releaseFile(path: String): File =
    File(path).let { if (it.isAbsolute) it else rootProject.file(path) }

val releaseKeystorePath = projectPropertyLocalOrEnv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword = projectPropertyLocalOrEnv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = projectPropertyLocalOrEnv("ANDROID_KEY_ALIAS")
val releaseKeyPassword = projectPropertyLocalOrEnv("ANDROID_KEY_PASSWORD")
val androidVersionCode = projectPropertyLocalOrEnv("ANDROID_VERSION_CODE")
    ?.toIntOrNull()
    ?: 2
val androidVersionName = projectPropertyLocalOrEnv("ANDROID_VERSION_NAME")
    ?: "1.0.2"
val hasReleaseSigning = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.concurrent.futures)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.ktor.client.okhttp)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.messaging)
            implementation(libs.google.play.services.auth)
            implementation(libs.guava)
            implementation(libs.mlkit.text.recognition)
        }
        androidInstrumentedTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.androidx.test.ext.junit)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTestJUnit4)
        }
        matching { it.name.lowercase().startsWith("ios") }.configureEach {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        commonMain.dependencies {
            implementation(project(":design-system"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.purchases.core)
            implementation(project(":observability"))
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.fxalways.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.fxalways.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        versionCode = androidVersionCode
        versionName = androidVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val backendUrl = projectPropertyOrLocal("FX_BACKEND_URL", "https://us-central1-fx-always.cloudfunctions.net")
        val revenueCatKey = projectPropertyOrLocal("REVENUECAT_API_KEY", projectPropertyOrLocal("REVENUECAT_ANDROID_KEY", ""))
        buildConfigField("String", "FX_BACKEND_URL", "\"$backendUrl\"")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatKey\"")
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = releaseFile(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    debugImplementation(libs.compose.ui.test.manifest)
}
