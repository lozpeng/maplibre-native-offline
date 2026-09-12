plugins {
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.android.application")
}

android {
    compileSdk = 37
    namespace = "org.cwcc.ani.map"

    defaultConfig {
        applicationId = "org.cwcc.ani.map"
        minSdk = 23
        targetSdk = 37
        versionCode = 14
        versionName = "6.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
                "LICENSE.txt"
            )
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // 已修改
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // 已修改
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        baseline = file("lint-baseline-local.xml")
        checkAllWarnings = true
        disable += setOf(
            "MissingTranslation",
            "GoogleAppIndexingWarning",
            "UnpackedNativeCode",
            "IconDipSize",
            "TypographyQuotes"
        )
        warningsAsErrors = true
    }
}

kotlin {
    compilerOptions {
        // 注意：使用 JvmTarget 枚举，而不是字符串
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":Maplibre"))

    implementation(libs.maplibre.geojson)
    implementation(libs.maplibre.gestures)
    implementation(libs.maplibre.turf)

    implementation(libs.support.annotations)
    implementation(libs.support.fragment)
    implementation(libs.support.recyclerview)
    implementation(libs.support.constraintlayout)
    implementation(libs.support.interpolator)
    implementation(libs.support.design)
    implementation(libs.support.print)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.timber)
    implementation(libs.okhttp3)
    //implementation(libs.sentry.android)
    implementation(libs.commons.io)
    debugImplementation(libs.leak.canary)

    implementation(libs.android.server.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.assertjcore)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.test.uiautomator)
}