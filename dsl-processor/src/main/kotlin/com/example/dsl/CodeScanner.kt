package com.example.dsl

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

class CodeScanner {
    private val modelPackages = mutableMapOf<String, String>()
    private val repositoryPackages = mutableMapOf<String, String>()
    private val sourceFiles = mutableListOf<File>()
    private val packageRegex = Regex("^package\\s+([a-zA-Z0-9_.]+)")
    private val classRegex = Regex("^.+(interface|class)\\s+([a-zA-Z0-9_]+)")
    private lateinit var basePath: Path

    fun scan() {
        // Find the mobile directory by traversing up from current directory
        basePath = findMobileDirectory() ?: run {
            println("Error: Could not find mobile directory")
            return
        }

        if (!Files.exists(basePath)) {
            println("Warning: Mobile app codebase not found at $basePath")
            return
        }

        // Scan for domain models
        Files.walk(basePath)
            .filter { path -> 
                path.toString().contains("/domain/model/") && 
                path.toString().endsWith(".kt")
            }
            .forEach { path -> scanModelFile(path) }

        // Scan for repositories
        Files.walk(basePath)
            .filter { path -> 
                path.toString().contains("/domain/") && 
                path.toString().endsWith("Repository.kt")
            }
            .forEach { path -> scanRepositoryFile(path) }

        println("Found ${modelPackages.size} models and ${repositoryPackages.size} repositories")
    }

    private fun findMobileDirectory(): Path? {
        var currentDir = File(".").absoluteFile
        while (currentDir.parentFile != null) {
            val mobileDir = currentDir.listFiles()?.find { it.name == "mobile" }
            if (mobileDir != null) {
                return Paths.get(mobileDir.absolutePath, "app/src/main/java/com/example")
            }
            currentDir = currentDir.parentFile
        }
        return null
    }

    private fun scanModelFile(path: Path) {
        try {
            val content = File(path.toUri()).readText()
            val packageName = extractPackageName(content)
            val className = path.name.removeSuffix(".kt")
            
            if (packageName != null && className.isNotBlank()) {
                modelPackages[className] = packageName
                println("Found model: $className in $packageName")
            }
        } catch (e: Exception) {
            println("Error scanning model file ${path}: ${e.message}")
        }
    }

    private fun scanRepositoryFile(path: Path) {
        try {
            val content = File(path.toUri()).readText()
            val packageName = extractPackageName(content)
            val className = path.name.removeSuffix(".kt")
            
            if (packageName != null && className.isNotBlank()) {
                repositoryPackages[className] = packageName
                println("Found repository: $className in $packageName")
            }
        } catch (e: Exception) {
            println("Error scanning repository file ${path}: ${e.message}")
        }
    }

    private fun extractPackageName(content: String): String? {
        val packageRegex = Regex("package\\s+([\\w.]+)")
        return packageRegex.find(content)?.groupValues?.get(1)
    }

    fun findModelPackage(modelName: String): String? {
        return modelPackages[modelName]
    }

    fun findRepositoryPackage(repositoryName: String): String? {
        return repositoryPackages[repositoryName]
    }

    fun getModelImports(modelNames: List<String>): List<String> {
        return modelNames.mapNotNull { modelName ->
            findModelPackage(modelName)?.let { packageName ->
                "import $packageName.$modelName"
            }
        }
    }

    fun getRepositoryImports(repositoryNames: List<String>): List<String> {
        return repositoryNames.mapNotNull { repositoryName ->
            findRepositoryPackage(repositoryName)?.let { packageName ->
                "import $packageName.$repositoryName"
            }
        }
    }

    private fun scanDirectory(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDirectory(file)
            } else if (file.extension == "kt") {
                sourceFiles.add(file)
            }
        }
    }

    fun findFileByClassName(className: String): File? {
        return sourceFiles.find { file ->
            file.readLines().any { line ->
                classRegex.find(line)?.groupValues?.get(2) == className
            }
        }
    }

    fun extractPackageName(file: File): String? {
        return file.readLines()
            .find { line -> packageRegex.matches(line) }
            ?.let { line -> packageRegex.find(line)?.groupValues?.get(1) }
    }
} 