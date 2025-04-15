plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
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
    // Add Firebase BOM to manage dependency versions
    implementation(platform("com.google.firebase:firebase-bom:31.2.0"))  // Corrected syntax

    // Firebase libraries will be automatically assigned compatible versions
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // implementation("com.google.firebase:firebase-common") // Only if needed explicitly
    implementation("com.google.android.material:material:1.6.0")
    // Other dependencies
    implementation("androidx.appcompat:appcompat:1.3.1")  // Update this with actual version if needed
    implementation("com.google.android.material:material:1.4.0")  // Same for this
    implementation("androidx.activity:activity-ktx:1.2.4")  // Same for this
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")  // Same for this

    testImplementation("junit:junit:4.13.2")  // Corrected format for testImplementation
    androidTestImplementation("androidx.test.ext:junit:1.1.3")  // Corrected format
    androidTestImplementation("androidx.espresso:espresso-core:3.4.0")  // Corrected format
}
