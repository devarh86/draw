plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.ads"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "splash_app_open_low", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "resume_app_open_low", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "splash_banner_low", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "banner_overall_low", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "native_exit_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "processing_native_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_language_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "native_language_select_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "on_boarding_one_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "on_boarding_two_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "on_boarding_three_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "full_native_one_low", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "full_native_two_low", "ca-app-pub-3940256099942544/2247696110")

            resValue("string", "rewarded_low", "ca-app-pub-3940256099942544/5224354917")

            resValue("string", "language_inter_low", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "all_inter_low", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "started_inter_low", "ca-app-pub-3940256099942544/1033173712")


        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "app_id", "")

            resValue("string", "splash_app_open_low", "")
            resValue("string", "resume_app_open_low", "")

            resValue("string", "splash_banner_low", "")

            resValue("string", "banner_overall_low", "")

            resValue("string", "native_exit_low", "")
            resValue("string", "processing_native_low", "")
            resValue("string", "native_language_low", "")
            resValue("string", "native_language_select_low", "")
            resValue("string", "on_boarding_one_low", "")
            resValue("string", "on_boarding_two_low", "")
            resValue("string", "on_boarding_three_low", "")
            resValue("string", "full_native_one_low", "")
            resValue("string", "full_native_two_low", "")

            resValue("string", "rewarded_low", "")

            resValue("string", "language_inter_low", "")
            resValue("string", "all_inter_low", "")
            resValue("string", "started_inter_low", "")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // admob
    api("com.google.android.gms:play-services-ads:24.6.0")
    api("com.google.android.ump:user-messaging-platform:3.2.0")

    //shimmer
    api("com.facebook.shimmer:shimmer:0.5.0")
    // analytics
    api(project(":analytics"))
    api(project(":common"))
    // sdp and ssp\
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    // lottie animation
    implementation("com.airbnb.android:lottie:6.3.0")
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // hilt DI
    implementation("com.google.dagger:hilt-android:2.56.1")
    kapt("com.google.dagger:hilt-compiler:2.56.1")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    // glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    // google in app billing
    api(project(":inapp"))

    // firebase
    api(platform("com.google.firebase:firebase-bom:33.12.0"))
    api("com.google.firebase:firebase-analytics-ktx")
    api("com.google.firebase:firebase-config")
    api("com.google.firebase:firebase-analytics")

    api("com.google.firebase:firebase-crashlytics-ktx") {
        exclude(group = "androidx.datastore", module = "datastore-preferences")
    }

    // LifeCycles ViewModel,LiveData,Runtime and Process
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}