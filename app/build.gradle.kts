plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myfirstandroidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myfirstandroidapp"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    //RecyclerView & Glide
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    //Google Calendar
    implementation ("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0"){
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.api-client:google-api-client-android:1.23.0"){
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    //Google Credentials API
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation(project(":chartmodule"))
//    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar", "*.aar"))))

}