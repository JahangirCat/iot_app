plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.edu.zime.tjh.iotapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.edu.zime.tjh.iotapp"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 配置 NDK 的 abiFilters方法
        ndk {
            //版本号和配置兼容
            //abiFilters += listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64")：使用 += 操作符添加 ABI 过滤器
            abiFilters += listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64")        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }




    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs") // 设置 jniLibs 的路径
        }
        getByName("debug") {
            setRoot("build-types/debug") // 设置 debug 的根目录
        }
        getByName("release") {
            setRoot("build-types/release") // 设置 release 的根目录
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("pub.devrel:easypermissions:3.0.0")

    implementation(libs.retrofit)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation(libs.core)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\AMap3DMap_10.1.200_AMapSearch_9.7.4_AMapLocation_6.4.9_20241226.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

