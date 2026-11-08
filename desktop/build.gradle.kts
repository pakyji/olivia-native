plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
    id("org.jetbrains.compose") version "1.5.4" apply false
}

group = "net.kuafuai.andee"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(project(":core"))
    implementation(platform(libs.composeBom))
    implementation(libs.ui)
    implementation(libs.uiGraphics)
    implementation(libs.material3)
    implementation(libs.materialIcons)
    implementation(libs.okhttp)
    implementation(libs.websocket)
    implementation(libs.coroutinesCore)
    implementation(libs.kotlinJvm)
    implementation(libs.kotlinSerialization)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions.jvmTarget = "17"
}