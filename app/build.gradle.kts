plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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
    //RecyclerView & Glide
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    //Google Calendar
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0")
    implementation ("com.google.android.gms:play-services-auth:20.4.0")
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.api-client:google-api-client-android:1.23.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation ("pub.devrel:easypermissions:3.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation(project(":chartmodule"))
//    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar", "*.aar"))))

}