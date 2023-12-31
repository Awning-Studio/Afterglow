plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.awning.afterglow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.awning.afterglow"
        minSdk = 29
        targetSdk = 34
        versionCode = 8
        versionName = "0.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.0-beta01")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0-alpha06")

    // 导航
    implementation("androidx.navigation:navigation-compose:2.7.2")
    // html解析
    implementation("org.jsoup:jsoup:1.17.1")
    // 光学文字识别
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.5.0")
    // viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // 图标
    implementation("androidx.compose.material:material-icons-extended-android:1.5.1")
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    // pager
    implementation("com.google.accompanist:accompanist-pager:0.33.2-alpha")
    // DateStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // Volley
    implementation("com.android.volley:volley:1.2.1")
}