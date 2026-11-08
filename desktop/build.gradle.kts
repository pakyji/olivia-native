plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
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
}

compose.desktop {
    application {
        mainClass = "net.kuafuai.andee.desktop.MainKt"
        nativeDistributions {
            dmg {
                packageName = "Olivia Native"
                packageVersion = "0.1.0"
            }
        }
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions.jvmTarget = "17"
}