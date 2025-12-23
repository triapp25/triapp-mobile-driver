import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.native.cocoapods")
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ktorfit {
    generateQualifiedTypeName = true
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Shared module for TriApp"
        homepage = "https://github.com/yourorg/TriAppDriver"
        ios.deploymentTarget = "16.0"

        framework {
            baseName = "ComposeApp"
            isStatic = false
        }

        pod("FirebaseCore") {
            version = "~> 12.0.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseAuth") {
            version = "~> 12.0.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseFirestore") {
            version = "~> 12.0.0"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    //listOf(
    //    iosX64(),
    //    iosArm64(),
    //    iosSimulatorArm64()
    //).forEach { iosTarget ->
    //    iosTarget.binaries.framework {
    //        baseName = "ComposeApp"
    //        isStatic = true
    //    }
    //}

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.firebase.auth)
            implementation(libs.google.firebase.crashlytics)
            implementation(libs.google.firebase.messaging)
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.play.services.location)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.android.ndk27)
            implementation(libs.maps.compose.ndk27)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)

            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.koin.compose.viewmodel.nav)
            implementation(libs.kamel.image)

            // Ktorfit Library
            implementation(libs.ktorfit.lib)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            implementation(libs.multiplatform.settings.core)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.permissions.location)
            implementation(libs.permissions.v0201)
            implementation(libs.permissions.compose)
            implementation(libs.geo)
            implementation(libs.geo.compose)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.permissions)
            // implementation(libs.geo) // Removido duplicado
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.triappdriver"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.triappdriver"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += listOf("environment")

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// --- CORREÇÃO 3: DEPENDÊNCIAS DO KSP ---
// Isso garante que o código do Ktorfit seja gerado para todas as plataformas
dependencies {

    implementation(platform(libs.firebase.bom))

    // Firebase Android SDKs
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlyticsKtx)

    debugImplementation(compose.uiTooling)

    // --- KTORFIT KSP ---
    // Pega a versão automaticamente do TOML para não quebrar se você atualizar
    val ktorfitVersion = libs.versions.ktorfit.get()
    val ktorfitKsp = "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion"

    add("kspCommonMainMetadata", ktorfitKsp)
    add("kspAndroid", ktorfitKsp)
    add("kspIosX64", ktorfitKsp)
    add("kspIosArm64", ktorfitKsp)
    add("kspIosSimulatorArm64", ktorfitKsp)

    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}