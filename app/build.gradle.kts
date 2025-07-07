plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(jimmers.plugins.ksp) version "${libs.versions.kotlin.get()}+"
    alias(jimmers.plugins.jimmer)
}

android {
    namespace = "cn.enaium.chat"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.enaium.chat"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "META-INF/license/**"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/native-image/io.netty/**"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    patch(jimmers.sqlKotlin) {
        exclude(module = "kotlin-stdlib")
        exclude(module = "annotations")
        exclude(module = "validation-api")
    }
    patchKsp(jimmers.ksp)
    implementation(libs.sqlite)
    implementation(libs.netty)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kotlin {
    jvmToolchain(11)
}

jimmer {
    patch {
        enable = true
    }
}