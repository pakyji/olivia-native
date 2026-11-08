plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}

android {
    namespace = "net.kuafuai.andee"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.kuafuai.andee"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // Core module
    implementation(project(":core"))

    // Android Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.coreKtx)
    implementation(libs.activity)

    // Room - using KSP instead of KAPT
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    implementation(libs.roomCompiler)

    // Network
    implementation(libs.okhttp)
    implementation(libs.websocket)
    implementation(libs.coroutines)

    // Camera + ML Kit
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation(libs.mlkitBarcode)
    implementation("com.google.zxing:core:3.5.3")

    // USB Serial
    implementation(libs.usbSerial)

    // JSON for unit tests
    testImplementation("org.json:json:20240303")

    // Testing
    testImplementation(libs.junitJupiter)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidxTestRunner)
    androidTestImplementation(libs.androidxTestRules)
    androidTestImplementation(libs.androidxTestCore)
    androidTestImplementation(libs.androidxTestExt)
    androidTestImplementation(libs.espressoCore)
    // androidTestImplementation(libs.truth)

    // Force resolution for duplicate IntelliJ annotations classes
    implementation("org.jetbrains:annotations:23.0.0") {
        isTransitive = false
    }
}