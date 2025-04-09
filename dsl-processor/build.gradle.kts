import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.example.dsl.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("dsl-processor.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// Task to create tools directory if it doesn't exist
tasks.register("createToolsDir") {
    doLast {
        val toolsDir = Paths.get("../mobile/tools")
        if (!Files.exists(toolsDir)) {
            Files.createDirectories(toolsDir)
        }
    }
}

// Task to copy the JAR to the tools directory
tasks.register("releaseToMobile", Copy::class) {
    dependsOn("jar", "createToolsDir")
    
    from(tasks.jar.get().outputs.files)
    into("../mobile/tools")
    
    doLast {
        println("DSL processor JAR has been released to mobile tools directory")
    }
}