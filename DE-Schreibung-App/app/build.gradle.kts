plugins {
    alias(libs.plugins.android.application)
}

android {
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.deschreibung"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.deschreibung"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Default dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // For creating lists (RecyclerView)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // For Networking: Retrofit for API calls and Gson for JSON parsing
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Optional, but highly recommended for debugging network calls
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.activity)

    // Default test implementations
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}