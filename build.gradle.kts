plugins {
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false

    id("com.android.application") version "9.3.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
}

// 只保留 clean 任务，不再有 allprojects { repositories { ... } }
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}