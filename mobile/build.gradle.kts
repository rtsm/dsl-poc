import java.io.File

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("generateJourney") {
    doLast {
        val journeyName = project.findProperty("journeyName") as? String
            ?: throw GradleException("Please provide journey name using -PjourneyName=<name>")

        val projectRoot = project.rootDir
        val dslFile = File(projectRoot, "definitions/${journeyName.lowercase()}.dsl")
        if (!dslFile.exists()) {
            throw GradleException("DSL file not found at: ${dslFile.absolutePath}")
        }

        println("Processing DSL file: ${dslFile.name}")
        
        val process = ProcessBuilder(
            "java",
            "-jar",
            File(projectRoot, "tools/dsl-processor.jar").absolutePath,
            dslFile.absolutePath
        ).start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val errorOutput = process.errorStream.bufferedReader().readText()
            throw GradleException("DSL processor failed with error: $errorOutput")
        }

        val output = process.inputStream.bufferedReader().readText()
        println(output)
        println("Successfully generated code for journey: $journeyName")
    }
} 