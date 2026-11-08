import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

// ফিক্সড ডিবাগ সিগনেচার: রিপোতে থাকা debug.keystore.b64 থেকে একই
// keystore বানিয়ে সব বিল্ড একই সিগনেচারে সই হবে। পাসওয়ার্ড "android"
// হলো Android-এর সবার জানা ডিফল্ট ডিবাগ পাসওয়ার্ড, এটা কোনো আসল
// প্রাইভেট কী নয়।
val debugKeystoreFile = file("debug.keystore")
val debugKeystoreB64 = file("debug.keystore.b64")
if (debugKeystoreB64.exists()) {
    debugKeystoreFile.writeBytes(Base64.getDecoder().decode(debugKeystoreB64.readText().trim()))
}

android {
    namespace = "com.aybay.app"
    compileSdk = 34

    signingConfigs {
        getByName("debug") {
            storeFile = debugKeystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeType = "PKCS12"
        }
    }

    defaultConfig {
        applicationId = "com.aybay.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 14
        versionName = "1.13"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // লাইভ সিঙ্ক (Firebase): google-services প্লাগইন ব্যবহার করা হয়নি,
    // SyncConfig-এ Firebase প্রজেক্টের তথ্য বসিয়ে হাতে Firebase চালু করা হয়।
    // তথ্য খালি থাকলে অ্যাপ অফলাইন-অনলি চলে, সিঙ্ক বন্ধ থাকে।
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
