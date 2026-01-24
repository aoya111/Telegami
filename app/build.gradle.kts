import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
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
    namespace = "com.aoya.telegami"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aoya.telegami"
        minSdk = 24
        targetSdk = 35
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
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false

            buildConfigField("String", "MODULE_TAG", "\"TelegamiDebug\"")
            buildConfigField("boolean", "ENABLE_LOGS", "true")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = false

            buildConfigField("String", "MODULE_TAG", "\"Telegami\"")
            buildConfigField("boolean", "ENABLE_LOGS", "false")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.gson)

    compileOnly(libs.xposed.api)

    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

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
