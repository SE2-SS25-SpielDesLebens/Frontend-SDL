plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testOptions {
            unitTests {
                isIncludeAndroidResources = true // für Robolectric
                isReturnDefaultValues = true     // für Mockito
                all {
                    it.useJUnitPlatform()
                    it.finalizedBy(tasks.named("jacocoTestReport"))
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

// ✅ Jacoco-Konfiguration
tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates code coverage report for the test task."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(file("${project.buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*"
    )

    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val javaDebugTree = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(kotlinDebugTree, javaDebugTree))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

// ✅ SonarCloud-Konfiguration
sonar {
    properties {
        property("sonar.projectKey", "SE2-SS25-SpielDesLebens_Frontend-SDL")
        property("sonar.organization", "se2-ss25-spieldeslebens")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.language", "kotlin")
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/test/kotlin")
        property("sonar.java.coveragePlugin", "jacoco")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        )
    }
}

dependencies {
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.builtin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Android Test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
