package com.example.dsl

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty() || args[0] == "--help" || args[0] == "-h") {
        printHelp()
        return
    }

    val dslFile = File(args[0])
    if (!dslFile.exists()) {
        println("Error: DSL file not found at path: ${dslFile.absolutePath}")
        printHelp()
        return
    }

    try {
        val feature = DSLParser().parse(dslFile)
        CodeGenerator(feature).generate()
        println("Successfully generated code from DSL file: ${dslFile.name}")
    } catch (e: Exception) {
        println("Error processing DSL file: ${e.message}")
        e.printStackTrace()
    }
}

private fun printHelp() {
    println("""
        DSL Code Generator
        
        Usage: java -jar dsl-processor.jar <dsl-file-path>
        
        Arguments:
            <dsl-file-path>    Path to the DSL file to process
        
        Options:
            -h, --help         Show this help message
        
        Description:
            This tool processes a DSL file and generates corresponding Kotlin code
            for a feature module. The generated code includes:
            - Domain models
            - Repository interfaces and implementations
            - UI state and actions
            - Reducers
            - Dependency injection setup
            - Navigation components
            
        Example DSL file format:
            feature {
                packageName = "profile"
                featureName = "Profile"
                
                domainModel {
                    name = "UserProfile"
                    property("id", "String")
                    property("name", "String")
                    property("email", "String")
                }
                
                apiEndpoint {
                    name = "getUserProfile"
                    path = "/api/user/profile"
                    method = "GET"
                    responseModel = "UserProfile"
                }
                
                uiState {
                    name = "ProfileState"
                    property("isLoading", "Boolean")
                    property("userProfile", "UserProfile?")
                    property("error", "String?")
                }
                
                uiAction {
                    name = "LoadProfile"
                }
                uiAction {
                    name = "ProfileLoaded"
                    property("profile", "UserProfile")
                }
            }
    """.trimIndent())
} 