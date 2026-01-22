import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.chonkcheck.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chonkcheck.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AUTH0_DOMAIN", "\"${localProperties.getProperty("AUTH0_DOMAIN", "")}\"")
        buildConfigField("String", "AUTH0_CLIENT_ID", "\"${localProperties.getProperty("AUTH0_CLIENT_ID", "")}\"")
        buildConfigField("String", "AUTH0_AUDIENCE", "\"${localProperties.getProperty("AUTH0_AUDIENCE", "")}\"")
        buildConfigField("String", "API_URL", "\"${localProperties.getProperty("API_URL", "https://api.chonkcheck.com")}\"")

        manifestPlaceholders["auth0Domain"] = localProperties.getProperty("AUTH0_DOMAIN", "")
        manifestPlaceholders["auth0Scheme"] = "com.chonkcheck.android"
        manifestPlaceholders["sentryDsn"] = localProperties.getProperty("SENTRY_DSN", "")
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // AndroidX Core
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Networking
    implementation(libs.bundles.networking)

    // Auth0
    implementation(libs.auth0)

    // ML Kit
    implementation(libs.mlkit.barcode)
    implementation(libs.mlkit.code.scanner)

    // CameraX
    implementation(libs.bundles.camerax)

    // Billing
    implementation(libs.billing)

    // Analytics
    implementation(libs.sentry)

    // Charts
    implementation(libs.vico.compose)

    // Image loading
    implementation(libs.coil.compose)

    // Work Manager
    implementation(libs.work.runtime)

    // DataStore
    implementation(libs.datastore)

    // Security
    implementation(libs.security.crypto)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
