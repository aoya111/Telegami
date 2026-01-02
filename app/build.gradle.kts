import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleKsp)
}

val versionProps =
    Properties().apply {
        rootProject.file("version.properties").inputStream().use { load(it) }
    }

val keystoreProps =
    Properties().apply {
        rootProject.file("keystore.properties").inputStream().use { load(it) }
    }

android {
    namespace = "com.aoya.televip"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aoya.televip"
        minSdk = 24
        targetSdk = 34
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile") as String)
            storePassword = keystoreProps.getProperty("storePassword") as String
            keyAlias = keystoreProps.getProperty("keyAlias") as String
            keyPassword = keystoreProps.getProperty("keyPassword") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("de.robv.android.xposed:api:82")

    val roomVersion = "2.7.1"

    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register("bumpVersion") {
    val bumpType = project.findProperty("type") ?: "patch"

    doLast {
        val major = versionProps.getProperty("major")!!.toInt()
        val minor = versionProps.getProperty("minor")!!.toInt()
        val patch = versionProps.getProperty("patch")!!.toInt()

        val (newMajor, newMinor, newPatch) =
            when (bumpType) {
                "major" -> Triple(major + 1, 0, 0)
                "minor" -> Triple(major, minor + 1, 0)
                else -> Triple(major, minor, patch + 1)
            }

        val newVersion = "$newMajor.$newMinor.$newPatch"

        val newCode =
            if (bumpType == "major") {
                newMajor * 10000
            } else {
                versionProps.getProperty("versionCode")!!.toInt() + 1
            }

        versionProps["major"] = newMajor.toString()
        versionProps["minor"] = newMinor.toString()
        versionProps["patch"] = newPatch.toString()
        versionProps["versionName"] = newVersion
        versionProps["versionCode"] = newCode.toString()

        rootProject.file("version.properties").outputStream().use {
            versionProps.store(it, "Bumped $bumpType → $newVersion")
        }
        println("✅ $bumpType → $newVersion (code: $newCode)")
    }
}
