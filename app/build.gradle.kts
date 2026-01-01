plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
}


android {
    signingConfigs {
        create("release") {
            storeFile = file("D:\\SHA Keys\\CampusX_new_key")
            storePassword = "iotabuild"
            keyPassword = "iotabuild"
            keyAlias = "key0"
        }
    }

    
    namespace = "com.iota.campusX"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iota.campusX"
        minSdk = 24
        targetSdk = 35
        versionCode = 22
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "env"
        }
    }



    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//            signingConfig = signingConfigs.getByName("release")
//        }

        getByName("debug") {
            isShrinkResources = false
            isMinifyEnabled = false  // Enable ProGuard in debug mode
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


//    kotlinOptions {
//        jvmTarget = "17"
//    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        )
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.coil.compose)
    implementation (libs.koin.androidx.compose)

    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation (libs.ktor.client.content.negotiation)
    implementation (libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    //implementation(libs.kotlinx.serialization.json)

    implementation (libs.play.services.auth)

    implementation(libs.lottie.compose)

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
   // implementation("com.cloudinary:cloudinary-http44:1.29.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation ("ch.qos.logback:logback-classic:1.2.11")

    implementation("com.google.auth:google-auth-library-oauth2-http:1.2.2")


    implementation("io.agora.rtc:voice-sdk:4.5.0")
    implementation("commons-codec:commons-codec:1.9")
    implementation ("com.airbnb.android:lottie-compose:6.6.6")

    implementation("com.google.android.play:app-update:2.1.0")

    implementation("io.github.mr0xf00:easycrop:0.1.1")

    implementation("androidx.paging:paging-runtime:3.3.4")
    implementation("androidx.paging:paging-compose:3.3.4")

    implementation("io.github.alihaider63:richlinkpreview:1.0.0")
    //implementation ("org.jsoup:jsoup:1.12.1")

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.13")

}