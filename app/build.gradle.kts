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
        abortOnError = false
        checkReleaseBuilds = false
    }


    defaultConfig {
        applicationId = "com.iota.campusX"

        minSdk = 24
        targetSdk = 36

        // Play Store version
        versionCode = 33
        versionName = "1.3.3"

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
        buildConfig = true
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"http://192.168.1.13:8080/api/v1/\"")
            buildConfigField("String", "OPPORTUNITIES_BASE_URL", "\"http://192.168.1.13:8081/api/jobs\"")
            buildConfigField("String", "COURSES_BASE_URL", "\"http://192.168.1.13:8081/api/courses\"")
            buildConfigField("Boolean", "IS_PRODUCTION", "false")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"https://iv52bugou5xppexnhffgj53hwq0rorrh.lambda-url.ap-south-1.on.aws/api/v1/\"")
            buildConfigField("String", "OPPORTUNITIES_BASE_URL", "\"https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/jobs\"")
            buildConfigField("String", "COURSES_BASE_URL", "\"https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/courses\"")
            buildConfigField("Boolean", "IS_PRODUCTION", "true")
        }
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

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.exifinterface)


    // =========================================================
    // JETPACK COMPOSE
    // =========================================================

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.runtime)

    implementation(libs.androidx.ui)

    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.material3)

    implementation(libs.androidx.material.icons.extended)

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

    implementation(libs.ktor.client.websockets)

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

    implementation(libs.androidx.datastore.preferences)


    // =========================================================
    // ROOM
    // =========================================================

    implementation(libs.androidx.room.runtime)

    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.room.paging)

    ksp(libs.androidx.room.compiler)


    // =========================================================
    // PAGING
    // =========================================================

    implementation(libs.androidx.paging.runtime)

    implementation(libs.androidx.paging.compose)


    // =========================================================
    // RICH LINK
    // =========================================================

    implementation(libs.richlinkpreview)


    // =========================================================
    // IMAGE CROPPING
    // =========================================================

    implementation(libs.easycrop)


    // =========================================================
    // CODEC
    // =========================================================

    implementation(libs.commons.codec)


    // =========================================================
    // GOOGLE PLAY
    // =========================================================

    implementation(libs.play.app.update)


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

    // Module Dependencies
    implementation(project(":feature:auth"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:collab"))
    implementation(project(":feature:explore_swipe"))
    implementation(project(":feature:feed"))
    implementation(project(":feature:notification"))
    implementation(project(":feature:opportunities"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:society"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:notifications"))

    debugImplementation(libs.leakcanary.android)


    // =========================================================
    // TESTING
    // =========================================================

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.ui.test.junit4
    )
}