import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

fun String.escapeForBuildConfig(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}

fun loadLocalProperty(name: String): String {
    val localPropertiesFile = rootProject.file("local.properties")
    if (!localPropertiesFile.exists()) return System.getenv(name).orEmpty()

    val localProperties = Properties().apply {
        localPropertiesFile.inputStream().use { load(it) }
    }

    return localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name).orEmpty()
}

android {
    namespace = "com.example.whisperwall"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.whisperwall"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/\"")
        buildConfigField("String", "SUPABASE_URL", "\"${loadLocalProperty("SUPABASE_URL").escapeForBuildConfig()}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${loadLocalProperty("SUPABASE_ANON_KEY").escapeForBuildConfig()}\"")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}