plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.simplecalc.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.simplecalc.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Not shrinking by default: this app is intentionally tiny and
            // simple, so ProGuard/R8 shrinking is unnecessary complexity.
            // Flip to `true` and see proguard-rules.pro if you extend the app.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
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
        viewBinding = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = false
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Minimal, lightweight dependency set — no networking, database, DI, or
    // image-loading libraries are needed for an offline arithmetic calculator.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Unit tests (pure JVM, no device/emulator needed) — see CalculatorEngineTest.
    testImplementation("junit:junit:4.13.2")

    // Instrumented tests (run on a device/emulator) — basic UI smoke test.
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
