plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("com.google.gms.google-services")
    id ("androidx.navigation.safeargs")
    id ("kotlin-parcelize")
    id ("kotlin-kapt")

}

android {
    namespace = "com.android.fitmoveai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.fitmoveai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\ml")
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.firebase:firebase-auth:22.3.0")

    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation ("com.google.android.gms:play-services-auth:20.1.0")

    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

    // Credential Manager
    implementation ("androidx.credentials:credentials:1.2.2")
    implementation ("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.0")

    implementation ("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.1.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.activity:activity-ktx:1.3.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.11.0")


    implementation ("androidx.camera:camera-camera2:1.1.0-beta02")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-beta02")
    implementation ("androidx.camera:camera-view:1.1.0-beta02")

    implementation ("androidx.camera:camera-core:1.1.0-alpha04")
    implementation ("androidx.camera:camera-camera2:1.1.0-alpha04")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Add the dependency for the Firebase ML model downloader library
    implementation ("com.google.firebase:firebase-ml-modeldownloader-ktx")

    // Also add the dependency for the TensorFlow Lite library and specify its version
    implementation ("org.tensorflow:tensorflow-lite:2.3.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.5.0")


    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.2")

    //Android Ktx
    implementation ("androidx.navigation:navigation-fragment-ktx:2.4.2")



}