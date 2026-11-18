plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.soul_android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.soul_android"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 只保留真机 ARM ABI，去掉模拟器 x86/x86_64，显著减小 APK 体积
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }

        // 后端地址可配置：
        //   模拟器默认 http://10.0.2.2:8080/
        //   真机打包： gradlew assembleRelease -PapiBaseUrl=http://<电脑IP>:8080/
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${project.findProperty("apiBaseUrl") ?: "http://10.0.2.2:8080/"}\""
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("../soul-release.keystore")
            storePassword = "soul123456"
            keyAlias = "soul"
            keyPassword = "soul123456"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        debug {
            ndk {
                abiFilters.clear()
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
            }
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Haze: real backdrop blur / frosted glass
    implementation(libs.haze)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Agora RTC（语音/视频通话）
    implementation(libs.agora.rtc)

    // Testing
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
