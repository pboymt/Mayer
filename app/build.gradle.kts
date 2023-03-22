import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
}

fun getVersionName(): String {
    val p = Properties()
    val versionFile = project.file("version.properties")
    p.load(FileInputStream(versionFile))
    val major = p.getProperty("version.major")
    val minor = p.getProperty("version.minor")
    val patch = p.getProperty("version.patch")
    return "$major.$minor.$patch"
}

fun getVersionCode(): Int {
    val p = Properties()
    val versionFile = project.file("version.properties")
    p.load(FileInputStream(versionFile))
    return p.getProperty("version.code").toInt()
}


android {
    namespace = "icu.pboymt.mayer"
    compileSdk = 33

    val vName = getVersionName()
    val vCode = getVersionCode()

    defaultConfig {
        applicationId = "icu.pboymt.mayer"
        minSdk = rootProject.extra["defaultMinSdkVersion"] as Int
        targetSdk = 33
        versionCode = vCode
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }

    // Output version info
    println("Version name: $vName")
//    println(System.getenv("KEYSTORE_PASSWORD").length)
//    println(System.getenv("KEY_ALIAS").length)
//    println(System.getenv("KEY_PASSWORD").length)

//    signingConfigs {
//        release {
//            storeFile = project.file("keystore/release.jks")
//            storePassword =
//            keyAlias =
//            keyPassword =
//        }
//    }
    val signConfig = signingConfigs.create("release") {
        storeFile = File(projectDir.path + "/keystore/release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signConfig

        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
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
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    externalNativeBuild {
        cmake {
            path = project.file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

}

dependencies {

    // 主要库
    implementation("androidx.core:core-ktx:1.9.0")
    //noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("androidx.savedstate:savedstate-ktx:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.compose.foundation:foundation:1.3.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // 第三方 UI 控件库
    implementation("com.github.alorma:compose-settings-ui-m3:0.25.0")
    implementation("com.github.alorma:compose-settings-storage-datastore:0.25.0")
    // 日志
    implementation("org.tinylog:tinylog-api-kotlin:2.6.1")
    implementation("org.tinylog:tinylog-impl:2.6.1")
    // 图像识别
    implementation(project(":opencv"))
    // OCR
    implementation("cz.adaptech.tesseract4android:tesseract4android-openmp:4.3.0")
    // 测试库
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${rootProject.extra["compose_version"]}")
    implementation(kotlin("reflect", "1.7.10"))

//    implementation project(":opencv")
}

// Create a task, which will watch assets folder and list all files to a kotlin file
// This file will be used to access assets from the app
tasks.register("listAssets") {
    doLast {
        val assetsDir = project.file("src/main/assets/templates")
        val assetsFile = project.file("src/main/java/icu/pboymt/mayer/assets/Tpls.kt")
        val assetsList = assetsDir.listFiles()?.map { it.name } ?: emptyList()
        assetsFile.writeText("package icu.pboymt.mayer.assets\n\n")
        assetsFile.appendText("import androidx.annotation.Keep\n\n")
        assetsFile.appendText("@Suppress(\"unused\", \"SpellCheckingInspection\")\n")
        assetsFile.appendText("@Keep\n")
        assetsFile.appendText("object Tpls {\n")
        assetsList.forEach {
            val key = it.replace(".png", "").replace("-", "_").toUpperCase()
            assetsFile.appendText("    const val $key = \"templates/$it\"\n")
        }
        assetsFile.appendText("}\n")
    }
}

tasks.register("outputVersionInfo") {
    doLast {
        val versionProps = Properties()
        val versionFile = project.file("version.properties")
        versionProps.load(FileInputStream(versionFile))
        // Concat major, minor and patch version
        val versionMajor = versionProps.getProperty("version.major")
        val versionMinor = versionProps.getProperty("version.minor")
        val versionPatch = versionProps.getProperty("version.patch")
        val versionName = "$versionMajor.$versionMinor.$versionPatch"
        // Load version code from version.properties
        val versionCode = versionProps.getProperty("version.code")
        // Print them to console
        println("Version name: $versionName")
        println("Version code: $versionCode")
    }
}

tasks.register("incrementVersionMajor") {
    doLast {
        val versionProps = Properties()
        val versionFile = project.file("version.properties")
        versionProps.load(FileInputStream(versionFile))
        // Increment major version
        versionProps["version.major"] =
            (versionProps.getProperty("version.major").toInt() + 1).toString()
        // Reset minor and patch version
        versionProps["version.minor"] = "0"
        versionProps["version.patch"] = "0"
        // Increment version code
        versionProps["version.code"] =
            (versionProps.getProperty("version.code").toInt() + 1).toString()
        // Save version.properties
        versionFile.outputStream().use { versionProps.store(it, null) }
    }
}

tasks.register("incrementVersionMinor") {
    doLast {
        val versionProps = Properties()
        val versionFile = project.file("version.properties")
        versionProps.load(FileInputStream(versionFile))
        // Increment minor version
        versionProps["version.minor"] =
            (versionProps.getProperty("version.minor").toInt() + 1).toString()
        // Reset patch version
        versionProps["version.patch"] = "0"
        // Increment version code
        versionProps["version.code"] =
            (versionProps.getProperty("version.code").toInt() + 1).toString()
        // Save version.properties
        versionFile.outputStream().use { versionProps.store(it, null) }
    }
}

tasks.register("incrementVersionPatch") {
    doLast {
        val versionProps = Properties()
        val versionFile = project.file("version.properties")
        versionProps.load(FileInputStream(versionFile))
        // Increment patch version
        versionProps["version.patch"] =
            (versionProps.getProperty("version.patch").toInt() + 1).toString()
        // Increment version code
        versionProps["version.code"] =
            (versionProps.getProperty("version.code").toInt() + 1).toString()
        // Save version.properties
        versionFile.outputStream().use { versionProps.store(it, null) }
    }
}