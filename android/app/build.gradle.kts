import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Apply the Google Services plugin only when google-services.json is present so the
// project still builds before Firebase is configured. Push notifications activate
// once app/google-services.json (from the Firebase console) is added.
val googleServicesJson = project.file("google-services.json")
if (googleServicesJson.exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val martApiBaseUrl: String = run {
    val f = rootProject.file("local.properties")
    if (!f.exists()) return@run "http://10.0.2.2:3000"
    val fromFile =
        f.inputStream().use { stream ->
            Properties().apply { load(stream) }.getProperty("mart.api.base.url")?.trim()
        }
    fromFile.takeUnless { it.isNullOrEmpty() } ?: "http://10.0.2.2:3000"
}

val martApiBaseUrlRelease: String = run {
    val f = rootProject.file("local.properties")
    val defaultRelease = "https://mart-api-m7f3o3ktba-el.a.run.app"
    if (!f.exists()) return@run defaultRelease
    val props = f.inputStream().use { stream -> Properties().apply { load(stream) } }
    val releaseUrl = props.getProperty("mart.api.base.url.release")?.trim()
    if (!releaseUrl.isNullOrEmpty()) return@run releaseUrl
    // Safety: avoid shipping emulator-only URL in release.
    val commonUrl = props.getProperty("mart.api.base.url")?.trim()
    if (!commonUrl.isNullOrEmpty() && !commonUrl.contains("10.0.2.2")) {
        return@run commonUrl
    }
    defaultRelease
}

/** Production release: false unless local.properties sets mart.use.local.demo.auth=true */
val martUseLocalDemoAuth: Boolean = run {
    val f = rootProject.file("local.properties")
    if (!f.exists()) return@run false
    f.inputStream().use { stream ->
        Properties().apply { load(stream) }.getProperty("mart.use.local.demo.auth")?.trim()
    }.equals("true", ignoreCase = true)
}

/** Production release: false unless local.properties sets mart.demo.mode=true */
val martDemoMode: Boolean = run {
    val f = rootProject.file("local.properties")
    if (!f.exists()) return@run false
    f.inputStream().use { stream ->
        Properties().apply { load(stream) }.getProperty("mart.demo.mode")?.trim()
    }.equals("true", ignoreCase = true)
}

val keystoreProperties: Properties =
    Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) {
            f.inputStream().use { load(it) }
        }
    }

android {
    namespace = "com.mart.distribution.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.knsrmart.flashmart"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
        // Emulator: default. Real device / client APK: set mart.api.base.url in local.properties (no trailing slash).
        buildConfigField("String", "API_BASE_URL", "\"$martApiBaseUrl\"")
        // Client investor APK: mart.use.local.demo.auth=true → email + Password@123, no server (any network).
        buildConfigField("boolean", "USE_LOCAL_DEMO_AUTH", "$martUseLocalDemoAuth")
        buildConfigField("boolean", "DEMO_MODE", "$martDemoMode")
        buildConfigField("boolean", "SHOW_BACKEND_URL", "false")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$martApiBaseUrl\"")
            buildConfigField("boolean", "REQUIRE_ONBOARDING_DOCUMENTS", "false")
            buildConfigField("boolean", "SHOW_BACKEND_URL", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"$martApiBaseUrlRelease\"")
            buildConfigField("boolean", "REQUIRE_ONBOARDING_DOCUMENTS", "true")
            buildConfigField("boolean", "SHOW_BACKEND_URL", "false")
            val releaseKeystore = keystoreProperties.getProperty("storeFile")
            signingConfig =
                if (!releaseKeystore.isNullOrBlank() && rootProject.file(releaseKeystore).exists()) {
                    signingConfigs.getByName("release")
                } else {
                    println("WARNING: keystore.properties missing — release signed with debug key (not for Play Store).")
                    signingConfigs.getByName("debug")
                }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    val composeBom = platform("androidx.compose:compose-bom:2024.11.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.razorpay:checkout:1.6.41")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
