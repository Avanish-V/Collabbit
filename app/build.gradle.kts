plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
}

android {

    namespace = "com.iota.campusX"

    // Android 16 / API 36
    compileSdk = 36


    lint {
        disable += "NullSafeMutableLiveData"
    }


    defaultConfig {
        applicationId = "com.iota.campusX"

        minSdk = 24
        targetSdk = 36

        // Play Store version
        versionCode = 30
        versionName = "1.3.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Only English resources
        resConfigs("en")
    }

    // ---------------------------------------------------------
    // SIGNING
    // ---------------------------------------------------------

    signingConfigs {

        // DEBUG
        getByName("debug") {
            storeFile = file("debug.keystore")
        }

        /*
         * PRODUCTION
         *
         * IMPORTANT:
         * Replace these with your actual production/upload keystore.
         *
         * Do NOT commit passwords to Git.
         *
         * If you already have Google Play App Signing configured,
         * make sure this is your registered UPLOAD KEY.
         */
        create("release") {

            storeFile = file("collabbit-release.jks")

            storePassword =
                System.getenv("KEYSTORE_PASSWORD")

            keyAlias =
                System.getenv("KEY_ALIAS")

            keyPassword =
                System.getenv("KEY_PASSWORD")
        }
    }

    // ---------------------------------------------------------
    // BUILD TYPES
    // ---------------------------------------------------------

    buildTypes {

        debug {
            signingConfig =
                signingConfigs.getByName("debug")
        }

        release {

            // Enable R8
            isMinifyEnabled = true

            // Remove unused resources
            isShrinkResources = true

            // Use production/upload key
           // signingConfig = signingConfigs.getByName("release")
            signingConfig = signingConfigs.getByName("debug")

            // R8 / ProGuard configuration
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    // ---------------------------------------------------------
    // JAVA
    // ---------------------------------------------------------

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ---------------------------------------------------------
    // KOTLIN / COMPOSE
    // ---------------------------------------------------------

    buildFeatures {
        compose = true
    }

    // ---------------------------------------------------------
    // PACKAGING
    // ---------------------------------------------------------

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

// ---------------------------------------------------------
// KOTLIN JVM
// ---------------------------------------------------------

kotlin {
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        )
    }
}

// ---------------------------------------------------------
// DEPENDENCIES
// ---------------------------------------------------------

dependencies {

    // =========================================================
    // ANDROID CORE
    // =========================================================

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.exifinterface)


    // =========================================================
    // JETPACK COMPOSE
    // =========================================================

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.runtime)

    implementation(libs.androidx.ui)

    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.material3)

    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.foundation)

    debugImplementation(libs.androidx.ui.tooling)

    debugImplementation(libs.androidx.ui.test.manifest)


    // =========================================================
    // NAVIGATION
    // =========================================================

    implementation(libs.androidx.navigation.compose)


    // =========================================================
    // FIREBASE
    // =========================================================

    implementation(libs.firebase.auth)

    implementation(libs.firebase.database)

    implementation(libs.firebase.firestore)

    implementation(libs.firebase.storage)

    implementation(libs.firebase.messaging)


    // =========================================================
    // GOOGLE AUTH
    // =========================================================

    implementation(libs.androidx.credentials)

    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.googleid)

    implementation(libs.play.services.auth)


    // =========================================================
    // KOIN
    // =========================================================

    implementation(libs.koin.androidx.compose)


    // =========================================================
    // KTOR
    // =========================================================

    implementation(libs.ktor.client.cio)

    implementation(libs.ktor.client.okhttp)

    implementation("io.ktor:ktor-client-websockets:2.3.12")

    implementation(libs.ktor.client.core)

    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.ktor.client.logging)


    // =========================================================
    // IMAGE LOADING
    // =========================================================

    implementation(libs.coil.compose)


    // =========================================================
    // MEDIA3
    // =========================================================

    implementation(libs.androidx.media3.exoplayer)

    implementation(libs.androidx.media3.ui)

    implementation(libs.androidx.media3.common)


    // =========================================================
    // LOTTIE
    // =========================================================

    implementation(libs.lottie.compose)


    // =========================================================
    // DATASTORE
    // =========================================================

    implementation(
        "androidx.datastore:datastore-preferences:1.0.0"
    )


    // =========================================================
    // ROOM
    // =========================================================

    implementation(libs.androidx.room.runtime)

    implementation(libs.androidx.room.ktx)

    implementation(
        "androidx.room:room-paging:2.7.2"
    )

    ksp(libs.androidx.room.compiler)


    // =========================================================
    // PAGING
    // =========================================================

    implementation(
        "androidx.paging:paging-runtime:3.3.4"
    )

    implementation(
        "androidx.paging:paging-compose:3.3.4"
    )


    // =========================================================
    // RICH LINK
    // =========================================================

    implementation(
        "io.github.alihaider63:richlinkpreview:1.0.0"
    )


    // =========================================================
    // IMAGE CROPPING
    // =========================================================

    implementation(
        "io.github.mr0xf00:easycrop:0.1.1"
    )


    // =========================================================
    // CODEC
    // =========================================================

    implementation(
        "commons-codec:commons-codec:1.9"
    )


    // =========================================================
    // GOOGLE PLAY
    // =========================================================

    implementation(
        "com.google.android.play:app-update:2.1.0"
    )


    // =========================================================
    // PROFILE / STARTUP OPTIMIZATION
    // =========================================================

    implementation(
        libs.androidx.profileinstaller
    )

    "baselineProfile"(
        project(":baselineprofile")
    )


    // =========================================================
    // LEAKCANARY
    // DEBUG ONLY
    // =========================================================

    debugImplementation(
        "com.squareup.leakcanary:leakcanary-android:2.13"
    )


    // =========================================================
    // TESTING
    // =========================================================

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.ui.test.junit4
    )
}