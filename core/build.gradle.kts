plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinJvm)
    implementation(libs.kotlinSerialization)
    implementation(libs.okhttp)
    implementation(libs.websocket)
    implementation(libs.coroutinesCore)
    testImplementation(libs.junitJupiter)
}