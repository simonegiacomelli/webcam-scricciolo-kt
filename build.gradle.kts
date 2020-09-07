import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainClass = "ServerKt"

plugins {
    kotlin("multiplatform") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    application
}
group = "me.simonegiacomelli"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
//            testTask {
//                useKarma {
//                    useChromeHeadless()
//                    webpackConfig.cssSupport.enabled = true
//                }
//            }
        }
    }
    sourceSets {
        all {
            //FIXME remove optin
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.0.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:1.4.0")
                implementation("io.ktor:ktor-html-builder:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.9")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
application {
    mainClassName = mainClass
}
tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "output.js"
}
tasks.getByName<Jar>("jvmJar") {
    dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
    from(File(jsBrowserProductionWebpack.destinationDirectory, "index.html"))
}
tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    classpath(tasks.getByName<Jar>("jvmJar"))
}

tasks.register<Jar>("buildFatJar3") {
    group = "application"
    val jsbpw = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
    dependsOn(tasks.getByName("build"))
    dependsOn(jsbpw)
    val main = kotlin.jvm().compilations.getByName("main")
    manifest {
        attributes["Main-Class"] = mainClass
    }
    fun jsOutput(suffix: String = "") = File(jsbpw.destinationDirectory, jsbpw.outputFileName + suffix)
    from(
        configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) },
        main.output.classesDirs,
        jsOutput(),
        jsOutput(".map")
    )
    from(File(jsbpw.destinationDirectory, "index.html"))
    archiveBaseName.set("fat3-${project.name}")
}
