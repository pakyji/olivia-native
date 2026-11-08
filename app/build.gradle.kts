plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose.compiler)
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
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packagingOptions {
        resources.excludes += "/META-INF/*.kotlin_module"
    }
}

dependencies {
    // Core module
    implementation(project(":core"))

    // Android Core
    implementation(libs.coreKtx)
    implementation(libs.activity)
    implementation(libs.activityCompose)
    implementation(libs.lifecycleRuntime)
    implementation(libs.lifecycleViewModel)

    // Compose
    implementation(platform(libs.composeBom))
    implementation(libs.ui)
    implementation(libs.uiGraphics)
    implementation(libs.material3)
    implementation(libs.materialIcons)

    // Room
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    kapt(libs.roomCompiler)

    // Network
    implementation(libs.okhttp)
    implementation(libs.websocket)
    implementation(libs.coroutines)

    // ML Kit
    implementation(libs.mlkitBarcode)

    // USB Serial
    implementation(libs.usbSerial)

    // Testing
    testImplementation(libs.junitJupiter)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidxTestRunner)
    androidTestImplementation(libs.androidxTestRules)
    androidTestImplementation(libs.androidxTestCore)
    androidTestImplementation(libs.androidxTestExt)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.uiTooling)
    debugImplementation(libs.uiToolingPreview)
}